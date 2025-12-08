#!/bin/bash

# 🚀 AnkurShala - Complete Docker Startup Script
# This script starts all services in Docker containers

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║         AnkurShala Docker Startup Script                 ║"
echo "║         Starting All Services                             ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Function to print status
print_status() {
    echo -e "${CYAN}[$(date +'%H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Check if Docker is installed
print_status "Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed!"
    echo ""
    echo "Please install Docker Desktop from:"
    echo "  https://www.docker.com/products/docker-desktop"
    exit 1
fi
print_success "Docker is installed"

# Check if Docker is running
print_status "Checking if Docker daemon is running..."
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running!"
    echo ""
    print_warning "Please start Docker Desktop and try again"
    echo ""
    echo "Steps:"
    echo "  1. Open Docker Desktop application"
    echo "  2. Wait for Docker to start (icon in menu bar/system tray)"
    echo "  3. Run this script again"
    exit 1
fi
print_success "Docker daemon is running"

# Check if docker-compose is available
print_status "Checking docker-compose..."
if ! command -v docker-compose &> /dev/null; then
    if ! docker compose version &> /dev/null; then
        print_error "docker-compose is not available!"
        exit 1
    fi
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi
print_success "docker-compose is available"

echo ""
print_status "Stopping any existing containers..."
$COMPOSE_CMD down 2>/dev/null || true
print_success "Stopped existing containers"

echo ""
print_status "Cleaning up old images (optional - press Ctrl+C to skip)..."
sleep 2
# docker system prune -f || true

echo ""
print_status "Building Docker images (this may take 5-10 minutes)..."
echo ""
$COMPOSE_CMD build --no-cache

echo ""
print_status "Starting all services..."
echo ""
$COMPOSE_CMD up -d

echo ""
print_status "Waiting for services to be healthy..."
echo ""

# Function to check service health
check_service() {
    local service=$1
    local max_attempts=60
    local attempt=0
    
    echo -n "  Checking $service: "
    
    while [ $attempt -lt $max_attempts ]; do
        if docker ps --filter "name=${service}" --filter "health=healthy" | grep -q ${service}; then
            echo -e "${GREEN}✅ Healthy${NC}"
            return 0
        fi
        
        if docker ps --filter "name=${service}" | grep -q ${service}; then
            echo -n "."
            sleep 2
            attempt=$((attempt + 1))
        else
            echo -e "${RED}❌ Not running${NC}"
            return 1
        fi
    done
    
    echo -e "${YELLOW}⚠️  Timeout${NC}"
    return 1
}

# Check each service
check_service "ankurshala_db_local"
check_service "ankurshala_redis_local"
check_service "ankurshala_backend_local"
check_service "ankurshala_frontend_local"

echo ""
print_success "All services are starting up!"

echo ""
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                 🎉 Services Started!                      ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}📊 Service URLs:${NC}"
echo ""
echo "  🌐 Frontend:         http://localhost:3000"
echo "  🔐 Login Page:       http://localhost:3000/login"
echo "  👨‍💼 Admin Dashboard:  http://localhost:3000/admin/dashboard"
echo "  👥 Admin Students:   http://localhost:3000/admin/users/students"
echo ""
echo "  🔧 Backend API:      http://localhost:8080/api"
echo "  📚 API Docs:         http://localhost:8080/swagger-ui.html"
echo "  🏥 Health Check:     http://localhost:8080/actuator/health"
echo ""
echo "  📧 MailHog:          http://localhost:8025"
echo "  🗄️  Database:         localhost:5432"
echo "  📦 Redis:            localhost:6379"
echo "  📨 Kafka:            localhost:9092"
echo ""

echo -e "${YELLOW}🔑 Admin Credentials:${NC}"
echo "  Email:    admin@ankurshala.com"
echo "  Password: admin123"
echo ""

echo -e "${CYAN}📋 Useful Commands:${NC}"
echo ""
echo "  View logs (all):        $COMPOSE_CMD logs -f"
echo "  View logs (backend):    $COMPOSE_CMD logs -f backend"
echo "  View logs (frontend):   $COMPOSE_CMD logs -f frontend"
echo "  Stop all services:      $COMPOSE_CMD down"
echo "  Restart a service:      $COMPOSE_CMD restart <service>"
echo "  Check service status:   docker ps"
echo ""

echo -e "${BLUE}🧪 Testing Admin Students Page:${NC}"
echo ""
echo "  1. Open: http://localhost:3000/login"
echo "  2. Login with admin credentials above"
echo "  3. Navigate to: http://localhost:3000/admin/users/students"
echo "  4. Test all features (search, filters, pagination)"
echo ""

echo -e "${GREEN}✅ Setup Complete! Wait 30-60 seconds for all services to fully start.${NC}"
echo ""

# Optionally follow logs
read -p "Do you want to follow the logs? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    print_status "Following logs (Press Ctrl+C to stop)..."
    echo ""
    $COMPOSE_CMD logs -f
fi
