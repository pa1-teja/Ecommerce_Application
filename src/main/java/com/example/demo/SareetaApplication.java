package com.example.demo;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splunk.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


@EnableJpaRepositories("com.example.demo.model.persistence.repositories")
@EntityScan("com.example.demo.model.persistence")
@SpringBootApplication
@EnableAutoConfiguration
public class SareetaApplication {

	public static String username = "admin";
	public static String password = "changeme";
	public static String host = "localhost";
	public static int port = 8089;
	public static String scheme = "http";


	public static void main(String[] args) {
		try {
			getQueryResultsIntoJsonString("search index=..."); //your Splunk query
			SpringApplication.run(SareetaApplication.class, args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public static Service getSplunkService() {

		HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);

		Map<String, Object> connectionArgs = new HashMap<>();

		connectionArgs.put("host", host);
		connectionArgs.put("port", port);
		connectionArgs.put("scheme", scheme);
		connectionArgs.put("username", username);
		connectionArgs.put("password", password);

		Service splunkService = Service.connect(connectionArgs);

		return splunkService;
	}

	/* Take the Splunk query as the argument and return the results as a JSON
    string */
	public static String getQueryResultsIntoJsonString(String query) throws IOException {

		Service splunkService = getSplunkService();

		Args queryArgs = new Args();

		//set "from" time of query. 1 = from beginning
		queryArgs.put("earliest_time", "1");

		//set "to" time of query. now = till now
		queryArgs.put("latest_time", "now");

		Job job = splunkService.getJobs().create(query);

		while(!job.isDone()) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		Args outputArgs = new Args();

		//set format of result set as json
		outputArgs.put("output_mode", "json");

		//set offset of result set (how many records to skip from the beginning)
		//Default is 0
		outputArgs.put("offset", 0);

		//set no. of records to get in the result set.
		//Default is 100
		//If you put 0 here then it would be set to "no limit"
		//(i.e. get all records, don't truncate anything in the result set)
		outputArgs.put("count", 0);

		InputStream inputStream = job.getResults(outputArgs);

		//Now read the InputStream of the result set line by line
		//And return the final result into a JSON string
		//I am using Jackson for JSON processing here,
		//which is the default in Spring boot

		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

		String resultString = null;
		String aLine = null;

		while((aLine = in.readLine()) != null) {

			//Convert the line from String to JsonNode
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(aLine);

			//Get the JsonNode with key "results"
			JsonNode resultNode = jsonNode.get("results");

			//Check if the resultNode is array
			if (resultNode.isArray()) {
				resultString = resultNode.toString();
			}
		}

		return resultString;
	}

}
