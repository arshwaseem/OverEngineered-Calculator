package com.arshwaseem.oe_calc;

import org.springframework.boot.SpringApplication;

public class TestApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApiGatewayApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
