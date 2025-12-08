# AnkurShala Deployment Guide

## Overview

This document describes how to deploy AnkurShala to production. The deployment is **fully automated** via GitHub Actions.

## Quick Start

### Automatic Deployment (Recommended)

Simply push to the `ankurshala/prod-1.0-final` branch:

```bash
git add .
git commit -m "Your changes"
git push origin ankurshala/prod-1.0-final
```

GitHub Actions will automatically:
1. ✅ Detect what changed (backend, frontend, nginx, etc.)
2. ✅ Run tests for changed components
3. ✅ Build Docker images
4. ✅ Push images to GitHub Container Registry
5. ✅ Deploy to production server
6. ✅ Run health checks
7. ✅ Rollback if deployment fails

### Manual Deployment

If you need to deploy manually:

```bash
# SSH to the server
ssh AnkurshalaVM@74.225.207.72

# Navigate to project
cd /opt/ankurshala

# Run deployment script
./scripts/deploy.sh

# Or with options:
./scripts/deploy.sh --build          # Build images locally
./scripts/deploy.sh --backend-only   # Only deploy backend
./scripts/deploy.sh --frontend-only  # Only deploy frontend
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRODUCTION SERVER                         │
│                      (74.225.207.72)                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────┐    ┌──────────────┐    ┌──────────────┐            │
│  │  NGINX  │───▶│   Frontend   │───▶│   Backend    │            │
│  │  :443   │    │   (Next.js)  │    │ (Spring Boot)│            │
│  └─────────┘    │    :3000     │    │    :8080     │            │
│       │         └──────────────┘    └──────────────┘            │
│       │                                    │                     │
│       │         ┌──────────────────────────┼──────────────┐     │
│       │         │                          │              │     │
│       │    ┌────┴────┐  ┌─────────┐  ┌────┴────┐        │     │
│       │    │ Postgres │  │  Redis  │  │  Kafka  │        │     │
│       │    │   :5432  │  │  :6379  │  │  :9092  │        │     │
│       │    └──────────┘  └─────────┘  └─────────┘        │     │
│       │                                                   │     │
│       └──────────── HTTPS ───────────────────────────────┘     │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## CI/CD Pipeline

The pipeline is defined in `.github/workflows/deploy-production.yml`:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Detect     │───▶│   Build &    │───▶│   Deploy     │
│   Changes    │    │    Test      │    │   to Prod    │
└──────────────┘    └──────────────┘    └──────────────┘
       │                   │                   │
       ▼                   ▼                   ▼
  • Backend?          • Compile            • Pull images
  • Frontend?         • Unit tests         • Restart services
  • Nginx?            • Docker build       • Health checks
  • Docker?           • Push to GHCR       • Rollback if fail
```

## Required GitHub Secrets

Configure these in your GitHub repository settings (Settings → Secrets and Variables → Actions):

| Secret | Description | Required |
|--------|-------------|----------|
| `SSH_PRIVATE_KEY` | SSH key to access production server | ✅ Yes |
| `GITHUB_TOKEN` | Automatically provided by GitHub | Auto |

### Setting up SSH_PRIVATE_KEY

1. Generate SSH key pair (if not exists):
   ```bash
   ssh-keygen -t ed25519 -C "github-actions-deploy"
   ```

2. Add public key to server:
   ```bash
   ssh-copy-id -i ~/.ssh/id_ed25519.pub AnkurshalaVM@74.225.207.72
   ```

3. Add private key to GitHub:
   - Go to Repository → Settings → Secrets → Actions
   - New secret: `SSH_PRIVATE_KEY`
   - Paste the contents of `~/.ssh/id_ed25519`

## Configuration Files

### Production Environment (.env-prod)

Located at `/opt/ankurshala/.env-prod` on the server:

```ini
# Security
JWT_SECRET=<your-secret>
BANK_ENC_KEY=<your-key>

# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=ankur
DB_PASSWORD=<your-password>

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<your-password>

# Flyway (migrations)
SPRING_FLYWAY_OUT_OF_ORDER=true
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true

# API URL
NEXT_PUBLIC_API_URL=https://ankurshala.com/api

# Profiles
SPRING_PROFILES_ACTIVE=production
NODE_ENV=production
```

### Docker Compose (docker-compose.prod.yml)

Key services:
- `postgres`: PostgreSQL 15
- `redis`: Redis 7
- `kafka`: Confluent Kafka 7.4.0
- `backend`: Spring Boot API
- `frontend`: Next.js app
- `nginx`: Reverse proxy with SSL

## Troubleshooting

### Check Service Status

```bash
ssh AnkurshalaVM@74.225.207.72
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml ps
```

### View Logs

```bash
# All services
docker compose -f docker-compose.prod.yml logs -f

# Specific service
docker logs -f ankurshala_backend_prod
docker logs -f ankurshala_frontend_prod
docker logs -f ankurshala_nginx_prod
```

### Common Issues

#### 1. Backend won't start (Flyway migration error)
**Solution**: Already fixed in docker-compose.prod.yml with:
```yaml
SPRING_FLYWAY_OUT_OF_ORDER: "true"
SPRING_FLYWAY_BASELINE_ON_MIGRATE: "true"
```

#### 2. 401 errors after login
**Solution**: The nginx config now routes `/api/auth/*` and `/api/admin/*` through Next.js to set httpOnly cookies properly.

#### 3. Kafka cluster ID mismatch
**Solution**: Remove Kafka volume and restart:
```bash
docker compose -f docker-compose.prod.yml stop kafka
docker volume rm ankurshala_kafka_data
docker compose -f docker-compose.prod.yml up -d kafka
```

#### 4. Redis won't start
**Solution**: Ensure `.env-prod` has `REDIS_PASSWORD` set.

### Manual Rollback

If you need to rollback:

```bash
ssh AnkurshalaVM@74.225.207.72
cd /opt/ankurshala

# Check previous commits
git log --oneline -5

# Rollback to specific commit
git reset --hard <commit-sha>

# Rebuild and restart
./scripts/deploy.sh --build
```

## URLs

| Environment | URL |
|-------------|-----|
| Production | https://ankurshala.com |
| Admin Dashboard | https://ankurshala.com/admin/dashboard |
| API Health | https://ankurshala.com/api/actuator/health |
| Health Check | https://ankurshala.com/health |

## Support

For deployment issues:
1. Check GitHub Actions logs
2. SSH to server and check Docker logs
3. Review this documentation
4. Check the troubleshooting section
