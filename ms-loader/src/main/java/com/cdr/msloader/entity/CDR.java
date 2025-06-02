package com.cdr.msloader.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

@Data
@Entity
@Table(name = "cdrs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source", "destination", "startTime"})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CDR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @Column(nullable=false)
    private String source;

    @Column(nullable=false)
    private String destination;

    @Column(nullable=false)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @Column(nullable=false)
    private String service;
    
    @Column(name = "cdr_usage", nullable=false)
    @JsonProperty("cdr_usage")
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
        if ("SMS".equals(service) && usage != 1) {
            throw new IllegalStateException("SMS usage must be 1");
        }
        if ("DATA".equals(service) && (destination == null || !destination.startsWith("http"))) {
            throw new IllegalStateException("DATA destination must be a URL");
        }
    }
}
