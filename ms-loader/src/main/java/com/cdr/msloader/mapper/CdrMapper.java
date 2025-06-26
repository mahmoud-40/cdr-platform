package com.cdr.msloader.mapper;

import com.cdr.msloader.dto.CdrDto;
import com.cdr.msloader.entity.CDR;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CdrMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static CDR toEntity(CdrDto dto) {
        CDR entity = new CDR();
        entity.setSource(dto.getSource());
        entity.setDestination(dto.getDestination());
        entity.setStartTime(LocalDateTime.parse(dto.getStartTime(), FORMATTER));
        entity.setService(dto.getService());
        entity.setUsage(dto.getUsage());
        return entity;
    }

    public static CdrDto toDto(CDR entity) {
        CdrDto dto = new CdrDto();
        dto.setSource(entity.getSource());
        dto.setDestination(entity.getDestination());
        dto.setStartTime(entity.getStartTime().format(FORMATTER));
        dto.setService(entity.getService());
        dto.setUsage(entity.getUsage());
        return dto;
    }
} 