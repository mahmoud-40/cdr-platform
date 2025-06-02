package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final CdrRepository cdrRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(CdrRepository cdrRepository) {
        this.cdrRepository = cdrRepository;
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(String message, Acknowledgment ack) {
        try {
            logger.info("Received message from Kafka: {}", message);
            Cdr cdr = objectMapper.readValue(message, Cdr.class);
            cdrRepository.save(cdr);
            ack.acknowledge();
            logger.info("Successfully processed and saved CDR: {}", cdr);
        } catch (Exception e) {
            logger.error("Error processing message: {}. Error: {}", message, e.getMessage(), e);
            throw new RuntimeException("Error processing CDR message: " + e.getMessage(), e);
        }
    }
}