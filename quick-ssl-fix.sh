#!/bin/bash
# =============================================================================
# Quick SSL Certificate Fix for Docker Setup
# =============================================================================
# This script uses certbot in Docker to obtain certificates
# =============================================================================

set -e

echo "🔒 Quick SSL Certificate Fix"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Variables
DOMAIN="ankurshala.com"
EMAIL="admin@ankurshala.com"  # Update this email

cd /opt/ankurshala

echo -e "${YELLOW}Step 1: Stop all services...${NC}"
docker compose -f docker-compose.prod.yml down

echo -e "${YELLOW}Step 2: Create directories...${NC}"
mkdir -p certbot/conf certbot/www

echo -e "${YELLOW}Step 3: Obtain certificates using certbot standalone...${NC}"
docker run -it --rm \
    -p 80:80 \
    -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
    -v "$(pwd)/certbot/www:/var/www/certbot" \
    certbot/certbot certonly \
    --standalone \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    -d $DOMAIN -d www.$DOMAIN

echo -e "${YELLOW}Step 4: Copy certificates to system location...${NC}"
sudo mkdir -p /etc/letsencrypt/live/$DOMAIN
sudo mkdir -p /etc/letsencrypt/archive/$DOMAIN
sudo cp -L certbot/conf/live/$DOMAIN/* /etc/letsencrypt/live/$DOMAIN/
sudo cp -L certbot/conf/archive/$DOMAIN/* /etc/letsencrypt/archive/$DOMAIN/

echo -e "${YELLOW}Step 5: Set permissions...${NC}"
sudo chmod -R 755 /etc/letsencrypt

echo -e "${YELLOW}Step 6: Start services...${NC}"
docker compose -f docker-compose.prod.yml up -d

echo -e "${YELLOW}Step 7: Wait for services...${NC}"
sleep 10

echo -e "${GREEN}✅ Done! Certificate should be valid now.${NC}"
echo ""
echo "Visit https://$DOMAIN to verify"
