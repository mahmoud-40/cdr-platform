package com.cdr.backend.controller;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.model.CdrReport;
import com.cdr.backend.service.CdrService;
import com.cdr.backend.service.KafkaConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cdrs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8083"})
public class CdrController {

    private final CdrService cdrService;
    private final KafkaConsumerService kafkaConsumerService;

    @Autowired
    public CdrController(CdrService cdrService, KafkaConsumerService kafkaConsumerService) {
        this.cdrService = cdrService;
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @GetMapping
    public ResponseEntity<List<Cdr>> getAllCdrs() {
        return ResponseEntity.ok(cdrService.getAllCdrs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cdr> getCdrById(@PathVariable Long id) {
        return ResponseEntity.ok(cdrService.getCdrById(id));
    }

    @PostMapping
    public ResponseEntity<Cdr> createCdr(@RequestBody Cdr cdr) {
        return ResponseEntity.ok(cdrService.createCdr(cdr));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cdr> updateCdr(@PathVariable Long id, @RequestBody Cdr cdrDetails) {
        return ResponseEntity.ok(cdrService.updateCdr(id, cdrDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCdr(@PathVariable Long id) {
        cdrService.deleteCdr(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<Cdr>> getCdrsBySource(@PathVariable String source) {
        return ResponseEntity.ok(cdrService.getCdrsBySource(source));
    }

    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<Cdr>> getCdrsByDestination(@PathVariable String destination) {
        return ResponseEntity.ok(cdrService.getCdrsByDestination(destination));
    }

    @GetMapping("/service/{service}")
    public ResponseEntity<List<Cdr>> getCdrsByService(@PathVariable String service) {
        return ResponseEntity.ok(cdrService.getCdrsByService(service));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Cdr>> getCdrsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(cdrService.getCdrsByDateRange(start, end));
    }

    @GetMapping("/report")
    public ResponseEntity<List<CdrReport>> getUsageReport() {
        return ResponseEntity.ok(cdrService.getUsageReport());
    }
} 