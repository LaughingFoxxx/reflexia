package com.project.me.central_java_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableMongoRepositories(basePackages = "com.project.me.central_java_service.repository")
@SpringBootApplication
@EnableAsync
public class CentralJavaServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CentralJavaServiceApplication.class, args);
	}
}
