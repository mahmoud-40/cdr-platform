package com.cdr.msloader.service;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CdrService {
    private static final Logger logger = LoggerFactory.getLogger(CdrService.class);

    private final CdrRepository cdrRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String cdrTopic;

    @Autowired
    public CdrService(CdrRepository cdrRepository,
                      KafkaTemplate<String, String> kafkaTemplate,
                      ObjectMapper objectMapper,
                      @Value("${spring.kafka.topic.cdr}") String cdrTopic) {
        this.cdrRepository = cdrRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.cdrTopic = cdrTopic;
    }

    @Transactional
    public void processCdr(CDR cdr) {
        try {
            // Save to PostgreSQL
            cdrRepository.save(cdr);
            logger.info("Saved CDR to PostgreSQL: {}", cdr);

            // Prepare JSON for Kafka (remove id, rename usage to cdr_usage)
            ObjectNode cdrNode = objectMapper.valueToTree(cdr);
            cdrNode.remove("id");
            if (cdrNode.has("usage")) {
                cdrNode.set("cdr_usage", cdrNode.get("usage"));
                cdrNode.remove("usage");
            }
            String jsonMessage = objectMapper.writeValueAsString(cdrNode);
            kafkaTemplate.send(cdrTopic, jsonMessage);
            logger.info("Sent CDR to Kafka: {}", jsonMessage);
        } catch (Exception e) {
            logger.error("Error processing CDR: {}", cdr, e);
            throw new RuntimeException("Failed to process CDR: " + e.getMessage(), e);
        }
    }
} 