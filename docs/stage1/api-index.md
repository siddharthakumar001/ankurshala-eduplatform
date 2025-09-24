# Stage-1 API Catalog

## 📚 **API ENDPOINT INDEX**

This document provides a comprehensive catalog of all Stage-1 API endpoints, organized by functional area.

## 🔐 **AUTHENTICATION ENDPOINTS**

### **AuthController** (`/api/auth`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/AuthController.java`

| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `POST` | `/signup/student` | Student registration | Public | `SignupRequest` | `AuthResponse` |
| `POST` | `/signup/teacher` | Teacher registration | Public | `SignupRequest` | `AuthResponse` |
| `POST` | `/signin` | User login | Public | `SigninRequest` | `AuthResponse` |
| `POST` | `/refresh` | Token refresh | Public | `RefreshTokenRequest` | `AuthResponse` |
| `POST` | `/logout` | User logout | Public | `RefreshTokenRequest` | `Void` |

### **Sample cURL Commands**

#### Student Registration
```bash
curl -X POST http://localhost:8080/api/auth/signup/student \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210"
  }'
```

#### Teacher Registration
```bash
curl -X POST http://localhost:8080/api/auth/signup/teacher \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@example.com",
    "password": "Password123",
    "firstName": "Jane",
    "lastName": "Smith",
    "mobileNumber": "9876543210"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "Password123"
  }'
```

#### Token Refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token"
  }'
```

## 👤 **USER CONTEXT ENDPOINTS**

### **UserController** (`/api/user`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/UserController.java`

| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/me` | Get current user info | Authenticated | None | `Map<String, Object>` |

### **Sample cURL Command**
```bash
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer your-access-token"
```

## 🎓 **STUDENT PROFILE ENDPOINTS**

### **StudentProfileController** (`/api/student`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/StudentProfileController.java`

| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile` | Get student profile | `STUDENT` | None | `StudentProfileDto` |
| `PUT` | `/profile` | Update student profile | `STUDENT` | `StudentProfileDto` | `StudentProfileDto` |
| `GET` | `/profile/documents` | Get student documents | `STUDENT` | None | `List<StudentDocumentDto>` |
| `POST` | `/profile/documents` | Add student document | `STUDENT` | `StudentDocumentDto` | `StudentDocumentDto` |
| `DELETE` | `/profile/documents/{id}` | Delete student document | `STUDENT` | None | `Void` |

### **Sample cURL Commands**

#### Get Student Profile
```bash
curl -X GET http://localhost:8080/api/student/profile \
  -H "Authorization: Bearer your-access-token"
```

#### Update Student Profile
```bash
curl -X PUT http://localhost:8080/api/student/profile \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "schoolName": "ABC School",
    "classLevel": "GRADE_10"
  }'
```

#### Add Student Document
```bash
curl -X POST http://localhost:8080/api/student/profile/documents \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "documentName": "Report Card",
    "documentUrl": "https://example.com/report.pdf"
  }'
```

## 👨‍🏫 **TEACHER PROFILE ENDPOINTS**

### **TeacherProfileController** (`/api/teacher`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/TeacherProfileController.java`

#### **Profile Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile` | Get teacher profile | `TEACHER` | None | `TeacherProfileDto` |
| `PUT` | `/profile` | Update teacher profile | `TEACHER` | `TeacherProfileDto` | `TeacherProfileDto` |

#### **Qualifications Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/qualifications` | Get qualifications | `TEACHER` | None | `List<TeacherQualificationDto>` |
| `POST` | `/profile/qualifications` | Add qualification | `TEACHER` | `TeacherQualificationDto` | `TeacherQualificationDto` |
| `PUT` | `/profile/qualifications/{id}` | Update qualification | `TEACHER` | `TeacherQualificationDto` | `TeacherQualificationDto` |
| `DELETE` | `/profile/qualifications/{id}` | Delete qualification | `TEACHER` | None | `Void` |

#### **Experience Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/experiences` | Get experiences | `TEACHER` | None | `List<TeacherExperienceDto>` |
| `POST` | `/profile/experiences` | Add experience | `TEACHER` | `TeacherExperienceDto` | `TeacherExperienceDto` |
| `PUT` | `/profile/experiences/{id}` | Update experience | `TEACHER` | `TeacherExperienceDto` | `TeacherExperienceDto` |
| `DELETE` | `/profile/experiences/{id}` | Delete experience | `TEACHER` | None | `Void` |

