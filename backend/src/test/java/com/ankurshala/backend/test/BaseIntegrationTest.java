package com.ankurshala.backend.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base test configuration for integration tests using Testcontainers.
 * Provides PostgreSQL, Redis, and Kafka containers for testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
                    .asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("ankurshala_test")
            .withUsername("ankur")
            .withPassword("password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure Kafka properties
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "ankurshala-test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        // Configure Redis properties (using container for tests)
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        
        // Configure JWT secret for tests
        registry.add("app.jwt.secret", () -> "test-jwt-secret-key-for-testing-purposes-only-must-be-at-least-64-characters-long");
        
        // Configure bank encryption key for tests
        registry.add("app.encryption.bank-key", () -> "test-bank-encryption-key-32-chars");
        
        // Disable Flyway baseline for tests
        registry.add("spring.flyway.baseline-on-migrate", () -> false);
    }
}
