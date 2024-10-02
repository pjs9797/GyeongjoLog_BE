package com.example.gyeongjoLog;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class GyeongjoLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(GyeongjoLogApplication.class, args);
	}

}
