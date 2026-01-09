# Ankurshala Education Platform - Documentation

**Version:** 1.0 Production Ready  
**Last Updated:** January 9, 2026  
**Status:** ✅ Deployed and Operational

---

## 📚 Documentation Index

This folder contains all consolidated documentation for the Ankurshala Education Platform.

### 🎯 Core Documentation

1. **[ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md)** - System architecture, tech stack, database
2. **[ENHANCED_CURRICULUM_IMPORT.md](./ENHANCED_CURRICULUM_IMPORT.md)** - Curriculum import system (complete guide with automation)
3. **[AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md)** - Auth flow, RBAC, security
4. **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Common issues, debugging, optimization

### 📦 Feature Modules

- **Booking System**: [BOOKING_SYSTEM_IMPLEMENTATION_SUMMARY.md](./BOOKING_SYSTEM_IMPLEMENTATION_SUMMARY.md), [BOOKING_SYSTEM_CONCURRENCY.md](./BOOKING_SYSTEM_CONCURRENCY.md), [BOOKING_SYSTEM_QUICK_REFERENCE.md](./BOOKING_SYSTEM_QUICK_REFERENCE.md)
- **Student Module**: [STUDENT_MODULE_COMPLETE.md](./STUDENT_MODULE_COMPLETE.md)
- **Teacher Module**: [TEACHER_SEARCH_IMPLEMENTATION.md](./TEACHER_SEARCH_IMPLEMENTATION.md)
- **Payments**: [PAYMENT_INTEGRATION_IMPLEMENTATION.md](./PAYMENT_INTEGRATION_IMPLEMENTATION.md)
- **Monitoring**: [MONITORING_IMPLEMENTATION.md](./MONITORING_IMPLEMENTATION.md)
- **Rate Limiting**: [RATE_LIMITING_IMPLEMENTATION.md](./RATE_LIMITING_IMPLEMENTATION.md)

### 🚀 Deployment & Testing

- [DEPLOYMENT.md](./DEPLOYMENT.md) - Production deployment
- [DEPLOYMENT_AND_TESTING_GUIDE.md](./DEPLOYMENT_AND_TESTING_GUIDE.md) - Deployment with testing
- [E2E_TESTING_GUIDE.md](./E2E_TESTING_GUIDE.md) - End-to-end testing
- [E2E_TESTING_CHECKLIST.md](./E2E_TESTING_CHECKLIST.md) - Testing checklist

---

## 🚀 Quick Start

**Developers:**  
Read [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md) → [DEPLOYMENT_AND_TESTING_GUIDE.md](./DEPLOYMENT_AND_TESTING_GUIDE.md)

**Content Managers:**  
See [ENHANCED_CURRICULUM_IMPORT.md](./ENHANCED_CURRICULUM_IMPORT.md) for curriculum import with automation

**Admins:**  
Review [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md) → [MONITORING_IMPLEMENTATION.md](./MONITORING_IMPLEMENTATION.md)

---

## 🔄 Recent Updates (January 2026)

- ✅ Consolidated curriculum import documentation into single comprehensive guide
- ✅ Created `.github/copilot-instructions.md` to prevent redundant documentation
- ✅ Added automation scripts (PowerShell + Python) for curriculum import
- ✅ Removed redundant documentation files
- ✅ Updated .gitignore to prevent future redundancy

---

## 📋 Documentation Guidelines

> **CRITICAL:** Review `.github/copilot-instructions.md` before creating ANY new documentation!

**Always update existing files instead of creating new ones:**
- New features → Update feature docs or `ARCHITECTURE_AND_DESIGN.md`
- Bug fixes → Update `TROUBLESHOOTING.md`
- API changes → Update feature-specific documentation
- Deployment changes → Update `DEPLOYMENT.md`

---

- **Payment Integration**: Multiple payment providers (Razorpay, Stripe, PayU)

**Developers:** Read SETUP_AND_DEPLOYMENT.md → ARCHITECTURE_AND_DESIGN.md → TESTING_GUIDE.md  - **Wallet System**: Built-in wallet for seamless transactions

**Admins:** See ADMIN_MODULE_GUIDE.md → AUTHENTICATION_AND_SECURITY.md  - **Notification System**: Real-time notifications via WebSocket

