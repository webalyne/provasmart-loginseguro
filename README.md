# ProvaSmart Auth

Projeto da atividade de login seguro, feito com Java 21, Spring Boot,
Spring Security, Thymeleaf e MongoDB Atlas.

O sistema tem cadastro, login, logout e três perfis: administrador,
professor e estudante. Cada perfil tem acesso às suas páginas.
O administrador também pode editar, desativar e excluir outras contas.
O cadastro pela tela cria uma conta de estudante.

As senhas são salvas com BCrypt. Os dados dos usuários e as sessões
ficam no MongoDB, nas coleções `users` e `sessions`.
Este repositório é separado do projeto de PFC.

## Como executar

É necessário ter o JDK 21 e uma conexão com o MongoDB Atlas.
O Maven Wrapper já está incluído no projeto.

No Atlas, crie um usuário com permissão de leitura e escrita no banco
`provasmart_auth` e libere o IP do computador na lista de acesso de rede.
Copie a conexão em **Connect > Drivers**, informando o nome do banco:

```text
mongodb+srv://USUARIO:SENHA@CLUSTER.mongodb.net/provasmart_auth?tls=true
```

Se a senha tiver caracteres especiais, eles precisam ser codificados
para uso na URI. A conexão real deve ficar fora do repositório.

No PowerShell, informe a conexão sem deixá-la no histórico:

```powershell
$uri = Read-Host 'Conexão com o Atlas' -AsSecureString
$env:MONGODB_URI = [System.Net.NetworkCredential]::new('', $uri).Password
```

O arquivo `.env.example` contém as variáveis de configuração.
Ele serve como exemplo; o Spring Boot não carrega o `.env` automaticamente.

Para iniciar no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, defina `MONGODB_URI` no ambiente e execute:

```sh
./mvnw spring-boot:run
```

Depois, acesse [localhost:8080](http://localhost:8080).

## Conta de administrador

Para criar o primeiro administrador, configure estas variáveis antes
de iniciar a aplicação:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:BOOTSTRAP_ADMIN_EMAIL = Read-Host 'E-mail do administrador'
$senha = Read-Host 'Senha do administrador' -AsSecureString
$env:BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new(
    '', $senha
).Password
```

A senha deve ter de 12 a 64 caracteres e até 72 bytes em UTF-8.
O e-mail não pode pertencer a uma conta já cadastrada.

Após criar o administrador, encerre a aplicação e remova essa configuração:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED = 'false'
Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD
```

Para criar uma conta de professor, faça o cadastro e altere o perfil
pela tela de usuários do administrador.

## Organização do código

- `config`: configurações de segurança, sessões e tema.
- `security`: autenticação e verificação da conta.
- `user`: dados, acesso ao banco e regras dos usuários.
- `web`: controladores e formulários.
- `templates`: páginas Thymeleaf e fragmentos de layout.
- `static/css`: estilos e cores dos temas.

As regras ficam nos serviços e o acesso ao banco fica no repositório.
As páginas recebem os dados dos controladores.

## Acesso às páginas

| Página | Quem pode acessar |
| --- | --- |
| Início, login e cadastro | Todos |
| Painel e perfil | Usuários autenticados |
| Administração de usuários | Administrador |
| Painel do professor | Professor |
| Painel do estudante | Estudante |

O administrador não pode excluir ou alterar a própria conta nessa tela.
Quando uma conta é desativada, excluída ou muda de perfil, sua sessão
é encerrada na próxima requisição.

Os formulários usam proteção CSRF e o logout é feito por POST.
As sessões expiram após 30 minutos sem atividade.
O cookie usa HttpOnly e SameSite=Lax. Para executar com HTTPS,
configure `COOKIE_SECURE=true`.

## Temas

O tema padrão é `provasmart`. Para usar o outro tema, defina
`APP_THEME=neutral` no ambiente.

As cores ficam em `static/css/themes/` e o estilo comum em `base.css`.
O cabeçalho e o rodapé ficam em `templates/fragments/layout.html`.
Assim, é possível mudar a aparência sem alterar as regras do sistema.
O nome exibido nas páginas pode ser alterado pela variável `APP_BRAND`.

## Testes

Para executar os testes:

```powershell
.\mvnw.cmd verify
```

Os testes de integração precisam de um banco separado para testes:

```powershell
$env:MONGODB_TEST_URI = 'mongodb://localhost:27017/provasmart_auth_test'
.\mvnw.cmd verify
```

Sem essa variável, os testes de integração são pulados.
Os demais testes continuam sendo executados.
O GitHub Actions executa a suíte com um MongoDB de teste.

## Branches

O projeto segue Gitflow: `main` para a entrega, `develop` para integração,
`feature/*` para funcionalidades e `release/*` para preparação da versão.
Correções da versão entregue podem usar `hotfix/*`.

## Documentação

A descrição da estrutura, da integração com o Atlas e das decisões
do projeto está em [docs/documentacao-base.md](docs/documentacao-base.md).
