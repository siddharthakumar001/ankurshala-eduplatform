#!/bin/bash

echo "Starting debug startup for Ankurshala platform..."

# Start the backend
echo "Starting backend..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!
echo "Backend started with PID: $BACKEND_PID"

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 10

# Test backend health
echo "Testing backend connectivity..."
curl -f http://localhost:8080/actuator/health || echo "Backend health check failed"

# Start the frontend
echo "Starting frontend..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!
echo "Frontend started with PID: $FRONTEND_PID"

echo "Services started:"
echo "Backend PID: $BACKEND_PID"
echo "Frontend PID: $FRONTEND_PID"
echo ""
echo "Access the application at: http://localhost:3000"
echo "Backend API at: http://localhost:8080"
echo ""
echo "To stop services, run:"
echo "kill $BACKEND_PID $FRONTEND_PID"

# Keep script running
wait
