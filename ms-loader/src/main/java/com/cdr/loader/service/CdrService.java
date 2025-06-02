package com.cdr.loader.service;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@Service
public class CdrService {
    private static final Logger logger = LoggerFactory.getLogger(CdrService.class);

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void processCdr(CDR cdr) {
        try {
            // Save to PostgreSQL
            cdrRepository.save(cdr);
            logger.info("Saved CDR to PostgreSQL: {}", cdr);

            // Serialize CDR to JSON
            String cdrJson = objectMapper.writeValueAsString(cdr);
            
            // Send to Kafka with async handling
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("cdr-topic2", cdrJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully sent CDR to Kafka: {}", cdr);
                } else {
                    logger.error("Failed to send CDR to Kafka: {}", cdr, ex);
                    // Here you could implement retry logic or move to dead letter queue
                }
            });
        } catch (Exception e) {
            logger.error("Error processing CDR: {}", cdr, e);
            // Here you could implement custom exception handling
            // For example, move the file to an error directory or send to dead letter queue
            throw new RuntimeException("Failed to process CDR: " + e.getMessage(), e);
        }
    }
} 