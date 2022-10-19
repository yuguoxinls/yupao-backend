package com.jack.yupaobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YupaoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(YupaoBackendApplication.class, args);
	}

}
