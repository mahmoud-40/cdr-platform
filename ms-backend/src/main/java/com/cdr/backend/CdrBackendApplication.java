package com.cdr.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CdrBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdrBackendApplication.class, args);
    }
} 