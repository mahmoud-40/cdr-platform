package com.cdr.backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.PostLoad;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cdrs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cdr {
    private static final Logger logger = LoggerFactory.getLogger(Cdr.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @Column(nullable = false)
    @JsonProperty("source")
    private String source;
    
    @Column(nullable = false)
    @JsonProperty("destination")
    private String destination;
    
    @Column(name = "start_time", nullable = false)
    @JsonProperty("startTime")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    @JsonProperty("service")
    private String service;
    
    @Column(name = "cdr_usage", nullable = false)
    @JsonProperty("cdr_usage")
    private Integer usage;
    
    @PostLoad
    public void validate() {
        try {
            if ("SMS".equals(service) && usage != 1) {
                logger.warn("SMS usage is not 1 for CDR ID {}: {}", id, usage);
            }
            if ("DATA".equals(service) && (destination == null || !destination.startsWith("http"))) {
                logger.warn("DATA destination is not a URL for CDR ID {}: {}", id, destination);
            }
        } catch (Exception e) {
            logger.error("Error validating CDR ID {}: {}", id, e.getMessage());
        }
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Cdr{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", startTime=" + startTime +
                ", service='" + service + '\'' +
                ", usage=" + usage +
                '}';
    }
} 