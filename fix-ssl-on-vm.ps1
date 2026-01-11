# ============================================================================
# SSL Certificate Fix Script for VM
# ============================================================================
# This script connects to the VM and fixes the SSL certificate issue
# ============================================================================

$VM_HOST = "74.225.207.72"
$VM_USER = "AnkurshalaVM"
$VM_PASS = "AnkurshalaVM@2025!2025"
$PROJECT_DIR = "/opt/ankurshala"
$EMAIL = "admin@ankurshala.com"  # Change this to your email

Write-Host "🔒 Starting SSL Certificate Fix on VM..." -ForegroundColor Cyan
Write-Host ""

# Function to execute SSH command
function Invoke-SSHCommand {
    param(
        [string]$Command
    )
    
    Write-Host "Executing: $Command" -ForegroundColor Yellow
    
    # Create a temporary script file
    $scriptContent = @"
#!/bin/bash
$Command
"@
    
    $tempScript = [System.IO.Path]::GetTempFileName()
    $scriptContent | Out-File -FilePath $tempScript -Encoding ASCII
    
    # Use plink if available, otherwise use ssh with password
    $sshCommand = "sshpass -p '$VM_PASS' ssh -o StrictHostKeyChecking=no ${VM_USER}@${VM_HOST} 'bash -s' < $tempScript"
    
    # For Windows without sshpass, we'll use different approach
    $result = ssh ${VM_USER}@${VM_HOST} "$Command"
    
    Remove-Item $tempScript -ErrorAction SilentlyContinue
    
    return $result
}

Write-Host "Step 1: Pulling latest code..." -ForegroundColor Green
$pullCmd = @"
cd $PROJECT_DIR
git pull origin ankurshala/prod-1.0-final
"@

Write-Host "Step 2: Checking Docker services..." -ForegroundColor Green
$dockerCheck = @"
cd $PROJECT_DIR
docker ps --format 'table {{.Names}}\t{{.Status}}'
"@

Write-Host "Step 3: Stopping services..." -ForegroundColor Green
$stopCmd = @"
cd $PROJECT_DIR
docker compose -f docker-compose.prod.yml down
"@

Write-Host "Step 4: Installing certbot..." -ForegroundColor Green
$certbotCmd = @"
sudo apt-get update -qq
sudo apt-get install -y certbot
"@

Write-Host "Step 5: Obtaining certificates..." -ForegroundColor Green
$getCertCmd = @"
sudo certbot certonly --standalone \
    --non-interactive \
    --agree-tos \
    --email $EMAIL \
    -d ankurshala.com \
    -d www.ankurshala.com \
    --force-renewal
"@

Write-Host "Step 6: Setting permissions..." -ForegroundColor Green
$permCmd = @"
sudo chmod -R 755 /etc/letsencrypt/live
sudo chmod -R 755 /etc/letsencrypt/archive
"@

Write-Host "Step 7: Starting services..." -ForegroundColor Green
$startCmd = @"
cd $PROJECT_DIR
docker compose -f docker-compose.prod.yml up -d
"@

Write-Host "Step 8: Verifying..." -ForegroundColor Green
$verifyCmd = @"
sleep 15
docker ps
sudo certbot certificates
curl -k -I https://ankurshala.com/health
"@

Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "Manual Steps Required:" -ForegroundColor Yellow
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Please run these commands on the VM:" -ForegroundColor White
Write-Host ""
Write-Host "1. SSH into the VM:" -ForegroundColor Yellow
Write-Host "   ssh AnkurshalaVM@74.225.207.72" -ForegroundColor White
Write-Host ""
Write-Host "2. Pull latest code:" -ForegroundColor Yellow
Write-Host "   cd /opt/ankurshala" -ForegroundColor White
Write-Host "   git pull origin ankurshala/prod-1.0-final" -ForegroundColor White
Write-Host ""
Write-Host "3. Update email in script:" -ForegroundColor Yellow
Write-Host "   nano quick-ssl-fix.sh" -ForegroundColor White
Write-Host "   # Change EMAIL=`"admin@ankurshala.com`" to your email" -ForegroundColor White
Write-Host "   # Press Ctrl+X, Y, Enter to save" -ForegroundColor White
Write-Host ""
Write-Host "4. Make script executable:" -ForegroundColor Yellow
Write-Host "   chmod +x quick-ssl-fix.sh" -ForegroundColor White
Write-Host ""
Write-Host "5. Run the fix script:" -ForegroundColor Yellow
Write-Host "   sudo ./quick-ssl-fix.sh" -ForegroundColor White
Write-Host ""
Write-Host "6. Wait 30 seconds and verify:" -ForegroundColor Yellow
Write-Host "   Visit: https://ankurshala.com" -ForegroundColor White
Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# Alternative: Create a single command to copy-paste
Write-Host "OR copy-paste this single command:" -ForegroundColor Green
Write-Host ""
$oneLineCommand = @"
cd /opt/ankurshala && git pull origin ankurshala/prod-1.0-final && sudo docker compose -f docker-compose.prod.yml down && sudo certbot certonly --standalone --non-interactive --agree-tos --email admin@ankurshala.com -d ankurshala.com -d www.ankurshala.com --force-renewal && sudo chmod -R 755 /etc/letsencrypt && cd /opt/ankurshala && sudo docker compose -f docker-compose.prod.yml up -d && sleep 15 && docker ps && sudo certbot certificates
"@
Write-Host $oneLineCommand -ForegroundColor Cyan
Write-Host ""
