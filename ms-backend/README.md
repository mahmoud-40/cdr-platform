# ms-backend (CDR Platform Backend Service)

A robust, secure, and scalable backend service for the CDR Platform. It manages Call Detail Records (CDRs), exposes REST APIs for frontend clients, processes real-time CDR events from Kafka, and enforces security with Keycloak.

---

## Table of Contents
- [Glossary](#glossary)
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [API Endpoints](#api-endpoints)
- [Data Model](#data-model)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Development & Testing](#development--testing)
- [License](#license)

---

## Glossary
- **CDR**: Call Detail Record – a log of a telecom event (call, SMS, or data session).
- **Keycloak**: Open-source identity and access management.
- **Kafka**: Distributed event streaming platform.
- **JPA**: Java Persistence API, for database access.
- **OpenAPI/Swagger**: API documentation and testing tool.

---

## Overview
ms-backend is the core backend service for the CDR Platform. It provides secure REST APIs for managing CDRs, processes real-time CDR events from Kafka, and integrates with Keycloak for authentication and authorization. It is designed for reliability, observability, and ease of deployment.

---

## Features
- **CDR Management**: Full CRUD operations for call detail records.
- **Real-time Processing**: Consumes CDR events from Kafka (produced by ms-loader).
- **Security**: OAuth2 resource server with JWT authentication via Keycloak.
- **API Documentation**: OpenAPI/Swagger UI for easy API exploration.
- **Monitoring**: Spring Boot Actuator endpoints for health checks and metrics.
- **Error Handling**: Global exception handling for robust APIs.

---

## Tech Stack
- **Java 17**
- **Spring Boot 3.2.x**
- **Spring Security & OAuth2**
- **Spring Data JPA**
- **Spring Kafka**
- **MySQL**
- **Keycloak**
- **Docker**
- **OpenAPI/Swagger**
- **Prometheus (metrics)**

---

## API Endpoints

### CDR Management
- `GET /api/cdrs` — List all CDR records
- `GET /api/cdrs/{id}` — Get a CDR by ID
- `POST /api/cdrs` — Create a new CDR
- `PUT /api/cdrs/{id}` — Update a CDR
- `DELETE /api/cdrs/{id}` — Delete a CDR

### Reporting
- `GET /api/cdrs/report` — Get usage report (aggregated)
- `GET /api/cdrs/report?startDate={date}&endDate={date}` — Filtered usage report

---

## Data Model

Each CDR record contains:
- **id**: Unique identifier
- **source**: Calling number (ANUM)
- **destination**: Called number (BNUM) for VOICE/SMS, or URL for DATA
- **startTime**: Service start timestamp (ISO-8601)
- **service**: VOICE, SMS, or DATA
- **usage**: Minutes (VOICE), MB (DATA), or 1 (SMS)

---

## Configuration

All configuration is managed in `src/main/resources/application.yml`.

**Key settings:**
- **Server Port**: Default is 8082.
- **Database**: MySQL connection details.
- **Kafka**: Broker, consumer, and producer settings.
- **Security**: Keycloak and OAuth2 resource server settings.
- **Monitoring**: Actuator endpoints for health and metrics.
- **Logging**: Log levels and patterns.

Example snippet:
```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/cdr_db
    username: root
    password: root
  kafka:
    bootstrap-servers: kafka:29092
    consumer:
      group-id: cdr-group
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/auth/realms/cdr-platform
keycloak:
  auth-server-url: http://keycloak:8080/auth
  realm: cdr-platform
  resource: ms-backend
```

---

## Project Structure

```
src/main/java/com/cdr/backend/
├── config/         # Configuration classes (Keycloak, Kafka, etc.)
├── controller/     # REST controllers (API endpoints)
├── model/          # Data models (entities, DTOs)
├── repository/     # Data access layer (JPA repositories)
├── service/        # Business logic and Kafka consumers
├── exception/      # Custom exceptions and global error handling
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL
- Kafka
- Keycloak

### Build the project
```bash
mvn clean install
```

### Run with Docker Compose
```bash
# Build and run all services
docker-compose up -d

# Or run only the backend
docker-compose up -d ms-backend
```

---

## Development & Testing

### Running Tests
```bash
mvn test
```

### API Documentation
- After starting the service, access the interactive API docs at:
  [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

---

## License

This project is licensed under the MIT License. See the LICENSE file for details. 