# CDR Backend Service

The backend service for the CDR Platform, responsible for managing Call Detail Records (CDR) and exposing REST APIs for the frontend. This service integrates with ms-loader to process CDR events in real-time.

## Features

- **CDR Management**: CRUD operations for call detail records
- **Real-time Processing**: Kafka consumer for processing CDR events from ms-loader
- **Security**: OAuth2 resource server with JWT authentication (Keycloak integration planned)
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

## Prerequisites

- Java 17 or higher
- Maven
- MySQL
- Kafka

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

- `GET /api/cdrs`: Get all CDR records
- `GET /api/cdrs/{id}`: Get CDR by ID
- `POST /api/cdrs`: Create new CDR record
- `GET /api/cdrs/report`: Get CDR usage report

## Data Model

Each CDR record contains:
- **Source**: Calling number (ANUM)
- **Destination**: Called number (BNUM) for VOICE/SMS, URL for DATA
- **StartTime**: Service start timestamp
- **Service**: VOICE, SMS, or DATA
- **Usage**: Minutes (VOICE), MB (DATA), or 1 (SMS)

## Configuration

The service can be configured through `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/cdr_db
spring.datasource.username=root
spring.datasource.password=root

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=cdr-group

# Security Configuration (Coming Soon)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/auth/realms/cdr
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

The service includes a Dockerfile for containerization:

```bash
# Build the image
docker build -t cdr-backend .

# Run the container
docker run -p 8080:8080 cdr-backend
```

## Upcoming Features

- Keycloak integration for authentication and authorization
- Enhanced reporting capabilities
- Kubernetes deployment support

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 