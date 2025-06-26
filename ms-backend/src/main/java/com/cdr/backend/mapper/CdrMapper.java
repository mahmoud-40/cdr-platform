package com.cdr.backend.mapper;

import com.cdr.backend.dto.CdrDto;
import com.cdr.backend.dto.CdrReportDto;
import com.cdr.backend.dto.CreateCdrDto;
import com.cdr.backend.dto.UpdateCdrDto;
import com.cdr.backend.entity.Cdr;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CdrMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static Cdr toEntity(CdrDto dto) {
        Cdr entity = new Cdr();
        entity.setSource(dto.getSource());
        entity.setDestination(dto.getDestination());
        entity.setStartTime(LocalDateTime.parse(dto.getStartTime(), FORMATTER));
        entity.setService(dto.getService());
        entity.setUsage(dto.getUsage());
        return entity;
    }

    public static CdrDto toDto(Cdr entity) {
        CdrDto dto = new CdrDto();
        dto.setSource(entity.getSource());
        dto.setDestination(entity.getDestination());
        dto.setStartTime(entity.getStartTime().format(FORMATTER));
        dto.setService(entity.getService());
        dto.setUsage(entity.getUsage());
        return dto;
    }

    public static Cdr toEntity(CreateCdrDto dto) {
        Cdr entity = new Cdr();
        entity.setSource(dto.getSource());
        entity.setDestination(dto.getDestination());
        entity.setStartTime(LocalDateTime.parse(dto.getStartTime(), FORMATTER));
        entity.setService(dto.getService());
        entity.setUsage(dto.getUsage());
        return entity;
    }

    public static void updateEntityFromDto(UpdateCdrDto dto, Cdr entity) {
        if (dto.getSource() != null) entity.setSource(dto.getSource());
        if (dto.getDestination() != null) entity.setDestination(dto.getDestination());
        if (dto.getStartTime() != null) entity.setStartTime(LocalDateTime.parse(dto.getStartTime(), FORMATTER));
        if (dto.getService() != null) entity.setService(dto.getService());
        if (dto.getUsage() != null) entity.setUsage(dto.getUsage());
    }
} 