# 🌍 Geo-Quiz 3D

[![Version](https://img.shields.io/badge/version-v1.1.0-blue?style=flat)](https://github.com/caiquenovaes1994/GeoQuiz3D/releases/tag/v1.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Three.js](https://img.shields.io/badge/Three.js-r128-000000?style=flat&logo=three.js&logoColor=white)](https://threejs.org/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=java&logoColor=white)](https://www.oracle.com/java/)

O **Geo-Quiz 3D** é uma plataforma imersiva de desafios geográficos que combina a precisão de um backend em Java com a interatividade de um globo 3D renderizado em tempo real. Os jogadores podem testar seus conhecimentos em três módulos distintos, acompanhando seu progresso através de um dashboard detalhado.

---

## 🎮 Modos de Jogo

### 🌎 Globle (Clássico)

O desafio definitivo de busca. Dê palpites de países e receba feedback instantâneo de distância e direção no globo 3D. O calor das cores indica quão perto você está do alvo.

### 🚩 Flaggle

Teste sua memória visual. Uma bandeira é exibida com um efeito de desfoque (*blur*) que diminui a cada palpite errado. Consiga identificar o país antes da revelação total!

### 🗺️ Worldle

Identifique o país apenas pela sua silhueta geográfica. Renderizado via SVG dinâmico, este modo desafia o reconhecimento de fronteiras e formas territoriais.

---

## ✨ Funcionalidades Premium

* **Globo 3D Interativo**: Navegação fluida com zoom e transições cinematográficas suaves entre palpites.
* **Dashboard do Explorador**: Visualize suas estatísticas globais, conquistas por continente e mapa múndi de países descobertos.
* **Inteligência Geográfica**: Motor de cálculo baseado nas fórmulas de Haversine e Bearing para precisão milimétrica.
* **Design Futurista**: Interface baseada em *Glassmorphism* com tema Neon/Dark, totalmente responsiva para dispositivos móveis.
* **Persistência de Sessão**: Reinicie jogos instantaneamente e mantenha seu progresso sem recarregar a página.

---

## 🛠️ Tecnologias Utilizadas

### Backend

* **Java 17 / Spring Boot 4**: Núcleo da aplicação e APIs REST.
* **Spring Data JPA / PostgreSQL**: Persistência de países, usuários e conquistas.
* **Lombok**: Otimização de boilerplate code.

### Frontend

* **Three.js**: Engine 3D para o globo e estrelas.
* **Tailwind CSS**: Estilização moderna e responsiva.
* **Tween.js**: Animações suaves de câmera.
* **Google Charts**: Visualização de dados e mapas de calor.
* **Thymeleaf**: Mecanismo de templates para integração SSR.

---

## 📸 Preview

![Geo-Quiz 3D In-Game](in_game.jpg)

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

### Desenvolvido por Caique Novaes

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com)
[![Gmail](https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:contato@antigravity.ai)

---

> **"O mundo na palma da sua mão. Desafie seus limites geográficos em uma experiência 3D única."**
