# =============================================================================
# Setup SSH Key Authentication for Ankurshala VM
# Run this once to enable password-less SSH deployments
# =============================================================================

$VM_USER = "AnkurshalaVM"
$VM_IP = "74.225.207.72"
$SSH_KEY_PATH = "$env:USERPROFILE\.ssh\id_rsa"

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   SSH KEY SETUP FOR ANKURSHALA VM" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Check if SSH key exists
if (Test-Path $SSH_KEY_PATH) {
    Write-Host "✅ SSH key already exists at: $SSH_KEY_PATH" -ForegroundColor Green
} else {
    Write-Host "🔑 Generating new SSH key..." -ForegroundColor Yellow
    ssh-keygen -t rsa -b 4096 -f $SSH_KEY_PATH -N '""'
    Write-Host "✅ SSH key generated" -ForegroundColor Green
}

Write-Host ""
Write-Host "📤 Copying public key to VM..." -ForegroundColor Yellow
Write-Host "   You'll be prompted for the VM password: AnkurshalaVM@2025!2025" -ForegroundColor Gray
Write-Host ""

# Copy the public key to VM
$pubKey = Get-Content "$SSH_KEY_PATH.pub"
$sshCmd = "mkdir -p ~/.ssh && echo '$pubKey' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && chmod 700 ~/.ssh"

ssh "$VM_USER@$VM_IP" $sshCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ SSH key copied successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Testing password-less connection..." -ForegroundColor Yellow
    ssh "$VM_USER@$VM_IP" "echo '✅ SSH connection successful - no password needed!'"
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
    Write-Host "   You can now deploy without entering a password!" -ForegroundColor Green
    Write-Host "   Just run: .\deploy-simple.ps1" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  SSH key copy may have failed. Try manually:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Copy this public key:" -ForegroundColor Gray
    Write-Host $pubKey -ForegroundColor White
    Write-Host ""
    Write-Host "2. SSH to VM and add it to ~/.ssh/authorized_keys" -ForegroundColor Gray
}

Write-Host ""