**API Integration:** Check API_REFERENCE.md → AUTHENTICATION_AND_SECURITY.md- **AI Integration**: Optional AI features for recommendations and analytics

- **Comprehensive Analytics**: Detailed reporting and insights

---

### Technology Stack

## 🔄 Recent Updates (October 2025)

**Backend:**

- ✅ Admin students page fixes (toggle status, dynamic filters, search)- Spring Boot 3.2+ with Java 17

- ✅ All Playwright tests passing- PostgreSQL 15+ for primary database

- ✅ Production deployment complete- Redis 7+ for caching and sessions

- ✅ Documentation consolidated to 7 key files- Apache Kafka for event streaming

- JWT for authentication

---- WebSocket for real-time communication



**Contact:** siddhartha@ankurshala.com**Frontend:**

- Next.js 14 with App Router
- TypeScript for type safety
- Tailwind CSS for styling
- shadcn/ui for components
- React Query for data fetching
- Zustand for state management

**Infrastructure:**
- Docker Compose for development
- Flyway for database migrations
- Playwright for E2E testing
- Jest for unit testing

## Architecture

### System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (Next.js)     │◄──►│   (Spring Boot) │◄──►│   (PostgreSQL)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   WebSocket     │    │   Redis Cache    │    │   File Storage  │
│   (Real-time)   │    │   (Sessions)     │    │   (Media)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   Kafka Events   │
                       │   (Messaging)    │
                       └─────────────────┘
```

### Domain Architecture

The application follows Domain-Driven Design (DDD) principles with clear domain boundaries:

- **User Domain**: Authentication, profiles, roles
- **Content Domain**: Topics, subjects, curriculum
- **Booking Domain**: Scheduling, availability, matching
- **Payment Domain**: Transactions, wallets, refunds
- **Notification Domain**: Real-time messaging, alerts
- **Analytics Domain**: Reports, metrics, insights

### API Architecture

RESTful APIs with the following patterns:
- Resource-based URLs (`/api/users`, `/api/bookings`)
- HTTP methods for operations (GET, POST, PUT, DELETE)
- Consistent response format with `ApiResponse<T>`
- Comprehensive error handling with RFC7807 Problem JSON
- JWT-based authentication
- Role-based authorization

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker and Docker Compose
- PostgreSQL 15+
- Redis 7+

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/ankurshala-eduplatform.git
   cd ankurshala-eduplatform
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d postgres redis kafka
   ```

3. **Backend setup**
   ```bash
   cd backend
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **Frontend setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

### Environment Configuration

Create `.env.local` in the frontend directory:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080
```

Create `.env` in the backend directory:
```env
JWT_SECRET=your-secret-key-here
DATABASE_URL=jdbc:postgresql://localhost:5432/ankurshala
REDIS_URL=redis://localhost:6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## API Documentation

### Authentication Endpoints

#### Student Signup
```http
POST /auth/signup/student
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "board": "CBSE",
  "grade": "10",
  "language": "English",
  "goals": ["Improve Math", "Better Science"],
  "school": "Delhi Public School",
  "dob": "2005-05-15",
  "pincode": "110001",
  "guardianName": "Jane Doe",
  "guardianContact": "9876543210"
}
```

#### Teacher Signup
```http
POST /auth/signup/teacher
Content-Type: application/json

{
  "name": "Dr. Smith",
  "email": "dr.smith@example.com",
  "password": "SecurePass123!",
  "bio": "Experienced mathematics teacher",
  "yearsExperience": 10,
  "languages": ["English", "Hindi"],
  "categories": ["STANDARD", "PREMIUM"],
  "hourlyRate": 500.00,
  "subjectExpertise": [...],
  "availability": [...]
}
```

#### Login
```http
POST /auth/signin
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

### Booking Endpoints

#### Get Booking Quote
```http
POST /booking/quote
Authorization: Bearer <token>
Content-Type: application/json

{
  "subjectId": 1,
  "topicId": 1,
  "startTimeISO": "2024-12-25T10:00:00",
  "teacherCategory": "STANDARD"
}
```

#### Create Booking
```http
POST /booking
Authorization: Bearer <token>
Content-Type: application/json

{
  "subjectId": 1,
  "topicId": 1,
  "teacherId": 1,
  "startTimeISO": "2024-12-25T10:00:00",
  "durationMinutes": 60,
  "category": "STANDARD",
  "appliedRuleId": 1,
  "priceMinCents": 50000,
  "priceMaxCents": 50000,
  "notes": "Need help with this topic"
}
```

