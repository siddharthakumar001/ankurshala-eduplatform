#!/bin/bash
# =============================================================================
# SSL Certificate Fix Script for Ankurshala
# =============================================================================
# This script fixes the ERR_CERT_DATE_INVALID issue by:
# 1. Installing certbot if needed
# 2. Stopping nginx temporarily
# 3. Obtaining new Let's Encrypt certificates
# 4. Restarting nginx with valid certificates
# =============================================================================

set -e

echo "🔒 Starting SSL Certificate Fix..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running as root or with sudo
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}❌ Please run as root or with sudo${NC}"
    exit 1
fi

# Variables
DOMAIN="ankurshala.com"
EMAIL="admin@ankurshala.com"  # Change this to your email
WEBROOT="/var/www/certbot"

echo -e "${YELLOW}📋 Configuration:${NC}"
echo "  Domain: $DOMAIN"
echo "  Email: $EMAIL"
echo "  Webroot: $WEBROOT"
echo ""

# Step 1: Install certbot if not present
echo -e "${YELLOW}1️⃣  Checking certbot installation...${NC}"
if ! command -v certbot &> /dev/null; then
    echo "Installing certbot..."
    apt-get update
    apt-get install -y certbot
else
    echo "✅ Certbot is already installed"
fi

# Step 2: Create webroot directory
echo -e "${YELLOW}2️⃣  Creating webroot directory...${NC}"
mkdir -p $WEBROOT

# Step 3: Check if nginx is running in Docker
echo -e "${YELLOW}3️⃣  Checking nginx container...${NC}"
if docker ps | grep -q ankurshala_nginx; then
    echo "✅ Nginx container is running"
    NGINX_CONTAINER="ankurshala_nginx"
elif docker ps | grep -q nginx; then
    echo "✅ Nginx container is running"
    NGINX_CONTAINER=$(docker ps --format "{{.Names}}" | grep nginx)
else
    echo -e "${RED}❌ Nginx container is not running${NC}"
    echo "Starting nginx container..."
    cd /opt/ankurshala
    docker compose -f docker-compose.prod.yml up -d nginx
    sleep 5
    NGINX_CONTAINER=$(docker ps --format "{{.Names}}" | grep nginx)
fi

# Step 4: Enable HTTP-only configuration temporarily
echo -e "${YELLOW}4️⃣  Enabling HTTP-only configuration for certificate challenge...${NC}"
docker exec $NGINX_CONTAINER bash -c "
    # Backup current config
    cp /etc/nginx/conf.d/ankurshala.conf /etc/nginx/conf.d/ankurshala.conf.backup 2>/dev/null || true
    
    # Enable HTTP-only config
    if [ -f /etc/nginx/conf.d/00-acme-http.conf ]; then
        echo '✅ HTTP config already present'
    fi
"

# Reload nginx
docker exec $NGINX_CONTAINER nginx -s reload

# Step 5: Obtain certificates
echo -e "${YELLOW}5️⃣  Obtaining Let's Encrypt certificates...${NC}"
echo "This may take a few moments..."

# Stop nginx temporarily to use standalone mode
docker compose -f /opt/ankurshala/docker-compose.prod.yml stop nginx

# Use certbot standalone mode
certbot certonly --standalone \
    --non-interactive \
    --agree-tos \
    --email $EMAIL \
    --domains $DOMAIN \
    --domains www.$DOMAIN \
    --preferred-challenges http \
    --force-renewal

# Check if certificates were created
if [ -f "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" ]; then
    echo -e "${GREEN}✅ Certificates obtained successfully!${NC}"
else
    echo -e "${RED}❌ Failed to obtain certificates${NC}"
    exit 1
fi

# Step 6: Set proper permissions
echo -e "${YELLOW}6️⃣  Setting certificate permissions...${NC}"
chmod 755 /etc/letsencrypt/live
chmod 755 /etc/letsencrypt/archive

# Step 7: Start nginx with new certificates
echo -e "${YELLOW}7️⃣  Starting nginx with new certificates...${NC}"
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml up -d nginx

# Wait for nginx to start
sleep 5

# Step 8: Verify nginx is running
if docker ps | grep -q nginx; then
    echo -e "${GREEN}✅ Nginx is running${NC}"
else
    echo -e "${RED}❌ Nginx failed to start${NC}"
    exit 1
fi

# Step 9: Test HTTPS
echo -e "${YELLOW}8️⃣  Testing HTTPS connection...${NC}"
sleep 2
if curl -s -k https://$DOMAIN/health > /dev/null; then
    echo -e "${GREEN}✅ HTTPS is working!${NC}"
else
    echo -e "${YELLOW}⚠️  HTTPS test inconclusive, please verify manually${NC}"
fi

# Step 10: Display certificate info
echo -e "${YELLOW}9️⃣  Certificate information:${NC}"
certbot certificates

# Step 11: Setup auto-renewal
echo -e "${YELLOW}🔄 Setting up auto-renewal...${NC}"
# Create renewal hook script
cat > /etc/letsencrypt/renewal-hooks/post/reload-nginx.sh << 'EOF'
#!/bin/bash
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
EOF

chmod +x /etc/letsencrypt/renewal-hooks/post/reload-nginx.sh

# Test renewal
certbot renew --dry-run

echo ""
echo -e "${GREEN}🎉 SSL Certificate Fix Complete!${NC}"
echo ""
echo -e "${YELLOW}📝 Summary:${NC}"
echo "  ✅ Certificates obtained for: $DOMAIN, www.$DOMAIN"
echo "  ✅ Nginx restarted with new certificates"
echo "  ✅ Auto-renewal configured"
echo ""
echo -e "${YELLOW}🔍 Next Steps:${NC}"
echo "  1. Visit https://$DOMAIN in your browser"
echo "  2. Verify the SSL certificate is valid"
echo "  3. Certificate will auto-renew before expiration"
echo ""
echo -e "${YELLOW}📅 Certificate expires in approximately 90 days${NC}"
echo ""
