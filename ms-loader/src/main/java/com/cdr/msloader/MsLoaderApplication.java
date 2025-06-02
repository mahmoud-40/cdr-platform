package com.cdr.msloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.cdr.msloader.entity")
@EnableJpaRepositories("com.cdr.msloader.repository")
@EnableScheduling
public class MsLoaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsLoaderApplication.class, args);
	}

}
