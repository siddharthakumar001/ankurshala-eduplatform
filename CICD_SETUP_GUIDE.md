# 🚀 AnkurShala CI/CD Pipeline Setup Guide

## Overview
This guide will help you set up a comprehensive CI/CD pipeline using Azure Storage for Docker images and automated deployment to your Azure VM.

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   GitHub Repo   │───▶│  GitHub Actions  │───▶│  Azure Storage  │
│                 │    │   (CI/CD)        │    │  (Docker Images)│
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Azure VM       │◀───│  Production     │
                       │  (74.225.207.72) │    │  Deployment     │
                       └──────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

### Azure Resources
- **Storage Account**: `ankurimage`
- **VM**: `AnkurshalaVM@74.225.207.72`
- **VM Password**: `AnkurshalaVM@2025!2025`

### Required Tools
- Azure CLI
- Docker
- Git
- SSH access to VM

## 🔧 Setup Steps

### 1. Configure Environment Variables

Create your environment file:
```bash
cp .env-azure.example .env-azure
```

Edit `.env-azure` with your actual values:
```bash
# Azure Storage Account
STORAGE_ACCOUNT=ankurimage
STORAGE_KEY=YOUR_AZURE_STORAGE_KEY_HERE
CONTAINER_NAME=docker-images

# VM Configuration
VM_USER=AnkurshalaVM
VM_IP=74.225.207.72
VM_PASSWORD=YOUR_VM_PASSWORD_HERE
```

**⚠️ Security Note**: Never commit `.env-azure` to version control!

### 2. Set Up Azure Storage

```bash
# Source your environment variables
source .env-azure

# Set up Azure Storage container
./scripts/setup-azure-storage.sh setup-storage
```

### 3. Test Azure Storage

```bash
# Test connectivity
./scripts/setup-azure-storage.sh test-storage
```

### 4. Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these secrets:
- `STORAGE_KEY`: Your Azure Storage account key
- `VM_PASSWORD`: Your VM password
- `SSH_PRIVATE_KEY`: SSH private key for VM access
- `AZURE_CREDENTIALS`: Azure service principal credentials
- `SLACK_WEBHOOK`: (Optional) Slack webhook for notifications

### 5. Test Deployment

```bash
# Deploy to VM
./scripts/deploy-to-vm.sh deploy
```

## 🔄 CI/CD Workflow

### Automatic Pipeline
1. **Push to `ankurshala/prod-1.0-final`** triggers the pipeline
2. **Tests run** (Backend + Frontend)
3. **Docker images built** and pushed to Azure Storage
4. **Deployment** to production VM
5. **Health checks** verify deployment
6. **Notifications** sent to Slack (if configured)

### Manual Deployment
```bash
# Check VM status
./scripts/deploy-to-vm.sh status

# Deploy application
./scripts/deploy-to-vm.sh deploy

# Check application health
./scripts/deploy-to-vm.sh health

# Clean up resources
./scripts/deploy-to-vm.sh cleanup
```

## 🛠️ Scripts Overview

### `scripts/setup-azure-storage.sh`
- Sets up Azure Storage container
- Tests connectivity
- Creates deployment scripts

### `scripts/deploy-to-vm.sh`
- Deploys to production VM
- Manages resources
- Health monitoring

### `.github/workflows/ci-cd-azure-storage.yml`
- GitHub Actions workflow
- Automated testing
- Image building and storage
- Production deployment

## 🔍 Monitoring and Troubleshooting

### Check VM Status
```bash
./scripts/deploy-to-vm.sh status
```

### Check Application Health
```bash
./scripts/deploy-to-vm.sh health
```

### View Logs
```bash
# SSH into VM
ssh AnkurshalaVM@74.225.207.72

# Check Docker logs
docker logs ankurshala-frontend-1
docker logs ankurshala-backend-1
```

### Resource Management
```bash
# Clean up resources
./scripts/deploy-to-vm.sh cleanup
```

## 🚨 Troubleshooting

### Common Issues

1. **Azure Storage Access Denied**
   - Verify storage account key
   - Check container permissions

2. **VM Connection Failed**
   - Verify VM IP and credentials
   - Check network connectivity

3. **Docker Build Failed**
   - Check disk space
   - Run cleanup script

4. **Deployment Health Check Failed**
   - Check service logs
   - Verify environment variables

### Resource Issues
```bash
# Check disk space
df -h

# Check memory
free -h

# Clean Docker resources
docker system prune -a
```

## 📊 Benefits

### ✅ Advantages
- **Automated Testing**: Every push runs tests
- **Consistent Deployments**: Standardized process
- **Resource Management**: Automatic cleanup
- **Health Monitoring**: Built-in checks
- **Security**: No secrets in code
- **Scalability**: Easy to extend

### 🔄 Workflow Benefits
- **Fast Feedback**: Immediate test results
- **Reliable Deployments**: Automated process
- **Easy Rollbacks**: Version tracking
- **Monitoring**: Health checks
- **Notifications**: Status updates

## 🎯 Next Steps

1. **Set up GitHub Secrets** (Required)
2. **Configure Azure Storage** (Required)
3. **Test deployment** (Recommended)
4. **Set up monitoring** (Optional)
5. **Configure notifications** (Optional)

## 📞 Support

If you encounter issues:
1. Check the troubleshooting section
2. Review logs and error messages
3. Verify environment variables
4. Test connectivity manually

---

**🎉 Congratulations!** You now have a modern, automated CI/CD pipeline for AnkurShala!