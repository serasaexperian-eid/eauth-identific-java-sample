# eauth-identific-java-sample
Exemplo Java de utilização do serviço e-Auth Identific de autenticação via certificado digital

### Executando o exemplo ###

1. Execute o IdentificSample.java
1. Acesse http://localhost:8080/login
1. Escolha o certificado a ser utilizado para autenticação
1. Após a autenticação o Identific redireciona o usuário para http://localhost:8080/autenticacao, que por sua vez, exibirá os dados do dono do certificado.

Você também pode fazer o deploy dessa aplicação de exemplo no Heroku:

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

### Como funciona a API ###

A API do Identific realiza a autenticação do usuário através de 3 passos: Obtenção do token, redirecionamento para a autenticação no Identific, validação das credenciais/obtenção dos dados do usuário autenticado.

Obtenção do Token:

```sh
$ curl https://<API_URL>/auth/generate_token -k
```

Redireciona o usuário para o login no browser:

```
https://<LOGIN_URL>/login?token=<generated_token>&redirect_url=<url_to_get_response>
```

O Identific redireciona para <url_to_get_response>:

```
<url_to_get_response>?credential=<credential>
```

Validar as credenciais e obter as informações do usuário:

```sh
$ curl https://<API_URL>/auth/user_data -d token=<token> -d credential=<credential> -k 
```