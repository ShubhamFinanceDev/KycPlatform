package com.example.reKyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReKyc {

	public static void main(String[] args) {
		SpringApplication.run(ReKyc.class, args);
	}

}
