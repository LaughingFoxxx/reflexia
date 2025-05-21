package com.project.me.authjavaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
public class AuthJavaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthJavaServiceApplication.class, args);
	}

}
