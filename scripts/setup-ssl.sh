#!/bin/bash
set -e

echo "🔒 Setting up SSL certificates for ankurshala.com"

# Create nginx directory if it doesn't exist
mkdir -p nginx/ssl

# Generate self-signed certificates for initial setup
if [ ! -f "nginx/ssl/ankurshala.crt" ]; then
    echo "📋 Generating temporary self-signed certificates..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/ankurshala.key \
        -out nginx/ssl/ankurshala.crt \
        -subj "/C=IN/ST=State/L=City/O=AnkurShala/CN=ankurshala.com"
    echo "✅ Temporary certificates generated"
fi

# Start services with self-signed certificates
echo "🚀 Starting services for Let's Encrypt verification..."
docker-compose -f docker-compose.prod.yml up -d nginx

# Wait for nginx to be ready
sleep 10

# Get Let's Encrypt certificates
echo "🔐 Obtaining Let's Encrypt certificates..."
docker-compose -f docker-compose.prod.yml run --rm certbot

# Restart nginx with real certificates
echo "🔄 Restarting nginx with real certificates..."
docker-compose -f docker-compose.prod.yml restart nginx

echo "✅ SSL setup completed!"
echo "🌐 Your site should now be available at https://ankurshala.com"