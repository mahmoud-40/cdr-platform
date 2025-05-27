package com.cdr.msloader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.cdr.msloader.config.TestContainersConfig;
import com.cdr.msloader.entity.CDR;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = TestContainersConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"cdr-records"})
class FileFormatIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("cdr_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    @Autowired
    private FileProcessor fileProcessor;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private File createTempFileFromResource(String resourceName, String extension) throws IOException {
        InputStream is = getClass().getResourceAsStream("/" + resourceName);
        assertNotNull(is, "Resource " + resourceName + " should be present");
        File tempFile = File.createTempFile("test", extension);
        Files.copy(is, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    @Test
    void testProcessJsonFile() throws IOException {
        File jsonFile = createTempFileFromResource("sample-cdr.json", ".json");
        List<CDR> records = fileProcessor.processFile(jsonFile);
        verifyRecords(records);
    }

    @Test
    void testProcessXmlFile() throws IOException {
        File xmlFile = createTempFileFromResource("sample-cdr.xml", ".xml");
        List<CDR> records = fileProcessor.processFile(xmlFile);
        verifyRecords(records);
    }

    @Test
    void testProcessYamlFile() throws IOException {
        File yamlFile = createTempFileFromResource("sample-cdr.yaml", ".yaml");
        List<CDR> records = fileProcessor.processFile(yamlFile);
        verifyRecords(records);
    }

    @Test
    void testProcessCsvFile() throws IOException {
        File csvFile = createTempFileFromResource("sample-cdr.csv", ".csv");
        List<CDR> records = fileProcessor.processFile(csvFile);
        verifyRecords(records);
    }

    private void verifyRecords(List<CDR> records) {
        assertNotNull(records);
        assertEquals(2, records.size());

        // Verify first record
        CDR first = records.get(0);
        assertEquals("12345", first.getSource());
        assertEquals("67890", first.getDestination());
        assertEquals("VOICE", first.getService());
        assertEquals(60, first.getUsage());
        assertNotNull(first.getStartTime());

        // Verify second record
        CDR second = records.get(1);
        assertEquals("54321", second.getSource());
        assertEquals("09876", second.getDestination());
        assertEquals("SMS", second.getService());
        assertEquals(1, second.getUsage());
        assertNotNull(second.getStartTime());
    }
} 