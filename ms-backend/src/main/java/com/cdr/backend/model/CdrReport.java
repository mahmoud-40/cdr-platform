package com.cdr.backend.model;

import java.time.LocalDateTime;

public class CdrReport {
    private String date;
    private String service;
    private double totalUsage;

    // Constructor
    public CdrReport(String date, String service, double totalUsage) {
        this.date = date;
        this.service = service;
        this.totalUsage = totalUsage;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getTotalUsage() {
        return totalUsage;
    }

    public void setTotalUsage(double totalUsage) {
        this.totalUsage = totalUsage;
    }
} 