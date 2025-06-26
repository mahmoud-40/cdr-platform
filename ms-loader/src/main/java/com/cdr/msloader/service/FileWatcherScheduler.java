package com.cdr.msloader.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.dto.CdrDto;
import com.cdr.msloader.mapper.CdrMapper;

@Component
@EnableScheduling
public class FileWatcherScheduler {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherScheduler.class);

    private final FileProcessor fileProcessor;
    private final CdrService cdrService;
    private final String inputDir;

    public FileWatcherScheduler(
        FileProcessor fileProcessor,
        CdrService cdrService,
        @Value("${app.input.dir:/app/input_files}") String inputDir
    ) {
        this.fileProcessor = fileProcessor;
        this.cdrService = cdrService;
        this.inputDir = inputDir;
    }

    @Scheduled(fixedRate = 1000)
    public void processFiles() {
        File folder = new File(inputDir);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    logger.info("Processing file: {}", file.getName());
                    List<CDR> cdrs = fileProcessor.processFile(file);
                    logger.info("Parsed {} CDRs from file {}", cdrs.size(), file.getName());
                    for (CDR cdr : cdrs) {
                        CdrDto dto = CdrMapper.toDto(cdr);
                        cdrService.processCdr(dto);
                    }
                    deleteFile(file);
                } catch (IOException e) {
                    // TODO: Implement retry logic - failed files are moved to a dead letter queue
                    logger.error("Error processing file: {}", file.getName(), e); 
                }
            }
        }
    }

    private void deleteFile(File file) {
        if (file.delete()) {
            logger.info("Successfully deleted file: {}", file.getName());
        } else {
            logger.warn("Failed to delete file: {}", file.getName());
        }
    }
}