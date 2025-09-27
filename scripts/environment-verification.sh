#!/bin/bash

echo "🔍 Verifying .env.prod Configuration"
echo "===================================="

if [ ! -f ".env.prod" ]; then
    echo "❌ .env.prod file not found!"
    exit 1
fi

# Load environment variables
source .env.prod

echo "✅ Environment Variables Check:"
echo "------------------------------"

# Critical production checks
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    echo "✅ Spring Profile: Production"
else
    echo "⚠️  Spring Profile: $SPRING_PROFILES_ACTIVE (should be 'prod')"
fi

if [ ${#JWT_SECRET} -ge 32 ]; then
    echo "✅ JWT Secret: Properly configured (${#JWT_SECRET} characters)"
else
    echo "❌ JWT Secret: Too short (${#JWT_SECRET} characters, need 32+)"
fi

if [ ${#BANK_ENC_KEY} -ge 32 ]; then
    echo "✅ Bank Encryption Key: Properly configured"
else
    echo "❌ Bank Encryption Key: Too short"
fi

if [[ "$NEXT_PUBLIC_API_URL" == "https://ankurshala.com/api" ]]; then
    echo "✅ Frontend API URL: Production HTTPS"
else
    echo "⚠️  Frontend API URL: $NEXT_PUBLIC_API_URL"
fi

# Security checks
if [ "$DEMO_SEED_ON_START" =# filepath: /Users/siddhartha/Documents/ankurshala-eduplatform/scripts/verify-env.sh
#!/bin/bash

echo "🔍 Verifying .env.prod Configuration"
echo "===================================="

if [ ! -f ".env.prod" ]; then
    echo "❌ .env.prod file not found!"
    exit 1
fi

# Load environment variables
source .env.prod

echo "✅ Environment Variables Check:"
echo "------------------------------"

# Critical production checks
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    echo "✅ Spring Profile: Production"
else
    echo "⚠️  Spring Profile: $SPRING_PROFILES_ACTIVE (should be 'prod')"
fi

if [ ${#JWT_SECRET} -ge 32 ]; then
    echo "✅ JWT Secret: Properly configured (${#JWT_SECRET} characters)"
else
    echo "❌ JWT Secret: Too short (${#JWT_SECRET} characters, need 32+)"
fi

if [ ${#BANK_ENC_KEY} -ge 32 ]; then
    echo "✅ Bank Encryption Key: Properly configured"
else
    echo "❌ Bank Encryption Key: Too short"
fi

if [[ "$NEXT_PUBLIC_API_URL" == "https://ankurshala.com/api" ]]; then
    echo "✅ Frontend API URL: Production HTTPS"
else
    echo "⚠️  Frontend API URL: $NEXT_PUBLIC_API_URL"
fi

# Security checks
if