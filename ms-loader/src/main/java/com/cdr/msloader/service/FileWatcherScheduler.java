// src/main/java/com/cdr/msloader/service/FileWatcherScheduler.java
package com.cdr.msloader.service;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.cdr.msloader.entity.CDR;
import com.cdr.msloader.repository.CdrRepository;

@Component
@EnableScheduling
public class FileWatcherScheduler {
    @Autowired
    private FileProcessor fileProcessor;

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Value("${spring.kafka.topic.cdr}")
    private String cdrTopic;

    @Scheduled(fixedRate = 1000) // Check every second
    public void processFiles() {
        File folder = new File("/app/input_files");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    List<CDR> cdrs = fileProcessor.processFile(file);
                    // Save to PostgreSQL
                    cdrRepository.saveAll(cdrs);
                    // Send to Kafka as Map
                    cdrs.forEach(cdr -> {
                        Map<String, Object> message = new HashMap<>();
                        message.put("source", cdr.getSource());
                        message.put("destination", cdr.getDestination());
                        message.put("startTime", cdr.getStartTime());
                        message.put("service", cdr.getService());
                        message.put("usage", cdr.getUsage());
                        kafkaTemplate.send(cdrTopic, message);
                    });
                    file.delete(); // Delete after processing
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}