# Manual Testing Guide - AnkurShala Platform

## 🚀 Current Status

**All Docker containers are running successfully!**

```
✅ Backend (Spring Boot): http://localhost:8080 - HEALTHY
✅ Frontend (Next.js): http://localhost:3000 - HEALTHY
✅ PostgreSQL Database: localhost:5432 - HEALTHY
✅ Redis Cache: localhost:6379 - HEALTHY
✅ Kafka Message Broker: localhost:9092 - HEALTHY
✅ Zookeeper: Running - HEALTHY
✅ MailHog (Email Testing): http://localhost:8025 - HEALTHY
✅ Prometheus (Metrics): http://localhost:9090 - HEALTHY
✅ Grafana (Monitoring): http://localhost:3001 - HEALTHY
```

## 🔑 Demo Credentials

### Super Admin
- **Email**: siddhartha@ankurshala.com
- **Password**: Maza@123

### Students (5 accounts)
- **Email Pattern**: student1@example.com to student5@example.com
- **Password**: password123

### Teachers (5 accounts)
- **Email Pattern**: teacher1@example.com to teacher5@example.com
- **Password**: password123

## 📍 Access Points

### Frontend Application
- **URL**: http://localhost:3000
- **Description**: Main user interface
- **Login Page**: http://localhost:3000/login

### Backend API
- **Base URL**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/actuator/health
- **Metrics**: http://localhost:8080/api/actuator/prometheus

### MailHog (Email Testing)
- **URL**: http://localhost:8025
- **Description**: View all emails sent by the application
- **Usage**: Login, registration, password reset emails appear here

### Prometheus (Metrics)
- **URL**: http://localhost:9090
- **Description**: Monitor application metrics and performance

### Grafana (Dashboard)
- **URL**: http://localhost:3001
- **Default Credentials**: admin/admin
- **Description**: Visual dashboards for monitoring

## 🧪 Testing Workflows

### 1. Admin Flow Testing

#### A. Admin Login & Dashboard
1. Open http://localhost:3000/login
2. Login with: siddhartha@ankurshala.com / Maza@123
3. Verify dashboard loads with:
   - Total students count
   - Total teachers count
   - Active bookings
   - System metrics

#### B. User Management
1. Navigate to Users section
2. Test filters:
   - Filter by role (STUDENT/TEACHER/ADMIN)
   - Search by email/name
3. Test user actions:
   - View user details
   - Update user status
   - View user activity logs

#### C. Content Management
1. Navigate to Content section
2. Test course management:
   - View all courses
   - Create new course (Grade 9 Chemistry, etc.)
   - Edit existing course
   - Add chapters
   - Add topics
   - Add questions
3. Verify bulk upload:
   - Upload CSV file
   - View upload progress
   - Verify data imported correctly

#### D. Booking Management
1. Navigate to Bookings section
2. View all bookings with filters:
   - Filter by status (REQUESTED, CONFIRMED, COMPLETED, CANCELLED)
   - Filter by date range
   - Search by student/teacher
3. Test booking actions:
   - View booking details
   - Update booking status
   - View payment details

### 2. Student Flow Testing

#### A. Student Registration
1. Open http://localhost:3000/register
2. Fill registration form:
   - Email: teststudent@example.com
   - Password: Test@123
   - Name: Test Student
   - Role: STUDENT
3. Verify email sent in MailHog: http://localhost:8025
4. Click verification link from email
5. Verify successful registration

#### B. Student Login & Dashboard
1. Login with: student1@example.com / password123
2. Verify dashboard shows:
   - Upcoming bookings
   - Available subjects
   - Recent activities
3. Test profile:
   - View profile details
   - Update profile
   - Change password

#### C. Find Teachers
1. Navigate to Find Teachers
2. Test search/filters:
   - Search by subject (Chemistry, Math, Physics)
   - Filter by rating
   - Filter by price range
   - Filter by availability
3. View teacher profiles:
   - Rating and reviews
   - Hourly rate
   - Experience
   - Availability calendar

#### D. Book Session
1. Select a teacher
2. Choose available time slot
3. Enter session details:
   - Subject
   - Topic
   - Session duration
   - Special requests
4. Confirm booking
5. Verify booking appears in "My Bookings"
6. Check payment status

#### E. My Bookings
1. Navigate to My Bookings
2. View bookings list with tabs:
   - Upcoming
   - Completed
   - Cancelled
3. Test booking actions:
   - View booking details
   - Join session (if time is near)
   - Cancel booking
   - Reschedule booking
   - Submit feedback/rating

### 3. Teacher Flow Testing

#### A. Teacher Registration
1. Open http://localhost:3000/register
2. Fill registration form as TEACHER
3. Complete profile with:
   - Subjects expertise
   - Hourly rate
   - Experience
   - Bio
4. Verify profile completion

#### B. Teacher Login & Dashboard
1. Login with: teacher1@example.com / password123
2. Verify dashboard shows:
   - Pending booking requests
   - Upcoming sessions
   - Earnings summary
   - Recent students

#### C. Availability Management
1. Navigate to Availability
2. Set weekly schedule:
   - Add available time slots
   - Set recurring availability
   - Block specific dates
3. Update hourly rate
4. Set maximum daily bookings

#### D. Booking Requests
1. Navigate to Booking Requests
2. View pending requests:
   - Student details
   - Subject/topic
   - Proposed time
   - Offered price
3. Test actions:
   - Accept request
   - Reject request
   - Suggest alternative time

#### E. My Sessions
1. Navigate to My Sessions
2. View sessions list:
   - Today's sessions
   - Upcoming sessions
   - Past sessions
