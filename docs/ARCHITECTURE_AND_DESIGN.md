# Architecture and Design

**Last Updated:** October 7, 2025

---

## System Architecture

### High-Level Architecture

```
┌─────────────┐      ┌──────────────┐      ┌────────────┐
│   Browser   │─────▶│   Next.js    │─────▶│ Spring Boot│
│   Client    │◀─────│   Frontend   │◀─────│  Backend   │
└─────────────┘      └──────────────┘      └────────────┘
                            │                      │
                            │                      │
                            ▼                      ▼
                     ┌─────────────┐        ┌───────────┐
                     │   Static    │        │PostgreSQL │
                     │   Assets    │        │ Database  │
                     └─────────────┘        └───────────┘
```

### Technology Stack

**Frontend:**
- Next.js 14 (React 18)
- TypeScript
- Tailwind CSS
- Shadcn/ui components
- Playwright for testing

**Backend:**
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL 14+

**Infrastructure:**
- Docker & Docker Compose
- Azure Container Registry
- Azure App Service
- GitHub Actions (CI/CD)

---

## Database Design

### Core Entities

**Users & Authentication:**
- `users` - All user accounts
- `roles` - User roles (ADMIN, TEACHER, STUDENT)
- `user_roles` - User-role mapping

**Educational Structure:**
- `boards` - Educational boards (CBSE, ICSE, etc.)
- `grades` - Grade levels (Grade 7-12, etc.)
- `subjects` - Subjects per grade
- `chapters` - Chapters per subject
- `topics` - Topics per chapter
  - **AI Integration Fields:**
    - `description` (TEXT) - Detailed topic description for semantic search
    - `summary` (TEXT) - Brief topic summary for quick reference
    - `suggested_topics` (TEXT) - Comma-separated list of related topic codes for knowledge graph
    - `expected_time_mins` (INTEGER) - Learning duration for personalized study plans
- `topic_links` - Relationships between topics (PREREQUISITE, RELATED)
- `topic_notes` - Additional notes for topics

**Students:**
- `students` - Student profiles
- `student_enrollments` - Course enrollments

**Content:**
- `courses` - Course definitions
- `course_content` - Learning materials

### Key Relationships

```sql
boards (1) ──────── (many) grades
grades (1) ──────── (many) subjects
subjects (1) ────── (many) chapters
chapters (1) ────── (many) topics
```

---

## API Structure

### Authentication Endpoints
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signout` - User logout

### Admin Endpoints
- `GET /api/admin/dashboard/metrics` - Dashboard stats
- `GET /api/admin/students` - List students
- `PATCH /api/admin/students/{id}/toggle-status` - Enable/disable student
- `PUT /api/admin/students/{id}` - Update student
- `DELETE /api/admin/students/{id}` - Delete student

### Content Management
- `GET /api/admin/boards` - List boards
- `POST /api/admin/boards` - Create board
- `GET /api/admin/grades` - List grades
- `GET /api/admin/subjects` - List subjects
- `GET /api/admin/chapters` - List chapters
- `GET /api/admin/topics` - List topics

---

## Frontend Architecture

### Directory Structure

```
frontend/
├── src/
│   ├── app/                 # Next.js App Router
│   │   ├── admin/          # Admin pages
│   │   ├── api/            # API routes (proxy)
│   │   ├── login/          # Auth pages
│   │   └── layout.tsx      # Root layout
│   ├── components/         # React components
│   │   ├── ui/            # Shadcn components
│   │   └── ...            # Custom components
│   ├── utils/             # Utility functions
│   │   └── api.ts         # API client
│   └── types/             # TypeScript types
├── tests/                 # Playwright tests
└── public/               # Static assets
```

### State Management
- React hooks for local state
- Server components for data fetching
- API client with automatic token handling

---

## Backend Architecture

### Package Structure

```
backend/src/main/java/com/ankurshala/
├── config/              # Configuration classes
│   ├── SecurityConfig  # Spring Security setup
│   └── JwtConfig       # JWT configuration
├── controller/         # REST controllers
│   ├── AuthController
│   ├── AdminController
│   └── ...
├── service/           # Business logic
├── repository/        # JPA repositories
├── model/            # Entity classes
├── dto/              # Data Transfer Objects
└── security/         # Security utilities
    ├── JwtUtils
    └── UserDetailsImpl
```

### Security Layer
- JWT-based authentication
- Role-based access control (RBAC)
- CORS configuration
- Method-level security annotations

---

## Design Patterns

### Frontend Patterns
- **Component Composition** - Reusable UI components
- **Custom Hooks** - Shared logic (useDebounce, useAuth)
- **API Proxy Pattern** - Next.js API routes proxy backend
- **Error Boundaries** - Graceful error handling

### Backend Patterns
- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Request/response objects
- **Service Layer** - Business logic separation
- **Dependency Injection** - Spring IoC container

---

## Security Design

### Authentication Flow
1. User submits credentials
2. Backend validates and generates JWT
3. Frontend stores JWT in httpOnly cookie
4. Subsequent requests include JWT in cookie
5. Backend validates JWT on each request

### Authorization
- Role-based access control (ADMIN, TEACHER, STUDENT)
- Method-level security in backend
- Frontend route guards for pages
- API-level permission checks

---

## Performance Considerations

### Frontend Optimization
- Server-side rendering (SSR) for initial load
- Static generation for public pages
- Image optimization with Next.js Image
- Code splitting and lazy loading
- Debounced search inputs

### Backend Optimization
- Database connection pooling
- Query optimization with indexes
- Pagination for large datasets
- Caching frequently accessed data
- Async processing for heavy operations

---

## Scalability

### Horizontal Scaling
- Stateless backend (JWT in cookies)
- Load balancer friendly
- Database connection pooling
- Containerized deployment

### Vertical Scaling
- Configurable JVM heap size
- Database resource allocation
- Frontend build optimization

---

**See Also:**
- [API_REFERENCE.md](./API_REFERENCE.md) for endpoint details
- [AUTHENTICATION_AND_SECURITY.md](./AUTHENTICATION_AND_SECURITY.md) for security details
