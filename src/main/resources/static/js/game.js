// Variáveis Globais
let scene, camera, renderer, globe, controls;
let targetCountryId = null;
let targetDetails = null;
let currentGameMode = 'classic';
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
    document.getElementById('btn-give-up').addEventListener('click', handleGiveUp);

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

async function selectRandomTarget() {
    if (allCountries.length > 0) {
        const randomIndex = Math.floor(Math.random() * allCountries.length);
        targetCountryId = allCountries[randomIndex].id;
        document.getElementById('target-display').innerText = "País Misterioso";
        console.log("Dica de Dev - Alvo:", allCountries[randomIndex].name);
        
        // Carregar detalhes (bandeira/geometria) logo no início
        await fetchTargetDetails();
    }
}

function startNewGame() {
    // Resetar variáveis de estado
    userGuesses = [];
    usedCountryIds = new Set();
    
    // Limpar UI de histórico
    document.getElementById('guess-history').innerHTML = '<p class="text-gray-500 text-sm italic">Nenhum palpite ainda.</p>';
    
    // Limpar Marcadores do Globo
    const toRemove = [];
    globe.children.forEach(child => {
        if (child.userData && (child.userData.isMarker || child.type === 'Mesh')) {
            toRemove.push(child);
        }
    });
    toRemove.forEach(child => globe.remove(child));
    
    // Resetar Dicas (Blur e Opacidade)
    updateHints();
    
    // Selecionar novo alvo
    selectRandomTarget();
}

async function fetchTargetDetails() {
    try {
        const response = await fetch(`/api/quiz/target-details?targetId=${targetCountryId}`);
        targetDetails = await response.json();
        
        // Setup Flaggle
        document.getElementById('flaggle-img').src = targetDetails.flagUrl;
        
        // Setup Worldle
        if (targetDetails.geometryJson) {
            renderSilhouette(JSON.parse(targetDetails.geometryJson));
        }
    } catch (e) {
        console.error("Erro ao buscar detalhes do alvo:", e);
    }
}