3. Test session actions:
   - View session details
   - Start session (if time)
   - Mark as completed
   - Add teacher notes
   - Submit feedback

### 4. API Testing

#### Health Check
```bash
curl http://localhost:8080/api/actuator/health
```
Expected: `{"status":"UP"}`

#### Authentication - Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student1@example.com",
    "password": "password123"
  }'
```
Expected: JWT token in response

#### Get Subjects
```bash
curl http://localhost:8080/api/subjects
```
Expected: List of subjects

#### Search Teachers
```bash
curl "http://localhost:8080/api/teachers/search?subject=Chemistry"
```
Expected: List of teachers teaching Chemistry

## 🔍 Testing Checklist

### Critical Features
- [ ] User Registration (Student, Teacher)
- [ ] Email Verification
- [ ] User Login (All roles)
- [ ] Password Reset Flow
- [ ] Profile Management
- [ ] Teacher Search & Filter
- [ ] Booking Creation
- [ ] Booking Confirmation
- [ ] Booking Cancellation
- [ ] Payment Flow
- [ ] Session Completion
- [ ] Feedback & Rating
- [ ] Admin User Management
- [ ] Admin Content Management
- [ ] Admin Booking Management

### Performance Testing
- [ ] Page load times (<3s)
- [ ] API response times (<500ms)
- [ ] Search functionality speed
- [ ] Large data set handling
- [ ] Concurrent booking handling

### Security Testing
- [ ] JWT authentication working
- [ ] Protected routes blocked
- [ ] Role-based access control
- [ ] CSRF protection
- [ ] XSS prevention
- [ ] SQL injection prevention
- [ ] Rate limiting working

### Integration Testing
- [ ] Database persistence
- [ ] Redis caching
- [ ] Kafka messaging
- [ ] Email sending (MailHog)
- [ ] File uploads
- [ ] Payment gateway (mock)
- [ ] Metrics collection (Prometheus)

## 🐛 Known Issues & Debugging

### Check Container Logs
```bash
# Backend logs
docker logs ankurshala_backend_local -f

# Frontend logs
docker logs ankurshala_frontend_local -f

# Database logs
docker logs ankurshala_db_local -f

# All logs
docker-compose logs -f
```

### Common Issues

#### 1. Frontend Not Loading
- Check frontend logs: `docker logs ankurshala_frontend_local`
- Verify port 3000 is not in use
- Check network connectivity

#### 2. API Errors
- Check backend logs: `docker logs ankurshala_backend_local`
- Verify database connection
- Check Redis connection
- Verify Kafka connectivity

#### 3. Database Connection Issues
- Check PostgreSQL logs: `docker logs ankurshala_db_local`
- Verify port 5432 is accessible
- Check credentials in .env file

#### 4. Email Not Sending
- Open MailHog: http://localhost:8025
- Check backend logs for email errors
- Verify SMTP configuration

## 📊 Monitoring

### Check Application Metrics
1. Open Prometheus: http://localhost:9090
2. Query examples:
   - `http_server_requests_seconds_count` - Total requests
   - `jvm_memory_used_bytes` - Memory usage
   - `system_cpu_usage` - CPU usage

### View Dashboards
1. Open Grafana: http://localhost:3001
2. Login: admin/admin
3. Import Prometheus data source
4. View pre-configured dashboards

## 🛠️ Useful Commands

### Docker Management
```bash
# View container status
docker-compose ps

# Stop all containers
docker-compose stop

# Start all containers
docker-compose start

# Restart specific service
docker-compose restart backend

# View logs
docker-compose logs -f [service-name]

# Execute command in container
docker-compose exec backend sh
docker-compose exec postgres psql -U ankurshala ankurshala_db
```

### Database Access
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U ankurshala ankurshala_db

# Common queries
SELECT COUNT(*) FROM users;
SELECT * FROM users WHERE role = 'TEACHER';
SELECT * FROM bookings WHERE status = 'CONFIRMED';
```

### Redis Access
```bash
# Connect to Redis
docker-compose exec redis redis-cli

# Common commands
KEYS *
GET key_name
TTL key_name
```

## 📝 Test Results Template

```markdown
## Test Results - [Date]

### Environment
- Frontend Version: [commit hash]
- Backend Version: [commit hash]
- Database: PostgreSQL 15
- All services: Docker containers

### Tests Executed
| Test Case | Status | Notes |
|-----------|--------|-------|
| Admin Login | ✅ | - |
| Student Registration | ✅ | Email sent successfully |
| Teacher Search | ✅ | - |
| Booking Creation | ✅ | - |
| Payment Flow | ❌ | Issue with payment gateway |

### Issues Found
1. **Issue Title**
   - Severity: High/Medium/Low
   - Steps to reproduce:
   - Expected behavior:
   - Actual behavior:
   - Screenshots:

### Performance Metrics
- Average page load: X seconds
- Average API response: Y ms
- Concurrent users tested: Z

### Recommendations
1. [Recommendation 1]
2. [Recommendation 2]
```

## 🎯 Next Steps

After manual testing:
1. Document all issues found
2. Verify critical flows work end-to-end
3. Test edge cases and error scenarios
4. Verify production-like behavior in containers
5. Review logs for any warnings/errors
6. Check resource usage (CPU, memory)
7. Prepare for production deployment

## 📞 Support

If you encounter any issues during testing:
1. Check container logs first
2. Verify .env configuration
3. Review documentation in docs/ folder
4. Check TROUBLESHOOTING.md for common issues

---

**Happy Testing! 🚀**
