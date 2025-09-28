# Docker Local Development Setup

This document explains how to run AnkurShala locally with Docker for development with hot reloading.

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Ports 3000, 8080, 5432, 6379, 9092, 8025 available

### Start All Services
```bash
# Start all services with hot reloading enabled
docker-compose up --build

# Or start in background
docker-compose up -d --build
```

### Services
- **Frontend**: http://localhost:3000 (with hot reloading)
- **Backend**: http://localhost:8080/api
- **Database**: localhost:5432 (PostgreSQL)
- **Redis**: localhost:6379
- **Kafka**: localhost:9092
- **MailHog**: http://localhost:8025 (Email testing)

## Development Features

### Frontend Hot Reloading
The frontend is configured with:
- Volume mounting for instant file changes
- Next.js development mode
- Webpack polling for file changes
- Optimized package imports

### Backend Hot Reloading
The backend runs in development mode with:
- Spring Boot dev tools
- Auto-restart on code changes
- Demo data seeding enabled

## Admin Dashboard Access

1. Navigate to http://localhost:3000/login
2. Use admin credentials (seeded automatically):
   - Email: admin@ankurshala.com
   - Password: admin123
3. Access admin dashboard at http://localhost:3000/admin

## Fixed Issues

### Student/Teacher Toggle Status
- ✅ Fixed HTTP method consistency (PATCH for both)
- ✅ Updated response format to include status
- ✅ Fixed frontend API calls

### Hot Reloading
- ✅ Configured Next.js for development mode
- ✅ Added volume mounting for instant updates
- ✅ Enabled webpack polling
- ✅ Optimized package imports

## Troubleshooting

### Frontend Not Updating
1. Check if volumes are mounted correctly
2. Verify NODE_ENV=development
3. Check browser console for errors

### Backend Not Responding
1. Check health status: `docker-compose ps`
2. View logs: `docker-compose logs backend`
3. Verify database connection

### Port Conflicts
If ports are in use, modify `docker-compose.yml` to use different ports.

## Development Commands

```bash
# View logs
docker-compose logs -f frontend
docker-compose logs -f backend

# Restart specific service
docker-compose restart frontend

# Stop all services
docker-compose down

# Clean restart (removes volumes)
docker-compose down -v
docker-compose up --build
```