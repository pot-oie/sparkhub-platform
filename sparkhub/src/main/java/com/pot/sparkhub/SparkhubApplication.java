package com.pot.sparkhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SparkhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(SparkhubApplication.class, args);
	}

}
