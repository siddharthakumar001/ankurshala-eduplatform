# 🚀 Deployment Steps for Latest Code

## Quick Deployment (Using Script)

SSH into your server and run:

```bash
ssh AnkurshalaVM@74.225.207.72
cd /opt/ankurshala
git pull origin ankurshala/prod-1.0-final
chmod +x scripts/deploy-latest.sh
./scripts/deploy-latest.sh
```

---

## Manual Deployment Steps

If you prefer to run commands manually, follow these steps:

### Step 1: SSH into the Server
```bash
ssh AnkurshalaVM@74.225.207.72
# Password: AnkurshalaVM@2025!2025
```

### Step 2: Navigate to Project Directory
```bash
cd /opt/ankurshala
```

### Step 3: Pull Latest Code
```bash
git fetch origin
git checkout ankurshala/prod-1.0-final
git pull origin ankurshala/prod-1.0-final
```

### Step 4: Get Current Commit SHA
```bash
COMMIT_SHA=$(git rev-parse --short HEAD)
echo "Deploying commit: $COMMIT_SHA"
```

### Step 5: Build Backend Image
```bash
export BACKEND_TAG="prod-$COMMIT_SHA"
docker compose -f docker-compose.prod.yml build --no-cache backend
```

### Step 6: Build Frontend Image
```bash
export FRONTEND_TAG="prod-$COMMIT_SHA"
docker compose -f docker-compose.prod.yml build --no-cache frontend
```

### Step 7: Stop Existing Containers
```bash
docker compose -f docker-compose.prod.yml stop backend frontend
```

### Step 8: Start Services with New Images
```bash
export BACKEND_TAG="prod-$COMMIT_SHA"
export FRONTEND_TAG="prod-$COMMIT_SHA"
docker compose -f docker-compose.prod.yml up -d backend frontend
```

### Step 9: Wait for Services to Start
```bash
sleep 15
```

### Step 10: Verify Deployment
```bash
# Check container status
docker compose -f docker-compose.prod.yml ps

# Check backend health
curl -f http://localhost:8080/api/actuator/health || echo "Backend not ready yet"

# Check frontend
curl -f http://localhost:3000 || echo "Frontend not ready yet"

# Check running images
docker images | grep -E "ankurshala/(backend|frontend)" | head -3
```

### Step 11: Reload Nginx (if needed)
```bash
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

---

## Verify Deployment

After deployment, verify the application is working:

1. **Check Application URL:**
   ```bash
   curl -I https://ankurshala.com
   ```

2. **Check Health Endpoint:**
   ```bash
   curl https://ankurshala.com/health
   ```

3. **View Container Logs:**
   ```bash
   # Backend logs
   docker compose -f docker-compose.prod.yml logs -f backend --tail=50
   
   # Frontend logs
   docker compose -f docker-compose.prod.yml logs -f frontend --tail=50
   ```

4. **Check Running Images:**
   ```bash
   docker images | grep ankurshala
   ```

---

## Troubleshooting

### If build fails:
```bash
# Clean Docker cache
docker system prune -f

# Try building again
docker compose -f docker-compose.prod.yml build --no-cache backend frontend
```

### If containers won't start:
```bash
# Check logs
docker compose -f docker-compose.prod.yml logs backend
docker compose -f docker-compose.prod.yml logs frontend

# Restart services
docker compose -f docker-compose.prod.yml restart backend frontend
```

### If old images are still running:
```bash
# Force recreate containers
docker compose -f docker-compose.prod.yml up -d --force-recreate backend frontend
```

---

## Expected Output

After successful deployment, you should see:
- ✅ Backend container running with image: `ankurshala/backend:prod-<COMMIT_SHA>`
- ✅ Frontend container running with image: `ankurshala/frontend:prod-<COMMIT_SHA>`
- ✅ Both containers showing "healthy" status
- ✅ Application accessible at https://ankurshala.com

