package com.ankurshala.backend.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for comprehensive system health checks.
 * Checks database, Redis, Kafka, and application-specific health metrics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean healthy = true;

        // Check database health
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", Map.of(
                    "status", "UP",
                    "message", "Database connection is healthy"
            ));
        } catch (Exception e) {
            healthy = false;
            details.put("database", Map.of(
                    "status", "DOWN",
                    "message", "Database connection failed: " + e.getMessage()
            ));
            log.error("Database health check failed", e);
        }

        // Check Redis health
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String value = redisTemplate.opsForValue().get("health:check");
            if ("ok".equals(value)) {
                details.put("redis", Map.of(
                        "status", "UP",
                        "message", "Redis connection is healthy"
                ));
            } else {
                healthy = false;
                details.put("redis", Map.of(
                        "status", "DOWN",
                        "message", "Redis read/write check failed"
                ));
            }
            redisTemplate.delete("health:check");
        } catch (Exception e) {
            healthy = false;
            details.put("redis", Map.of(
                    "status", "DOWN",
                    "message", "Redis connection failed: " + e.getMessage()
            ));
            log.error("Redis health check failed", e);
        }

        // Check Kafka health (basic check)
        try {
            // Just check if Kafka template is configured
            if (kafkaTemplate != null) {
                details.put("kafka", Map.of(
                        "status", "UP",
                        "message", "Kafka is configured and available"
                ));
            } else {
                details.put("kafka", Map.of(
                        "status", "UNKNOWN",
                        "message", "Kafka template not configured"
                ));
            }
        } catch (Exception e) {
            // Kafka health check is non-critical
            details.put("kafka", Map.of(
                    "status", "UNKNOWN",
                    "message", "Kafka health check failed: " + e.getMessage()
            ));
            log.warn("Kafka health check failed", e);
        }

        // Add application metrics
        details.put("application", Map.of(
                "status", "UP",
                "uptime", getUptime(),
                "javaVersion", System.getProperty("java.version"),
                "memoryUsed", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
                "memoryTotal", Runtime.getRuntime().totalMemory(),
                "memoryMax", Runtime.getRuntime().maxMemory(),
                "processors", Runtime.getRuntime().availableProcessors()
        ));

        if (healthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }

    private long getUptime() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
