package com.cdr.backend.service;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.model.CdrReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CdrService {
    List<Cdr> getAllCdrs();
    Cdr getCdrById(Long id);
    Cdr createCdr(Cdr cdr);
    Cdr updateCdr(Long id, Cdr cdrDetails);
    void deleteCdr(Long id);
    Page<Cdr> getAllCdrs(Pageable pageable);
    Page<Cdr> searchCdrs(Map<String, String> filters, Pageable pageable);
    List<Cdr> getCdrsBySource(String source);
    List<Cdr> getCdrsByDestination(String destination);
    List<Cdr> getCdrsByService(String service);
    List<Cdr> getCdrsByDateRange(LocalDateTime start, LocalDateTime end);
    List<CdrReport> getUsageReport();
} 