#### **Certifications Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/certifications` | Get certifications | `TEACHER` | None | `List<TeacherCertificationDto>` |
| `POST` | `/profile/certifications` | Add certification | `TEACHER` | `TeacherCertificationDto` | `TeacherCertificationDto` |
| `PUT` | `/profile/certifications/{id}` | Update certification | `TEACHER` | `TeacherCertificationDto` | `TeacherCertificationDto` |
| `DELETE` | `/profile/certifications/{id}` | Delete certification | `TEACHER` | None | `Void` |

#### **Documents Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/documents` | Get documents | `TEACHER` | None | `List<TeacherDocumentDto>` |
| `POST` | `/profile/documents` | Add document | `TEACHER` | `TeacherDocumentDto` | `TeacherDocumentDto` |
| `PUT` | `/profile/documents/{id}` | Update document | `TEACHER` | `TeacherDocumentDto` | `TeacherDocumentDto` |
| `DELETE` | `/profile/documents/{id}` | Delete document | `TEACHER` | None | `Void` |

#### **Availability Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/availability` | Get availability | `TEACHER` | None | `TeacherAvailabilityDto` |
| `PUT` | `/profile/availability` | Update availability | `TEACHER` | `TeacherAvailabilityDto` | `TeacherAvailabilityDto` |

#### **Addresses Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/addresses` | Get addresses | `TEACHER` | None | `List<TeacherAddressDto>` |
| `POST` | `/profile/addresses` | Add address | `TEACHER` | `TeacherAddressDto` | `TeacherAddressDto` |
| `PUT` | `/profile/addresses/{id}` | Update address | `TEACHER` | `TeacherAddressDto` | `TeacherAddressDto` |
| `DELETE` | `/profile/addresses/{id}` | Delete address | `TEACHER` | None | `Void` |

#### **Bank Details Management**
| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile/bank-details` | Get bank details (masked) | `TEACHER` | None | `TeacherBankDetailsDto` |
| `PUT` | `/profile/bank-details` | Update bank details | `TEACHER` | `TeacherBankDetailsDto` | `TeacherBankDetailsDto` |

### **Sample cURL Commands**

#### Get Teacher Profile
```bash
curl -X GET http://localhost:8080/api/teacher/profile \
  -H "Authorization: Bearer your-access-token"
```

#### Add Teacher Qualification
```bash
curl -X POST http://localhost:8080/api/teacher/profile/qualifications \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "degree": "Masters",
    "institution": "University of Technology",
    "yearOfCompletion": 2020,
    "subject": "Computer Science"
  }'
```

#### Update Teacher Availability
```bash
curl -X PUT http://localhost:8080/api/teacher/profile/availability \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "availableFrom": "09:00",
    "availableTo": "17:00",
    "preferredLanguages": ["English", "Hindi"],
    "preferredLevels": ["GRADE_9", "GRADE_10"]
  }'
```

#### Update Bank Details (Encrypted)
```bash
curl -X PUT http://localhost:8080/api/teacher/profile/bank-details \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "123456789012",
    "bankName": "State Bank of India",
    "ifscCode": "SBIN0001234",
    "accountHolderName": "John Doe"
  }'
```

## 👨‍💼 **ADMIN PROFILE ENDPOINTS**

### **AdminProfileController** (`/api/admin`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/AdminProfileController.java`

| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `GET` | `/profile` | Get admin profile | `ADMIN` | None | `AdminProfileDto` |
| `PUT` | `/profile` | Update admin profile | `ADMIN` | `AdminProfileDto` | `AdminProfileDto` |

### **Sample cURL Commands**

#### Get Admin Profile
```bash
curl -X GET http://localhost:8080/api/admin/profile \
  -H "Authorization: Bearer your-access-token"
```

#### Update Admin Profile
```bash
curl -X PUT http://localhost:8080/api/admin/profile \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "9876543210",
    "department": "IT Administration"
  }'
```

## 🛠️ **DEVELOPMENT ENDPOINTS**

