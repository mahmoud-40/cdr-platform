package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final CdrRepository cdrRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(CdrRepository cdrRepository, ObjectMapper objectMapper) {
        this.cdrRepository = cdrRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(String message, Acknowledgment ack) {
        try {
            logger.info("Received message from Kafka: {}", message);
            
            // Use the Cdr.fromJson method which is already configured to handle the message format
            Cdr cdr = Cdr.fromJson(message);
            
            // Save to database
            cdrRepository.save(cdr);
            logger.info("Successfully processed and saved CDR: {}", cdr);
            
            // Acknowledge the message
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing message: {}. Error: {}", message, e.getMessage(), e);
            // Don't acknowledge the message so it can be retried
            throw e;
        }
    }
}