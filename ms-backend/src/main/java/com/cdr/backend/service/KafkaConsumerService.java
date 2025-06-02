package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final CdrRepository cdrRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(CdrRepository cdrRepository) {
        this.cdrRepository = cdrRepository;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        logger.info("Received message: {}", message);

        try {
            // Remove extra quotes and clean the message
            String cleanedMessage = message.replaceAll("^\"|\"$", "").replaceAll(",\"cdr_usage\":\\d+(?=,\"cdr_usage\":)", "");
            logger.info("Cleaned message: {}", cleanedMessage);

            Cdr cdr = objectMapper.readValue(cleanedMessage, Cdr.class);
            logger.info("Parsed CDR: {}", cdr);

            if (validateCdr(cdr)) {
                cdrRepository.save(cdr);
                logger.info("Successfully saved CDR: {}", cdr);
                ack.acknowledge();
            } else {
                logger.error("Invalid CDR format: {}", cleanedMessage);
                ack.acknowledge(); // Skip invalid messages
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
            ack.acknowledge(); // Optionally send to dead-letter topic instead
        }
    }

    private boolean validateCdr(Cdr cdr) {
        if (cdr == null || cdr.getSource() == null || cdr.getSource().trim().isEmpty() ||
            cdr.getDestination() == null || cdr.getDestination().trim().isEmpty() ||
            cdr.getStartTime() == null || cdr.getService() == null || cdr.getService().trim().isEmpty() ||
            cdr.getUsage() == null) {
            logger.error("CDR failed validation: {}", cdr);
            return false;
        }
        return true;
    }
}