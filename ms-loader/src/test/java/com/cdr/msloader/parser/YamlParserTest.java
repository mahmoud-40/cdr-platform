package com.cdr.msloader.parser;

import com.cdr.msloader.entity.CDR;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlParserTest {
    @Test
    void testParseValidYaml() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.registerModule(new JavaTimeModule());
        YamlParser parser = new YamlParser(objectMapper);
        InputStream is = getClass().getResourceAsStream("/sample-cdr.yaml");
        assertNotNull(is, "Sample YAML file should be present");
        File tempFile = File.createTempFile("sample-cdr", ".yaml");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            is.transferTo(fos);
        }
        List<CDR> cdrs = parser.parse(tempFile);
        assertEquals(2, cdrs.size());
        CDR first = cdrs.get(0);
        assertEquals("12345", first.getSource());
        assertEquals("67890", first.getDestination());
        assertEquals("VOICE", first.getService());
        assertEquals(60, first.getUsage());
        assertNotNull(first.getStartTime());
        CDR second = cdrs.get(1);
        assertEquals("54321", second.getSource());
        assertEquals("09876", second.getDestination());
        assertEquals("SMS", second.getService());
        assertEquals(1, second.getUsage());
        assertNotNull(second.getStartTime());
    }
} 