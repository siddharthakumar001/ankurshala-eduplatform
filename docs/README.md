# Ankurshala Education Platform - Documentation

**Version:** 1.0 Production Ready  
**Last Updated:** January 15, 2026  
**Status:** ✅ Deployed and Operational

---

## 📚 Documentation Index

This folder contains all consolidated documentation for the Ankurshala Education Platform.

### 🎯 Core Documentation (10 Files)

1. **[README.md](./README.md)** - This file - Documentation index and overview
2. **[ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md)** - System architecture, tech stack, database schema
3. **[AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md)** - Auth flow, JWT, RBAC, security best practices
4. **[ENHANCED_CURRICULUM_IMPORT.md](./ENHANCED_CURRICULUM_IMPORT.md)** - Curriculum import system with automation scripts
5. **[COURSE_CONTENT_FORMAT_RULES.md](./COURSE_CONTENT_FORMAT_RULES.md)** - ⭐ Content upload format rules and validation guidelines
6. **[AI_CONTENT_DATA_MODEL.md](./AI_CONTENT_DATA_MODEL.md)** - AI integration fields and use cases
7. **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** - Common issues, debugging, optimization guides

### 📦 Feature Modules (3 Files)

8. **[STUDENT_MODULE.md](./STUDENT_MODULE.md)** - Complete student module documentation (dashboard, profile, booking, study list, etc.)
9. **[TEACHER_MODULE.md](./TEACHER_MODULE.md)** - Complete teacher module documentation (profile, search, availability, qualifications)
10. **[BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md)** - Booking system with concurrency control, status flow, cancellation policy

---

## 🚀 Quick Start

**New Developers:**  
1. Read [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md) to understand the system
2. Review [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md) for auth patterns
3. Check module-specific docs ([STUDENT_MODULE.md](./STUDENT_MODULE.md), [TEACHER_MODULE.md](./TEACHER_MODULE.md), [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md))

**Content Managers:**  
1. **Start here:** [COURSE_CONTENT_FORMAT_RULES.md](./COURSE_CONTENT_FORMAT_RULES.md) - **All upload format rules and validation**
2. See [ENHANCED_CURRICULUM_IMPORT.md](./ENHANCED_CURRICULUM_IMPORT.md) for curriculum import process
3. Check [../CLASS7_PHYSICS_SAMPLE.md](../CLASS7_PHYSICS_SAMPLE.md) for sample content format
4. Review [AI_CONTENT_DATA_MODEL.md](./AI_CONTENT_DATA_MODEL.md) for AI integration guidelines

