package com.cdr.backend.service;

import com.cdr.backend.dto.CdrDto;
import com.cdr.backend.dto.CdrReportDto;
import com.cdr.backend.dto.CreateCdrDto;
import com.cdr.backend.dto.UpdateCdrDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CdrService {
    List<CdrDto> getAllCdrs();
    CdrDto getCdrById(Long id);
    CdrDto createCdr(CreateCdrDto cdrDto);
    CdrDto updateCdr(Long id, UpdateCdrDto cdrDto);
    void deleteCdr(Long id);
    Page<CdrDto> getAllCdrs(Pageable pageable);
    Page<CdrDto> searchCdrs(Map<String, String> filters, Pageable pageable);
    List<CdrDto> getCdrsBySource(String source);
    List<CdrDto> getCdrsByDestination(String destination);
    List<CdrDto> getCdrsByService(String service);
    List<CdrDto> getCdrsByDateRange(LocalDateTime start, LocalDateTime end);
    List<CdrReportDto> getUsageReport();
} 