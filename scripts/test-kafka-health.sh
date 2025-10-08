#!/bin/bash
# Kafka KRaft Health Check Script - Tests multiple health check methods

echo "🔍 Testing Kafka KRaft Health Check Methods"
echo "==========================================="

# Method 1: Simple port check
echo "Method 1: Port 9092 check"
if timeout 5 bash -c 'exec 3<>/dev/tcp/127.0.0.1/9092' 2>/dev/null; then
    echo "✅ Port 9092 is open"
else
    echo "❌ Port 9092 is not accessible"
fi

# Method 2: Netcat check
echo "Method 2: Netcat check"
if timeout 5 nc -z 127.0.0.1 9092 2>/dev/null; then
    echo "✅ Netcat can connect to port 9092"
else
    echo "❌ Netcat cannot connect to port 9092"
fi

# Method 3: Kafka broker API versions (if available)
echo "Method 3: Kafka broker API versions"
if timeout 10 kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; then
    echo "✅ Kafka broker API versions check passed"
else
    echo "❌ Kafka broker API versions check failed"
fi

# Method 4: Kafka topics list (if available)
echo "Method 4: Kafka topics list"
if timeout 10 kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
    echo "✅ Kafka topics list check passed"
else
    echo "❌ Kafka topics list check failed"
fi

# Method 5: Check if Kafka process is running
echo "Method 5: Kafka process check"
if docker exec ankurshala_kafka_prod ps aux | grep -q kafka; then
    echo "✅ Kafka process is running"
else
    echo "❌ Kafka process not found"
fi

# Method 6: Check Kafka KRaft logs for errors
echo "Method 6: Recent Kafka KRaft logs (last 10 lines)"
docker logs --tail 10 ankurshala_kafka_prod 2>&1 | head -10

# Method 7: Check for KRaft-specific errors
echo "Method 7: KRaft cluster status check"
if docker logs ankurshala_kafka_prod 2>&1 | grep -q "KRaft"; then
    echo "✅ Kafka is running in KRaft mode"
else
    echo "⚠️ KRaft mode status unclear"
fi

echo ""
echo "🎯 Recommended Health Check Method for KRaft:"
echo "Use Method 1 (port check) as it's most reliable:"
echo 'test: ["CMD-SHELL", "timeout 5 bash -c \"exec 3<>/dev/tcp/127.0.0.1/9092\" || exit 1"]'
echo ""
echo "🚀 KRaft Mode Benefits:"
echo "• No Zookeeper dependency"
echo "• Simplified cluster management"
echo "• Better performance and reliability"
echo "• No cluster ID mismatch issues"
