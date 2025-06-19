# ms-loader (CDR Loader Service)

MS Loader is a microservice that automates the ingestion and processing of Call Detail Records (CDRs)—logs of telecom activity—provided as files in various formats. 
It is part of a microservices-based CDR Platform.

---

## Table of Contents
- [Glossary](#glossary)
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Usage](#usage)
- [Error Handling](#error-handling)
- [Docker Compose Integration](#docker-compose-integration)
- [License](#license)

---

## Glossary
- **CDR**: Call Detail Record – a log of a telecom event (call, SMS, or data session).
- **Kafka**: A distributed event streaming platform.
- **PostgreSQL**: An open-source relational database.

---

## Overview
MS Loader ingests CDR files in CSV, JSON, XML, or YAML format, validates and parses them, stores the records in a PostgreSQL database, and publishes them to Kafka for further processing. It is designed for reliability, observability, and ease of deployment.

---

## Features
- **Multi-Format Parsing**: Supports CSV, JSON, XML, and YAML CDR files.
- **File Validation**: Ensures files are present, of correct type, and not corrupted before processing.
- **Persistence**: Stores parsed records in PostgreSQL.
- **Event Streaming**: Publishes records to Kafka for downstream services.
- **Containerization**: Ready for Docker and Kubernetes deployment.

---

## Architecture
- **Input Directory**: Watches a directory for new CDR files.
- **Parsers**: Modular parsers for each supported file format.
- **Database**: Persists records in PostgreSQL.
- **Kafka Producer**: Publishes records to a Kafka topic.

---

## Prerequisites
- Java 17 or higher
- Maven (for building the project)
- Docker & Docker Compose (for running containers)
- PostgreSQL (database)
- Kafka (event streaming)

---

## Setup
1. **Clone the Repository**
   ```bash
   git clone <https://github.com/mahmoud-40/cdr-platform>
   cd ms-loader
   ```
2. **Build the Project**
   ```bash
   ./mvnw clean install
   ```
3. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

---

## Configuration
All configuration is managed in `src/main/resources/application.yml`.

**Key settings:**
- **Server Port**: Default is 8081.
- **Database**: Connection details for PostgreSQL.
- **Kafka**: Broker and serialization settings.
- **File Input**: Directory and file patterns to watch.
- **Processing**: Batch size and retry logic.

Example snippet:
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

For Docker-specific configuration, see the `docker-compose.yml` file.

---

## Usage
1. **Add CDR Files**: Place your CDR files in the `input_files/` directory (mounted in the container).
2. **Automatic Processing**: The service detects and processes new files automatically.
3. **Supported File Formats**:
   - **CSV**
     ```csv
     source,destination,starttime,service,usage
     12345,67890,2024-01-01T00:00:00,VOICE,60
     ```
   - **JSON**
     ```json
     [
       {
         "source": "12345",
         "destination": "67890",
         "starttime": "2024-01-01T00:00:00",
         "service": "VOICE",
         "usage": 60
       }
     ]
     ```
   - **XML**
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <cdrs>
       <cdr>
         <source>12345</source>
         <destination>67890</destination>
         <starttime>2024-01-01T00:00:00</starttime>
         <service>VOICE</service>
         <usage>60</usage>
       </cdr>
     </cdrs>
     ```
   - **YAML**
     ```yaml
     - source: "12345"
       destination: "67890"
       startTime: "2024-01-01T00:00:00"
       service: "VOICE"
       usage: 60
     ```

---

## Error Handling
- **Retries**: Failed files or records are retried up to a configurable limit.
- **Validation**: Files are checked for existence, type, and size before processing.
- **Logging**: Errors are logged with details for troubleshooting.

---

## Docker Compose Integration
- To run the entire CDR Platform stack:
  ```bash
  docker-compose up -d
  ```
- To run only the loader service:
  ```bash
  docker-compose up -d ms-loader
  ```

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.
