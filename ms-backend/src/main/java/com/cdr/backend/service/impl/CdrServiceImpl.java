package com.cdr.backend.service.impl;

import com.cdr.backend.dto.CdrDto;
import com.cdr.backend.dto.CdrReportDto;
import com.cdr.backend.dto.CreateCdrDto;
import com.cdr.backend.dto.UpdateCdrDto;
import com.cdr.backend.exception.ResourceNotFoundException;
import com.cdr.backend.mapper.CdrMapper;
import com.cdr.backend.entity.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.cdr.backend.service.CdrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.KafkaException;
import com.cdr.backend.exception.CdrProcessingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CdrServiceImpl implements CdrService {
    private static final Logger logger = LoggerFactory.getLogger(CdrServiceImpl.class);

    private final CdrRepository cdrRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.cdr}")
    private String cdrTopic;

    @Autowired
    public CdrServiceImpl(CdrRepository cdrRepository, 
                         KafkaTemplate<String, String> kafkaTemplate,
                         ObjectMapper objectMapper) {
        this.cdrRepository = cdrRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CdrDto> getAllCdrs() {
        return cdrRepository.findAll().stream().map(CdrMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CdrDto getCdrById(Long id) {
        Cdr cdr = cdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDR", "id", id));
        return CdrMapper.toDto(cdr);
    }

    @Override
    @Transactional
    public CdrDto createCdr(CreateCdrDto cdrDto) {
        Cdr cdr = CdrMapper.toEntity(cdrDto);
        Cdr savedCdr = cdrRepository.save(cdr);
        sendToKafka(savedCdr, "CREATE");
        return CdrMapper.toDto(savedCdr);
    }

    @Override
    @Transactional
    public CdrDto updateCdr(Long id, UpdateCdrDto cdrDto) {
        Cdr cdr = cdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDR", "id", id));
        CdrMapper.updateEntityFromDto(cdrDto, cdr);
        Cdr updatedCdr = cdrRepository.save(cdr);
        sendToKafka(updatedCdr, "UPDATE");
        return CdrMapper.toDto(updatedCdr);
    }

    @Override
    @Transactional
    public void deleteCdr(Long id) {
        Cdr cdr = cdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDR", "id", id));
        cdrRepository.delete(cdr);
        sendToKafka(cdr, "DELETE");
    }

    @Override
    public Page<CdrDto> getAllCdrs(Pageable pageable) {
        return cdrRepository.findAll(pageable).map(CdrMapper::toDto);
    }

    @Override
    public Page<CdrDto> searchCdrs(Map<String, String> filters, Pageable pageable) {
        Specification<Cdr> spec = Specification.where(null);
        if (filters.containsKey("source")) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("source"), "%" + filters.get("source") + "%"));
        }
        if (filters.containsKey("destination")) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("destination"), "%" + filters.get("destination") + "%"));
        }
        if (filters.containsKey("service")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("service"), filters.get("service")));
        }
        return cdrRepository.findAll(spec, pageable).map(CdrMapper::toDto);
    }

    @Override
    public List<CdrDto> getCdrsBySource(String source) {
        return cdrRepository.findBySource(source).stream().map(CdrMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CdrDto> getCdrsByDestination(String destination) {
        return cdrRepository.findByDestination(destination).stream().map(CdrMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CdrDto> getCdrsByService(String service) {
        return cdrRepository.findByService(service).stream().map(CdrMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CdrDto> getCdrsByDateRange(LocalDateTime start, LocalDateTime end) {
        return cdrRepository.findByStartTimeBetween(start, end).stream().map(CdrMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CdrReportDto> getUsageReport() {
        List<Cdr> cdrs = cdrRepository.findAll();
        Map<String, Map<String, Double>> usageByDateAndService = new HashMap<>();
        for (Cdr cdr : cdrs) {
            String date = cdr.getStartTime().toLocalDate().toString();
            String service = cdr.getService();
            double usage = cdr.getUsage();
            usageByDateAndService.computeIfAbsent(date, k -> new HashMap<>());
            usageByDateAndService.get(date).merge(service, usage, Double::sum);
        }
        List<CdrReportDto> reports = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : usageByDateAndService.entrySet()) {
            String date = entry.getKey();
            for (Map.Entry<String, Double> serviceEntry : entry.getValue().entrySet()) {
                CdrReportDto dto = new CdrReportDto();
                dto.setDate(date);
                dto.setService(serviceEntry.getKey());
                dto.setTotalUsage(serviceEntry.getValue());
                reports.add(dto);
            }
        }
        return reports;
    }

    private void sendToKafka(Cdr cdr, String operation) {
        try {
            String key = String.format("%s-%d", operation, cdr.getId());
            String value = objectMapper.writeValueAsString(cdr);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(cdrTopic, key, value);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully sent CDR to Kafka: {} with operation: {}", cdr, operation);
                } else {
                    logger.error("Failed to send CDR to Kafka: {} with operation: {}", cdr, operation, ex);
                }
            });
        } catch (JsonProcessingException e) {
            logger.error("JSON processing error sending CDR to Kafka: {} with operation: {}", cdr, operation, e);
            throw new CdrProcessingException("Failed to serialize CDR for Kafka", e);
        } catch (KafkaException e) {
            logger.error("Kafka error sending CDR to Kafka: {} with operation: {}", cdr, operation, e);
            throw new CdrProcessingException("Failed to send CDR to Kafka", e);
        }
    }
} 