# Release Notes - v1.3.0 🚀

Temos o prazer de anunciar a versão **1.3.0** do Geo-Quiz 3D! Esta atualização foca fortemente na experiência e segurança do usuário, substituindo o antigo modelo de usuários fixos por um sistema completo de contas e autenticação.

## ✨ Novidades e Funcionalidades

- **Google Login Integrado (OAuth2)**: Agora os jogadores podem entrar no Geo-Quiz 3D em segundos utilizando suas contas do Google, sem a necessidade de criar senhas!
- **Sistema de Autenticação Tradicional**: Adicionado suporte para cadastro e login convencionais (e-mail e senha) para quem prefere não vincular contas sociais.
- **Nova Interface de Login Premium**: Tela de login completamente redesenhada com efeito *Glassmorphism*, totalmente responsiva e alinhada ao visual do jogo (com o logo 3D e animações integradas).
- **Sessões de Jogador**: O Dashboard e o progresso do globo agora são vinculados à conta autenticada do jogador, impedindo que outros usuários sobreponham seu histórico.
- **Logout Seguro**: Adicionado botão de saída na barra de navegação para encerramento seguro de sessão.

## 🛠 Modificações Técnicas e Backend

- **Spring Security Implementado**: Toda a aplicação agora é protegida, redirecionando usuários não logados para a nova tela de Login e protegendo as APIs rest.
- **Novas Tabelas e Estrutura no Banco de Dados**: A tabela de `users` foi expandida com novos campos (`email`, `provider`, `provider_id`) para suportar perfeitamente o ecossistema OAuth2. As senhas locais agora utilizam criptografia `BCrypt`.
- **Refatoração no Código**: Remoção da dependência "hardcoded" de repositórios nos Controllers, adotando o uso mais elegante de `@AuthenticationPrincipal` para acessar o usuário da sessão.
- **Correção de Dependências de Front-End**: Arquivos do `tween.js` agora são servidos localmente, evitando bloqueios de rastreamento de navegadores e melhorando a performance.

Obrigado por jogar e testar seus conhecimentos geográficos conosco! 🌍
