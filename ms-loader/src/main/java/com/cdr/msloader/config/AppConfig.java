package com.cdr.msloader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import java.util.List;
import com.cdr.avro.CdrAvro;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private File file = new File();
    private Processing processing = new Processing();

    @Data
    public static class File {
        private Input input = new Input();
        private Processing processing = new Processing();

        @Data
        public static class Input {
            private String directory;
            private List<String> patterns;
        }
    }

    @Data
    public static class Processing {
        private int batchSize;
        private Schedule schedule = new Schedule();
        private Retry retry = new Retry();

        @Data
        public static class Schedule {
            private String cron;
        }

        @Data
        public static class Retry {
            private int maxAttempts;
            private long initialInterval;
            private double multiplier;
            private long maxInterval;
        }
    }
} 