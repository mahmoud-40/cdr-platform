# MS Loader

A Spring Boot microservice for processing Call Detail Records (CDR) from various file formats (CSV, JSON, XML, YAML).

## Features

- **Multi-Format Support**: Parses CDR files in CSV, JSON, XML, and YAML formats.
- **File Validation**: Validates file existence, size, and MIME type.
- **Metrics**: Tracks processed files, failed files, and processed records using Micrometer.
- **Integration**: Uses PostgreSQL for persistence and Kafka for event streaming.
- **Docker Support**: Includes Dockerfile and docker-compose.yml for containerized deployment.

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
   docker-compose up
   ```

## Configuration

- **application.yml**: Main Spring Boot configuration.
- **application-docker.properties**: Docker-specific configuration.
- **application-test.properties**: Test-specific configuration.

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

## Testing

Run tests with:
```bash
./mvnw test
```
