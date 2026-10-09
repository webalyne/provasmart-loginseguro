# ProvaSmart Auth: sistema de login seguro

## Orientações para montar o documento

Complete a capa e a folha de rosto com nome, instituição, curso, disciplina,
professor, cidade e ano. Use o modelo da sua instituição.

Como referência de formatação: papel A4, fonte Arial ou Times New Roman
tamanho 12, espaçamento 1,5 no texto e margens superior/esquerda de 3 cm
e inferior/direita de 2 cm. Confira as exceções e a estrutura no manual local.
Gere o sumário depois de organizar as seções e exporte a versão final em PDF.

## 1 Introdução

O ProvaSmart Auth é um sistema de autenticação e controle de usuários para
uma aplicação educacional. A proposta utiliza como referência o tema do
ProvaSmart, um projeto de preparação para o ENEM, mas foi desenvolvida
separadamente dos repositórios do PFC.

A aplicação utiliza Java com Spring Boot, Spring Security, Thymeleaf e
MongoDB. A interface oferece cadastro, login, logout e páginas reservadas
aos perfis de administrador, professor e estudante.

## 2 Objetivo

Desenvolver um sistema de login seguro que controle o acesso às páginas
conforme o perfil do usuário e permita a administração das contas.
A organização do código deve facilitar mudanças posteriores no tema visual
sem modificar as regras centrais de autenticação.

## 3 Estrutura do sistema

O código está organizado em quatro pacotes principais.

O pacote config reúne a configuração de segurança, o armazenamento de
sessões, o administrador inicial e as informações do tema.

O pacote security carrega os dados usados pelo Spring Security e verifica
a situação da conta durante as requisições autenticadas.

O pacote user contém o modelo do usuário, o repositório MongoDB e o serviço
que aplica as regras de cadastro e administração.

O pacote web recebe as requisições, valida os formulários e seleciona as
páginas Thymeleaf.

Os templates e os arquivos CSS ficam em recursos separados das classes Java.
Essa divisão permite alterar a aparência sem acessar diretamente o banco
ou modificar as regras de autenticação.

## 4 Cadastro, autenticação e autorização

No cadastro, o usuário informa nome, e-mail, senha e confirmação.
A aplicação valida os campos, normaliza o e-mail e verifica se ele já existe.
A senha precisa ter de 12 a 64 caracteres e até 72 bytes em UTF-8.
O limite em bytes respeita a entrada aceita pelo BCrypt.

O cadastro público sempre atribui o perfil ESTUDANTE.
A escolha de um perfil administrativo não é aceita pelo formulário público.

A senha é armazenada com hash BCrypt e salt, utilizando fator de custo 12.
A senha original não é gravada no banco.
Durante o login, o Spring Security compara a senha informada com o hash.

As rotas são protegidas pelos perfis ADMIN, PROFESSOR e ESTUDANTE.
Todos os usuários autenticados podem consultar o próprio perfil.
A gestão de usuários exige ADMIN.
Os outros dois perfis possuem páginas exclusivas para demonstrar a autorização.

Os formulários incluem o token CSRF, e o logout é realizado por POST.
O identificador de sessão muda após o login para evitar a reutilização
de um identificador anterior à autenticação.

## 5 Administração de usuários

O administrador pode listar usuários, alterar nome e perfil, ativar ou
desativar contas e realizar a exclusão permanente.

A própria conta do administrador não pode ser alterada ou excluída
pela tela de gestão. Isso evita que ele remova seu próprio acesso.

A aplicação verifica a conta em cada requisição autenticada.
Quando a conta é excluída, desativada ou muda de perfil, a sessão anterior
é encerrada na próxima requisição.

O primeiro administrador é criado por uma configuração temporária de
inicialização. O e-mail e a senha são fornecidos por variáveis de ambiente.
Não há credencial padrão no código.

## 6 Integração com o MongoDB Atlas

A conexão é informada pela variável MONGODB_URI.
O arquivo application.yml utiliza essa variável, sem conter credenciais reais.

A configuração no Atlas exige um usuário de banco com acesso ao banco
da aplicação e a autorização do IP da máquina na lista de acesso de rede.
A conexão utiliza TLS.

A coleção users armazena identificador, nome, e-mail, hash da senha, perfil,
situação, data de criação e versão do documento.
O e-mail tem índice único para impedir duplicidade.
A versão permite detectar alterações concorrentes no mesmo documento.

As sessões são armazenadas na coleção sessions pela biblioteca Spring Session.
O navegador recebe apenas o cookie com o identificador da sessão.
O cookie utiliza HttpOnly e SameSite=Lax.

A sessão expira após 30 minutos sem atividade.
O índice TTL permite remover os registros expirados.
O logout invalida a sessão e remove o cookie do navegador.

Em um ambiente com HTTPS, a configuração COOKIE_SECURE deve ser true.

## 7 Separação e adaptação do tema

As páginas são construídas com Thymeleaf.
O cabeçalho e o rodapé são compartilhados por fragmentos.

O arquivo base.css contém os estilos comuns.
Os arquivos provasmart.css e neutral.css definem as cores dos temas.
A variável APP_THEME escolhe o tema, e APP_BRAND altera o nome exibido.

Uma futura adaptação pode substituir os textos, os templates e as cores,
preservando o serviço de usuários, o repositório e a configuração de segurança.

## 8 Testes

Os testes verificam o hash das senhas, a normalização do e-mail, a atribuição
do perfil ESTUDANTE e o tratamento de cadastro duplicado.

Também verificam páginas protegidas, requisições sem CSRF, confirmação
de senha e encerramento de sessões de contas inválidas.

Os testes de integração utilizam um MongoDB separado e verificam login,
persistência de sessão, logout e mudança de perfil.

Antes de entregar, registre aqui o resultado da execução com o seu cluster
Atlas e acrescente capturas das páginas de cadastro, login e administração.
Não inclua senhas nem a URI completa nas imagens.

## 9 Versionamento

O projeto utiliza Gitflow, com main para a entrega e develop para integração.
As funcionalidades são organizadas em branches feature.
Uma branch release reúne a preparação da versão, que recebe uma tag.

O repositório de entrega é separado dos repositórios do PFC:

https://github.com/webalyne/provasmart-loginseguro

## 10 Considerações finais

O sistema reúne cadastro, autenticação, autorização e gestão de usuários.
O uso do Spring Security centraliza as regras de acesso, e o MongoDB
armazena as contas e as sessões.

A divisão entre classes Java, templates e estilos permite adaptar o tema
do projeto sem reescrever o mecanismo de login.

## Referências para formatar no documento final

SPRING. Spring Security Reference.
Disponível em: https://docs.spring.io/spring-security/reference/.
Acesso em: 8 out. 2026.

SPRING. Spring Data MongoDB Reference.
Disponível em: https://docs.spring.io/spring-data/mongodb/reference/.
Acesso em: 8 out. 2026.

SPRING. Spring Session MongoDB Reference Documentation.
Disponível em:
https://docs.spring.io/spring-session-data-mongodb/docs/current/reference/html/.
Acesso em: 8 out. 2026.

MONGODB. Connect to an Atlas Cluster.
Disponível em: https://www.mongodb.com/docs/atlas/connect-to-database-deployment/.
Acesso em: 8 out. 2026.

UNIVERSIDADE ESTADUAL DO RIO GRANDE DO SUL.
Manual para publicação de trabalhos acadêmicos e científicos. 2025.
Disponível em:
https://admin.uergs.rs.gov.br/upload/arquivos/202510/14183113-manual-versao-final.pdf.
Acesso em: 8 out. 2026.
