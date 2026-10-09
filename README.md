# ProvaSmart Auth

Sistema de login seguro para uma aplicação educacional, com Spring Boot,
Thymeleaf, Spring Security e MongoDB Atlas.

Este é um projeto individual e separado dos repositórios do PFC.

## Funcionalidades

- Cadastro de estudantes, login e logout.
- Senhas armazenadas com hash BCrypt.
- Validação de nome, e-mail e confirmação de senha.
- Perfis ADMIN, PROFESSOR e ESTUDANTE.
- Páginas protegidas conforme o perfil.
- Administração: listar, editar, ativar, desativar e excluir usuários.
- Usuários e sessões armazenados no MongoDB.
- Temas visuais separados da lógica de negócio.

O cadastro público sempre cria uma conta ESTUDANTE.
O administrador pode alterar o perfil de outras contas.
Ele não pode alterar nem excluir a própria conta pela tela de administração.

## Requisitos

- JDK 21.
- Maven 3.9 ou Maven Wrapper.
- Um cluster MongoDB Atlas.

## Configurar o MongoDB Atlas

1. Crie um cluster e um usuário de banco de dados.
2. Dê ao usuário permissão de leitura e escrita no banco `provasmart_auth`.
3. Na lista de acesso de rede, autorize o IP da máquina que executará o projeto.
4. Em Connect > Drivers, copie a URI de conexão.
5. Informe o banco `provasmart_auth` na URI e mantenha TLS habilitado.

O usuário do banco é diferente da conta usada para entrar no site do Atlas.
Se a senha tiver caracteres especiais, codifique-os para uso na URI.

Formato de referência, sem credenciais reais:

```text
mongodb+srv://USUARIO:SENHA@CLUSTER.mongodb.net/provasmart_auth?tls=true
```

Defina a URI na variável de ambiente `MONGODB_URI`.
O arquivo `.env.example` mostra as variáveis usadas.
O Spring Boot não lê arquivos `.env` automaticamente.

No PowerShell, use entrada protegida para não salvar a URI no histórico:

```powershell
$uriSegura = Read-Host 'URI do MongoDB Atlas' -AsSecureString
$env:MONGODB_URI = [System.Net.NetworkCredential]::new('', $uriSegura).Password
```

No Linux ou macOS:

```sh
read -rs MONGODB_URI
export MONGODB_URI
```

Não publique a URI real nem senhas no GitHub.

## Primeiro administrador

Na primeira execução, configure:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BOOTSTRAP_ADMIN_EMAIL = Read-Host 'E-mail do administrador'
$senhaSegura = Read-Host 'Senha do administrador' -AsSecureString
$env:BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new(
    '', $senhaSegura
).Password
```

Use uma senha de 12 a 64 caracteres, com no máximo 72 bytes em UTF-8.
Não existe senha padrão.

A inicialização cria o administrador apenas se o e-mail ainda não existir.
Ela não transforma uma conta de estudante em administrador.

Depois de criar a conta, encerre a aplicação e desative a inicialização:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = 'false'
Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD
```

## Executar

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```sh
./mvnw spring-boot:run
```

Se já tiver Maven instalado, use `mvn spring-boot:run`.
Acesse [http://localhost:8080](http://localhost:8080).

Para cadastrar professores, primeiro crie a conta pelo cadastro público.
Entre como administrador e altere o perfil em Usuários > Editar.

## Estrutura

```text
src/main/java/br/com/provasmart/auth/
  config/       configurações de segurança, sessão e tema
  security/     autenticação e verificação de conta
  user/         modelo, repositório e regras de usuários
  web/          controladores e formulários

src/main/resources/
  templates/    páginas Thymeleaf e fragmentos compartilhados
  static/css/   estilos básicos e temas
  application.yml

src/test/       testes de cadastro, autorização e sessões
docs/          base da documentação
```

Os controladores recebem os formulários e chamam o serviço.
O serviço aplica as regras e utiliza o repositório para acessar o MongoDB.
As páginas não acessam o banco diretamente.

## Rotas

| Rota | Acesso |
| --- | --- |
| /, /login, /cadastro | Público |
| /painel, /perfil | Usuário autenticado |
| /admin/usuarios | ADMIN |
| /professor/painel | PROFESSOR |
| /estudante/painel | ESTUDANTE |

## Segurança e sessões

Os formulários usam a proteção CSRF do Spring Security.
O logout aceita POST com o token CSRF.
O identificador da sessão é alterado após o login.

O cookie SESSION usa HttpOnly e SameSite=Lax.
As sessões expiram após 30 minutos sem atividade.
A biblioteca Spring Session grava as sessões na coleção `sessions`.
O índice TTL remove os documentos expirados; o MongoDB pode levar algum tempo
para executar essa limpeza.

A coleção `users` contém os dados da conta e o hash da senha.
O índice único de e-mail impede contas duplicadas.

Em cada requisição autenticada, a aplicação verifica se a conta ainda existe,
está ativa e mantém o mesmo perfil.
Se houver mudança de perfil, desativação ou exclusão, a sessão é encerrada.

Para servir a aplicação com HTTPS, configure `COOKIE_SECURE=true`.
O valor false permite testar o projeto em HTTP no localhost.

## Alterar o tema

Defina `APP_THEME=neutral` para usar o tema neutro.
O tema padrão é `provasmart`.

As cores ficam em `static/css/themes/`.
O estilo comum fica em `static/css/base.css`.
O cabeçalho e o rodapé ficam em `templates/fragments/layout.html`.

Para mudar o nome mostrado nas páginas, configure `APP_BRAND`.

## Testes

```sh
./mvnw verify
```

Sem um banco de testes configurado, os testes de integração ficam desativados.
Os testes de cadastro e rotas continuam sendo executados.

Para testar também a persistência, use um MongoDB exclusivo para testes:

```powershell
$env:MONGODB_TEST_URI = 'mongodb://localhost:27017/provasmart_auth_test'
.\mvnw.cmd verify
```

Não use o banco de produção nos testes.
O workflow do GitHub executa a suíte com um MongoDB de teste.

## Gitflow

- `main`: versão de entrega.
- `develop`: integração do desenvolvimento.
- `feature/*`: implementação das funcionalidades.
- `release/*`: preparação da versão.
- `hotfix/*`: correções da versão entregue.

As funcionalidades entram em develop.
A release é integrada em main e develop, e recebe uma tag de versão.

## Documentação

A base está em [docs/documentacao-base.md](docs/documentacao-base.md).
Complete os dados acadêmicos e exporte o documento final em PDF conforme
o modelo exigido pela instituição.

## Referências

- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/reference/)
- [Spring Session MongoDB](https://docs.spring.io/spring-session-data-mongodb/docs/current/reference/html/)
- [Conexão com o Atlas](https://www.mongodb.com/docs/atlas/connect-to-database-deployment/)
