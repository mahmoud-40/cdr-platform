package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CdrRepository cdrRepository;

    @Value("${spring.kafka.topic.cdr}")
    private String cdrTopic;

    @Autowired
    public KafkaConsumerService(CdrRepository cdrRepository) {
        this.cdrRepository = cdrRepository;
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Map<String, Object> message) {
        try {
            logger.info("Received CDR message: {}", message);
            
            // Create a new Cdr object from the message
            Cdr cdr = new Cdr();
            cdr.setSource((String) message.get("source"));
            cdr.setDestination((String) message.get("destination"));
            cdr.setStartTime(LocalDateTime.parse((String) message.get("startTime")));
            cdr.setService((String) message.get("service"));
            cdr.setUsage((Integer) message.get("usage"));
            
            // Validate required fields
            if (cdr.getSource() == null || cdr.getDestination() == null || cdr.getService() == null) {
                logger.error("Missing required fields in CDR message: {}", message);
                return;
            }
            
            // Set default values for optional fields
            if (cdr.getUsage() == null) {
                cdr.setUsage(0);
            }
            
            // Save the CDR
            cdrRepository.save(cdr);
            logger.info("Successfully saved CDR: {}", cdr);
            
        } catch (Exception e) {
            logger.error("Error processing CDR message: {}", message, e);
        }
    }
} 