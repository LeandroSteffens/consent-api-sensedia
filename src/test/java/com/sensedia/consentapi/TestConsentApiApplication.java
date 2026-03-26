package com.sensedia.consentapi;

import org.springframework.boot.SpringApplication;

public class TestConsentApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ConsentApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
