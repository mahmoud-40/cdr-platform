package com.cdr.backend.service.impl;

import com.cdr.backend.exception.ResourceNotFoundException;
import com.cdr.backend.model.Cdr;
import com.cdr.backend.repository.CdrRepository;
import com.cdr.backend.service.CdrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CdrServiceImpl implements CdrService {

    private final CdrRepository cdrRepository;
    private final KafkaTemplate<String, Cdr> kafkaTemplate;
    private static final String CDR_TOPIC = "cdr-topic";

    @Autowired
    public CdrServiceImpl(CdrRepository cdrRepository, KafkaTemplate<String, Cdr> kafkaTemplate) {
        this.cdrRepository = cdrRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Cdr> getAllCdrs() {
        return cdrRepository.findAll();
    }

    @Override
    public Cdr getCdrById(Long id) {
        return cdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cdr", "id", id));
    }

    @Override
    @Transactional
    public Cdr createCdr(Cdr cdr) {
        return cdrRepository.save(cdr);
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
        return cdrRepository.save(cdr);
    }

    @Override
    @Transactional
    public void deleteCdr(Long id) {
        Cdr cdr = getCdrById(id);
        cdrRepository.delete(cdr);
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
} 