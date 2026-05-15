# 🗺️ Roadmap de Desenvolvimento: Geo-Quiz 3D

## Etapa 1: Base e Configuração (Backend)

- [x] **Configuração Spring Boot:** Inicializar projeto com Spring Web e Thymeleaf.
- [x] **Persistência (Etapa 6 antecipada):** Configurar PostgreSQL + Spring Security para o sistema de usuários (Login/Senha).
- [x] **Modelo de Dados:** Criar entidade `Country` mapeando os campos do seu `countries.json`.

## Etapa 2: Importação e Bandeiras

- [x] **Parser JSON:** Criar um serviço em Java para ler o `countries.json` e popular o banco de dados.
- [x] **Integração Flagpedia:** Criar lógica para montar a URL da bandeira usando o código `ISO3166-1-Alpha-2` do seu JSON.

## Etapa 3: Inteligência Geográfica (Java)

- [x] **Cálculo de Proximidade:** Implementar a Fórmula de Haversine para distância entre centroides.
- [x] **Cálculo de Direção:** Lógica de rumo (Bearing) para indicar Norte, Sul, Leste, Oeste.
- [x] **API de Palpites:** Endpoint que recebe o ID do país chutado e retorna:
  - Distância em KM.
  - Direção cardinal.
  - Cor Hexadecimal (Baseada na tabela de estratégia de cores).

## Etapa 4: O Globo 3D (Three.js)

- [x] **Cena Base:** Configurar cena, câmera, luz e controles de órbita (zoom/giro).
- [x] **Desenho dos Países:** Usar as coordenadas do seu JSON para criar "Meshes" clicáveis ou destacáveis sobre a esfera.
- [x] **Sistema de Feedback Visual:** Ao receber o resultado da API, o país chutado deve ser pintado no globo com a cor retornada (gradiente de Vermelho para Verde).

## Etapa 5: Módulos de Jogo (Flaggle & Worldle)

- [ ] **Interface Flaggle:** Exibição da bandeira oficial com revelação progressiva em 5 tentativas.
- [ ] **Interface Worldle:** Gerar imagem/silhueta do país a partir dos contornos do GeoJSON.
- [ ] **UI de Dicas:** Lista de tentativas abaixo do input, mostrando distância e seta de direção.

## Etapa 6: Estatísticas e Dashboard

- [ ] **Registro de Histórico:** Salvar cada acerto associado ao usuário.
- [ ] **Cálculo de % Global:** Lógica para definir quanto do mundo o usuário "desbloqueou".
- [ ] **Google Charts (Barras):** Implementar gráficos na seção de perfil:
  - Top 10 países mais encontrados.
  - Distribuição de descobertas por continente.

## Etapa 7: UI/UX e Polimento

- [ ] **Design Responsivo:** Adaptar o layout do Thymeleaf para dispositivos móveis.
- [ ] **Animações:** Suavizar a transição da câmera do globo para focar no país selecionado.
- [ ] **Modo Noturno:** Estilizar o mapa e o globo com texturas escuras/neons.
