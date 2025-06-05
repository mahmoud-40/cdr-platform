# CDR Platform

A microservices-based platform for processing and managing Call Detail Records (CDRs) with real-time processing capabilities.

## System Overview

The CDR Platform is designed to handle telecommunications activity logs (CDRs) with the following data structure:
- **Source**: The calling number (ANUM)
- **Destination**: 
  - For VOICE and SMS: the called number (BNUM)
  - For DATA: the accessed URL
- **StartTime**: Timestamp of service initiation
- **Service**: One of VOICE, SMS, or DATA
- **Usage**:
  - VOICE: minutes
  - DATA: megabytes (MB)
  - SMS: fixed value "1"

## Architecture

The platform consists of three main microservices:

### 1. Loader Microservice (ms-loader)
- Processes CDR files in multiple formats (CSV, JSON, YAML, XML)
- Persists records to PostgreSQL
- Publishes events to Kafka for real-time processing
- Implements file validation and error handling
- Provides metrics and monitoring

### 2. Backend Microservice (ms-backend)
- Built with Java 21 and Spring Boot 3.4
- Manages CDR records via RESTful APIs
- Secured with Keycloak (OAuth2/OpenID Connect)
- Consumes Kafka events for data synchronization
- Stores data in MySQL database

### 3. Frontend Application (ms-frontend)
- React-based user interface
- Keycloak authentication integration
- CDR management and visualization
- Usage reporting and analytics
- Responsive Material-UI design

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Docker and Docker Compose
- PostgreSQL
- MySQL
- Kafka
- Keycloak

## Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/mahmoud-40/cdr-platform.git
   cd cdr-platform
   ```

2. **Build and Start All Services**:
   ```bash
   # Build and start all services in one command
   docker-compose up --build
   ```

3. **Access the Application**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8082
   - Loader Service: http://localhost:8081
   - Keycloak: http://localhost:8080
   - Kafdrop (Kafka UI): http://localhost:9000

## Troubleshooting

If the backend or frontend services don't start properly, try restarting them:

```bash
# Restart backend service
docker-compose restart ms-backend

# Restart frontend service
docker-compose restart ms-frontend
```

## Development

### Project Structure
```
cdr-platform/
├── ms-frontend/          # React frontend application
├── ms-backend/          # Spring Boot backend service
├── ms-loader/           # File processing service
├── docker-compose.yml   # Docker Compose configuration
└── README.md           # This file
```

### Running in Development Mode

1. **Frontend**:
   ```bash
   cd ms-frontend
   npm install
   npm run dev
   ```

2. **Backend**:
   ```bash
   cd ms-backend
   ./mvnw spring-boot:run
   ```

3. **Loader**:
   ```bash
   cd ms-loader
   ./mvnw spring-boot:run
   ```

## Deployment

The platform is currently deployed using Docker Compose. Kubernetes deployment will be implemented in a future update.

Each service includes:
- Dockerfile for containerization
- Health checks and monitoring endpoints

## Security

- OAuth2/OpenID Connect authentication via Keycloak
- Secure API endpoints
- Role-based access control
- HTTPS support (to be configured)

## Monitoring

Each service exposes monitoring endpoints:
- Health checks
- Metrics (Prometheus format)
- Application logs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

