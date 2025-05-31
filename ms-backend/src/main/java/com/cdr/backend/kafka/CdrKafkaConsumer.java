package com.cdr.backend.kafka;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.service.CdrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CdrKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(CdrKafkaConsumer.class);
    private final CdrService cdrService;

    public CdrKafkaConsumer(CdrService cdrService) {
        this.cdrService = cdrService;
    }

    // DTO to match ms-loader's CDR structure
    private static class LoaderCdrDTO {
        public String source;
        public String destination;
        public String startTime;
        public String service;
        public Integer usage;
    }

    @KafkaListener(topics = "${spring.kafka.topic.cdr}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String rawMessage) {
        log.info("Received raw message: {}", rawMessage);
        try {
            ObjectMapper mapper = new ObjectMapper();
            LoaderCdrDTO loaderCdr = mapper.readValue(rawMessage, LoaderCdrDTO.class);
            log.info("Deserialized LoaderCdrDTO: {}", loaderCdr);
            Cdr newCdr = new Cdr();
            newCdr.setCallingNumber(loaderCdr.source);
            newCdr.setCalledNumber(loaderCdr.destination);
            newCdr.setStartTime(java.time.LocalDateTime.parse(loaderCdr.startTime));
            newCdr.setCallType(Cdr.CallType.valueOf(loaderCdr.service));
            newCdr.setDurationSeconds(loaderCdr.usage);
            // For demonstration, set endTime as startTime + usage seconds (if usage is not null)
            if (loaderCdr.usage != null) {
                newCdr.setEndTime(newCdr.getStartTime().plusSeconds(loaderCdr.usage));
            }
            newCdr.setCallStatus(Cdr.CallStatus.COMPLETED);
            cdrService.createCdr(newCdr);
            log.info("Successfully processed CDR event");
        } catch (Exception e) {
            log.error("Error processing CDR event: {}", e.getMessage(), e);
            // TODO: Implement retry mechanism or dead letter queue
        }
    }
} 