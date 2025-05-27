package com.cdr.msloader.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection(name = "postgres")
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("cdr_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    @ServiceConnection(name = "kafka")
    public static KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer()::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer()::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer()::getBootstrapServers);
    }
} 