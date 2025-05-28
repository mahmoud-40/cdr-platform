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
import java.time.format.DateTimeFormatter;
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
        Cdr savedCdr = cdrRepository.save(cdr);
        kafkaTemplate.send(CDR_TOPIC, savedCdr);
        return savedCdr;
    }

    @Override
    @Transactional
    public Cdr updateCdr(Long id, Cdr cdrDetails) {
        Cdr cdr = getCdrById(id);
        cdr.setCallingNumber(cdrDetails.getCallingNumber());
        cdr.setCalledNumber(cdrDetails.getCalledNumber());
        cdr.setStartTime(cdrDetails.getStartTime());
        cdr.setCallType(cdrDetails.getCallType());
        cdr.setDurationSeconds(cdrDetails.getDurationSeconds());
        cdr.setEndTime(cdrDetails.getEndTime());
        cdr.setCallStatus(cdrDetails.getCallStatus());
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
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        if (filters.containsKey("callingNumber")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("callingNumber"), filters.get("callingNumber")));
        }

        if (filters.containsKey("calledNumber")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("calledNumber"), filters.get("calledNumber")));
        }

        if (filters.containsKey("callType")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("callType"), Cdr.CallType.valueOf(filters.get("callType"))));
        }

        if (filters.containsKey("startTimeFrom")) {
            LocalDateTime startTime = LocalDateTime.parse(filters.get("startTimeFrom"), formatter);
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
        }

        if (filters.containsKey("startTimeTo")) {
            LocalDateTime endTime = LocalDateTime.parse(filters.get("startTimeTo"), formatter);
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("startTime"), endTime));
        }

        if (filters.containsKey("callStatus")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("callStatus"), Cdr.CallStatus.valueOf(filters.get("callStatus"))));
        }

        return cdrRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cdr> getCdrsByCallingNumber(String number) {
        return cdrRepository.findByCallingNumber(number);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cdr> getCdrsByCalledNumber(String number) {
        return cdrRepository.findByCalledNumber(number);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cdr> getCdrsByCallType(Cdr.CallType type) {
        return cdrRepository.findByCallType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cdr> getCdrsByDateRange(LocalDateTime start, LocalDateTime end) {
        return cdrRepository.findByStartTimeBetween(start, end);
    }
} 