**System Administrators:**  
Review [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md) → [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

**Feature Development:**
- Student features → [STUDENT_MODULE.md](./STUDENT_MODULE.md)
- Teacher features → [TEACHER_MODULE.md](./TEACHER_MODULE.md)
- Booking features → [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md)
- AI features → [AI_CONTENT_DATA_MODEL.md](./AI_CONTENT_DATA_MODEL.md)

---

## 🔄 Recent Updates (January 15, 2026)

### Enhanced Curriculum Import with AI Integration
- ✅ Added `suggested_topics` field to topics table (V37 migration)
- ✅ Updated EnhancedCurriculumImportService to populate all AI fields
- ✅ Created AI_CONTENT_DATA_MODEL.md documentation
- ✅ Added sample Class 7 Physics CBSE content
- ✅ All 4 AI integration fields now supported: description, summary, suggested_topics, expected_time_mins

---

## 🔄 Previous Updates (January 9, 2026)

### Documentation Consolidation ✅
- ✅ Consolidated 32 documentation files into 8 comprehensive files
- ✅ Removed 24 redundant/duplicate documentation files
- ✅ Created comprehensive module documentation:
  - STUDENT_MODULE.md (consolidates 8 files)
  - TEACHER_MODULE.md (consolidates 2 files)
  - BOOKING_SYSTEM.md (consolidates 3 files)
- ✅ Achieved target: 8 files (under 10 file maximum)
- ✅ Updated all cross-references
- ✅ Maintained complete information with zero data loss

### Previous Updates
- ✅ Created `.github/copilot-instructions.md` to prevent redundant documentation
- ✅ Added automation scripts (PowerShell + Python) for curriculum import
- ✅ Updated .gitignore to prevent future redundancy
- ✅ Production deployment complete
- ✅ All Playwright tests passing

---

## 📋 Documentation Guidelines

> **CRITICAL:** Review `.github/copilot-instructions.md` before creating ANY new documentation!

**Golden Rule: Update, Don't Create**

- ✅ New features → Update module docs (STUDENT_MODULE.md, TEACHER_MODULE.md, etc.)
- ✅ Bug fixes → Update [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- ✅ Architecture changes → Update [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md)
- ✅ Security changes → Update [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md)
- ✅ Deployment changes → Update [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md) (Deployment section)
- ❌ DO NOT create new .md files unless absolutely necessary

**Acceptable New Files:**
- New major feature modules (create ONE comprehensive doc)
- Migration guides for major version upgrades
- Quick reference cards (only if approved)

---

## 📊 Platform Overview

### Key Features

**Educational Platform:**
- Comprehensive curriculum management (Board → Grade → Subject → Chapter → Topic)
- Student-teacher matching system
- Real-time booking and scheduling
- Progress tracking and analytics

**User Modules:**
- **Students**: Dashboard, profile, content discovery, booking, study list, calendar, notifications
- **Teachers**: Profile management, availability, qualifications, search/discovery
- **Admin**: Content management, user management, analytics

**Technical Capabilities:**
- Payment integration (Multiple providers)
- Wallet system for seamless transactions
- Real-time notifications via WebSocket
- Comprehensive analytics and reporting
- AI-powered recommendations (optional)

### Technology Stack

**Backend:**
- Spring Boot 3.2+ with Java 17
- PostgreSQL 15+ for primary database
- Redis 7+ for caching and sessions
- Apache Kafka for event streaming
- JWT for authentication
- WebSocket for real-time communication

**Frontend:**
- Next.js 14 with App Router
- TypeScript for type safety
- Tailwind CSS for styling
- shadcn/ui for components
- React Query for data fetching
- Zustand for state management

**Infrastructure:**
- Docker Compose for containerization
- Flyway for database migrations
- Playwright for E2E testing
- Jest for unit testing
- Prometheus & Grafana for monitoring

---

## 🏗️ Architecture Overview

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

For complete architecture details, see [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md).

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker and Docker Compose
- PostgreSQL 15+
- Redis 7+

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/siddharthakumar001/ankurshala-eduplatform.git
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
2. **Start services with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - API Health: http://localhost:8080/actuator/health

For complete setup and deployment instructions, see [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md).

---

## 📖 Documentation Structure

### Core Documentation

| File | Purpose | When to Read |
|------|---------|--------------|
| [README.md](./README.md) | Overview and navigation | Start here |
| [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md) | System architecture, database schema, tech stack | Understanding system design |
| [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md) | Auth flow, JWT, RBAC, security | Implementing auth features |
| [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) | Common issues and solutions | When things break |

### Module Documentation

| File | Purpose | When to Read |
|------|---------|--------------|
| [STUDENT_MODULE.md](./STUDENT_MODULE.md) | Complete student features | Working on student functionality |
| [TEACHER_MODULE.md](./TEACHER_MODULE.md) | Complete teacher features | Working on teacher functionality |
| [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md) | Booking system & concurrency | Working on bookings |

### Specialized Documentation

| File | Purpose | When to Read |
|------|---------|--------------|
| [ENHANCED_CURRICULUM_IMPORT.md](./ENHANCED_CURRICULUM_IMPORT.md) | Curriculum import automation | Adding/importing content |

---

## 🎯 Development Guidelines

### Adding New Features

1. **Plan:** Review architecture in [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md)
2. **Implement:** Follow patterns from module documentation
3. **Test:** Add unit and integration tests
4. **Document:** Update relevant module documentation (don't create new files!)
5. **Deploy:** Follow CI/CD pipeline

### Code Organization

**Backend (Java/Spring Boot):**
```
backend/src/main/java/com/ankurshala/backend/
├── controller/        # REST endpoints
├── service/           # Business logic
├── repository/        # Data access
├── dto/              # Data transfer objects
├── entity/           # JPA entities
└── config/           # Configuration
```

**Frontend (Next.js/TypeScript):**
```
frontend/src/
├── app/              # Next.js pages (App Router)
├── components/       # Reusable components
├── services/         # API clients
├── lib/              # Utilities
└── types/            # TypeScript types
```

### Testing Strategy

**Backend Testing:**
- Unit tests for services
- Integration tests for controllers
- Repository tests with H2
- Test coverage: 80%+

**Frontend Testing:**
- Component tests with Jest
- E2E tests with Playwright
- Integration tests for API calls

**E2E Testing (NEW - January 10, 2026):**
- ✅ **One-Command Local E2E:** `.\run-e2e-local.ps1`
- ✅ **Deterministic Test Data:** Auto-seeded on startup (E2ETestDataSeeder)
- ✅ **DEV AI Provider:** Zero-cost AI testing without external APIs
- ✅ **Comprehensive Test Suite:** 12+ Playwright tests covering all features
- ✅ **Test Credentials:** 5 pre-seeded accounts (students, teachers, admin)
- ✅ **CI/CD Ready:** Runs without external dependencies

**Quick E2E Start:**
```powershell
# One command to start everything and run tests
.\run-e2e-local.ps1

# View test report
cd frontend
npx playwright show-report
```

**E2E Test Credentials:**
- Student 1: `student-e2e1@ankurshala.com` / `Test@123`
- Student 2: `student-e2e2@ankurshala.com` / `Test@123`
- Admin: `admin-e2e@ankurshala.com` / `Test@123`

See [STUDENT_MODULE.md](./STUDENT_MODULE.md#update-january-10-2026---end-to-end-integration) for complete E2E documentation.

---

## 🔗 Key Resources

**API Endpoints:**
- Student APIs: See [STUDENT_MODULE.md](./STUDENT_MODULE.md#api-endpoints)
- Teacher APIs: See [TEACHER_MODULE.md](./TEACHER_MODULE.md#api-endpoints)
- Booking APIs: See [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md#api-endpoints)

**Database Schema:**
- Complete schema: [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md#database-design)
- Student tables: [STUDENT_MODULE.md](./STUDENT_MODULE.md#database-schema)
- Teacher tables: [TEACHER_MODULE.md](./TEACHER_MODULE.md#database-schema)
- Booking tables: [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md#database-schema)

**Authentication:**
- JWT flow: [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md#authentication-flow)
- RBAC: [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md#role-based-access-control)
- Security best practices: [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md#security-best-practices)

---

## 🆘 Getting Help

**Common Issues?** Check [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

**Module-Specific Questions?**
- Student features → [STUDENT_MODULE.md](./STUDENT_MODULE.md)
- Teacher features → [TEACHER_MODULE.md](./TEACHER_MODULE.md)
- Booking issues → [BOOKING_SYSTEM.md](./BOOKING_SYSTEM.md)

**Architecture Questions?** Review [ARCHITECTURE_AND_DESIGN.md](./ARCHITECTURE_AND_DESIGN.md)

**Security Concerns?** See [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md)

---

## ✅ Documentation Quality

This documentation set follows strict quality standards:

- ✅ **Complete:** All features documented
- ✅ **Current:** Updated January 9, 2026
- ✅ **Consolidated:** 8 files (down from 32)
- ✅ **Cross-referenced:** Easy navigation
- ✅ **Maintainable:** Clear ownership
- ✅ **Searchable:** Well-organized
- ✅ **Accessible:** Clear structure

---

**Version:** 1.0 Production Ready  
**Last Updated:** January 9, 2026  
**Maintained By:** Development Team  
**Status:** ✅ Deployed and Operational

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
