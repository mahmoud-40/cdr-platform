package com.cdr.backend.dto;

import lombok.Data;

@Data
public class CdrReportDto {
    private String date;
    private String service;
    private double totalUsage;
} 