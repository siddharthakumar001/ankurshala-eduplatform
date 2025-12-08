# Troubleshooting Guide

**Last Updated:** October 7, 2025

---

## Common Issues

### 1. Cannot Login - 401 Unauthorized

**Symptoms:**
- Login fails with "Invalid credentials"
- Even with correct password

**Solutions:**
```bash
# Check if backend is running
docker ps | grep backend

# Check backend logs
docker-compose logs backend

# Verify database connection
docker-compose logs postgres

# Test login endpoint directly
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"siddhartha@ankurshala.com","password":"Maza@123"}'
```

---

### 2. Toggle Status Returns 405

**Symptoms:**
- Clicking toggle gives 405 Method Not Allowed

**Solution:**
✅ **FIXED** - PATCH handler added in `/frontend/src/app/api/admin/[...path]/route.ts`

If still failing:
```bash
# Restart frontend
docker-compose restart frontend

# Check frontend logs
docker-compose logs -f frontend
```

---

### 3. Class Dropdown Shows Wrong Classes

**Symptoms:**
- CBSE board shows grades 1-6
- Classes don't update when board changes

**Solution:**
✅ **FIXED** - Dynamic filtering implemented

Verify fix:
1. Open browser console (F12)
2. Check for JavaScript errors
3. Verify `getAvailableClasses()` function exists

---

### 4. Search Not Working

**Symptoms:**
- Typing in search doesn't filter results

**Solution:**
✅ Already working with 500ms debounce

Check:
```bash
# Test search via API
curl -b cookies.txt \
  "http://localhost:3000/api/admin/students?page=0&size=10&search=test"
```

---

### 5. Docker Container Won't Start

**Symptoms:**
- `docker-compose up` fails
- Port already in use

**Solutions:**
```bash
# Check what's using port 3000
lsof -i :3000
kill -9 <PID>

# Check port 8080
lsof -i :8080
kill -9 <PID>

# Clean Docker
docker-compose down -v
docker system prune -a
docker-compose up -d
```

---

### 6. Database Connection Failed

**Symptoms:**
- Backend logs show "Connection refused"

**Solutions:**
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Restart database
docker-compose restart postgres

# Check logs
docker-compose logs postgres

# Verify connection
docker exec -it ankurshala_postgres_local psql -U postgres -c '\l'
```

---

### 7. Frontend Build Fails

**Symptoms:**
- `docker-compose up` shows frontend errors

**Solutions:**
```bash
# Clear Next.js cache
cd frontend
rm -rf .next node_modules
npm install
npm run build

# Rebuild Docker image
docker-compose up -d --build frontend
```

---

### 8. Playwright Tests Fail

**Symptoms:**
- Tests timeout or fail to find elements

**Solutions:**
```bash
# Ensure services are running
docker-compose up -d
sleep 30

# Run tests in debug mode
cd frontend
npm run test:e2e -- --debug

# Run in headed mode to see browser
npm run test:e2e -- --headed

# Check selectors match current UI
# Update test files if UI changed
```

---

### 9. API Returns Empty Data

**Symptoms:**
- Students page shows "No data"
- Dashboard metrics are 0

**Solutions:**
```bash
# Check if data exists in database
docker exec -it ankurshala_postgres_local psql -U postgres ankurshala

# In psql:
SELECT COUNT(*) FROM students;
SELECT COUNT(*) FROM users;

# If no data, run seeder or create test data
```

---

### 10. Session Expires Too Quickly

**Symptoms:**
- Logged out after few minutes

**Solutions:**
```bash
# Check JWT expiration in backend
# backend/src/main/resources/application.properties

# Update JWT_EXPIRATION environment variable
# Default: 86400000 (24 hours)
```

---

## Performance Issues

### Slow API Responses

**Check:**
```bash
# Backend logs
docker-compose logs backend | grep "took"

# Database query performance
# Add indexes for frequently queried columns
```

**Optimize:**
- Add database indexes
- Enable query caching
- Use pagination everywhere

---

### Frontend Slow to Load

**Check:**
```bash
# Build size
cd frontend
npm run build
# Check .next/static size
```

**Optimize:**
- Enable Next.js optimizations
- Lazy load components
- Optimize images

---

## Debugging Tools

### Check All Services
```bash
./scripts/verify-deployment.sh
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f frontend
docker-compose logs -f backend
```

### Database Access
```bash
docker exec -it ankurshala_postgres_local psql -U postgres ankurshala
```

### Test API Endpoints
```bash
# Use test scripts
./scripts/test-admin-students.sh
./scripts/test-patch-direct.sh
```

---

## Getting Help

1. Check this troubleshooting guide first
2. Review relevant documentation
3. Check Docker and application logs
4. Test API endpoints directly
5. Verify database state

---

**See Also:**
- [SETUP_AND_DEPLOYMENT.md](./SETUP_AND_DEPLOYMENT.md) for setup issues
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) for test failures
- [API_REFERENCE.md](./API_REFERENCE.md) for API issues
