package com.cdr.loader.service;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.repository.CdrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class CdrService {
    private static final Logger logger = LoggerFactory.getLogger(CdrService.class);

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Transactional
    public void processCdr(CDR cdr) {
        try {
            // Save to PostgreSQL
            cdrRepository.save(cdr);
            logger.info("Saved CDR to PostgreSQL: {}", cdr);

            // Send to Kafka
            Map<String, Object> message = new HashMap<>();
            // Removed id field to prevent update instead of insert
            message.put("source", cdr.getSource());
            message.put("destination", cdr.getDestination());
            message.put("startTime", cdr.getStartTime());
            message.put("service", cdr.getService());
            message.put("usage", cdr.getUsage());

            kafkaTemplate.send("cdr-topic2", message);
            logger.info("Sent CDR to Kafka: {}", message);
        } catch (Exception e) {
            logger.error("Error processing CDR: {}", cdr, e);
            throw e;
        }
    }
} 