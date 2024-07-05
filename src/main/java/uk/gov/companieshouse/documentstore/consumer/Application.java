package uk.gov.companieshouse.documentstore.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static final String NAMESPACE = "document-store-delta-consumer";
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
