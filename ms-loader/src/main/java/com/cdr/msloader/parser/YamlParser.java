package com.cdr.msloader.parser;

import com.cdr.msloader.dto.CdrDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class YamlParser implements CDRParser {
    private static final Logger log = LoggerFactory.getLogger(YamlParser.class);
    private final ObjectMapper yamlMapper;

    @Autowired
    public YamlParser(ObjectMapper objectMapper) {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public List<CdrDto> parse(File file) throws IOException {
        log.debug("Parsing YAML file: {}", file.getName());
        List<CdrDto> records = new ArrayList<>();
        try {
            // Read the YAML file as a list of maps
            List<Map<String, Object>> yamlData = yamlMapper.readValue(file, List.class);
            for (Map<String, Object> data : yamlData) {
                try {
                    CdrDto cdr = new CdrDto();
                    cdr.setSource((String) data.get("source"));
                    cdr.setDestination((String) data.get("destination"));
                    cdr.setStartTime((String) data.get("starttime"));
                    cdr.setService((String) data.get("service"));
                    cdr.setUsage(parseUsage(data.get("usage"), (String) data.get("service")));
                    records.add(cdr);
                } catch (NumberFormatException e) {
                    log.error("Error parsing usage in YAML file: {}", file.getName(), e);
                    throw new IOException("Invalid usage format in YAML file. Expected a number.", e);
                }
            }
        } catch (IOException e) {
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