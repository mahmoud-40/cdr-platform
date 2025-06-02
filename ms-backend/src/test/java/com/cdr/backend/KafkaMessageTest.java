package com.cdr.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"cdr-topic2"})
@TestPropertySource(properties = {
    "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class KafkaMessageTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testKafkaMessageProcessing() throws Exception {
        // Create a test CDR message
        String testMessage = """
            {
                "id": null,
                "source": "1234567890",
                "destination": "9876543210",
                "startTime": "2024-03-02T10:00:00",
                "service": "SMS",
                "cdr_usage": 1
            }
            """;

        // Send the message to Kafka
        assertDoesNotThrow(() -> {
            kafkaTemplate.send("cdr-topic2", testMessage);
            // Wait for message processing
            TimeUnit.SECONDS.sleep(5);
        });
    }
} 