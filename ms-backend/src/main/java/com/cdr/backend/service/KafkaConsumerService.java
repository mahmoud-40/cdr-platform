package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private CdrRepository cdrRepository;

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Map<String, Object> message) {
        try {
            logger.info("Received CDR message: {}", message);
            
            Cdr cdr = new Cdr();
            
            // Set required fields first
            cdr.setCallStatus(Cdr.CallStatus.COMPLETED);  // Set default status first
            
            // Map source and destination
            String source = (String) message.get("source");
            String destination = (String) message.get("destination");
            if (source != null && destination != null) {
                cdr.setCallingNumber(source);
                cdr.setCalledNumber(destination);
            } else {
                logger.error("Missing required fields source or destination in message: {}", message);
                return;
            }
            
            // Handle startTime array format
            Object startTimeObj = message.get("startTime");
            if (startTimeObj instanceof Map) {
                Map<String, Integer> startTimeMap = (Map<String, Integer>) startTimeObj;
                LocalDateTime startTime = LocalDateTime.of(
                    startTimeMap.get("year"),
                    startTimeMap.get("month"),
                    startTimeMap.get("day"),
                    startTimeMap.get("hour"),
                    startTimeMap.get("minute")
                );
                cdr.setStartTime(startTime);
            } else if (startTimeObj instanceof String) {
                cdr.setStartTime(LocalDateTime.parse((String) startTimeObj));
            } else {
                logger.error("Invalid startTime format in message: {}", message);
                return;
            }
            
            // Map service to callType
            String service = (String) message.get("service");
            if (service != null) {
                try {
                    cdr.setCallType(Cdr.CallType.valueOf(service));
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid service type: {}", service);
                    return;
                }
            } else {
                logger.error("Missing service field in message: {}", message);
                return;
            }
            
            // Map usage to durationSeconds
            Object usageObj = message.get("usage");
            if (usageObj instanceof Number) {
                cdr.setDurationSeconds(((Number) usageObj).intValue());
            } else {
                logger.error("Invalid usage format in message: {}", message);
                return;
            }
            
            // Calculate end time based on start time and duration
            if (cdr.getStartTime() != null && cdr.getDurationSeconds() != null) {
                cdr.setEndTime(cdr.getStartTime().plusSeconds(cdr.getDurationSeconds()));
            }
            
            cdrRepository.save(cdr);
            logger.info("Successfully saved CDR: {}", cdr);
        } catch (Exception e) {
            logger.error("Error processing CDR message: {}", message, e);
        }
    }
} 