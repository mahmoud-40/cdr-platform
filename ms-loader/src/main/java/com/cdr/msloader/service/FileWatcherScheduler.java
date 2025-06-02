// src/main/java/com/cdr/msloader/service/FileWatcherScheduler.java
package com.cdr.msloader.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdr.msloader.entity.CDR;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@EnableScheduling
public class FileWatcherScheduler {
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherScheduler.class);

    private final FileProcessor fileProcessor;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String cdrTopic;
    private final String inputDir;

    public FileWatcherScheduler(
        FileProcessor fileProcessor,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${spring.kafka.topic.cdr}") String cdrTopic,
        @Value("${app.input.dir:/app/input_files}") String inputDir
    ) {
        this.fileProcessor = fileProcessor;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.cdrTopic = cdrTopic;
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
                    sendToKafka(cdrs);
                    deleteFile(file);
                } catch (IOException e) {
                    logger.error("Error processing file: {}", file.getName(), e);
                }
            }
        }
    }

    private void sendToKafka(List<CDR> cdrs) {
        for (CDR cdr : cdrs) {
            try {
                // Convert to ObjectNode to manipulate JSON
                ObjectNode cdrNode = objectMapper.valueToTree(cdr);
                // Remove the id field
                cdrNode.remove("id");
                // Convert usage to cdr_usage
                if (cdrNode.has("usage")) {
                    cdrNode.set("cdr_usage", cdrNode.get("usage"));
                    cdrNode.remove("usage");
                }
                
                String jsonMessage = objectMapper.writeValueAsString(cdrNode);
                kafkaTemplate.send(cdrTopic, jsonMessage);
                logger.info("Sent CDR to Kafka: {}", jsonMessage);
            } catch (Exception e) {
                logger.error("Error sending CDR to Kafka: {}", cdr, e);
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