# Local Deployment Guide - AnkurShala

## Quick Start - Running Without Full Docker Build

Since the Docker build has some issues with Maven Wrapper, here's the fastest way to get the application running locally for testing.

### Option 1: Infrastructure in Docker + Local Backend/Frontend (RECOMMENDED)

This runs only the infrastructure (PostgreSQL, Redis, Kafka, etc.) in Docker, while running the backend and frontend directly on your machine.

#### Step 1: Start Infrastructure Services Only

```powershell
cd C:\Users\SiddharthaSingh\Documents\workspace\Ankurshala\ankurshala-eduplatform

# Start only infrastructure services
docker-compose up -d postgres redis zookeeper kafka mailhog
```

Wait for services to be healthy (about 30-60 seconds):
```powershell
docker-compose ps
```

#### Step 2: Build and Run Backend Locally

```powershell
# Navigate to backend directory
cd backend

# Build with Maven Wrapper (Windows)
.\mvnw.cmd clean package -DskipTests

# Run the Spring Boot application
.\mvnw.cmd spring-boot:run
```

Or if you have Maven installed:
```powershell
mvn clean package -DskipTests
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

#### Step 3: Run Frontend Locally

Open a new terminal:

```powershell
# Navigate to frontend directory
cd C:\Users\SiddharthaSingh\Documents\workspace\Ankurshala\ankurshala-eduplatform\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

The frontend will start on **http://localhost:3000**

---

### Option 2: Full Docker Deployment (If you want to fix Docker build)

The Docker build is failing because `mvnw` doesn't have execute permissions. Here's the fix:

1. **Fix Applied**: The Dockerfile has been updated to use `mvn` directly instead of `./mvnw`

2. **Build and Start**:
```powershell
cd C:\Users\SiddharthaSingh\Documents\workspace\Ankurshala\ankurshala-eduplatform

# Build all services (this will take 5-10 minutes)
docker-compose build

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

---

## Access Points

Once running, access the application at:

- **Frontend (Student/Teacher/Admin)**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Backend Health Check**: http://localhost:8080/api/actuator/health
- **MailHog (Email Testing)**: http://localhost:8025
- **Prometheus (Metrics)**: http://localhost:9090
- **Grafana (Monitoring)**: http://localhost:3001

---

## Demo Credentials

After the application starts, you can log in with these demo credentials:

### Admin
- **Email**: siddhartha@ankurshala.com
- **Password**: Maza@123

### Students
- **Email**: student1@ankurshala.com (or student2, student3, student4, student5)
- **Password**: Maza@123

### Teachers
- **Email**: teacher1@ankurshala.com (or teacher2, teacher3, teacher4, teacher5)
- **Password**: Maza@123

---

## Testing Features

### 1. Admin Module
- Navigate to http://localhost:3000
- Login with admin credentials
- Test features:
  - Dashboard with metrics
  - Student management (list, search, toggle status, edit, delete)
  - Teacher management
  - Content management (boards, grades, subjects, chapters, topics)
  - Pricing management
  - Fee waiver management

### 2. Student Module
- Login with student credentials
- Test features:
  - **Dashboard**: View learning analytics and upcoming classes
  - **Discover**: Browse content (Board → Grade → Subject → Chapter → Topic)
  - **Booking**: Book a class (3-step wizard)
  - **Calendar**: View scheduled classes
  - **History**: View past bookings with pagination
  - **Study List**: Manage learning materials
  - **Notifications**: View system notifications
  - **Profile**: Update personal information

### 3. Teacher Module
- Login with teacher credentials
- Test features:
  - Dashboard with statistics
  - Profile management
  - Qualifications management
  - Experience management
  - Certifications
  - Document uploads
  - Availability management
  - Bank details (encrypted)

---

## Troubleshooting

### Backend Won't Start

**Issue**: Maven dependencies download failure
```powershell
# Clear Maven cache and rebuild
cd backend
rmdir /s /q target
rmdir /s /q %USERPROFILE%\.m2\repository\com\ankurshala
.\mvnw.cmd clean install -DskipTests
```

**Issue**: Port 8080 already in use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Frontend Won't Start

**Issue**: Port 3000 already in use
```powershell
# Find and kill process
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

**Issue**: Node modules corrupted
```powershell
cd frontend
rmdir /s /q node_modules
rmdir /s /q .next
npm install
npm run dev
```

### Database Issues

**Issue**: Can't connect to PostgreSQL
```powershell
# Check if PostgreSQL container is running
docker ps | findstr postgres

# If not running, start it
docker-compose up -d postgres

# Check logs
docker-compose logs postgres
```

**Issue**: Need to reset database
```powershell
# Stop all services
docker-compose down

# Remove volumes (THIS DELETES ALL DATA)
docker-compose down -v

# Start fresh
docker-compose up -d postgres redis zookeeper kafka mailhog
```

### Docker Build Issues

**Issue**: Backend Docker build fails
```
# The Dockerfile has been fixed to use maven directly
# Rebuild without cache:
docker-compose build --no-cache backend
```

---

## Seeding Demo Data

The application automatically seeds demo data on first run. If you need to manually seed data:

```powershell
# Make sure backend is running
# The demo data includes:
# - 1 Admin user
# - 5 Student users
# - 5 Teacher users  
# - 3 Boards (CBSE, ICSE, State)
# - 24 Grades
# - 14 Subjects

# Check if data exists by accessing:
# http://localhost:8080/api/public/boards
```

---

## Stopping the Application

### If running locally (Option 1):
```powershell
# Stop backend: Press Ctrl+C in backend terminal
# Stop frontend: Press Ctrl+C in frontend terminal

# Stop Docker infrastructure:
docker-compose down
```

### If running in full Docker (Option 2):
```powershell
# Stop all containers
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

---

## Next Steps

1. **Test Authentication**: Try logging in with different user roles
2. **Test Student Booking Flow**: Go through the complete booking process
3. **Test Admin Features**: Create boards, grades, subjects, chapters, and topics
4. **Check API Health**: Visit http://localhost:8080/api/actuator/health
5. **Monitor Logs**: Use `docker-compose logs -f` to see real-time logs

---

## Support

If you encounter any issues:

1. Check the logs: `docker-compose logs -f [service-name]`
2. Verify all services are healthy: `docker-compose ps`
3. Check the troubleshooting section above
4. Restart services: `docker-compose restart [service-name]`

For more detailed information, see:
- [README.md](README.md) - Project overview
- [docs/SETUP_AND_DEPLOYMENT.md](docs/SETUP_AND_DEPLOYMENT.md) - Complete setup guide
- [docs/E2E_TESTING_CHECKLIST.md](docs/E2E_TESTING_CHECKLIST.md) - Testing guide
