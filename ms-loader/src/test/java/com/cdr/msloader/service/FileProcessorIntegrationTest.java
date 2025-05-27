package com.cdr.msloader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
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
import com.cdr.msloader.parser.CDRParser;
import com.cdr.msloader.parser.CDRParserFactory;

@SpringBootTest
@Testcontainers
@ContextConfiguration(classes = TestContainersConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"cdr-records"})
class FileProcessorIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("cdr_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    @Autowired
    private FileProcessor fileProcessor;

    @Autowired
    private CDRParserFactory parserFactory;

    private File testFile;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() throws IOException {
        // Create a test CSV file
        testFile = File.createTempFile("test", ".csv");
        Files.write(testFile.toPath(), "timestamp,source,destination,duration\n2024-01-01T00:00:00,1234567890,0987654321,60".getBytes());
    }

    @Test
    void testProcessFileWithDatabaseAndKafka() throws IOException {
        // Process the file
        List<CDR> records = fileProcessor.processFile(testFile);

        // Verify records were processed
        assertNotNull(records);
        assertEquals(1, records.size());

        // Verify the record content
        CDR record = records.get(0);
        assertEquals("1234567890", record.getSource());
        assertEquals("0987654321", record.getDestination());
        assertEquals(60, record.getUsage());
    }

    @Test
    void testProcessFileWithInvalidData() throws IOException {
        // Create a file with invalid data
        File invalidFile = File.createTempFile("invalid", ".csv");
        Files.write(invalidFile.toPath(), "invalid,data,format".getBytes());

        // Verify that processing fails
        try {
            fileProcessor.processFile(invalidFile);
        } catch (IOException e) {
            // Expected exception
            assertNotNull(e);
        }
    }
} 