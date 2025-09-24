# AnkurShala Backend

This is the backend application for AnkurShala, built with Spring Boot 3 and Java 17.

## Features

- **Spring Boot 3** with Java 17
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **Spring Validation** for input validation
- **Spring WebSocket** for real-time communication
- **Spring Actuator** for monitoring and health checks
- **Flyway** for database migrations
- **PostgreSQL** database support
- **Redis** for caching
- **Apache Kafka** for messaging

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- PostgreSQL 13 or later
- Redis 6 or later

### Installation

```bash
# Install dependencies
./mvnw dependency:resolve

# Run tests
./mvnw test

# Start application
./mvnw spring-boot:run
```

### Development

The application runs on http://localhost:8080

### Environment Variables

Create a `.env` file or set environment variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=ankur
DB_PASSWORD=password
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BROKERS=localhost:9092
JWT_SECRET=your-secret-key
```

## Project Structure

```
src/main/java/com/ankurshala/backend/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── repository/          # Data repositories
├── security/            # Security configuration
└── service/             # Business logic

src/main/resources/
├── db/migration/        # Flyway migrations
└── application.yml      # Application configuration
```

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signin` - User login

### Health & Monitoring
- `GET /api/actuator/health` - Application health
- `GET /api/actuator/info` - Application info
- `GET /api/actuator/metrics` - Application metrics

## Database Migrations

Database schema is managed by Flyway migrations. Migrations are located in `src/main/resources/db/migration/`.

To create a new migration:
1. Create a new file: `V{version}__{description}.sql`
2. Add your SQL statements
3. Restart the application

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest

# Run tests with coverage
./mvnw test jacoco:report
```

## Building for Production

```bash
# Build JAR file
./mvnw clean package

# Build Docker image
docker build -t ankurshala-backend .
```

## Available Scripts

- `./mvnw spring-boot:run` - Start development server
- `./mvnw test` - Run tests
- `./mvnw clean package` - Build JAR file
- `./mvnw dependency:tree` - Show dependency tree
