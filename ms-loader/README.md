# MS Loader

A Spring Boot microservice for processing Call Detail Records (CDR) from various file formats (CSV, JSON, XML, YAML).

## Features

- **Multi-Format Support**: Parses CDR files in CSV, JSON, XML, and YAML formats.
- **File Validation**: Validates file existence, size, and MIME type.
- **Metrics**: Tracks processed files, failed files, and processed records using Micrometer.
- **Integration**: Uses PostgreSQL for persistence and Kafka for event streaming.
- **Docker Support**: Includes Dockerfile and docker-compose.yml for containerized deployment.
- **Error Handling**: Implements retry mechanisms and dead letter queues for failed records.
- **Real-time Processing**: Processes files as they are added to the input directory.

## Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL
- Kafka

## Setup

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd ms-loader
   ```

2. **Build the Project**:
   ```bash
   ./mvnw clean install
   ```

3. **Run with Docker**:
   ```bash
   docker-compose up -d
   ```

## Configuration

### Main Configuration (application.yml)
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/cdr_db
    username: cdr_user
    password: cdr_password
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

file:
  input:
    directory: /app/input_files
    patterns:
      - "*.csv"
      - "*.json"
      - "*.xml"
      - "*.yaml"
      - "*.yml"
  processing:
    batch-size: 1000
    retry-attempts: 3
    retry-delay: 1000
```

### Docker Configuration
The service is configured to:
- Mount the input directory for file processing
- Connect to PostgreSQL and Kafka services
- Expose metrics and health endpoints

## Usage

Place CDR files in the `input_files/` directory. The service will automatically process them.

### Sample File Formats

- **CSV**:
  ```
  source,destination,startTime,service,usage
  12345,67890,2024-01-01T00:00:00,VOICE,60
  ```

- **JSON**:
  ```json
  [
    {
      "source": "12345",
      "destination": "67890",
      "startTime": "2024-01-01T00:00:00",
      "service": "VOICE",
      "usage": 60
    }
  ]
  ```

- **XML**:
  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <cdrs>
    <cdr>
      <source>12345</source>
      <destination>67890</destination>
      <startTime>2024-01-01T00:00:00</startTime>
      <service>VOICE</service>
      <usage>60</usage>
    </cdr>
  </cdrs>
  ```

- **YAML**:
  ```yaml
  - source: "12345"
    destination: "67890"
    startTime: "2024-01-01T00:00:00"
    service: "VOICE"
    usage: 60
  ```

## Error Handling

The service implements several error handling mechanisms:
- Retry logic for failed record processing
- Dead letter queue for records that fail after all retries
- Detailed error logging and metrics
- File validation before processing

## Monitoring

Access monitoring endpoints:
- Health check: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/actuator/metrics`
- Prometheus: `http://localhost:8081/actuator/prometheus`

## Testing

Run tests with:
```bash
./mvnw test
```

## Docker Compose Integration

The service is part of the CDR Platform stack and can be run with:
```bash
# Run all services
docker-compose up -d

# Run only the loader
docker-compose up -d ms-loader
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
