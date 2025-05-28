package com.cdr.backend.controller;

import com.cdr.backend.model.Cdr;
import com.cdr.backend.service.CdrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cdrs")
public class CdrController {

    private final CdrService cdrService;

    @Autowired
    public CdrController(CdrService cdrService) {
        this.cdrService = cdrService;
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

    @GetMapping("/calling/{number}")
    public ResponseEntity<List<Cdr>> getCdrsByCallingNumber(@PathVariable String number) {
        return ResponseEntity.ok(cdrService.getCdrsByCallingNumber(number));
    }

    @GetMapping("/called/{number}")
    public ResponseEntity<List<Cdr>> getCdrsByCalledNumber(@PathVariable String number) {
        return ResponseEntity.ok(cdrService.getCdrsByCalledNumber(number));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Cdr>> getCdrsByCallType(@PathVariable Cdr.CallType type) {
        return ResponseEntity.ok(cdrService.getCdrsByCallType(type));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Cdr>> getCdrsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(cdrService.getCdrsByDateRange(start, end));
    }
} 