# Teacher Module - Comprehensive Documentation

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Features](#core-features)
4. [API Endpoints](#api-endpoints)
5. [Database Schema](#database-schema)
6. [Services](#services)
7. [Testing](#testing)
8. [Integration Guide](#integration-guide)
9. [Troubleshooting](#troubleshooting)

---

## Overview

The Teacher Module provides comprehensive functionality for teacher profile management, search, availability tracking, bookings, earnings, and dashboard analytics. It enables teachers to manage their professional profiles, set availability schedules, accept bookings, and track performance metrics.

**Key Components**:
- Teacher profile management with qualifications, experience, certifications
- Public teacher search for students
- Availability and scheduling management
- Dashboard with analytics and earnings
- Admin teacher management

**Technologies**:
- **Backend**: Spring Boot, JPA/Hibernate, PostgreSQL
- **Authentication**: JWT with role-based access control
- **API**: RESTful with DTOs
- **Caching**: Redis for search results
- **Validation**: Jakarta Validation (Bean Validation)

---

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (Next.js)                        │
│                                                              │
│  Teacher Pages:                                              │
│  - /teacher/dashboard        (Dashboard & Analytics)         │
│  - /teacher/profile          (Profile Management)            │
│  - /teacher/availability     (Schedule Management)           │
│  - /teacher/bookings         (Booking Requests)              │
│  - /teacher/earnings         (Payment History)               │
│                                                              │
│  Public Pages (for Students):                                │
│  - /student/find-teachers    (Teacher Search)                │
│  - /student/teacher/:id      (Teacher Profile View)          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                         ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                   BACKEND (Spring Boot)                      │
│                                                              │
│  Controllers:                                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ TeacherProfileController                             │  │
│  │  - GET/PUT /teacher/profile                          │  │
│  │  - GET/POST/PUT/DELETE qualifications/experience     │  │
│  │  - GET/POST/PUT/DELETE certifications/documents      │  │
│  │  - GET/PUT /teacher/availability                     │  │
│  │  - GET/PUT /teacher/bank-details                     │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ TeacherDashboardController                           │  │
│  │  - GET /teacher/dashboard (stats, bookings, earnings)│  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ PublicTeacherController                              │  │
│  │  - POST /public/teachers/search                      │  │
│  │  - POST /public/teachers/availability                │  │
│  │  - GET /public/teachers/{id}/profile                 │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ AdminTeachersController                              │  │
│  │  - GET /admin/teachers (list with filters)           │  │
│  │  - GET /admin/teachers/{id}                          │  │
│  │  - PUT /admin/teachers/{id}                          │  │
│  │  - POST /admin/teachers/{id}/toggle-status           │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Services:                                                   │
│  - TeacherService                                            │
│  - TeacherProfileService                                     │
│  - TeacherDashboardService                                   │
│  - PublicTeacherService                                      │
│  - AdminTeacherService                                       │
│                                                              │
│  Repositories:                                               │
│  - TeacherRepository                                         │
│  - TeacherProfileRepository                                  │
│  - BookingRepository                                         │
│  - TeacherEarningsRepository                                 │
│  - TeacherPerformanceMetricsRepository                       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                         ↓
                  ┌──────────────┐
                  │  PostgreSQL  │
                  │   Database   │
                  │              │
                  │ Tables:      │
                  │ - teachers   │
                  │ - teacher_   │
                  │   profiles   │
                  │ - bookings   │
                  │ - earnings   │
                  │ - users      │
                  └──────────────┘
```

### Security Model

- **Teacher Endpoints** (`/teacher/*`): Requires `ROLE_TEACHER` authentication
- **Public Endpoints** (`/public/teachers/*`): No authentication required
- **Admin Endpoints** (`/admin/teachers/*`): Requires `ROLE_ADMIN` authentication
- **Data Isolation**: Teachers can only access/modify their own data
- **JWT Tokens**: HTTP-only cookies with CSRF protection

---

## Core Features

### 1. Teacher Profile Management

Complete profile system with:
- **Basic Information**: Name, email, mobile, contact details
- **Professional Details**: Highest education, bio, profile photo
- **Location**: Postal address, city, state, country
- **Rating**: Aggregate rating from student reviews
- **Hourly Rate**: Teaching fee per hour

**Key Fields**:
```java
TeacherProfile {
    id: Long
    user: User
    firstName: String
    lastName: String
    mobileNumber: String
    contactEmail: String
    highestEducation: String
    bio: String
    profilePhotoUrl: String
    rating: BigDecimal (0.0 - 5.0)
    hourlyRate: BigDecimal
    city: String
    state: String
    country: String
    status: TeacherStatus (PENDING, ACTIVE, INACTIVE)
    verified: Boolean
}
```

### 2. Qualifications Management

CRUD operations for academic qualifications:
- Degree name and field
- Institution and location
- Graduation year
- Grade/CGPA

### 3. Experience Management

CRUD operations for teaching experience:
- Company/Institution name
- Position/Role
- Start and end dates
- Description of responsibilities

### 4. Certifications Management

CRUD operations for certifications:
- Certification name
- Issuing organization
- Issue and expiry dates
- Certificate URL

### 5. Documents Management

Upload and manage documents:
- ID proofs
- Educational certificates
- Address proofs
- Other supporting documents

### 6. Availability Management

Set weekly schedule and time slots:
- Weekly recurring availability
- Specific date availability
- Block dates when unavailable
- Configure buffer time between sessions

### 7. Bank Details Management

Securely store payment information:
- Bank account number (encrypted)
- IFSC code
- Account holder name
- Bank name and branch

### 8. Teacher Search (Public)

Students can search for teachers with:
- **Subject Filter**: Search by teaching subjects
- **Rating Filter**: Minimum rating (1-5 stars)
- **Price Range**: Minimum and maximum hourly rate
- **Availability**: Teachers with available slots
- **Location**: City/state filtering
- **Sorting**: By rating, price, experience
- **Pagination**: 10 results per page

**Search Response**:
```java
TeacherSearchResponse {
    id: Long
    name: String
    email: String
    profilePictureUrl: String
    bio: String
    rating: Double
    totalReviews: Integer
    hourlyRate: BigDecimal
    specializations: List<String>
    languages: List<String>
    city: String
    state: String
    experienceYears: Integer
}
```

### 9. Teacher Availability Check (Public)

Students can check specific teacher availability:
- View available time slots for a specific date
- Slots generated: 9 AM - 9 PM (1-hour intervals)
- Real-time booking conflict detection
- Shows booked, available, and blocked slots

### 10. Teacher Dashboard

Comprehensive dashboard with:
- **Statistics**:
  - Total bookings (all-time)
  - Upcoming bookings (next 7 days)
  - Completed bookings
  - Total earnings
  - Average rating
  - Pending requests
- **Upcoming Bookings**: Next 5 sessions with student details
- **Recent Earnings**: Last 10 payment transactions
- **Performance Metrics**: Session completion rate, cancellation rate

### 11. Admin Management

Administrators can:
- List all teachers with filters (status, verified, enabled)
- Search teachers by name/email
- View detailed teacher profiles
- Update teacher information
- Toggle teacher account status (enable/disable)
- Verify teacher profiles

---

## API Endpoints

### Teacher Profile Endpoints (Authenticated)

#### GET /teacher/profile
Get authenticated teacher's profile.

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "+919876543210",
    "contactEmail": "john.doe@example.com",
    "highestEducation": "M.Sc. Chemistry",
    "bio": "Experienced chemistry teacher with 10 years...",
    "profilePhotoUrl": "https://...",
    "rating": 4.5,
    "hourlyRate": 500.00,
    "city": "Mumbai",
    "state": "Maharashtra",
    "status": "ACTIVE",
    "verified": true
  }
}
```

#### PUT /teacher/profile
Update teacher profile.

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "mobileNumber": "+919876543210",
  "contactEmail": "john.doe@example.com",
  "highestEducation": "M.Sc. Chemistry",
  "bio": "Experienced chemistry teacher...",
  "profilePhotoUrl": "https://...",
  "hourlyRate": 500.00,
  "city": "Mumbai",
  "state": "Maharashtra"
}
```

#### GET /teacher/qualifications
Get all qualifications for authenticated teacher.

#### POST /teacher/qualifications
Add new qualification.

**Request Body**:
```json
{
  "degree": "M.Sc.",
  "fieldOfStudy": "Chemistry",
  "institution": "University of Mumbai",
  "location": "Mumbai, India",
  "graduationYear": 2015,
  "grade": "First Class"
}
```

#### PUT /teacher/qualifications/{id}
Update existing qualification.

#### DELETE /teacher/qualifications/{id}
Delete qualification.

#### GET /teacher/experience
Get all experience entries.

#### POST /teacher/experience
Add new experience.

**Request Body**:
```json
{
  "company": "ABC Academy",
  "position": "Senior Chemistry Teacher",
  "startDate": "2015-06-01",
  "endDate": "2020-05-31",
  "description": "Taught CBSE Class 11 and 12 Chemistry..."
}
```

#### PUT /teacher/experience/{id}
Update experience entry.

#### DELETE /teacher/experience/{id}
Delete experience entry.

#### GET /teacher/certifications
Get all certifications.

#### POST /teacher/certifications
Add new certification.

**Request Body**:
```json
{
  "name": "Google Certified Educator Level 1",
  "issuingOrganization": "Google for Education",
  "issueDate": "2022-01-15",
  "expiryDate": "2025-01-15",
  "certificateUrl": "https://..."
}
```

#### PUT /teacher/certifications/{id}
Update certification.

#### DELETE /teacher/certifications/{id}
Delete certification.

#### GET /teacher/documents
Get all uploaded documents.

#### POST /teacher/documents
Upload new document.

**Request Body**:
```json
{
  "documentName": "Aadhar Card",
  "documentType": "ID_PROOF",
  "documentUrl": "https://..."
}
```

#### PUT /teacher/documents/{id}
Update document details.

#### DELETE /teacher/documents/{id}
Delete document.

#### GET /teacher/availability
Get teacher's availability schedule.

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "weeklySchedule": {
      "MONDAY": ["09:00-12:00", "14:00-18:00"],
      "TUESDAY": ["09:00-12:00", "14:00-18:00"],
      "WEDNESDAY": ["09:00-12:00"],
      "THURSDAY": ["09:00-12:00", "14:00-18:00"],
      "FRIDAY": ["09:00-12:00", "14:00-18:00"],
      "SATURDAY": ["10:00-13:00"],
      "SUNDAY": []
    },
    "blockedDates": ["2024-12-25", "2024-12-26"],
    "bufferMinutes": 15
  }
}
```

#### PUT /teacher/availability
Update availability schedule.

#### GET /teacher/bank-details
Get bank details (sensitive data).

#### PUT /teacher/bank-details
Update bank details (encrypted storage).

**Request Body**:
```json
{
  "accountNumber": "1234567890",
  "ifscCode": "SBIN0001234",
  "accountHolderName": "John Doe",
  "bankName": "State Bank of India",
  "branchName": "Mumbai Main Branch"
}
```

---

### Teacher Dashboard Endpoints (Authenticated)

#### GET /teacher/dashboard
Get comprehensive dashboard data.

**Response**:
```json
{
  "success": true,
  "data": {
    "stats": {
      "totalBookings": 156,
      "upcomingBookings": 8,
      "completedBookings": 142,
      "totalEarnings": 78000.00,
      "averageRating": 4.6,
      "pendingRequests": 3
    },
    "upcomingBookings": [
      {
        "id": 101,
        "studentName": "Alice Student",
        "subject": "Chemistry",
        "scheduledDate": "2024-01-10T10:00:00",
        "duration": 60,
        "status": "CONFIRMED"
      }
    ],
    "recentEarnings": [
      {
        "id": 501,
        "bookingId": 98,
        "amount": 500.00,
        "date": "2024-01-08",
        "status": "CREDITED"
      }
    ],
    "performanceMetrics": {
      "completionRate": 95.5,
      "cancellationRate": 2.5,
      "averageSessionDuration": 58.5
    }
  }
}
```

---

### Public Teacher Endpoints (No Authentication)

#### POST /public/teachers/search
Search for teachers with filters.

**Request Body**:
```json
{
  "subject": "Chemistry",
  "minRating": 4.0,
  "maxRating": 5.0,
  "minPrice": 300,
  "maxPrice": 1000,
  "city": "Mumbai",
  "state": "Maharashtra"
}
```

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sortBy`: Sort field (rating, hourlyRate, experienceYears)
- `sortDir`: Sort direction (asc, desc)

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@example.com",
        "profilePictureUrl": "https://...",
        "bio": "Experienced chemistry teacher...",
        "rating": 4.5,
        "totalReviews": 87,
        "hourlyRate": 500.00,
        "specializations": ["CBSE Chemistry", "NEET Prep"],
        "languages": ["English", "Hindi"],
        "city": "Mumbai",
        "state": "Maharashtra",
        "experienceYears": 10
      }
    ],
    "totalElements": 45,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

#### POST /public/teachers/availability
Check teacher availability for a specific date.

**Request Body**:
```json
{
  "teacherId": 1,
  "date": "2024-01-15"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "teacherId": 1,
    "teacherName": "John Doe",
    "available": true,
    "availableSlots": [
      {
        "startTime": "09:00",
        "endTime": "10:00",
        "available": true
      },
      {
        "startTime": "10:00",
        "endTime": "11:00",
        "available": false
      },
      {
        "startTime": "11:00",
        "endTime": "12:00",
        "available": true
      }
    ],
    "message": "Teacher has available slots"
  }
}
```

#### GET /public/teachers/{teacherId}/profile
Get public teacher profile details.

**Response**: Same as search response but for single teacher

---

### Admin Teacher Endpoints (Admin Authentication Required)

#### GET /admin/teachers
List all teachers with filters.

**Query Parameters**:
- `search`: Search by name/email
- `enabled`: Filter by account status (true/false)
- `status`: Filter by teacher status (PENDING, ACTIVE, INACTIVE)
- `verified`: Filter by verification status (true/false)
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort field (default: id)

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 10,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "mobileNumber": "+919876543210",
        "status": "ACTIVE",
        "verified": true,
        "enabled": true,
        "rating": 4.5,
        "totalBookings": 156,
        "createdAt": "2023-06-15T10:00:00"
      }
    ],
    "totalElements": 234,
    "totalPages": 12,
    "currentPage": 0
  }
}
```

#### GET /admin/teachers/{id}
Get detailed teacher information.

**Response**: Complete teacher profile with all nested data

#### PUT /admin/teachers/{id}
Update teacher information.

**Request Body**: Same as teacher profile update

#### POST /admin/teachers/{id}/toggle-status
Enable or disable teacher account.

**Response**:
```json
{
  "success": true,
  "message": "Teacher status updated successfully",
  "data": {
    "id": 1,
    "enabled": false
  }
}
```

---

## Database Schema

### teachers Table
```sql
CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teachers_user_id ON teachers(user_id);
CREATE INDEX idx_teachers_status ON teachers(status);
CREATE INDEX idx_teachers_email ON teachers(email);
```

### teacher_profiles Table
```sql
CREATE TABLE teacher_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    teacher_id BIGINT REFERENCES teachers(id),
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    mobile_number VARCHAR(20),
    alternate_mobile_number VARCHAR(20),
    contact_email VARCHAR(255),
    highest_education VARCHAR(255),
    bio TEXT,
    profile_photo_url VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.00,
    hourly_rate DECIMAL(10,2),
    postal_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teacher_profiles_user_id ON teacher_profiles(user_id);
CREATE INDEX idx_teacher_profiles_status ON teacher_profiles(status);
CREATE INDEX idx_teacher_profiles_rating ON teacher_profiles(rating);
CREATE INDEX idx_teacher_profiles_city ON teacher_profiles(city);
```

### teacher_qualifications Table
```sql
CREATE TABLE teacher_qualifications (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    degree VARCHAR(255) NOT NULL,
    field_of_study VARCHAR(255),
    institution VARCHAR(255),
    location VARCHAR(255),
    graduation_year INTEGER,
    grade VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_qualifications_teacher_id ON teacher_qualifications(teacher_id);
```

### teacher_experiences Table
```sql
CREATE TABLE teacher_experiences (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    position VARCHAR(255),
    start_date DATE,
    end_date DATE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_experiences_teacher_id ON teacher_experiences(teacher_id);
```

### teacher_certifications Table
```sql
CREATE TABLE teacher_certifications (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    certificate_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_certifications_teacher_id ON teacher_certifications(teacher_id);
```

### teacher_documents Table
```sql
CREATE TABLE teacher_documents (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(100),
    document_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_teacher_id ON teacher_documents(teacher_id);
```

### teacher_availabilities Table
```sql
CREATE TABLE teacher_availabilities (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
    weekly_schedule JSONB,
    blocked_dates JSONB,
    buffer_minutes INTEGER DEFAULT 15,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_availabilities_teacher_id ON teacher_availabilities(teacher_id);
```

### teacher_bank_details Table
```sql
CREATE TABLE teacher_bank_details (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
    account_number VARCHAR(255) NOT NULL, -- Encrypted
    ifsc_code VARCHAR(50) NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255),
    branch_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bank_details_teacher_id ON teacher_bank_details(teacher_id);
```

---

## Services

### TeacherService
Core service for teacher entity management.

**Methods**:
- `createTeacher(User user)`: Create teacher profile on registration
- `getTeacherByUserId(Long userId)`: Fetch teacher by user ID
- `getTeacherProfileByUserId(Long userId)`: Fetch teacher profile

### TeacherProfileService
Comprehensive service for profile, qualifications, experience, certifications, documents, availability, and bank details.

**Key Methods**:
- Profile: `getTeacherProfile()`, `updateTeacherProfile()`
- Qualifications: CRUD operations
- Experience: CRUD operations
- Certifications: CRUD operations
- Documents: CRUD operations
- Availability: `getTeacherAvailability()`, `updateTeacherAvailability()`
- Bank Details: `getTeacherBankDetails()`, `updateTeacherBankDetails()` (encrypted)

### PublicTeacherService
Public-facing service for teacher search and availability.

**Key Methods**:
- `searchTeachers(request, pageable)`: Search with filters
- `getTeacherAvailability(request)`: Check specific date availability
- `getTeacherProfile(teacherId)`: Get public profile
- `generateTimeSlotsForTeacher()`: Generate hourly slots for a day
- `checkTeacherAvailability()`: Real-time booking conflict detection

### TeacherDashboardService
Dashboard analytics and statistics.

**Key Methods**:
- `getTeacherDashboard(userId)`: Fetch complete dashboard data
- `getDashboardStats()`: Calculate statistics
- `getUpcomingBookings()`: Next 5 bookings
- `getRecentEarnings()`: Last 10 earnings

### AdminTeacherService
Admin management of teachers.

**Key Methods**:
- `getTeachersWithFilters()`: List with pagination and filters
- `getTeacherById()`: Get detailed teacher info
- `updateTeacher()`: Update teacher data
- `toggleTeacherStatus()`: Enable/disable account

---

## Testing

### Unit Tests

#### TeacherServiceTest
Tests for core teacher service:
```java
@Test
void testCreateTeacher_Success() {
    // Test teacher creation on user registration
}

@Test
void testGetTeacherByUserId_Success() {
    // Test fetching teacher by user ID
}
```

#### AdminTeachersControllerTest
Tests for admin endpoints:
```java
@Test
void testGetTeachers_Success() {
    // Test listing teachers with filters
}

@Test
void testUpdateTeacher_Success() {
    // Test updating teacher information
}

@Test
void testToggleTeacherStatus() {
    // Test enabling/disabling teacher
}
```

### Integration Tests

#### PublicTeacherControllerIntegrationTest
```bash
# Test teacher search
POST /public/teachers/search
{
  "subject": "Chemistry",
  "minRating": 4.0
}

# Test availability check
POST /public/teachers/availability
{
  "teacherId": 1,
  "date": "2024-01-15"
}
```

#### TeacherProfileControllerIntegrationTest
```bash
# Test profile update (authenticated)
PUT /teacher/profile
Authorization: Bearer <JWT_TOKEN>
{
  "firstName": "Updated",
  "bio": "New bio..."
}

# Test adding qualification
POST /teacher/qualifications
Authorization: Bearer <JWT_TOKEN>
{
  "degree": "Ph.D.",
  "fieldOfStudy": "Chemistry"
}
```

### Manual Testing

#### Teacher Registration Flow
1. Register as TEACHER role
2. Verify profile created with PENDING status
3. Complete profile (bio, education, hourly rate)
4. Add qualifications, experience, certifications
5. Set weekly availability schedule
6. Admin verifies and activates profile

#### Teacher Search Flow
1. Student searches for "Chemistry" teachers
2. Filters by rating (4+ stars) and price (₹300-₹800)
3. Sorts by rating (descending)
4. Views teacher profile
5. Checks availability for specific date
6. Books available time slot

#### Teacher Dashboard Flow
1. Login as teacher
2. View dashboard statistics
3. See pending booking requests
4. Check upcoming sessions
5. Review recent earnings
6. Monitor performance metrics

---

## Integration Guide

### Frontend Integration

#### 1. Teacher Search Component
```typescript
import { teacherAPI } from '@/services/teacherAPI'

const SearchTeachers = () => {
  const [teachers, setTeachers] = useState([])
  
  const handleSearch = async (filters) => {
    const response = await teacherAPI.searchTeachers({
      subject: filters.subject,
      minRating: filters.minRating,
      maxPrice: filters.maxPrice
    }, 0, 10, 'rating', 'desc')
    
    setTeachers(response.content)
  }
  
  return (
    // Search UI with filters
  )
}
```

#### 2. Teacher Profile Update
```typescript
import { teacherAPI } from '@/services/teacherAPI'

const TeacherProfile = () => {
  const handleUpdate = async (profileData) => {
    await teacherAPI.updateProfile({
      firstName: profileData.firstName,
      lastName: profileData.lastName,
      bio: profileData.bio,
      hourlyRate: profileData.hourlyRate
    })
    
    toast.success('Profile updated successfully!')
  }
  
  return (
    // Profile form
  )
}
```

#### 3. Availability Management
```typescript
import { teacherAPI } from '@/services/teacherAPI'

const AvailabilityManager = () => {
  const handleUpdate = async (schedule) => {
    await teacherAPI.updateAvailability({
      weeklySchedule: {
        MONDAY: ['09:00-12:00', '14:00-18:00'],
        TUESDAY: ['09:00-12:00']
      },
      blockedDates: ['2024-12-25'],
      bufferMinutes: 15
    })
    
    toast.success('Availability updated!')
  }
  
  return (
    // Weekly schedule editor
  )
}
```

### Backend Integration

#### 1. Create Teacher on User Registration
```java
@Service
public class UserRegistrationService {
    
    @Autowired
    private TeacherService teacherService;
    
    public void registerTeacher(UserRegistrationDto dto) {
        // Create user
        User user = createUser(dto);
        
        // Create teacher profile
        Teacher teacher = teacherService.createTeacher(user);
        
        // Send welcome email
        emailService.sendTeacherWelcomeEmail(user);
    }
}
```

#### 2. Search Integration with Caching
```java
@Service
public class PublicTeacherService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public Page<TeacherSearchResponse> searchTeachers(request, pageable) {
        String cacheKey = generateCacheKey(request, pageable);
        
        // Check cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Page<TeacherSearchResponse>) cached;
        }
        
        // Query database
        Page<TeacherProfile> teachers = repository.searchWithFilters(request, pageable);
        Page<TeacherSearchResponse> response = teachers.map(this::mapToSearchResponse);
        
        // Cache for 5 minutes
        redisTemplate.opsForValue().set(cacheKey, response, 5, TimeUnit.MINUTES);
        
        return response;
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. Teacher Search Returns No Results

**Symptoms**: Search returns empty array even when teachers exist

**Diagnosis**:
```sql
-- Check if teachers are marked as ACTIVE and verified
SELECT COUNT(*) FROM teacher_profiles 
WHERE status = 'ACTIVE' AND verified = true;

-- Check search filters
SELECT * FROM teacher_profiles 
WHERE status = 'ACTIVE' 
  AND verified = true
  AND rating >= 4.0
  AND hourly_rate BETWEEN 300 AND 1000;
```

**Solutions**:
- Verify teachers have `status = 'ACTIVE'` and `verified = true`
- Check if filters are too restrictive
- Ensure teachers have non-null rating and hourlyRate
- Review search query in `TeacherProfileRepository`

#### 2. Availability Slots Show Incorrect Status

**Symptoms**: Available slots show as booked, or vice versa

**Diagnosis**:
```sql
-- Check bookings for teacher on specific date
SELECT * FROM bookings 
WHERE teacher_id = 1 
  AND DATE(scheduled_time) = '2024-01-15'
  AND status IN ('CONFIRMED', 'REQUESTED');

-- Check teacher availability configuration
SELECT weekly_schedule, blocked_dates 
FROM teacher_availabilities 
WHERE teacher_id = 1;
```

**Solutions**:
- Verify booking times don't overlap
- Check if date is in `blocked_dates`
- Ensure `weekly_schedule` has correct day mappings
- Review `generateTimeSlotsForTeacher()` logic

#### 3. Profile Update Returns 403 Forbidden

**Symptoms**: Teacher cannot update their own profile

**Diagnosis**:
```bash
# Check JWT token
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/teacher/profile

# Verify user ID in token matches teacher user_id
```

**Solutions**:
- Ensure JWT token is valid and not expired
- Verify token contains correct user ID and ROLE_TEACHER
- Check `SecurityConfig` allows `/teacher/**` for ROLE_TEACHER
- Verify `@PreAuthorize` annotations on controller methods

#### 4. Dashboard Statistics Incorrect

**Symptoms**: Dashboard shows wrong booking counts or earnings

**Diagnosis**:
```sql
-- Verify booking counts
SELECT status, COUNT(*) 
FROM bookings 
WHERE teacher_id = 1 
GROUP BY status;

-- Verify earnings sum
SELECT SUM(amount) 
FROM teacher_earnings 
WHERE teacher_id = 1 AND status = 'CREDITED';

-- Check query in TeacherDashboardService
```

**Solutions**:
- Review booking status filters in service
- Verify earnings calculation includes only CREDITED payments
- Check date range filters for upcoming bookings
- Ensure proper JOIN conditions in repository queries

#### 5. Search Performance Slow

**Symptoms**: Teacher search takes > 2 seconds

**Diagnosis**:
```sql
-- Check query performance
EXPLAIN ANALYZE 
SELECT * FROM teacher_profiles 
WHERE status = 'ACTIVE' 
  AND verified = true
  AND rating >= 4.0;

-- Check missing indexes
SELECT * FROM pg_indexes 
WHERE tablename = 'teacher_profiles';
```

**Solutions**:
- Add indexes on commonly filtered columns:
  ```sql
  CREATE INDEX idx_teacher_profiles_status_verified ON teacher_profiles(status, verified);
  CREATE INDEX idx_teacher_profiles_rating_rate ON teacher_profiles(rating, hourly_rate);
  ```
- Enable Redis caching for search results
- Implement pagination properly
- Use `@Transactional(readOnly = true)` for queries

---

## Summary

✅ **Complete CRUD Operations**: Profile, qualifications, experience, certifications, documents  
✅ **Public Teacher Search**: Advanced filtering, sorting, pagination  
✅ **Real-time Availability**: Slot generation with booking conflict detection  
✅ **Comprehensive Dashboard**: Statistics, bookings, earnings, performance metrics  
✅ **Admin Management**: Full control over teacher accounts  
✅ **Secure & Scalable**: JWT authentication, encrypted bank details, Redis caching  
✅ **Well-tested**: Unit tests, integration tests, manual test guides  

**Status**: ✅ Production Ready

---

**Last Updated**: January 9, 2026  
**Version**: 1.0.0  
**Module Status**: Complete & Deployed
