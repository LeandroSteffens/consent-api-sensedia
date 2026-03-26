package com.sensedia.consentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ConsentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsentApiApplication.class, args);
	}

}
