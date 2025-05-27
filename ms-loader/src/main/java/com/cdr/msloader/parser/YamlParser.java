package com.cdr.msloader.parser;

import com.cdr.msloader.entity.CDR;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class YamlParser implements CDRParser {
    private static final Logger log = LoggerFactory.getLogger(YamlParser.class);
    private final ObjectMapper yamlMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    public YamlParser(ObjectMapper objectMapper) {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public List<CDR> parse(File file) throws Exception {
        log.debug("Parsing YAML file: {}", file.getName());
        List<CDR> records = new ArrayList<>();
        
        try {
            // Read the YAML file as a list of maps
            List<Map<String, Object>> yamlData = yamlMapper.readValue(file, List.class);
            
            for (Map<String, Object> data : yamlData) {
                try {
                    CDR cdr = new CDR();
                    cdr.setSource((String) data.get("source"));
                    cdr.setDestination((String) data.get("destination"));
                    cdr.setStartTime(parseDateTime((String) data.get("starttime")));
                    cdr.setService((String) data.get("service"));
                    cdr.setUsage(parseUsage(data.get("usage"), (String) data.get("service")));
                    records.add(cdr);
                } catch (DateTimeParseException e) {
                    log.error("Error parsing date in YAML file: {}", file.getName(), e);
                    throw new IOException("Invalid date format in YAML file. Expected ISO-8601 format (yyyy-MM-ddTHH:mm:ss)", e);
                } catch (NumberFormatException e) {
                    log.error("Error parsing usage in YAML file: {}", file.getName(), e);
                    throw new IOException("Invalid usage format in YAML file. Expected a number.", e);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing YAML file: {}", file.getName(), e);
            throw e;
        }
        
        return records;
    }

    @Override
    public boolean canHandle(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".yaml") || name.endsWith(".yml");
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    private Integer parseUsage(Object usage, String service) {
        if (service.equals("SMS")) {
            return 1;
        }
        if (usage instanceof Number) {
            return ((Number) usage).intValue();
        }
        return Integer.parseInt(usage.toString());
    }
} 