### Wallet Endpoints

#### Get Wallet Balance
```http
GET /wallet/balance
Authorization: Bearer <token>
```

#### Credit Wallet
```http
POST /wallet/credit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amountCents": 10000,
  "reason": "Wallet top-up"
}
```

### Notification Endpoints

#### Get Notifications
```http
GET /api/notifications?unreadOnly=false
Authorization: Bearer <token>
```

#### Mark Notifications as Read
```http
POST /api/notifications/mark-read
Authorization: Bearer <token>
Content-Type: application/json

{
  "notificationIds": [1, 2, 3]
}
```

## Frontend Guide

### Project Structure

```
frontend/src/
├── app/                    # Next.js App Router
│   ├── (auth)/            # Authentication pages
│   ├── student/           # Student-specific pages
│   ├── teacher/           # Teacher-specific pages
│   ├── admin/             # Admin-specific pages
│   └── api/               # API routes
├── components/            # Reusable components
│   ├── ui/                # shadcn/ui components
│   ├── forms/             # Form components
│   └── layout/            # Layout components
├── hooks/                 # Custom React hooks
├── lib/                   # Utility functions
├── store/                 # Zustand stores
└── types/                 # TypeScript types
```

### State Management

The application uses Zustand for state management:

```typescript
// store/auth.ts
interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  signupStudent: (data: StudentSignupData) => Promise<void>;
  signupTeacher: (data: TeacherSignupData) => Promise<void>;
}
```

### Data Fetching

React Query is used for server state management:

```typescript
// hooks/useBookings.ts
export const useBookings = () => {
  return useQuery({
    queryKey: ['bookings'],
    queryFn: () => api.getBookings(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
```

### Component Patterns

#### Form Components
```typescript
// components/forms/BookingForm.tsx
export const BookingForm = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<BookingFormData>();
  
  const onSubmit = (data: BookingFormData) => {
    // Handle form submission
  };
  
  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {/* Form fields */}
    </form>
  );
};
```

#### Layout Components
```typescript
// components/layout/StudentLayout.tsx
export const StudentLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="min-h-screen bg-blue-50">
      <StudentSidebar />
      <main className="ml-64 p-6">
        {children}
      </main>
    </div>
  );
};
```

## Database Schema

### Core Tables

#### Users and Authentication
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

#### Student Profiles
```sql
CREATE TABLE student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    mobile_number VARCHAR(20),
    date_of_birth DATE,
    educational_board VARCHAR(50),
    class_level VARCHAR(20),
    grade_level VARCHAR(20),
    school_name VARCHAR(200),
    board VARCHAR(50),
    grade VARCHAR(50),
    language VARCHAR(50),
    goals JSONB,
    pincode VARCHAR(10),
    guardian_name VARCHAR(100),
    guardian_contact VARCHAR(20)
);
```

#### Teacher Profiles
```sql
CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(100),
    email VARCHAR(255),
    bio TEXT,
    years_experience INTEGER,
    languages TEXT[],
    categories TEXT[],
    hourly_rate NUMERIC(10,2),
    rating_avg NUMERIC(3,2),
    rating_count INTEGER,
    active BOOLEAN DEFAULT TRUE
);
```

#### Bookings
```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT REFERENCES users(id),
    teacher_id BIGINT REFERENCES users(id),
    board VARCHAR(50),
    grade VARCHAR(50),
    subject_id BIGINT,
    topic_id BIGINT,
    start_ts TIMESTAMPTZ,
    end_ts TIMESTAMPTZ,
    category VARCHAR(50),
    price_min_cents INTEGER,
    price_max_cents INTEGER,
    state VARCHAR(20) DEFAULT 'REQUESTED',
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    version BIGINT DEFAULT 0
);
```

