// src/main/java/com/cdr/msloader/parser/JsonParser.java
package com.cdr.msloader.parser;

import com.cdr.msloader.entity.CDR;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class JsonParser implements CDRParser {
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CDR> parse(File file) throws Exception {
        return objectMapper.readValue(file, new TypeReference<List<CDR>>() {});
    }

    @Override
    public boolean canHandle(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".json");
    }
}