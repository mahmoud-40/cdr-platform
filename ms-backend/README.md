# CDR Backend Service

The backend service for the CDR Platform, responsible for managing Call Detail Records (CDR) and exposing REST APIs for the frontend. This service integrates with ms-loader to process CDR events in real-time.

## Features

- **CDR Management**: CRUD operations for call detail records
- **Real-time Processing**: Kafka consumer for processing CDR events from ms-loader
- **Security**: OAuth2 resource server with JWT authentication via Keycloak
- **API Documentation**: OpenAPI/Swagger documentation
- **Monitoring**: Spring Boot Actuator for health checks and metrics

## Tech Stack

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- Spring Kafka
- MySQL
- Docker
- Keycloak

## Prerequisites

- Java 17 or higher
- Maven
- MySQL
- Kafka
- Keycloak

## Getting Started

1. **Build the project**
   ```bash
   mvn clean install
   ```

2. **Run with Docker**
   ```bash
   docker-compose up -d ms-backend
   ```

## API Endpoints

### CDR Management
- `GET /api/cdrs`: Get all CDR records
- `GET /api/cdrs/{id}`: Get CDR by ID
- `POST /api/cdrs`: Create new CDR record
- `PUT /api/cdrs/{id}`: Update CDR record
- `DELETE /api/cdrs/{id}`: Delete CDR record

### Reporting
- `GET /api/cdrs/report`: Get CDR usage report
- `GET /api/cdrs/report?startDate={date}&endDate={date}`: Get filtered usage report

## Data Model

Each CDR record contains:
- **id**: Unique identifier
- **source**: Calling number (ANUM)
- **destination**: Called number (BNUM) for VOICE/SMS, URL for DATA
- **startTime**: Service start timestamp (ISO-8601 format)
- **service**: VOICE, SMS, or DATA
- **usage**: Minutes (VOICE), MB (DATA), or 1 (SMS)

## Configuration

The service can be configured through `application.properties`:

```properties
# Server Configuration
server.port=8082

# Database Configuration
spring.datasource.url=jdbc:mysql://mysql:3306/cdr_db
spring.datasource.username=cdr_user
spring.datasource.password=cdr_password

# Kafka Configuration
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=cdr-group

# Security Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:8080/realms/cdr-platform
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:8080/realms/cdr-platform/protocol/openid-connect/certs
```

## Development

### Project Structure

```
src/main/java/com/cdr/backend/
├── config/         # Configuration classes
├── controller/     # REST controllers
├── model/         # Data models
├── repository/    # Data access layer
├── service/       # Business logic
└── exception/     # Custom exceptions
```

### Running Tests

```bash
mvn test
```

## Docker Support

The service is containerized and can be run using Docker Compose:

```bash
# Build and run all services
docker-compose up -d

# Build and run only the backend
docker-compose up -d ms-backend
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 