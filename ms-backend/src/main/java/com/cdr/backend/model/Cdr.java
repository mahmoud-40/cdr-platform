package com.cdr.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.cdr.backend.config.ArrayToLocalDateTimeDeserializer;

@Entity
@Table(name = "cdrs")
public class Cdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String source;
    private String destination;
    @JsonDeserialize(using = ArrayToLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    private String service;
    private Integer usage;
    
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
        return usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }
    
    @PostLoad
    public void validate() {
        if (service.equals("SMS") && usage != 1) {
            throw new IllegalStateException("SMS usage must be 1");
        }
        if (service.equals("DATA") && !destination.startsWith("http")) {
            throw new IllegalStateException("DATA destination must be a URL");
        }
    }
} 