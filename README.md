# AnkurShala

A modern educational platform built with Next.js 14 and Spring Boot 3, featuring a complete development environment with Docker Compose.

## 🏗️ Architecture

### Frontend
- **Next.js 14** with App Router
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **shadcn/ui** for UI components
- **React Query** for data fetching
- **Zustand** for state management
- **FullCalendar** for calendar functionality
- **Recharts** for data visualization

### Backend
- **Spring Boot 3** with Java 17
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **Spring Validation** for input validation
- **Spring WebSocket** for real-time communication
- **Spring Actuator** for monitoring
- **Flyway** for database migrations

### Infrastructure
- **PostgreSQL** for primary database
- **Redis** for caching and sessions
- **Apache Kafka** with Zookeeper for messaging
- **MailHog** for email testing
- **LocalStack** for AWS services (S3, etc.)

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Make (optional, for convenience commands)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ankurshala
   ```

2. **Initial setup**
   ```bash
   make setup
   ```
   This creates a `.env` file from the example template.

3. **Start the development environment**
   ```bash
   make dev-up
   ```

4. **Seed demo data**
   ```bash
   make seed-dev
   ```

5. **Start frontend development server**
   ```bash
   make fe-dev
   ```

6. **Verify services are running**
   ```bash
   make health
   ```

### Access Points

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Backend Health**: http://localhost:8080/api/actuator/health
- **API Documentation**: http://localhost:8080/api/actuator
- **MailHog UI**: http://localhost:8025
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **LocalStack**: http://localhost:4566

### Demo Credentials (Development Only)

After running `make seed-dev`, you can use these credentials:

- **Admin**: siddhartha@ankurshala.com / Maza@123
- **Students**: student1@ankurshala.com to student5@ankurshala.com / Maza@123
- **Teachers**: teacher1@ankurshala.com to teacher5@ankurshala.com / Maza@123

⚠️ **Important**: These are demo credentials for development only. Change them in production!

## 🛠️ Development Commands

### Docker Compose Commands
```bash
# Start all services
make dev-up

# Stop all services
make dev-down

# View logs
make dev-logs

# Clean up (remove volumes)
make dev-clean

# Check service health
make health

# Seed demo data
make seed-dev
```

### Individual Service Commands
```bash
# Build frontend
make build-frontend

# Build backend
make build-backend

# Run frontend tests
make test-frontend

# Run backend tests
make test-backend

# Start frontend development server
make fe-dev

# Run frontend E2E tests
make fe-test-e2e

# Run comprehensive E2E test suite
./scripts/run-local-e2e.sh
```

### Manual Docker Commands
```bash
# Start specific services
docker-compose -f docker-compose.dev.yml up postgres redis

# Scale services
docker-compose -f docker-compose.dev.yml up --scale backend=2

# View service logs
docker-compose -f docker-compose.dev.yml logs backend

# Execute commands in containers
docker-compose -f docker-compose.dev.yml exec backend bash
docker-compose -f docker-compose.dev.yml exec postgres psql -U ankur -d ankurshala
```

## 📁 Project Structure

```
ankurshala/
├── frontend/                 # Next.js 14 frontend
│   ├── src/
│   │   ├── app/             # App Router pages
│   │   ├── components/      # React components
│   │   ├── lib/             # Utilities
│   │   └── store/           # Zustand stores
│   ├── Dockerfile
│   └── package.json
├── backend/                  # Spring Boot 3 backend
│   ├── src/main/java/
│   │   └── com/ankurshala/backend/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data repositories
│   │       ├── security/    # Security configuration
│   │       └── service/     # Business logic
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway migrations
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.dev.yml   # Development environment
├── .github/workflows/       # GitHub Actions CI/CD
├── Makefile                 # Convenience commands
├── env.example             # Environment variables template
└── README.md
```

## 🔧 Configuration

### Environment Variables

Copy `env.example` to `.env` and customize:

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=ankur
DB_PASSWORD=password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BROKERS=localhost:9092
```

### Database Migrations

Database schema is managed by Flyway migrations in `backend/src/main/resources/db/migration/`. 

To create a new migration:
1. Create a new file: `V{version}__{description}.sql`
2. Add your SQL statements
3. Restart the backend service

## 🧪 Testing

### Frontend Tests
```bash
cd frontend
npm test
```

### Backend Tests
```bash
cd backend
./mvnw test
```

### E2E Testing (Playwright)

#### ⚡ Quick Start (Recommended) - NEW January 10, 2026
```powershell
# One command to start everything and run E2E tests
.\run-e2e-local.ps1

# View test report
cd frontend
npx playwright show-report
```

**What it does:**
- ✅ Starts backend with DEV AI mode (zero external API costs)
- ✅ Auto-seeds deterministic test data (5 test accounts)
- ✅ Starts frontend dev server
- ✅ Runs complete Playwright test suite
- ✅ Generates HTML report

