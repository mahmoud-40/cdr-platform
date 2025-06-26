package com.cdr.msloader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class CdrDto {
    private String source;
    private String destination;
    private String startTime; 
    private String service;
    @JsonProperty("cdr_usage")
    private Integer usage;
} 