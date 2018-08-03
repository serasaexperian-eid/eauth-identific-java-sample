package br.com.experian.eid.identific.sample;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@EnableAutoConfiguration
public class IdentificSample {

	@Value("${identific.rest-url}")
	private String eauthIdentificRestUrl;

	@Value("${identific.login-url}")
	private String eauthIdentificLoginUrl;

	private RestTemplate identificRest;

	private String token;
	//Identificador da aplicação cadastrada. Este identificador foi enviado para o seu e-mail quando 
	//o cadastro da sua aplicação foi efetivado
	private String appID="<Identificador da aplicação recebido por e-mail>"; 

	//Chave da aplicação cadastrada. Esta chave foi enviado para o seu e-mail quando 
	//o cadastro da sua aplicação foi efetivado
	private String apiKey="<Chave recebida por e-mail>";

	private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    
	
	@PostConstruct
	protected void init() throws GeneralSecurityException {
		headers.add("Authorization", "Bearer " + apiKey);
		identificRest = new RestTemplate();
	}

	private String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme() + "://";
		String serverName = request.getServerName();
		String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
		String contextPath = request.getContextPath();
		return scheme + serverName + serverPort + contextPath;
	}

	@RequestMapping("/autenticacao")
	@ResponseBody
	public String authCallback(@RequestParam(name = "credential", required = true) String credential) {

		ResponseEntity<Map> resp = identificRest.exchange(eauthIdentificRestUrl + "/auth/user_data?token=" + token + "&credential=" + credential, HttpMethod.GET, new HttpEntity<Object>(headers),
				Map.class);
		
		return "Obrigado por logar, " + resp.getBody().get("name") + "!<br/>"
				+ "<br/> CPF: " + resp.getBody().get("cpf")
				+ "<br/> Email: " + resp.getBody().get("email")
				+ "<br/> Status: " + resp.getBody().get("status");
	}

	@RequestMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ResponseEntity<String> genTokenResp = identificRest.exchange(eauthIdentificRestUrl + "/auth/generate_token", HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);

		if (genTokenResp.getStatusCode() != HttpStatus.OK) {
			response.sendError(genTokenResp.getStatusCodeValue(), genTokenResp.toString());
			return;
		}
		// Obtem o token gerado.
		token = genTokenResp.getBody();

		// redireciona para o login Identific
		response.sendRedirect(eauthIdentificLoginUrl + "?token=" + token + "&appId=" + appID);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IdentificSample.class, args);

	}
}