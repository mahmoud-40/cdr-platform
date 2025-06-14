package com.cdr.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EntityScan("com.cdr.backend.model")
@EnableJpaRepositories("com.cdr.backend.repository")
public class CdrBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdrBackendApplication.class, args);
    }
} 