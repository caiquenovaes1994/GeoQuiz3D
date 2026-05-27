<!-- markdownlint-disable MD033 MD041 -->
<div align="center">

# 🌍 Geo-Quiz 3D

[![Version](https://img.shields.io/badge/version-v1.3.0-blue?style=flat)](https://github.com/caiquenovaes1994/GeoQuiz3D/releases/tag/v1.3.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-4.0.6-6DB33F?style=flat&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-4.0.6-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![SQLite](https://img.shields.io/badge/SQLite-3-003B57?style=flat&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Lombok](https://img.shields.io/badge/Lombok-v1.18-BC2A2A?style=flat)](https://projectlombok.org/)
[![Three.js](https://img.shields.io/badge/Three.js-r128-000000?style=flat&logo=three.js&logoColor=white)](https://threejs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v3-38B2AC?style=flat&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Tween.js](https://img.shields.io/badge/Tween.js-18.6.4-F7DF1E?style=flat&logo=javascript&logoColor=black)](https://github.com/tweenjs/tween.js/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=flat&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Google Charts](https://img.shields.io/badge/Google_Charts-v1-4285F4?style=flat&logo=google&logoColor=white)](https://developers.google.com/chart)
[![Google OAuth2](https://img.shields.io/badge/Google%20OAuth2-v2.0-4285F4?style=flat&logo=google&logoColor=white)](https://developers.google.com/identity)

O **Geo-Quiz 3D** é uma plataforma imersiva de desafios geográficos que combina a precisão de um backend em Java com a interatividade de um globo 3D renderizado em tempo real. Os jogadores podem testar seus conhecimentos em três módulos distintos, acompanhando seu progresso através de um dashboard detalhado.

</div>

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
* **Segurança e Autenticação**: Sistema de login robusto utilizando Spring Security com suporte a Login Social via Google (OAuth2).
* **Persistência de Sessão**: Reinicie jogos instantaneamente e mantenha seu progresso e conta salvos de forma segura.

---

## 🛠️ Tecnologias Utilizadas

### Backend

* **Java 17 / Spring Boot 4**: Núcleo da aplicação e APIs REST.
* **Spring Security & OAuth2**: Proteção de rotas, controle de acesso e autenticação via Google.
* **Spring Data JPA / SQLite**: Persistência de países, usuários e conquistas.
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

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white)](https://github.com/caiquenovaes1994)
[![Gmail](https://img.shields.io/badge/Gmail-D14836?style=flat&logo=gmail&logoColor=white)](mailto:caiquenovaes1994@gmail.com)

---

> **"O mundo na palma da sua mão. Desafie seus limites geográficos em uma experiência 3D única."**
