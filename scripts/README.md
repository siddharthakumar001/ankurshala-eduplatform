# Utility Scripts

This folder contains utility scripts for testing and deployment.

## Available Scripts

### 1. start-docker.sh
Start all Docker containers for local development.
```bash
./scripts/start-docker.sh
```

### 2. test-admin-students.sh  
Test admin students API endpoints.
```bash
./scripts/test-admin-students.sh
```

### 3. test-patch-direct.sh
Test the PATCH toggle-status endpoint (fixed 405 error).
```bash
./scripts/test-patch-direct.sh
```

### 4. test-students-all-features.sh
Comprehensive test of all student page features (toggle, search, filters).
```bash
./scripts/test-students-all-features.sh
```

### 5. test-students-browser.sh
Interactive browser testing guide for admin students page.
```bash
./scripts/test-students-browser.sh
```

### 6. verify-deployment.sh
Verify production deployment health and endpoints.
```bash
./scripts/verify-deployment.sh
```

---

## Prerequisites

All scripts require:
- Docker containers running (`docker-compose up -d`)
- Valid admin credentials (stored in cookies.txt or provided)

---

## Usage

Make scripts executable (if needed):
```bash
chmod +x scripts/*.sh
```

Run any script:
```bash
./scripts/<script-name>.sh
```

---

**See Also:** [TESTING_GUIDE.md](../docs/TESTING_GUIDE.md) for detailed testing procedures.
