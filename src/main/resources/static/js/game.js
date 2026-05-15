// Variáveis Globais
let scene, camera, renderer, globe, controls;
let targetCountryId = null;
let allCountries = [];
let userGuesses = [];
let usedCountryIds = new Set();
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();

// Inicialização
async function init() {
    // Cena
    scene = new THREE.Scene();
    
    // Câmera
    camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.position.z = 400;

    // Renderer
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    document.getElementById('canvas-container').appendChild(renderer.domElement);

    // Controles
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.rotateSpeed = 0.5;

    // Luzes
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
    scene.add(ambientLight);

    const pointLight = new THREE.PointLight(0xffffff, 1);
    pointLight.position.set(200, 200, 200);
    scene.add(pointLight);

    // Globo e Estrelas
    createGlobe();
    createStars();

    // Carregar Países e Configurar UI
    await loadCountries();

    // Eventos
    window.addEventListener('resize', onWindowResize, false);
    window.addEventListener('mousemove', onMouseMove, false);
    document.getElementById('btn-guess').addEventListener('click', handleGuess);

    animate();
}

function createGlobe() {
    const geometry = new THREE.SphereGeometry(150, 64, 64);
    
    const textureLoader = new THREE.TextureLoader();
    const earthTexture = textureLoader.load('https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg');
    const bumpMap = textureLoader.load('https://unpkg.com/three-globe/example/img/earth-topology.png');
    
    const material = new THREE.MeshPhongMaterial({
        map: earthTexture,
        bumpMap: bumpMap,
        bumpScale: 2,
        specular: new THREE.Color('grey'),
        shininess: 10
    });

    globe = new THREE.Mesh(geometry, material);
    globe.rotation.y = Math.PI; // Rotação de 180 graus para alinhar o meridiano 0
    scene.add(globe);

    // Atmosfera
    const atmosGeo = new THREE.SphereGeometry(152, 64, 64);
    const atmosMat = new THREE.MeshBasicMaterial({
        color: 0x4488ff,
        transparent: true,
        opacity: 0.1,
        side: THREE.BackSide
    });
    scene.add(new THREE.Mesh(atmosGeo, atmosMat));
}

function createStars() {
    const starGeometry = new THREE.BufferGeometry();
    const starMaterial = new THREE.PointsMaterial({ color: 0xffffff, size: 0.7 });

    const starVertices = [];
    for (let i = 0; i < 8000; i++) {
        const x = (Math.random() - 0.5) * 3000;
        const y = (Math.random() - 0.5) * 3000;
        const z = (Math.random() - 0.5) * 3000;
        starVertices.push(x, y, z);
    }

    starGeometry.setAttribute('position', new THREE.Float32BufferAttribute(starVertices, 3));
    scene.add(new THREE.Points(starGeometry, starMaterial));
}

async function loadCountries() {
    try {
        const response = await fetch('/api/countries');
        allCountries = await response.json();
        setupAutocomplete();
        selectRandomTarget();
    } catch (e) {
        console.error("Erro ao carregar países:", e);
    }
}

function setupAutocomplete() {
    const input = document.getElementById('country-search');
    const list = document.getElementById('autocomplete-list');
    const hiddenId = document.getElementById('selected-country-id');

    input.addEventListener('input', () => {
        const val = input.value.toLowerCase();
        list.innerHTML = '';
        if (!val) {
            list.classList.add('hidden');
            return;
        }

        const filtered = allCountries.filter(c => 
            !usedCountryIds.has(c.id) && (
                c.name.toLowerCase().includes(val) || 
                c.iso2.toLowerCase().includes(val)
            )
        ).slice(0, 10);
        
        if (filtered.length > 0) {
            list.classList.remove('hidden');
            filtered.forEach(country => {
                const item = document.createElement('div');
                item.className = 'flex items-center gap-3 p-3 hover:bg-white/10 cursor-pointer transition-all border-b border-white/5 pointer-events-auto';
                item.innerHTML = `
                    <img src="${country.flagUrl}" class="w-8 h-5 object-cover rounded shadow-sm">
                    <span class="text-sm font-semibold">${country.name}</span>
                `;
                item.onclick = () => {
                    input.value = country.name;
                    hiddenId.value = country.id;
                    list.classList.add('hidden');
                };
                list.appendChild(item);
            });
        } else {
            list.classList.add('hidden');
        }
    });

    document.addEventListener('click', (e) => {
        if (!input.contains(e.target) && !list.contains(e.target)) {
            list.classList.add('hidden');
        }
    });
}

function selectRandomTarget() {
    if (allCountries.length > 0) {
        const randomIndex = Math.floor(Math.random() * allCountries.length);
        targetCountryId = allCountries[randomIndex].id;
        document.getElementById('target-display').innerText = "País Misterioso";
        console.log("Dica de Dev - Alvo:", allCountries[randomIndex].name);
    }
}

async function handleGuess() {
    const guessedId = document.getElementById('selected-country-id').value;
    const input = document.getElementById('country-search');

    if (!guessedId) {
        alert("Por favor, selecione um país da lista!");
        return;
    }

    try {
        const response = await fetch(`/api/quiz/guess?guessedId=${guessedId}&targetId=${targetCountryId}`);
        const data = await response.json();
        
        addGuessToHistory(data);
        usedCountryIds.add(parseInt(guessedId));
        
        const country = allCountries.find(c => c.id == guessedId);
        if (country) {
            markOnGlobe(country.lat, country.lon, data.color, country.name, country.flagUrl);
        }
        
        input.value = '';
        document.getElementById('selected-country-id').value = '';
        
        if (data.correct) {
            showCustomModal("VOCÊ VENCEU!", `Parabéns! O país era realmente ${data.countryName}.`, data.flagUrl, true);
        }
    } catch (error) {
        console.error("Erro ao processar palpite:", error);
    }
}

