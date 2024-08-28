package com.example.gyeongjoLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

@SpringBootApplication
public class GyeongjoLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(GyeongjoLogApplication.class, args);
	}
}
