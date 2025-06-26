package com.cdr.backend.controller;

import com.cdr.backend.dto.CdrDto;
import com.cdr.backend.dto.CdrReportDto;
import com.cdr.backend.dto.CreateCdrDto;
import com.cdr.backend.dto.UpdateCdrDto;
import com.cdr.backend.service.CdrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cdrs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8083"})
@PreAuthorize("hasRole('ADMIN')")
public class CdrController {

    private final CdrService cdrService;

    @Autowired
    public CdrController(CdrService cdrService) {
        this.cdrService = cdrService;
    }

    @GetMapping
    public ResponseEntity<List<CdrDto>> getAllCdrs() {
        return ResponseEntity.ok(cdrService.getAllCdrs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CdrDto> getCdrById(@PathVariable Long id) {
        return ResponseEntity.ok(cdrService.getCdrById(id));
    }

    @PostMapping
    public ResponseEntity<CdrDto> createCdr(@RequestBody CreateCdrDto cdrDto) {
        return ResponseEntity.ok(cdrService.createCdr(cdrDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CdrDto> updateCdr(@PathVariable Long id, @RequestBody UpdateCdrDto cdrDto) {
        return ResponseEntity.ok(cdrService.updateCdr(id, cdrDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CdrDto> patchCdr(@PathVariable Long id, @RequestBody UpdateCdrDto cdrDto) {
        return ResponseEntity.ok(cdrService.updateCdr(id, cdrDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCdr(@PathVariable Long id) {
        cdrService.deleteCdr(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/source/{source}")
    public ResponseEntity<List<CdrDto>> getCdrsBySource(@PathVariable String source) {
        return ResponseEntity.ok(cdrService.getCdrsBySource(source));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<CdrDto>> getCdrsByDestination(@PathVariable String destination) {
        return ResponseEntity.ok(cdrService.getCdrsByDestination(destination));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/service/{service}")
    public ResponseEntity<List<CdrDto>> getCdrsByService(@PathVariable String service) {
        return ResponseEntity.ok(cdrService.getCdrsByService(service));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/date-range")
    public ResponseEntity<List<CdrDto>> getCdrsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(cdrService.getCdrsByDateRange(start, end));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/report")
    public ResponseEntity<List<CdrReportDto>> getUsageReport() {
        return ResponseEntity.ok(cdrService.getUsageReport());
    }
} 