#### Wallets
```sql
CREATE TABLE student_wallet (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT REFERENCES users(id),
    balance_cents BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE wallet_txns (
    id BIGSERIAL PRIMARY KEY,
    owner_type VARCHAR(20),
    owner_id BIGINT,
    booking_id BIGINT REFERENCES bookings(id),
    type VARCHAR(20),
    amount_cents BIGINT,
    source VARCHAR(20),
    meta JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Indexes

```sql
-- Performance indexes
CREATE INDEX idx_bookings_student_ts ON bookings(student_id, start_ts, end_ts);
CREATE INDEX idx_bookings_teacher_ts ON bookings(teacher_id, start_ts, end_ts);
CREATE INDEX idx_bookings_state_ts ON bookings(state, start_ts);
CREATE INDEX idx_wallet_txns_owner ON wallet_txns(owner_type, owner_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, created_at);
```

## Deployment Guide

### Production Environment

#### Docker Compose Production
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ankurshala
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://postgres:5432/ankurshala
      REDIS_URL: redis://redis:6379
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
  redis_data:
```

#### Environment Variables
```env
# Production environment
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://localhost:5432/ankurshala
REDIS_URL=redis://localhost:6379
JWT_SECRET=your-production-secret-key
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Deployment Steps

1. **Build the application**
   ```bash
   # Backend
   cd backend
   ./mvnw clean package -Pprod
   
   # Frontend
   cd frontend
   npm run build
   ```

2. **Deploy with Docker**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **Run database migrations**
   ```bash
   docker exec -it ankurshala-backend-1 ./mvnw flyway:migrate
   ```

4. **Verify deployment**
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:3000
   ```

## Security

### Authentication & Authorization

- **JWT Tokens**: Secure token-based authentication
- **Role-Based Access**: STUDENT, TEACHER, ADMIN roles
- **Password Security**: BCrypt hashing with salt
- **Session Management**: Redis-based session storage

### API Security

- **CORS Configuration**: Restricted origins
- **Rate Limiting**: Request throttling
- **Input Validation**: Comprehensive validation
- **SQL Injection Prevention**: Parameterized queries

### Data Protection

- **Encryption**: Sensitive data encryption
- **PII Handling**: Personal information protection
- **Audit Logging**: Comprehensive audit trails
- **Data Retention**: Configurable retention policies

## Monitoring

### Health Checks

```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### Logging

- **Structured Logging**: JSON format logs
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Trace IDs**: Request tracing
- **Centralized Logging**: ELK Stack integration

### Metrics

- **Application Metrics**: Custom business metrics
- **System Metrics**: JVM and system metrics
- **Database Metrics**: Connection pool and query metrics
- **Performance Metrics**: Response times and throughput

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database status
docker exec -it ankurshala-postgres-1 psql -U ankurshala -d ankurshala -c "SELECT 1;"

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### 2. Redis Connection Issues
```bash
# Check Redis status
docker exec -it ankurshala-redis-1 redis-cli ping

# Check Redis metrics
curl http://localhost:8080/actuator/metrics/redis.connection.pool.active
```

#### 3. WebSocket Connection Issues
```bash
# Check WebSocket endpoint
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Key: test" -H "Sec-WebSocket-Version: 13" http://localhost:8080/ws
```

#### 4. Frontend Build Issues
```bash
# Clear Next.js cache
cd frontend
rm -rf .next
npm run build
```

### Performance Optimization

#### Backend Optimization
- **Database Indexing**: Optimize query performance
- **Caching**: Redis caching for frequently accessed data
- **Connection Pooling**: Optimize database connections
- **JVM Tuning**: Memory and GC optimization

#### Frontend Optimization
- **Code Splitting**: Lazy loading of components
- **Image Optimization**: Next.js Image component
- **Bundle Analysis**: Webpack bundle analyzer
- **CDN Integration**: Static asset delivery

## Contributing

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/new-feature
   ```
3. **Make changes and test**
   ```bash
   ./scripts/run-tests.sh
   ```
4. **Commit changes**
   ```bash
   git commit -m "feat: add new feature"
   ```
5. **Push and create PR**

### Code Standards

- **Java**: Google Java Style Guide
- **TypeScript**: ESLint + Prettier
- **Commits**: Conventional Commits
- **Testing**: 80% coverage requirement

### Pull Request Process

1. **Create detailed PR description**
2. **Include tests for new features**
3. **Update documentation**
4. **Ensure all tests pass**
5. **Request code review**

---

This documentation provides a comprehensive guide to the AnkurShala platform. For additional support or questions, please refer to the troubleshooting section or contact the development team.
