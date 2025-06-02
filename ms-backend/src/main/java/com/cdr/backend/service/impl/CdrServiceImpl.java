package com.cdr.backend.service.impl;

import com.cdr.backend.exception.ResourceNotFoundException;
import com.cdr.backend.model.Cdr;
import com.cdr.backend.model.CdrReport;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.ArrayList;

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
    public List<Cdr> getAllCdrs() {
        return cdrRepository.findAll();
    }

    @Override
    public Cdr getCdrById(Long id) {
        return cdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDR", "id", id));
    }

    @Override
    @Transactional
    public Cdr createCdr(Cdr cdr) {
        Cdr savedCdr = cdrRepository.save(cdr);
        sendToKafka(savedCdr, "CREATE");
        return savedCdr;
    }

    @Override
    @Transactional
    public Cdr updateCdr(Long id, Cdr cdrDetails) {
        Cdr cdr = getCdrById(id);
        cdr.setSource(cdrDetails.getSource());
        cdr.setDestination(cdrDetails.getDestination());
        cdr.setStartTime(cdrDetails.getStartTime());
        cdr.setService(cdrDetails.getService());
        cdr.setUsage(cdrDetails.getUsage());
        
        Cdr updatedCdr = cdrRepository.save(cdr);
        sendToKafka(updatedCdr, "UPDATE");
        return updatedCdr;
    }

    @Override
    @Transactional
    public void deleteCdr(Long id) {
        Cdr cdr = getCdrById(id);
        cdrRepository.delete(cdr);
        sendToKafka(cdr, "DELETE");
    }

    @Override
    public Page<Cdr> getAllCdrs(Pageable pageable) {
        return cdrRepository.findAll(pageable);
    }

    @Override
    public Page<Cdr> searchCdrs(Map<String, String> filters, Pageable pageable) {
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
        
        return cdrRepository.findAll(spec, pageable);
    }

    @Override
    public List<Cdr> getCdrsBySource(String source) {
        return cdrRepository.findBySource(source);
    }

    @Override
    public List<Cdr> getCdrsByDestination(String destination) {
        return cdrRepository.findByDestination(destination);
    }

    @Override
    public List<Cdr> getCdrsByService(String service) {
        return cdrRepository.findByService(service);
    }

    @Override
    public List<Cdr> getCdrsByDateRange(LocalDateTime start, LocalDateTime end) {
        return cdrRepository.findByStartTimeBetween(start, end);
    }

    @Override
    public List<CdrReport> getUsageReport() {
        List<Cdr> cdrs = cdrRepository.findAll();
        Map<String, Map<String, Double>> usageByDateAndService = new HashMap<>();

        for (Cdr cdr : cdrs) {
            String date = cdr.getStartTime().toLocalDate().toString();
            String service = cdr.getService();
            double usage = cdr.getUsage();

            usageByDateAndService.computeIfAbsent(date, k -> new HashMap<>());
            usageByDateAndService.get(date).merge(service, usage, Double::sum);
        }

        List<CdrReport> reports = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : usageByDateAndService.entrySet()) {
            String date = entry.getKey();
            for (Map.Entry<String, Double> serviceEntry : entry.getValue().entrySet()) {
                reports.add(new CdrReport(date, serviceEntry.getKey(), serviceEntry.getValue()));
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
        } catch (Exception e) {
            logger.error("Error sending CDR to Kafka: {} with operation: {}", cdr, operation, e);
        }
    }
} 