function showCustomModal(title, message, iconOrUrl = "ℹ️", reloadOnClose = false) {
    const container = document.getElementById('modal-container');
    const content = document.getElementById('modal-content');
    const iconDiv = document.getElementById('modal-icon');
    
    document.getElementById('modal-title').innerText = title;
    document.getElementById('modal-message').innerText = message;
    
    if (iconOrUrl && iconOrUrl.startsWith('http')) {
        iconDiv.innerHTML = `<img src="${iconOrUrl}" class="w-full h-full object-cover">`;
    } else {
        iconDiv.innerHTML = iconOrUrl;
    }
    
    container.classList.remove('hidden');
    setTimeout(() => {
        container.classList.remove('opacity-0');
        content.classList.remove('scale-95');
    }, 10);

    const closeBtn = document.getElementById('btn-modal-close');
    closeBtn.onclick = () => {
        container.classList.add('opacity-0');
        content.classList.add('scale-95');
        setTimeout(() => {
            container.classList.add('hidden');
            if (reloadOnClose) location.reload();
        }, 300);
    };
}

function addGuessToHistory(data) {
    const history = document.getElementById('guess-history');
    
    // Adicionar à lista global de palpites
    userGuesses.push(data);
    
    // Ordenar: menor distância primeiro
    userGuesses.sort((a, b) => a.distanceKm - b.distanceKm);
    
    // Limpar e Re-renderizar
    history.innerHTML = '';
    
    userGuesses.forEach(guess => {
        const item = document.createElement('div');
        item.className = 'flex items-center justify-between p-3 bg-white/5 rounded-lg border-l-4 transition-all hover:bg-white/10';
        item.style.borderLeftColor = guess.color;
        
        item.innerHTML = `
            <div class="flex items-center gap-3">
                <img src="${guess.flagUrl}" class="w-8 h-5 object-cover rounded shadow-sm">
                <div>
                    <p class="font-bold text-sm">${guess.countryName}</p>
                    <p class="text-[10px] text-gray-400 uppercase font-semibold">${guess.bearing} • ${guess.distanceKm} KM</p>
                </div>
            </div>
            <div class="w-3 h-3 rounded-full" style="background-color: ${guess.color}"></div>
        `;
        history.appendChild(item);
    });
}

function markOnGlobe(lat, lon, color, name, flagUrl) {
    if (lat === null || lon === null || isNaN(lat) || isNaN(lon)) {
        console.warn("Coordenadas inválidas para este país.");
        return;
    }

    const radius = 154;
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lon + 180) * (Math.PI / 180);

    const x = -radius * Math.sin(phi) * Math.cos(theta);
    const y = radius * Math.cos(phi);
    const z = radius * Math.sin(phi) * Math.sin(theta);

    const markerGeo = new THREE.ConeGeometry(2, 6, 8);
    const markerMat = new THREE.MeshBasicMaterial({ color: color });
    const marker = new THREE.Mesh(markerGeo, markerMat);
    marker.position.set(x, y, z);
    
    // Guardar metadados para o tooltip
    marker.userData = { name: name, flagUrl: flagUrl, isMarker: true };
    
    marker.lookAt(0, 0, 0);
    marker.rotateX(Math.PI / 2);
    
    globe.add(marker);

    // Efeito de brilho circular
    const glowGeo = new THREE.SphereGeometry(3, 16, 16);
    const glowMat = new THREE.MeshBasicMaterial({ color: color, transparent: true, opacity: 0.5 });
    const glow = new THREE.Mesh(glowGeo, glowMat);
    glow.position.set(x, y, z);
    globe.add(glow);

    // Câmera focando no pino (convertendo posição local para mundial)
    scene.updateMatrixWorld(); // Forçar atualização das posições
    const worldPos = new THREE.Vector3();
    marker.getWorldPosition(worldPos);
    
    const cameraTarget = worldPos.clone().normalize().multiplyScalar(450);
    camera.position.copy(cameraTarget);
    camera.lookAt(0, 0, 0);
    controls.update();
}

function onMouseMove(event) {
    // Normalizar posição do mouse
    mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;

    // Atualizar posição visual do tooltip
    const tooltip = document.getElementById('globe-tooltip');
    if (!tooltip) return; // Proteção contra null
    
    tooltip.style.left = (event.clientX + 15) + 'px';
    tooltip.style.top = (event.clientY + 15) + 'px';

    // Raycasting em toda a cena para garantir captura
    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(scene.children, true);

    let markerData = null;
    for (let intersect of intersects) {
        // Verificar se o objeto ou o pai dele é um marcador
        let obj = intersect.object;
        if (obj.userData && obj.userData.isMarker) {
            markerData = obj.userData;
            break;
        }
    }

    if (markerData) {
        document.getElementById('tooltip-name').innerText = markerData.name;
        document.getElementById('tooltip-flag').src = markerData.flagUrl;
        tooltip.classList.remove('hidden');
        tooltip.style.opacity = '1';
        document.body.style.cursor = 'pointer';
    } else {
        tooltip.style.opacity = '0';
        setTimeout(() => {
            if (tooltip.style.opacity === '0') tooltip.classList.add('hidden');
        }, 200);
        document.body.style.cursor = 'default';
    }
}

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
}

function animate() {
    requestAnimationFrame(animate);
    controls.update();
    globe.rotation.y += 0.001;
    renderer.render(scene, camera);
}

// Start
init();
