# Admin Suite Documentation

## Overview

The AnkurShala Admin Suite provides comprehensive administrative capabilities for managing the educational platform. This document outlines the implemented features, endpoints, and usage instructions.

## Implemented Features

### S2A.1 - Admin Dashboard ✅
- **Endpoint**: `/api/admin/dashboard/metrics`
- **Features**: 
  - User counts (students/teachers)
  - Active/inactive user statistics
  - Registration trends (7-day, 30-day)
  - Content statistics (boards, subjects, chapters, topics)
  - Import job statistics
- **Frontend**: `/admin` - Dashboard with KPIs and charts

### S2A.2 - Student Management ✅
- **Endpoints**: 
  - `GET /api/admin/students` - List students with filters
  - `GET /api/admin/students/{id}` - Get student details
  - `PUT /api/admin/students/{id}` - Update student
  - `PATCH /api/admin/students/{id}/toggle-status` - Toggle active status
  - `DELETE /api/admin/students/{id}` - Delete student
- **Features**:
  - Search and filtering (status, board, class)
  - Pagination and sorting
  - CRUD operations
  - Status management
- **Frontend**: `/admin/users/students` - Complete student management interface

### S2A.3 - Teacher Management ✅
- **Endpoints**:
  - `GET /api/admin/teachers` - List teachers with filters
  - `GET /api/admin/teachers/{id}` - Get teacher details
  - `PUT /api/admin/teachers/{id}` - Update teacher
  - `PATCH /api/admin/teachers/{id}/toggle-status` - Toggle active status
  - `DELETE /api/admin/teachers/{id}` - Delete teacher
- **Features**:
  - Search and filtering (status, verification, subjects)
  - Pagination and sorting
  - CRUD operations
  - Status management
- **Frontend**: `/admin/users/teachers` - Complete teacher management interface

### S2A.4 - Content Management ✅
- **CSV Import**:
  - `POST /api/admin/content/import/csv` - Import CSV content
  - `GET /api/admin/content/import/{jobId}` - Get import job status
  - `GET /api/admin/content/import/sample-csv` - Download sample CSV
- **Content CRUD**:
  - Boards: `GET/POST/PATCH/DELETE /api/admin/content/boards`
  - Subjects: `GET/POST/PATCH/DELETE /api/admin/content/subjects`
  - Chapters: `GET/POST/PATCH/DELETE /api/admin/content/chapters`
  - Topics: `GET/POST/PUT/PATCH/DELETE /api/admin/content/topics`
  - Topic Notes: `GET/POST/PUT/PATCH/DELETE /api/admin/content/topics/{id}/notes`
- **Features**:
  - CSV-only import with strict schema validation
  - Hours conversion (decimal hours → minutes)
  - Idempotent upserts
  - Soft delete with hard delete option
- **Frontend**: 
  - `/admin/content/import` - CSV import interface
  - `/admin/content/manage` - Content management interface
  - `/admin/content/browse` - Content browsing interface

### S2A.5 - Advanced Admin Features ✅

#### Analytics
- **Endpoints**:
  - `GET /api/admin/analytics/overview` - Overview analytics
  - `GET /api/admin/analytics/users` - User analytics
  - `GET /api/admin/analytics/content` - Content analytics
  - `GET /api/admin/analytics/imports` - Import analytics
- **Features**:
  - User distribution by role/board/grade
  - Content statistics
  - Import job analytics
  - Registration trends
- **Frontend**: `/admin/analytics` - Analytics dashboard

#### Pricing Rules
- **Endpoints**:
  - `GET /api/admin/pricing` - List pricing rules
  - `POST /api/admin/pricing` - Create pricing rule
  - `PUT /api/admin/pricing/{id}` - Update pricing rule
  - `PATCH /api/admin/pricing/{id}/active` - Toggle rule status
  - `DELETE /api/admin/pricing/{id}` - Delete pricing rule
  - `GET /api/admin/pricing/resolve` - Resolve pricing for taxonomy
- **Features**:
  - Taxonomy-based pricing (board/grade/subject/chapter/topic)
  - Rule priority resolution
  - Active/inactive status management
- **Frontend**: `/admin/pricing` - Pricing management interface

#### Notifications
- **Endpoints**:
  - `GET /api/admin/notifications` - List notifications
  - `POST /api/admin/notifications/broadcast` - Broadcast notification
  - `GET /api/admin/notifications/stats` - Notification statistics
- **Features**:
  - In-app and email notifications
  - Audience targeting (students/teachers/both)
  - Delivery method selection
  - Notification history
- **Frontend**: `/admin/notifications` - Notification management interface

#### Fee Waivers
- **Endpoints**:
  - `GET /api/admin/fees/waivers` - List fee waivers
  - `POST /api/admin/fees/waive` - Create fee waiver
  - `GET /api/admin/fees/waivers/stats` - Fee waiver statistics
- **Features**:
  - Fee waiver tracking
  - Reason documentation
  - Amount tracking
  - Statistics and reporting
- **Frontend**: `/admin/fees` - Fee waiver management interface

## Database Schema

### New Tables Created
1. **pricing_rules** - Taxonomy-based pricing rules
2. **notifications** - In-app and email notifications
3. **fee_waivers** - Fee waiver tracking
4. **boards, grades, subjects, chapters, topics, topic_notes** - Content taxonomy

### Migrations Applied
- `V5__create_pricing_rules.sql` - Pricing rules table
- `V6__create_notifications.sql` - Notifications table
- `V7__create_fee_waivers.sql` - Fee waivers table

## API Examples

### Analytics Overview
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/analytics/overview"
```

### Create Pricing Rule
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"boardId": 1, "hourlyRate": 500.00}' \
  "http://localhost:8080/api/admin/pricing"
```

### Broadcast Notification
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "System Maintenance", "body": "Scheduled maintenance tonight", "audience": "BOTH", "delivery": "IN_APP"}' \
  "http://localhost:8080/api/admin/notifications/broadcast"
```

### Create Fee Waiver
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "reason": "Financial hardship", "amount": 1000.00}' \
  "http://localhost:8080/api/admin/fees/waive"
```

## Testing

### Backend Tests
```bash
make admin-test-backend
```

### Frontend Tests
```bash
make admin-test-e2e
```

### Sample Data
```bash
make seed-content-samples
```

### Generate Report
```bash
make report-admin
```

## Security

- All admin endpoints require `ROLE_ADMIN` authentication
- JWT token validation on all requests
- CORS configured for frontend access
- Input validation and sanitization
- SQL injection prevention through parameterized queries

## Status

✅ **COMPLETED**: All S2A.1 through S2A.5 features implemented and tested
- Admin Dashboard with KPIs and charts
- Student and Teacher management with full CRUD
- Content management with CSV import and taxonomy CRUD
- Analytics with comprehensive metrics
- Pricing rules with taxonomy-based resolution
- Notifications with in-app and email support
- Fee waivers with tracking and reporting

## Next Steps

The admin suite is production-ready and fully functional. All endpoints are tested and working correctly. The system provides comprehensive administrative capabilities for managing the AnkurShala educational platform.
