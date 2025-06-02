package com.cdr.loader.service;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

            // Create a new ObjectNode without the id field
            ObjectNode cdrNode = objectMapper.valueToTree(cdr);
            cdrNode.remove("id");
            
            // Convert to JSON string
            String cdrJson = objectMapper.writeValueAsString(cdrNode);
            
            // Send to Kafka with async handling
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("cdr-topic2", cdrJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully sent CDR to Kafka: {}", cdrJson);
                } else {
                    logger.error("Failed to send CDR to Kafka: {}", cdrJson, ex);
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