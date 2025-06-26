package com.cdr.msloader.service;

import com.cdr.msloader.dto.CdrDto;
import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.mapper.CdrMapper;
import com.cdr.msloader.parser.CDRParser;
import com.cdr.msloader.parser.CDRParserFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileProcessorTest {

    @TempDir
    Path tempDir;

    @Mock
    private CDRParserFactory parserFactory;

    @Mock
    private CDRParser mockParser;

    private FileProcessor fileProcessor;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        meterRegistry = new SimpleMeterRegistry();
        fileProcessor = new FileProcessor(parserFactory, meterRegistry);
        ReflectionTestUtils.setField(fileProcessor, "maxFileSize", 10485760L); // 10MB
    }

    @Test
    void testProcessValidFile() throws Exception {
        // Create a test.csv file
        File testFile = tempDir.resolve("test.csv.csv").toFile();
        Files.write(testFile.toPath(), "test.csv content".getBytes());

        // Mock parser behavior
        List<CdrDto> dtos = Arrays.asList(
            createDto("123", "456", "2024-01-01T00:00:00", "VOICE", 60),
            createDto("789", "012", "2024-01-01T00:00:00", "SMS", 1)
        );
        when(parserFactory.getParser(any(File.class))).thenReturn(mockParser);
        when(mockParser.parse(any(File.class))).thenReturn(dtos);

        // Process the file
        List<CDR> result = fileProcessor.processFile(testFile);

        // Verify results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CdrMapper.toEntity(dtos.get(0)), result.get(0));
        assertEquals(CdrMapper.toEntity(dtos.get(1)), result.get(1));

        // Verify metrics
        assertEquals(1, meterRegistry.get("cdr.files.processed").counter().count());
        assertEquals(2, meterRegistry.get("cdr.records.processed").counter().count());
        assertEquals(0, meterRegistry.get("cdr.files.failed").counter().count());
    }

    @Test
    void testProcessNonExistentFile() {
        File nonExistentFile = new File("nonexistent.csv");
        assertThrows(IOException.class, () -> fileProcessor.processFile(nonExistentFile));
        assertEquals(0, meterRegistry.get("cdr.files.processed").counter().count());
        assertEquals(1, meterRegistry.get("cdr.files.failed").counter().count());
    }

    @Test
    void testProcessFileTooLarge() throws IOException {
        // Create a file larger than the max size
        File largeFile = tempDir.resolve("large.csv").toFile();
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        Files.write(largeFile.toPath(), largeContent);

        assertThrows(IOException.class, () -> fileProcessor.processFile(largeFile));
        assertEquals(0, meterRegistry.get("cdr.files.processed").counter().count());
        assertEquals(1, meterRegistry.get("cdr.files.failed").counter().count());
    }

    @Test
    void testProcessFileWithParserError() throws Exception {
        // Create a test.csv file
        File testFile = tempDir.resolve("test.csv.csv").toFile();
        Files.write(testFile.toPath(), "test.csv content".getBytes());

        // Mock parser to throw an exception
        when(parserFactory.getParser(any(File.class))).thenReturn(mockParser);
        doThrow(new RuntimeException("Parser error")).when(mockParser).parse(any(File.class));

        // Verify exception is thrown and metrics are updated
        IOException exception = assertThrows(IOException.class, () -> fileProcessor.processFile(testFile));
        assertTrue(exception.getMessage().contains("Error processing file"));
        assertEquals(0, meterRegistry.get("cdr.files.processed").counter().count());
        assertEquals(1, meterRegistry.get("cdr.files.failed").counter().count());
    }

    @Test
    void testProcessFileWithInvalidMimeType() throws IOException {
        // Create a test.csv file with an invalid extension
        File invalidFile = tempDir.resolve("test.csv.invalid").toFile();
        Files.write(invalidFile.toPath(), "test.csv content".getBytes());

        assertThrows(IOException.class, () -> fileProcessor.processFile(invalidFile));
        assertEquals(0, meterRegistry.get("cdr.files.processed").counter().count());
        assertEquals(1, meterRegistry.get("cdr.files.failed").counter().count());
    }

    private CdrDto createDto(String source, String destination, String startTime, String service, int usage) {
        CdrDto dto = new CdrDto();
        dto.setSource(source);
        dto.setDestination(destination);
        dto.setStartTime(startTime);
        dto.setService(service);
        dto.setUsage(usage);
        return dto;
    }
} 