package com.cdr.backend.kafka;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.service.CdrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CdrKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CdrKafkaConsumer.class);
    private final CdrService cdrService;

    public CdrKafkaConsumer(CdrService cdrService) {
        this.cdrService = cdrService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Cdr cdr) {
        try {
            log.info("Received CDR event: {}", cdr);
            cdrService.createCdr(cdr);
            log.info("Successfully processed CDR event");
        } catch (Exception e) {
            log.error("Error processing CDR event: {}", e.getMessage(), e);
            // TODO: Implement retry mechanism or dead letter queue
        }
    }
} 