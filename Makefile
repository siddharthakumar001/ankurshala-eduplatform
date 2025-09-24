.PHONY: help dev-up dev-down dev-logs dev-clean build-frontend build-backend test-frontend test-backend fe-dev fe-test-e2e smoke seed-dev

# Default target
help:
	@echo "AnkurShala Development Commands"
	@echo "================================"
	@echo "dev-up          - Start all development services"
	@echo "dev-down         - Stop all development services"
	@echo "dev-logs         - Show logs from all services"
	@echo "dev-clean        - Stop services and remove volumes"
	@echo "build-frontend   - Build frontend Docker image"
	@echo "build-backend    - Build backend Docker image"
	@echo "test-frontend    - Run frontend tests"
	@echo "test-backend     - Run backend tests"
	@echo "fe-dev           - Start frontend development server"
	@echo "fe-test-e2e      - Run frontend E2E tests"
	@echo "smoke            - Run smoke tests (start services and check health)"
	@echo "seed-dev         - Seed database with demo data"
	@echo "setup            - Initial setup (copy env file)"

# Development commands
dev-up:
	@echo "Starting AnkurShala development environment..."
	docker-compose -f docker-compose.dev.yml up -d
	@echo "Services starting up..."
	@echo "Frontend: http://localhost:3000"
	@echo "Backend: http://localhost:8080"
	@echo "MailHog: http://localhost:8025"
	@echo "PostgreSQL: localhost:5432"
	@echo "Redis: localhost:6379"
	@echo "Kafka: localhost:9092"
	@echo "LocalStack: http://localhost:4566"

dev-down:
	@echo "Stopping AnkurShala development environment..."
	docker-compose -f docker-compose.dev.yml down

dev-logs:
	docker-compose -f docker-compose.dev.yml logs -f

dev-clean:
	@echo "Cleaning up AnkurShala development environment..."
	docker-compose -f docker-compose.dev.yml down -v
	docker system prune -f

# Build commands
build-frontend:
	@echo "Building frontend Docker image..."
	docker build -t ankurshala-frontend ./frontend

build-backend:
	@echo "Building backend Docker image..."
	docker build -t ankurshala-backend ./backend

# Test commands
test-frontend:
	@echo "Running frontend tests..."
	cd frontend && npm test

test-backend:
	@echo "Running backend tests..."
	cd backend && ./mvnw test

# Setup commands
setup:
	@echo "Setting up AnkurShala..."
	@if [ ! -f .env ]; then \
		cp env.example .env; \
		echo "Created .env file from env.example"; \
		echo "Please update .env with your configuration"; \
	else \
		echo ".env file already exists"; \
	fi
	@echo "Setup complete!"

# Frontend development
fe-dev:
	@echo "Starting frontend development server..."
	cd frontend && npm install && npm run dev

# Frontend E2E tests
fe-test-e2e:
	@echo "Running frontend E2E tests..."
	cd frontend && npm run test:e2e

# Smoke tests
smoke:
	@echo "Running smoke tests..."
	@echo "Starting services..."
	docker-compose -f docker-compose.dev.yml up -d
	@echo "Waiting for services to be ready..."
	@sleep 30
	@echo "Checking backend health..."
	@curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1 || (echo "Backend not ready" && exit 1)
	@echo "Starting frontend..."
	cd frontend && npm run dev &
	@sleep 10
	@echo "Checking frontend..."
	@curl -f http://localhost:3000 > /dev/null 2>&1 || (echo "Frontend not ready" && exit 1)
	@echo "✅ Smoke tests passed!"
	@echo "Frontend: http://localhost:3000"
	@echo "Backend: http://localhost:8080/api/actuator/health"

# Seed development data
seed-dev:
	@echo "Seeding comprehensive demo data..."
	@curl -s -X POST http://localhost:8080/api/admin/dev-seed -H "Content-Type: application/json" -d '{}' | jq . || (echo "Failed to seed data. Make sure backend is running." && exit 1)
	@echo "✅ Demo data seeded successfully!"
	@echo ""
	@echo "📋 Demo Credentials (Password: Maza@123):"
	@echo "  Admin: siddhartha@ankurshala.com"
	@echo "  Students: student1@ankurshala.com to student5@ankurshala.com"
	@echo "  Teachers: teacher1@ankurshala.com to teacher5@ankurshala.com"

# Health check
health:
	@echo "Checking service health..."
	@echo "Frontend: $$(curl -s -o /dev/null -w '%{http_code}' http://localhost:3000 || echo 'DOWN')"
	@echo "Backend: $$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/actuator/health || echo 'DOWN')"
	@echo "PostgreSQL: $$(docker exec ankur_db pg_isready -U ankur -d ankurshala > /dev/null 2>&1 && echo 'UP' || echo 'DOWN')"
	@echo "Redis: $$(docker exec ankur_redis redis-cli ping > /dev/null 2>&1 && echo 'UP' || echo 'DOWN')"
	@echo "Kafka: $$(docker exec ankur_kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1 && echo 'UP' || echo 'DOWN')"