### **DevController** (`/admin`)
**File**: `backend/src/main/java/com/ankurshala/backend/controller/DevController.java`

| Method | Path | Description | RBAC | Request DTO | Response DTO |
|--------|------|-------------|------|-------------|--------------|
| `POST` | `/dev-seed` | Seed demo data | `ADMIN` | None | `SeedResult` |

### **Sample cURL Command**
```bash
curl -X POST http://localhost:8080/admin/dev-seed \
  -H "Authorization: Bearer your-access-token"
```

## 📊 **DATA TRANSFER OBJECTS (DTOs)**

### **Authentication DTOs**
- **SignupRequest**: `email`, `password`, `firstName`, `lastName`, `mobileNumber`
- **SigninRequest**: `email`, `password`
- **RefreshTokenRequest**: `refreshToken`
- **AuthResponse**: `accessToken`, `refreshToken`, `user` (id, email, name, role)

### **Student DTOs**
- **StudentProfileDto**: Personal info, academic info, contact details
- **StudentDocumentDto**: `documentName`, `documentUrl`, `uploadDate`

### **Teacher DTOs**
- **TeacherProfileDto**: Basic profile information
- **TeacherQualificationDto**: `degree`, `institution`, `yearOfCompletion`, `subject`
- **TeacherExperienceDto**: `institution`, `position`, `startDate`, `endDate`, `description`
- **TeacherCertificationDto**: `certificationName`, `issuingOrganization`, `issueDate`, `expiryDate`
- **TeacherDocumentDto**: `documentName`, `documentUrl`, `documentType`
- **TeacherAvailabilityDto**: `availableFrom`, `availableTo`, `preferredLanguages`, `preferredLevels`
- **TeacherAddressDto**: `addressType`, `street`, `city`, `state`, `postalCode`, `country`
- **TeacherBankDetailsDto**: `accountNumber` (encrypted), `bankName`, `ifscCode`, `accountHolderName`

### **Admin DTOs**
- **AdminProfileDto**: `phoneNumber`, `department`, `role`

## 🔒 **SECURITY & RBAC**

### **Role-Based Access Control**
- **Public**: Authentication endpoints (`/api/auth/**`)
- **Authenticated**: User context (`/api/user/**`)
- **STUDENT**: Student profile endpoints (`/api/student/**`)
- **TEACHER**: Teacher profile endpoints (`/api/teacher/**`)
- **ADMIN**: Admin profile and dev endpoints (`/api/admin/**`, `/admin/**`)

### **Rate Limiting**
- **Signin**: 5 requests/minute per IP
- **Signup**: 3 requests/hour per IP
- **General API**: 100 requests/minute per IP

### **JWT Token Configuration**
- **Access Token**: 15 minutes expiration
- **Refresh Token**: 7 days expiration
- **Algorithm**: HS256
- **Storage**: HTTP-only cookies (recommended) or localStorage

## 📝 **ERROR HANDLING**

### **Standard Error Response Format**
```json
{
  "type": "https://example.com/probs/validation-error",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Field validation failed",
  "instance": "/api/student/profile",
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

### **Common HTTP Status Codes**
- **200**: Success
- **201**: Created
- **400**: Bad Request (validation errors)
- **401**: Unauthorized (invalid/missing token)
- **403**: Forbidden (insufficient permissions)
- **404**: Not Found
- **409**: Conflict (duplicate resource)
- **429**: Too Many Requests (rate limit exceeded)
- **500**: Internal Server Error

## 🔗 **OPENAPI SPECIFICATION**

The complete OpenAPI specification is available at:
- **Development**: `http://localhost:8080/api-docs`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Generated Spec**: `backend/openapi.yaml`

## 📋 **TESTING**

### **E2E Test Coverage**
All endpoints are covered by Playwright E2E tests:
- **File**: `frontend/tests/e2e.stage1.full.spec.ts`
- **Coverage**: 100% of Stage-1 endpoints
- **Status**: 26/26 tests passing

### **Test Credentials**
```
Admin: siddhartha@ankurshala.com / Maza@123
Students: student1@ankurshala.com / Maza@123 (student1-5)
Teachers: teacher1@ankurshala.com / Maza@123 (teacher1-5)
```
