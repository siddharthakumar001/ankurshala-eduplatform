#!/bin/bash

echo "🚀 Complete VM Deployment Script for AnkurShala"
echo "==============================================="

# Update system
echo "📦 Updating system packages..."
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker
echo "🐳 Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
rm get-docker.sh

# Install Docker Compose
echo "📦 Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install additional tools
echo "🛠️ Installing additional tools..."
sudo apt-get install -y curl wget git make nano htop

# Set up firewall (optional but recommended)
echo "🔥 Configuring firewall..."
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

echo "✅ VM setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Clone your repository"
echo "2. Configure .env.prod with your settings"
echo "3. Run the deployment script"
echo ""
echo "Example commands:"
echo "git clone https://github.com/yourusername/ankurshala-eduplatform.git"
echo "cd ankurshala-eduplatform"
echo "# Configure .env.prod file"
echo "./scripts/deploy-production-fixed.sh"