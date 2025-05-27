// src/main/java/com/cdr/msloader/service/FileWatcherScheduler.java
package com.cdr.msloader.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private KafkaTemplate<String, CDR> kafkaTemplate;

    @Scheduled(fixedRate = 1000) // 5 minutes
    public void processFiles() {
        File folder = new File("input_files");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    List<CDR> cdrs = fileProcessor.processFile(file);
                    // Save to PostgreSQL
                    cdrRepository.saveAll(cdrs);
                    // Send to Kafka
                    cdrs.forEach(cdr -> kafkaTemplate.send("cdr-topic", cdr));
                    file.delete(); // Delete after processing
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}