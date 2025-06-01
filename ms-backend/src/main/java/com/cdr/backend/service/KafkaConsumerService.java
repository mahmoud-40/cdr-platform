package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final CdrRepository cdrRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(CdrRepository cdrRepository, ObjectMapper objectMapper) {
        this.cdrRepository = cdrRepository;
        this.objectMapper = objectMapper;
        logger.info("KafkaConsumerService started!");
    }

    @KafkaListener(topics = "cdr-topic2", groupId = "cdr-group")
    public void listen(String message) {
        logger.info("Received CDR message from Kafka: {}", message);
        processMessage(message);
    }

    public void processMessage(String message) {
        try {
            Cdr cdr = objectMapper.readValue(message, Cdr.class);
            cdrRepository.save(cdr);
            logger.info("Successfully saved CDR record: {}", cdr);
        } catch (Exception e) {
            logger.error("Error processing CDR message: {}", message, e);
            // TEMP: Also print to stderr for visibility in container logs
            System.err.println("Error processing CDR message: " + message);
            e.printStackTrace();
        }
    }
}