**Test Credentials (Auto-Seeded):**
- Student 1: `student-e2e1@ankurshala.com` / `Test@123`
- Student 2: `student-e2e2@ankurshala.com` / `Test@123`
- Admin: `admin-e2e@ankurshala.com` / `Test@123`

#### Manual E2E Test Execution
```bash
# 1) Start services
make dev-up

# 2) Seed demo data
make seed-dev

# 3) Start frontend
make fe-dev

# 4) Run E2E tests (in another terminal)
cd frontend
npm run test:e2e              # Headless mode
npm run test:e2e:headed       # With browser UI
npm run test:e2e:ui           # Interactive UI mode
```

#### E2E Test Coverage

**Student Flow Tests (NEW):**
- ✅ **Dashboard Navigation**: Stats, quick actions, upcoming classes
- ✅ **Today Home**: Daily plan, progress tracking, step completion
- ✅ **AI Tutor Chat**: Streaming responses, suggested actions, voice mode
- ✅ **Practice Flow**: Start session, submit answers, view results
- ✅ **Notes Creation**: Generate from AI chat, save, organize
- ✅ **Focus Sessions**: Start timer, check-ins, AI scoping
- ✅ **Booking Companion**: Prep generation, warmup, live notes, summary
- ✅ **DEV AI Provider**: Deterministic responses, RAG retrieval

**Authentication & RBAC:**
- ✅ **Authentication Flow**: Login, logout, token refresh
- ✅ **Student Profile**: CRUD operations, document management
- ✅ **Teacher Profile**: All tabs, nested resources, bank details masking
- ✅ **RBAC Enforcement**: Role-based access control
- ✅ **UI Polish**: Form validation, error handling, navigation
- ✅ **Error Pages**: 404, forbidden, unauthenticated redirects

#### Test Artifacts
After running E2E tests, check:
- **Screenshots**: `frontend/test-results/screenshots/`
- **Traces**: `frontend/test-results/traces/`
- **Videos**: `frontend/test-results/videos/`
- **HTML Report**: `frontend/test-results/playwright-report/`
- **Summary**: `frontend/test-results/summary.md`

### Integration Tests
The CI pipeline runs integration tests with all services running in Docker containers.

## 🚀 Deployment

### Docker Images

The project builds optimized Docker images:

- **Frontend**: Multi-stage build with Node.js Alpine
- **Backend**: Multi-stage build with Eclipse Temurin JRE Alpine

### Production Considerations

1. **Security**: Change default JWT secret and database passwords
2. **SSL/TLS**: Configure HTTPS for production
3. **Monitoring**: Enable Spring Actuator metrics
4. **Scaling**: Use Docker Swarm or Kubernetes for orchestration
5. **Database**: Use managed PostgreSQL service
6. **Caching**: Configure Redis clustering
7. **Message Queue**: Use managed Kafka service

## 🔍 Monitoring & Debugging

### Health Checks

All services include health checks:
- Frontend: HTTP endpoint check
- Backend: Spring Actuator health endpoint
- Database: PostgreSQL connection check
- Redis: Ping command
- Kafka: Broker API version check

### Logs

View logs for all services:
```bash
make dev-logs
```

View logs for specific service:
```bash
docker-compose -f docker-compose.dev.yml logs backend
```

### Debugging

Access running containers:
```bash
# Backend container
docker-compose -f docker-compose.dev.yml exec backend bash

# Database container
docker-compose -f docker-compose.dev.yml exec postgres psql -U ankur -d ankurshala

# Redis container
docker-compose -f docker-compose.dev.yml exec redis redis-cli
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Development Workflow

1. Start development environment: `make dev-up`
2. Make changes to frontend or backend
3. Test your changes
4. Run tests: `make test-frontend` and `make test-backend`
5. Commit and push changes

## 📝 API Documentation

### Authentication Endpoints

- `POST /api/auth/signup` - User registration
- `POST /api/auth/signin` - User login

### Health Endpoints

- `GET /api/actuator/health` - Application health
- `GET /api/actuator/info` - Application info
- `GET /api/actuator/metrics` - Application metrics

## 🐛 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 3000, 8080, 5432, 6379, 9092, 8025, 4566 are available
2. **Database connection**: Check PostgreSQL is running and accessible
3. **Memory issues**: Increase Docker memory allocation
4. **Build failures**: Clear Docker cache and rebuild

### Reset Environment

If you encounter issues, reset the entire environment:
```bash
make dev-clean
make dev-up
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Next.js team for the React framework
- Docker team for containerization
- All open-source contributors
# CI/CD Pipeline Test - Thu Oct  2 09:43:20 IST 2025
