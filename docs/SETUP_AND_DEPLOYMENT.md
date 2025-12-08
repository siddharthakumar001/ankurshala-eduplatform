# Setup and Deployment Guide

**Last Updated:** October 7, 2025

---

## Table of Contents
- [Local Development Setup](#local-development-setup)
- [Docker Setup](#docker-setup)
- [Environment Configuration](#environment-configuration)
- [Production Deployment](#production-deployment)
- [CI/CD Pipeline](#cicd-pipeline)

---

## Local Development Setup

### Prerequisites
- **Docker** and **Docker Compose** installed
- **Node.js** 18+ (for frontend development)
- **Java** 17+ (for backend development)
- **Git** for version control

### Quick Start
```bash
# Clone repository
git clone https://github.com/siddharthakumar001/ankurshala-eduplatform.git
cd ankurshala-eduplatform

# Start all services with Docker
docker-compose up -d

# Access the application
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
```

---

## Docker Setup

### Docker Compose Configuration

The project uses `docker-compose.yml` for local development with 3 services:
- **PostgreSQL** database (port 5432)
- **Backend** (Java Spring Boot on port 8080)
- **Frontend** (Next.js on port 3000)

### Starting Services
```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Individual Service Management
```bash
# Restart specific service
docker-compose restart frontend
docker-compose restart backend

# View specific service logs
docker-compose logs -f frontend
docker-compose logs -f backend

# Rebuild specific service
docker-compose up -d --build frontend
```

---

## Environment Configuration

### Backend Environment Variables

Create `.env` file in backend directory:

```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
```

### Frontend Environment Variables

Create `.env.local` file in frontend directory:

```env
# Backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080/api

# For production
# NEXT_PUBLIC_API_URL=https://api.ankurshala.com/api
```

### Admin Credentials

**Default Admin Login:**
- Email: `siddhartha@ankurshala.com`
- Password: `Maza@123`

**Change these in production!**

---

## Production Deployment

### Azure Deployment

The application is configured for Azure deployment using:
- **Azure Container Registry** for images
- **Azure App Service** for backend
- **Azure Static Web Apps** for frontend
- **Azure Database for PostgreSQL** for database

### Pre-Deployment Checklist

- [ ] Update environment variables for production
- [ ] Change default admin password
- [ ] Configure SSL certificates
- [ ] Set up database backups
- [ ] Configure logging and monitoring
- [ ] Run all tests (Playwright + API)
- [ ] Update CORS settings for production domain

### Deployment Steps

1. **Build Docker Images**
```bash
# Backend
cd backend
docker build -t ankurshala-backend:latest .

# Frontend
cd frontend
docker build -t ankurshala-frontend:latest .
```

2. **Push to Azure Container Registry**
```bash
# Login to ACR
az acr login --name yourregistry

# Tag images
docker tag ankurshala-backend:latest yourregistry.azurecr.io/ankurshala-backend:latest
docker tag ankurshala-frontend:latest yourregistry.azurecr.io/ankurshala-frontend:latest

# Push images
docker push yourregistry.azurecr.io/ankurshala-backend:latest
docker push yourregistry.azurecr.io/ankurshala-frontend:latest
```

3. **Deploy to Azure App Service**
```bash
# Deploy backend
az webapp create --resource-group ankurshala-rg \
  --plan ankurshala-plan \
  --name ankurshala-backend \
  --deployment-container-image-name yourregistry.azurecr.io/ankurshala-backend:latest

# Deploy frontend
az staticwebapp create --name ankurshala-frontend \
  --resource-group ankurshala-rg \
  --source https://github.com/siddharthakumar001/ankurshala-eduplatform \
  --location "East US" \
  --branch main \
  --app-location "/frontend" \
  --output-location "out"
```

4. **Configure Database**
```bash
# Create PostgreSQL server
az postgres flexible-server create \
  --resource-group ankurshala-rg \
  --name ankurshala-db \
  --location eastus \
  --admin-user adminuser \
  --admin-password 'YourSecurePassword!' \
  --sku-name Standard_B2s \
  --version 14

# Create database
az postgres flexible-server db create \
  --resource-group ankurshala-rg \
  --server-name ankurshala-db \
  --database-name ankurshala
```

### Post-Deployment Verification

```bash
# Run verification script
./scripts/verify-deployment.sh

# Check health endpoints
curl https://api.ankurshala.com/actuator/health
curl https://ankurshala.com

# Run smoke tests
cd frontend
npm run test:e2e -- --grep "smoke"
```

---

## CI/CD Pipeline

### GitHub Actions Workflow

Located at `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Tests
        run: |
          docker-compose up -d
          cd frontend && npm test

  build-and-deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and Push to ACR
        run: |
          docker build -t $ACR_NAME/backend:$GITHUB_SHA ./backend
          docker build -t $ACR_NAME/frontend:$GITHUB_SHA ./frontend
          docker push $ACR_NAME/backend:$GITHUB_SHA
          docker push $ACR_NAME/frontend:$GITHUB_SHA
      
      - name: Deploy to Azure
        run: |
          az webapp config container set --name ankurshala-backend \
            --resource-group ankurshala-rg \
            --docker-custom-image-name $ACR_NAME/backend:$GITHUB_SHA
```

### Required GitHub Secrets

Set these in GitHub repository settings → Secrets:

- `AZURE_CREDENTIALS` - Service principal credentials
- `ACR_NAME` - Azure Container Registry name
- `ACR_USERNAME` - ACR username
- `ACR_PASSWORD` - ACR password
- `DATABASE_URL` - Production database connection string
- `JWT_SECRET` - Production JWT secret

---

## Troubleshooting Setup

### Docker Issues

**Problem:** Port already in use
```bash
# Find process using port 3000
lsof -i :3000
# Kill process
kill -9 <PID>
```

**Problem:** Database connection failed
```bash
# Check PostgreSQL is running
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Build Issues

**Problem:** Frontend build fails
```bash
# Clear Next.js cache
cd frontend
rm -rf .next
npm run build
```

**Problem:** Backend build fails
```bash
# Clean and rebuild
cd backend
./mvnw clean install
```

---

## Performance Optimization

### Production Build Optimization

**Frontend:**
```bash
# Build optimized production bundle
cd frontend
npm run build
npm run start  # Production server
```

**Backend:**
```bash
# Build with production profile
cd backend
./mvnw clean package -P production
```

### Database Optimization

```sql
-- Create indexes for frequently queried columns
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_enabled ON students(enabled);
CREATE INDEX idx_courses_board ON courses(educational_board);
```

---

## Monitoring and Logging

### Application Logs

```bash
# View frontend logs
docker-compose logs -f frontend

# View backend logs
docker-compose logs -f backend

# Export logs
docker-compose logs > app-logs.txt
```

### Health Checks

**Backend Health:** `http://localhost:8080/actuator/health`  
**Frontend Health:** `http://localhost:3000/api/health`

---

## Backup and Recovery

### Database Backup

```bash
# Backup
docker exec ankurshala_postgres_local pg_dump -U postgres ankurshala > backup.sql

# Restore
docker exec -i ankurshala_postgres_local psql -U postgres ankurshala < backup.sql
```

### Application State Backup

```bash
# Backup Docker volumes
docker run --rm -v ankurshala_postgres_data:/data -v $(pwd):/backup \
  ubuntu tar czf /backup/postgres-backup.tar.gz /data
```

---

## Quick Reference Commands

```bash
# Development
docker-compose up -d              # Start all services
docker-compose down               # Stop all services
docker-compose restart frontend   # Restart frontend
docker-compose logs -f            # View all logs

# Testing
cd frontend && npm test           # Run frontend tests
cd frontend && npm run test:e2e   # Run Playwright tests
./scripts/test-api.sh             # Test API endpoints

# Deployment
./scripts/deploy-production.sh   # Deploy to production
./scripts/verify-deployment.sh   # Verify deployment

# Maintenance
docker system prune -a            # Clean up Docker
docker-compose down -v            # Remove all data
```

---

**Need Help?** See [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for common issues and solutions.
