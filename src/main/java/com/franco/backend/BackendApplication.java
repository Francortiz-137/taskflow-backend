package com.franco.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.franco.backend.config.JwtProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class BackendApplication {

	public static void main(String[] args) {
		log.info("Starting Taskflow Backend...");
		SpringApplication.run(BackendApplication.class, args);
	}

}
