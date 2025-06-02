package com.cdr.msloader.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cdrs")
public class CDR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String source;
    private String destination;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    private String service;
    
    @Column(name = "cdr_usage")
    private Integer cdr_usage;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getUsage() {
        return cdr_usage;
    }

    public void setUsage(Integer usage) {
        this.cdr_usage = usage;
    }
    
    @PostLoad
    public void validate() {
        if ("SMS".equals(service) && cdr_usage != 1) {
            throw new IllegalStateException("SMS usage must be 1");
        }
        if ("DATA".equals(service) && (destination == null || !destination.startsWith("http"))) {
            throw new IllegalStateException("DATA destination must be a URL");
        }
    }
}