function setMode(mode) {
    currentGameMode = mode;
    
    // Atualizar UI de Botões
    document.querySelectorAll('.mode-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(`mode-${mode}`).classList.add('active');
    
    // Atualizar Containers de Dica
    const hintContainer = document.getElementById('hint-container');
    const flaggleBox = document.getElementById('flaggle-box');
    const worldleBox = document.getElementById('worldle-box');
    
    hintContainer.classList.add('opacity-0');
    
    setTimeout(() => {
        flaggleBox.classList.add('hidden');
        worldleBox.classList.add('hidden');
        
        if (mode === 'classic') {
            hintContainer.classList.add('opacity-0');
        } else {
            hintContainer.classList.remove('opacity-0');
            if (mode === 'flaggle') flaggleBox.classList.remove('hidden');
            if (mode === 'worldle') worldleBox.classList.remove('hidden');
        }
    }, 300);
}

function renderSilhouette(geometry) {
    if (!geometry || !geometry.coordinates) return;
    
    const svgPath = document.getElementById('worldle-silhouette');
    let coords = [];
    
    if (geometry.type === 'Polygon') {
        coords = geometry.coordinates[0];
    } else if (geometry.type === 'MultiPolygon') {
        // Pega o polígono com mais pontos para ser a silhueta principal
        coords = geometry.coordinates.reduce((prev, curr) => 
            curr[0].length > prev.length ? curr[0] : prev, geometry.coordinates[0][0]);
    }
    
    if (coords.length === 0) return;

    // Encontrar Bounding Box
    let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
    coords.forEach(p => {
        if (p[0] < minX) minX = p[0];
        if (p[0] > maxX) maxX = p[0];
        if (p[1] < minY) minY = p[1];
        if (p[1] > maxY) maxY = p[1];
    });

    const width = maxX - minX;
    const height = maxY - minY;
    const padding = 20;
    const scale = Math.min((300 - padding * 2) / width, (300 - padding * 2) / height);
    
    const offsetX = (300 - width * scale) / 2;
    const offsetY = (300 - height * scale) / 2;

    let d = "";
    coords.forEach((p, i) => {
        const x = offsetX + (p[0] - minX) * scale;
        const y = 300 - (offsetY + (p[1] - minY) * scale); // Inverter Y para SVG
        d += (i === 0 ? "M" : "L") + x + " " + y;
    });
    d += "Z";
    
    svgPath.setAttribute('d', d);
}

async function handleGiveUp() {
    if (!targetDetails) return;
    
    // Revelar tudo
    const flagImg = document.getElementById('flaggle-img');
    const silhouette = document.getElementById('worldle-silhouette');
    
    flagImg.style.filter = "none";
    silhouette.setAttribute('fill-opacity', "0.8");
    silhouette.setAttribute('fill', "#ff4444"); // Silhueta vermelha indicando que desistiu

    // Voar para o país correto ao desistir
    const targetCountry = allCountries.find(c => c.id == targetCountryId);
    if (targetCountry) {
        flyToCountry(targetCountry.lat, targetCountry.lon, 280);
    }

    // Mostrar modal com a resposta
    showCustomModal(
        "VOCÊ DESISTIU", 
        `O país misterioso era ${targetDetails.countryName}.`, 
        targetDetails.flagUrl, 
        true
    );
    
    // Inserir a bandeira branca e a do país em espaços SEPARADOS no ícone do modal
    const iconDiv = document.getElementById('modal-icon');
    iconDiv.classList.remove('glass', 'border-white/20', 'shadow-2xl', 'overflow-hidden'); // Remover estilo do container pai
    iconDiv.innerHTML = `
        <div class="flex gap-6 w-full h-full items-center justify-center">
            <div class="w-20 h-20 glass border border-white/20 rounded-xl flex items-center justify-center text-4xl shadow-2xl">🏳️</div>
            <div class="min-w-[120px] h-20 glass border border-white/20 rounded-xl overflow-hidden shadow-2xl flex items-center justify-center p-2">
                <img src="${targetDetails.flagUrl}" class="max-w-full max-h-full object-contain rounded-md">
            </div>
        </div>
    `;
}

async function handleGuess() {
    const guessedId = document.getElementById('selected-country-id').value;
    const input = document.getElementById('country-search');

    if (!guessedId) {
        alert("Por favor, selecione um país da lista!");
        return;
    }

    try {
        const attempts = userGuesses.length + 1; // +1 porque este é o palpite atual
        const response = await fetch(`/api/quiz/guess?guessedId=${guessedId}&targetId=${targetCountryId}&gameMode=${currentGameMode}&attempts=${attempts}`);
        const data = await response.json();
        
        addGuessToHistory(data);
        usedCountryIds.add(parseInt(guessedId));
        
        // Atualizar Dicas Baseado nas Tentativas
        updateHints();

        const country = allCountries.find(c => c.id == guessedId);
        if (country) {
            markOnGlobe(country.lat, country.lon, data.color, country.name, country.flagUrl);
            flyToCountry(country.lat, country.lon, 280);
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

function updateHints() {
    const attempts = userGuesses.length;
    
    // Flaggle: Reduzir blur
    const flagImg = document.getElementById('flaggle-img');
    const blurAmount = Math.max(0, 40 - (attempts * 8));
    const brightness = 0.5 + (attempts * 0.1);
    flagImg.style.filter = `blur(${blurAmount}px) brightness(${Math.min(brightness, 1)})`;
    
    // Worldle: Aumentar opacidade e resetar cor
    const silhouette = document.getElementById('worldle-silhouette');
    const opacity = 0.2 + (attempts * 0.15);
    silhouette.setAttribute('fill-opacity', Math.min(opacity, 0.8));
    silhouette.setAttribute('fill', "white");
}

function showCustomModal(title, message, iconOrUrl = "ℹ️", reloadOnClose = false) {
    const container = document.getElementById('modal-container');
    const content = document.getElementById('modal-content');
    const iconDiv = document.getElementById('modal-icon');
    
    // Restaurar classes padrão caso tenham sido removidas na desistência
    iconDiv.className = "w-32 h-20 mx-auto rounded-xl flex items-center justify-center text-4xl shadow-2xl overflow-hidden glass border-white/20";
    
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
            if (reloadOnClose) {
                startNewGame();
            }
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
                    <p class="text-[10px] text-gray-400 uppercase font-semibold flex items-center gap-1">
                        <span class="inline-block bearing-arrow" style="transform: rotate(${guess.bearingDegrees}deg)">↑</span>
                        ${guess.bearing} • ${guess.distanceKm} KM
                    </p>
                </div>
            </div>
            <div class="w-3 h-3 rounded-full shadow-lg" style="background-color: ${guess.color}; box-shadow: 0 0 10px ${guess.color}"></div>
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
    TWEEN.update();
    controls.update();
    
    // Auto-rotação suave se não estiver interagindo
    if (!controls.active) {
        globe.rotation.y += 0.001;
    }
    
    renderer.render(scene, camera);
}

function latLonToVector3(lat, lon, radius) {
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lon + 180) * (Math.PI / 180);

    const x = -(radius * Math.sin(phi) * Math.cos(theta));
    const z = (radius * Math.sin(phi) * Math.sin(theta));
    const y = (radius * Math.cos(phi));

    return new THREE.Vector3(x, y, z);
}

function flyToCountry(lat, lon, distance = 300) {
    const phi = (90 - lat) * (Math.PI / 180);
    const theta = (lon + 180) * (Math.PI / 180);

    // Considerar a rotação atual do globo para o cálculo da posição da câmera no espaço mundial
    const adjustedTheta = theta + globe.rotation.y;

    const x = -(distance * Math.sin(phi) * Math.cos(adjustedTheta));
    const z = (distance * Math.sin(phi) * Math.sin(adjustedTheta));
    const y = (distance * Math.cos(phi));

    const targetPos = new THREE.Vector3(x, y, z);
    
    new TWEEN.Tween(camera.position)
        .to({ x: targetPos.x, y: targetPos.y, z: targetPos.z }, 3000) // 50% mais lento (de 1.5s para 3s)
        .easing(TWEEN.Easing.Cubic.InOut)
        .start();
}

// Start
init();

// DASHBOARD & GOOGLE CHARTS
google.charts.load("current", {"packages":["corechart", "geochart"]});
let currentDashboardFilter = 'all';

async function toggleDashboard() {
    const container = document.getElementById("dashboard-container");
    const content = document.getElementById("dashboard-content");
    
    if (container.classList.contains("hidden")) {
        container.classList.remove("hidden");
        // Forçar um pequeno delay para garantir que o container está visível antes de desenhar os gráficos
        setTimeout(async () => {
            await updateDashboardStats();
            container.classList.remove("opacity-0");
            content.classList.remove("scale-95");
        }, 50);
    } else {
        container.classList.add("opacity-0");
        content.classList.add("scale-95");
        setTimeout(() => container.classList.add("hidden"), 300);
    }
}

async function changeDashboardFilter(filter) {
    currentDashboardFilter = filter;
    
    // Atualizar UI dos botões do dashboard
    document.querySelectorAll('.db-mode-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.innerText.toLowerCase() === filter.toLowerCase() || 
           (filter === 'classic' && btn.innerText === 'Globle') ||
           (filter === 'all' && btn.innerText === 'Todos')) {
            btn.classList.add('active');
        }
    });

    await updateDashboardStats();
}

async function updateDashboardStats() {
    try {
        const response = await fetch(`/api/dashboard/stats?gameMode=${currentDashboardFilter}`);
        const stats = await response.json();
        
        document.getElementById("stat-count").innerText = stats.conqueredUnique;
        document.getElementById("stat-percent").innerText = stats.conqueredPercentage.toFixed(1) + "%";
        
        const avgAttempts = stats.topCountries.length > 0 
            ? (stats.topCountries.reduce((acc, curr) => acc + curr.attempts, 0) / stats.topCountries.length).toFixed(1)
            : "--";
        document.getElementById("stat-avg").innerText = avgAttempts;

        drawCharts(stats);
    } catch (e) {
        console.error("Erro ao carregar estatísticas:", e);
    }
}

function drawCharts(stats) {
    // 1. Mapa Mundi (Geochart)
    const mapDataArr = [["Country", "Conquered"]];
    if (stats.conqueredIsoCodes && stats.conqueredIsoCodes.length > 0) {
        stats.conqueredIsoCodes.forEach(iso => mapDataArr.push([iso, 1]));
    } else {
        mapDataArr.push(["ZZ", 0]); // Dummy para evitar erro se estiver vazio
    }
    
    const mapChart = new google.visualization.GeoChart(document.getElementById("chart-map"));
    mapChart.draw(google.visualization.arrayToDataTable(mapDataArr), {
        backgroundColor: "transparent",
        datalessRegionColor: "#1f2937",
        defaultColor: "#10b981",
        colorAxis: { colors: ["#10b981", "#10b981"] },
        legend: "none",
        keepAspectRatio: true
    });

    // 2. Gráfico de Continentes
    const continentData = [["Continente", "Países"]];
    if (stats.continentStats && stats.continentStats.length > 0) {
        stats.continentStats.forEach(s => continentData.push([s.continent, s.count]));
    } else {
        continentData.push(["Nenhum", 1]);
    }
    
    const continentChart = new google.visualization.PieChart(document.getElementById("chart-continents"));
    continentChart.draw(google.visualization.arrayToDataTable(continentData), {
        backgroundColor: "transparent",
        colors: ["#60a5fa", "#34d399", "#fbbf24", "#f87171", "#a78bfa", "#fb923c"],
        legend: { textStyle: { color: "#9ca3af" } },
        pieHole: 0.4,
        chartArea: { width: "90%", height: "80%" },
        pieSliceBorderStyle: { color: "transparent" }
    });

    // 3. Gráfico de Top 10
    const topData = [["País", "Tentativas", { role: "style" }]];
    if (stats.topCountries && stats.topCountries.length > 0) {
        stats.topCountries.forEach(s => topData.push([s.name, s.attempts, "color: #34d399; opacity: 0.8"]));
    } else {
        topData.push(["Nenhum", 0, "color: #374151"]);
    }
    
    const topChart = new google.visualization.ColumnChart(document.getElementById("chart-top-countries"));
    topChart.draw(google.visualization.arrayToDataTable(topData), {
        backgroundColor: "transparent",
        legend: { position: "none" },
        vAxis: { 
            textStyle: { color: "#9ca3af" },
            gridlines: { color: "#374151" },
            baselineColor: "#374151"
        },
        hAxis: { textStyle: { color: "#9ca3af" } },
        chartArea: { width: "80%", height: "70%" }
    });
}
