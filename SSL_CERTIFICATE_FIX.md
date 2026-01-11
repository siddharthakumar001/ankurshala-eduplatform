# 🔒 SSL Certificate Fix Guide

## Problem
You're seeing this error when accessing ankurshala.com:
```
ERR_CERT_DATE_INVALID
Your connection is not private
```

This means the SSL certificate is either expired or has invalid dates.

## Solution

### Option 1: Automated Fix (Recommended)

SSH into your VM and run the automated fix script:

```bash
# SSH into the VM
ssh AnkurshalaVM@74.225.207.72
# Password: AnkurshalaVM@2025!2025

# Navigate to project directory
cd /opt/ankurshala

# Pull latest code (includes the fix script)
git pull origin ankurshala/prod-1.0-final

# Make script executable
chmod +x quick-ssl-fix.sh

# Run the fix script
sudo ./quick-ssl-fix.sh
```

**Important:** Update the email in the script before running:
```bash
nano quick-ssl-fix.sh
# Change EMAIL="admin@ankurshala.com" to your actual email
```

### Option 2: Manual Fix

If the automated script doesn't work, follow these manual steps:

#### Step 1: SSH into the VM
```bash
ssh AnkurshalaVM@74.225.207.72
# Password: AnkurshalaVM@2025!2025
```

#### Step 2: Stop Docker Services
```bash
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml down
```

#### Step 3: Install Certbot (if not installed)
```bash
sudo apt-get update
sudo apt-get install -y certbot
```

#### Step 4: Obtain New Certificates
```bash
sudo certbot certonly --standalone \
    --non-interactive \
    --agree-tos \
    --email your-email@example.com \
    -d ankurshala.com \
    -d www.ankurshala.com \
    --force-renewal
```

Replace `your-email@example.com` with your actual email address.

#### Step 5: Verify Certificates Were Created
```bash
sudo ls -la /etc/letsencrypt/live/ankurshala.com/
```

You should see:
- `fullchain.pem`
- `privkey.pem`
- `cert.pem`
- `chain.pem`

#### Step 6: Set Proper Permissions
```bash
sudo chmod -R 755 /etc/letsencrypt/live
sudo chmod -R 755 /etc/letsencrypt/archive
```

#### Step 7: Start Docker Services
```bash
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml up -d
```

#### Step 8: Wait and Verify
```bash
# Wait for services to start
sleep 15

# Check nginx logs
docker logs ankurshala_nginx --tail 50

# Test HTTPS
curl -I https://ankurshala.com/health
```

### Option 3: Using Docker Certbot

If you prefer to use certbot in Docker:

```bash
cd /opt/ankurshala

# Stop services
docker compose -f docker-compose.prod.yml down

# Create directories
mkdir -p certbot/conf certbot/www

# Run certbot in Docker
docker run -it --rm \
    -p 80:80 \
    -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
    -v "$(pwd)/certbot/www:/var/www/certbot" \
    certbot/certbot certonly \
    --standalone \
    --email your-email@example.com \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    -d ankurshala.com -d www.ankurshala.com

# Copy certificates to system location
sudo mkdir -p /etc/letsencrypt/live/ankurshala.com
sudo cp -rL certbot/conf/live/ankurshala.com/* /etc/letsencrypt/live/ankurshala.com/

# Set permissions
sudo chmod -R 755 /etc/letsencrypt

# Start services
docker compose -f docker-compose.prod.yml up -d
```

## Verification

After running any of the above options, verify the fix:

1. **Check Certificate Expiration:**
```bash
sudo certbot certificates
```

2. **Check Online:**
Visit https://ankurshala.com in your browser. You should see a valid SSL certificate.

3. **Check Certificate Details:**
Click on the padlock icon in your browser to view certificate details.

## Auto-Renewal Setup

Let's Encrypt certificates expire every 90 days. Set up auto-renewal:

### Create Renewal Script
```bash
sudo nano /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

Add this content:
```bash
#!/bin/bash
cd /opt/ankurshala
docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

Make it executable:
```bash
sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

### Test Auto-Renewal
```bash
sudo certbot renew --dry-run
```

### Setup Cron Job
```bash
sudo crontab -e
```

Add this line:
```
0 0 * * 0 certbot renew --quiet --deploy-hook "cd /opt/ankurshala && docker compose -f docker-compose.prod.yml exec nginx nginx -s reload"
```

This will check for renewal every Sunday at midnight.

## Troubleshooting

### Issue: Port 80 is already in use
```bash
# Check what's using port 80
sudo lsof -i :80

# If it's nginx in Docker, stop it first
docker compose -f docker-compose.prod.yml down
```

### Issue: Certbot fails with "connection refused"
```bash
# Make sure firewall allows port 80
sudo ufw allow 80
sudo ufw allow 443

# Check if port 80 is accessible from outside
curl -I http://ankurshala.com
```

### Issue: Nginx fails to start after getting certificates
```bash
# Check nginx logs
docker logs ankurshala_nginx --tail 100

# Verify certificate files exist
sudo ls -la /etc/letsencrypt/live/ankurshala.com/

# Check nginx configuration
docker compose -f docker-compose.prod.yml exec nginx nginx -t
```

### Issue: Certificate shows wrong domain
```bash
# Check which domains the certificate covers
sudo certbot certificates

# If wrong, delete and recreate
sudo certbot delete --cert-name ankurshala.com
# Then run the certbot certonly command again
```

## Important Notes

1. **Email Address:** Always use a valid email address with certbot. Let's Encrypt will send expiration warnings to this email.

2. **Rate Limits:** Let's Encrypt has rate limits (50 certificates per domain per week). Don't run the renewal command too many times.

3. **DNS Propagation:** If you just set up the domain, wait for DNS to propagate (can take up to 48 hours).

4. **Firewall:** Make sure ports 80 and 443 are open in your firewall and cloud provider's security groups.

5. **Backup:** The automated renewal script creates backups, but it's good practice to backup certificates manually:
```bash
sudo tar -czf letsencrypt-backup-$(date +%Y%m%d).tar.gz /etc/letsencrypt/
```

## Prevention

To prevent this issue in the future:

1. ✅ Set up auto-renewal (see above)
2. ✅ Monitor certificate expiration
3. ✅ Use monitoring tools to alert before expiration
4. ✅ Test renewal monthly: `sudo certbot renew --dry-run`

## Support

If you continue to experience issues:

1. Check nginx error logs: `docker logs ankurshala_nginx`
2. Check certbot logs: `sudo cat /var/log/letsencrypt/letsencrypt.log`
3. Verify DNS: `nslookup ankurshala.com`
4. Test SSL: `openssl s_client -connect ankurshala.com:443`

---

**Last Updated:** January 11, 2026
