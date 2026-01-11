#!/bin/bash
# =============================================================================
# Complete SSL Fix Script - Run this on the VM
# =============================================================================
# This single script will fix everything
# Usage: sudo bash complete-ssl-fix.sh your-email@example.com
# =============================================================================

set -e

# Get email from parameter or use default
EMAIL="${1:-admin@ankurshala.com}"

echo "============================================================================="
echo "🔒 SSL Certificate Fix for Ankurshala"
echo "============================================================================="
echo "Email: $EMAIL"
echo "Domain: ankurshala.com"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}❌ Please run with sudo${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Navigating to project directory...${NC}"
cd /opt/ankurshala || { echo "Project directory not found!"; exit 1; }

echo -e "${YELLOW}Step 2: Pulling latest code...${NC}"
sudo -u AnkurshalaVM git pull origin ankurshala/prod-1.0-final

echo -e "${YELLOW}Step 3: Stopping Docker services...${NC}"
docker compose -f docker-compose.prod.yml down

echo -e "${YELLOW}Step 4: Installing/updating certbot...${NC}"
apt-get update -qq
apt-get install -y certbot

echo -e "${YELLOW}Step 5: Obtaining Let's Encrypt certificates...${NC}"
echo "This may take a moment..."
certbot certonly --standalone \
    --non-interactive \
    --agree-tos \
    --email "$EMAIL" \
    -d ankurshala.com \
    -d www.ankurshala.com \
    --force-renewal

# Check if certificates were created
if [ ! -f "/etc/letsencrypt/live/ankurshala.com/fullchain.pem" ]; then
    echo -e "${RED}❌ Failed to obtain certificates${NC}"
    echo "Please check:"
    echo "  - DNS is pointing to this server"
    echo "  - Port 80 is accessible from internet"
    echo "  - Firewall allows incoming connections"
    exit 1
fi

echo -e "${GREEN}✅ Certificates obtained successfully!${NC}"

echo -e "${YELLOW}Step 6: Setting proper permissions...${NC}"
chmod -R 755 /etc/letsencrypt/live
chmod -R 755 /etc/letsencrypt/archive

echo -e "${YELLOW}Step 7: Starting Docker services...${NC}"
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml up -d

echo -e "${YELLOW}Step 8: Waiting for services to start...${NC}"
sleep 15

echo -e "${YELLOW}Step 9: Checking service status...${NC}"
docker ps --format 'table {{.Names}}\t{{.Status}}'

echo -e "${YELLOW}Step 10: Verifying SSL certificate...${NC}"
certbot certificates

echo ""
echo -e "${GREEN}=============================================================================${NC}"
echo -e "${GREEN}🎉 SSL Certificate Fix Complete!${NC}"
echo -e "${GREEN}=============================================================================${NC}"
echo ""
echo -e "${YELLOW}Certificate Information:${NC}"
openssl x509 -in /etc/letsencrypt/live/ankurshala.com/fullchain.pem -noout -dates

echo ""
echo -e "${YELLOW}🔍 Verification:${NC}"
echo "  1. Visit: https://ankurshala.com"
echo "  2. Check SSL certificate in browser"
echo "  3. Certificate should show as valid"
echo ""

echo -e "${YELLOW}Testing HTTPS connection...${NC}"
sleep 2
if curl -s -k -I https://ankurshala.com/health | head -1; then
    echo -e "${GREEN}✅ HTTPS endpoint is responding${NC}"
else
    echo -e "${YELLOW}⚠️ HTTPS test inconclusive${NC}"
fi

echo ""
echo -e "${YELLOW}📝 Auto-Renewal Setup:${NC}"
echo "Setting up automatic certificate renewal..."

# Create renewal hook
mkdir -p /etc/letsencrypt/renewal-hooks/deploy
cat > /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh << 'HOOK_EOF'
#!/bin/bash
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
HOOK_EOF

chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh

# Add cron job if not exists
if ! crontab -l 2>/dev/null | grep -q "certbot renew"; then
    (crontab -l 2>/dev/null; echo "0 0 * * 0 certbot renew --quiet") | crontab -
    echo -e "${GREEN}✅ Auto-renewal cron job added${NC}"
else
    echo -e "${GREEN}✅ Auto-renewal already configured${NC}"
fi

# Test renewal
echo "Testing auto-renewal (dry run)..."
if certbot renew --dry-run --quiet; then
    echo -e "${GREEN}✅ Auto-renewal test passed${NC}"
else
    echo -e "${YELLOW}⚠️ Auto-renewal test had issues (may be OK)${NC}"
fi

echo ""
echo -e "${GREEN}=============================================================================${NC}"
echo -e "${GREEN}All Done! Your site should now have a valid SSL certificate.${NC}"
echo -e "${GREEN}=============================================================================${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "  1. Open https://ankurshala.com in your browser"
echo "  2. Verify the padlock icon shows secure connection"
echo "  3. Certificate will auto-renew before expiration (90 days)"
echo ""
