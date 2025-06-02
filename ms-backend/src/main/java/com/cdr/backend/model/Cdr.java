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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cdrs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @Column(nullable = false)
    private String source;
    
    @Column(nullable = false)
    private String destination;
    
    @Column(name = "start_time", nullable = false)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private String service;
    
    @Column(name = "cdr_usage", nullable = false)
    @JsonProperty("cdr_usage")
    private Integer usage;
    
    @PostLoad
    public void validate() {
        if ("SMS".equals(service) && usage != 1) {
            throw new IllegalStateException("SMS usage must be 1");
        }
        if ("DATA".equals(service) && (destination == null || !destination.startsWith("http"))) {
            throw new IllegalStateException("DATA destination must be a URL");
        }
    }

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

    public static Cdr fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            
            Cdr cdr = new Cdr();
            cdr.setSource(node.get("source").asText());
            cdr.setDestination(node.get("destination").asText());
            cdr.setService(node.get("service").asText());
            
            // Handle duplicate cdr_usage field by taking the first occurrence
            JsonNode usageNode = node.get("cdr_usage");
            if (usageNode != null) {
                cdr.setUsage(usageNode.asInt());
            }
            
            // Handle startTime which is now a string in ISO format
            String startTimeStr = node.get("startTime").asText();
            cdr.setStartTime(LocalDateTime.parse(startTimeStr));
            
            return cdr;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CDR JSON: " + e.getMessage(), e);
        }
    }
} 