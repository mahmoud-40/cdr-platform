// src/main/java/com/cdr/msloader/service/FileProcessor.java
package com.cdr.msloader.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.parser.CDRParser;
import com.cdr.msloader.parser.CDRParserFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class FileProcessor {
    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);
    private final CDRParserFactory parserFactory;
    private final MeterRegistry meterRegistry;
    private final Counter processedFilesCounter;
    private final Counter failedFilesCounter;
    private final Counter processedRecordsCounter;

    @Value("${app.file.processing.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    @Autowired
    public FileProcessor(CDRParserFactory parserFactory, MeterRegistry meterRegistry) {
        this.parserFactory = parserFactory;
        this.meterRegistry = meterRegistry;
        this.processedFilesCounter = Counter.builder("cdr.files.processed")
                .description("Number of files processed successfully")
                .register(meterRegistry);
        this.failedFilesCounter = Counter.builder("cdr.files.failed")
                .description("Number of files that failed processing")
                .register(meterRegistry);
        this.processedRecordsCounter = Counter.builder("cdr.records.processed")
                .description("Number of CDR records processed")
                .register(meterRegistry);
    }

    public List<CDR> processFile(File file) throws IOException {
        validateFile(file);
        
        try {
            CDRParser parser = parserFactory.getParser(file);
            List<CDR> records = parser.parse(file);
            processedFilesCounter.increment();
            processedRecordsCounter.increment(records.size());
            return records;
        } catch (Exception e) {
            failedFilesCounter.increment();
            log.error("Error processing file: {}", file.getName(), e);
            throw new IOException("Error processing file: " + file.getName(), e);
        }
    }

    private void validateFile(File file) throws IOException {
        if (!file.exists()) {
            failedFilesCounter.increment();
            throw new IOException("File does not exist: " + file.getName());
        }

        if (!file.isFile()) {
            failedFilesCounter.increment();
            throw new IOException("Not a file: " + file.getName());
        }

        if (file.length() > maxFileSize) {
            failedFilesCounter.increment();
            throw new IOException("File too large: " + file.getName() + ". Max size: " + maxFileSize + " bytes");
        }

        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null || !isValidMimeType(mimeType)) {
            failedFilesCounter.increment();
            throw new IOException("Invalid file type: " + file.getName() + ". Mime type: " + mimeType);
        }
    }

    private boolean isValidMimeType(String mimeType) {
        if (mimeType == null) {
            return true; // Allow null MIME types for YAML files
        }
        return mimeType.equals("text/csv") ||
               mimeType.equals("application/json") ||
               mimeType.equals("text/xml") ||
               mimeType.equals("text/yaml") ||
               mimeType.equals("application/x-yaml");
    }
}