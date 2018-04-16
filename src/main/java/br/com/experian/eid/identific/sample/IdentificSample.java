package br.com.experian.eid.identific.sample;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
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
	
	/**
	 * ATENCAO! Nao utilizar esse bypass em producao.
	 * Apenas para testes em desenvolvimento com certificado para hostname localhost.
	 * @return
	 * @throws GeneralSecurityException
	 */
	private ClientHttpRequestFactory createDummyReqFactory() throws GeneralSecurityException {
		HostnameVerifier acceptingHostnameVerifier = (hostname, session) -> true;
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, acceptingHostnameVerifier)).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		return requestFactory;
	}

	@PostConstruct
	protected void init() throws GeneralSecurityException {
		ClientHttpRequestFactory doesntUseInProduction = createDummyReqFactory();
	
		identificRest = new RestTemplate(doesntUseInProduction);
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
	public String authCallback(@RequestParam(name = "code", required = true) String code) {
		@SuppressWarnings("unchecked")
		Map<String, Object> resp = identificRest
				.getForObject(eauthIdentificRestUrl + "/auth/user_data?token=" + token + "&code=" + code, Map.class);
		
		return "Obrigado por logar, " + resp.get("name") + "!<br/>"
				+ "<br/> CPF: " + resp.get("cpf")
				+ "<br/> Email: " + resp.get("email")
				+ "<br/> Status: " + resp.get("status");
	}

	@RequestMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ResponseEntity<String> genTokenResp = identificRest.getForEntity(eauthIdentificRestUrl + "/auth/generate_token",
				String.class);
		if (genTokenResp.getStatusCode() != HttpStatus.OK) {
			response.sendError(genTokenResp.getStatusCodeValue(), genTokenResp.toString());
			return;
		}
		// Obtem o token gerado.
		token = genTokenResp.getBody();

		// Callback URL
		String callbackAuthUrl = getBaseUrl(request) + "/autenticacao";

		// redireciona para o login Identific
		response.sendRedirect(eauthIdentificLoginUrl + "?token=" + token + "&redirect_url=" + callbackAuthUrl);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IdentificSample.class, args);

	}
}