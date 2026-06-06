# Geo-Quiz 3D - Release Notes

## Versão 1.5.2

**Data:** 06/06/2026

### Correções de Bugs e Melhorias de UI

* **Seletor de Idioma (Dropdown):**
  * **Alinhamento Interno:** Corrigido o problema onde as bandeiras no menu suspenso de seleção de idioma apareciam em zigue-zague devido ao tamanho variável do texto (PT-BR, EN, ES). O alinhamento foi ajustado para a esquerda mantendo o espaçamento uniforme.
  * **Alinhamento das Caixas (Molduras):** Resolvido o desalinhamento lateral e a sobreposição vertical entre o botão do idioma escolhido e o menu suspenso. Ambas as caixas agora possuem a mesma largura definida e o menu suspenso aparece perfeitamente posicionado abaixo do botão principal, sem sobrepor a borda do contêiner *glass*.

* **Bandeira da Turquia:**
  * **Correção de Bloqueio por AdBlockers:** Corrigido o problema onde a bandeira da Turquia não era exibida na interface. A URL anterior (`tr.png`) era falsamente identificada como um pixel de rastreamento (tracking pixel) por extensões de bloqueio de anúncios (como o uBlock Origin). A origem da imagem foi alterada para um arquivo hospedado localmente no próprio servidor, evitando bloqueios de extensões e bloqueios de "hotlinking" de serviços externos.
