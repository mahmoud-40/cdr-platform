package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
    public void consume(String message) {
        try {
            logger.info("Received message from Kafka: {}", message);
            
            // Convert the message to a Map first
            Map<String, Object> cdrMap = objectMapper.readValue(message, Map.class);
            
            // Create a new CDR entity
            Cdr cdr = new Cdr();
            cdr.setSource((String) cdrMap.get("source"));
            cdr.setDestination((String) cdrMap.get("destination"));
            cdr.setStartTime(objectMapper.convertValue(cdrMap.get("startTime"), java.time.LocalDateTime.class));
            cdr.setService((String) cdrMap.get("service"));
            cdr.setUsage(((Number) cdrMap.get("cdr_usage")).intValue());

            // Save to database
            cdrRepository.save(cdr);
            logger.info("Successfully processed and saved CDR: {}", cdr);
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
        }
    }
}