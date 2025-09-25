#!/bin/bash
# filepath: scripts/fix-nginx-config.sh

echo "🔧 Fixing Nginx configuration error..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if nginx.conf exists
if [ ! -f "nginx/nginx.conf" ]; then
    echo -e "${RED}❌ nginx/nginx.conf not found!${NC}"
    exit 1
fi

# Backup current config
echo -e "${YELLOW}📋 Backing up current nginx.conf...${NC}"
cp nginx/nginx.conf nginx/nginx.conf.backup.$(date +%Y%m%d_%H%M%S)

# Fix the gzip_proxied line
echo -e "${YELLOW}🔧 Fixing gzip_proxied directive...${NC}"
sed -i 's/gzip_proxied expired no-cache no-store private must-revalidate;/gzip_proxied expired no-cache no-store private auth;/g' nginx/nginx.conf

# Validate nginx config by building a test container
echo -e "${YELLOW}🔍 Validating Nginx configuration...${NC}"
docker run --rm -v "$(pwd)/nginx/nginx.conf:/etc/nginx/nginx.conf:ro" -v "$(pwd)/nginx/conf.d:/etc/nginx/conf.d:ro" nginx:alpine nginx -t

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Nginx configuration is valid!${NC}"
else
    echo -e "${RED}❌ Nginx configuration still has errors!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Nginx configuration fixed successfully!${NC}"