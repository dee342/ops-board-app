package gov.nyc.dsny.smart.opsboard.configs.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RabbitMQRestAPI {

	private RestTemplate restTemplate;
	private String url;

	public RabbitMQRestAPI(String url, String username, String password) {
		BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(username, password));

		HttpClient httpClient = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(credentialsProvider).build();
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
				httpClient);
		restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
		this.url = url + "/api/";
	}

	public List<String> getMatchedQueueNames(String prefix, String suffix) {
		List<String> queueNames = new ArrayList<String>();
		String response = restTemplate.getForEntity(url + "queues",
				String.class).getBody();
		Pattern p = Pattern.compile("(\"name\"):(.+?)\"");

		Matcher m = p.matcher(response);
		while (m.find()) {
			String queueName = m.group(2).replaceAll("\"", "");
			if(queueName.startsWith(prefix) && queueName.endsWith(suffix)){
				queueNames.add(queueName);
			}
		}
		return queueNames;
	}
}
