package com.cdr.msloader.service;

import com.cdr.msloader.dto.CdrDto;
import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.mapper.CdrMapper;
import com.cdr.msloader.repository.CdrRepository;
import com.cdr.msloader.exception.CdrProcessingException;
import com.cdr.avro.CdrAvro;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CdrService {
    private static final Logger logger = LoggerFactory.getLogger(CdrService.class);

    private final CdrRepository cdrRepository;
    private final KafkaTemplate<String, CdrAvro> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String cdrTopic;

    @Autowired
    public CdrService(CdrRepository cdrRepository,
                      @Qualifier("avroKafkaTemplate") KafkaTemplate<String, CdrAvro> kafkaTemplate,
                      ObjectMapper objectMapper,
                      @Value("${spring.kafka.topic.cdr}") String cdrTopic) {
        this.cdrRepository = cdrRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.cdrTopic = cdrTopic;
    }

    @Transactional
    public void processCdr(CdrDto cdrDto) {
        try {
            // Map DTO to entity and save to PostgreSQL
            CDR cdr = CdrMapper.toEntity(cdrDto);
            cdrRepository.save(cdr);
            logger.info("Saved CDR to PostgreSQL: {}", cdr);

            // Map DTO to Avro and send to Kafka
            CdrAvro avro = new CdrAvro(
                cdr.getSource(),
                cdr.getDestination(),
                cdr.getStartTime().toString(),
                cdr.getService(),
                cdr.getUsage()
            );
            kafkaTemplate.send(cdrTopic, avro);
            logger.info("Sent CDR to Kafka (Avro): {}", avro);
        } catch (DataAccessException e) {
            logger.error("Database error for CDR DTO: {}", cdrDto, e);
            throw new CdrProcessingException("Failed to save CDR to database: " + e.getMessage(), e);
        }
    }
} 