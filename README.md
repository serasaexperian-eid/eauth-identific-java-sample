# eauth-identific-java-sample
Exemplo Java de utilização do serviço e-Auth Identific de autenticação via certificado digital

### Executando o exemplo ###

1. Execute o IdentificSample.java
1. Acesse http://localhost:8080/login
1. Escolha o certificado a ser utilizado para autenticação
1. Após a autenticação o Identific redireciona o usuário para http://localhost:8080/autenticacao, que por sua vez, exibirá os dados do dono do certificado.

### Como funciona a API ###

A API do Identific realiza a autenticação do usuário através de 3 passos: Obtenção do token, redirecionamento para a autenticação no Identific, validação das credenciais/obtenção dos dados do usuário autenticado.

![alt text](docs/identific_api_flow.png)

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

O Identific retorna o JSON com os dados do usuário e o status de validação do certificado:

```
{
	"status":"Certificado OK",
	"email":"JOAO.SILVA@BR.EXPERIAN.COM",
	"cpf":"12345678912",
	"name":"JOAO DA SILVA",
	"notBefore":1521752400000,
	"notAfter":1616360400000,
	"subjectCN":"JOAO DA SILVA:12345678912",
	"issuerCN":"AC SERASA RFB v5",
	"certificateType":"A3",
	"redirectUrl":"<url_to_get_response>",
	"aki":"ecf1415157a8e63ae95eb3a022f9088ab53a878f",
	"serialNumber":"98347489321",
	"token":"1d868b96-56d0-4572-a6c4-a952a10e8fd8",
	"certificateB64": "eyJhbGciOiJIUzUxMiJ9.eyJzdGF0dXMiOiJDZXJ0..."
}
```
