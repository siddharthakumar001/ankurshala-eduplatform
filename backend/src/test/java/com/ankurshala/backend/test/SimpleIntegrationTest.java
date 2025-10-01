package com.ankurshala.backend.test;

import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration test to verify basic Spring Boot setup works
 */
@SpringBootTest
@ActiveProfiles("test")
public class SimpleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testBasicSpringBootSetup() {
        // Test that Spring Boot context loads successfully
        assertNotNull(userRepository);
    }

    @Test
    public void testUserRepositoryBasicOperation() {
        // Test basic repository operation
        long initialCount = userRepository.count();
        assertTrue(initialCount >= 0);
    }

    @Test
    public void testCreateAndFindUser() {
        // Test creating and finding a user with unique email
        String uniqueEmail = "test-" + System.currentTimeMillis() + "@example.com";
        User user = new User();
        user.setName("Test User");
        user.setEmail(uniqueEmail);
        user.setPassword("password");
        user.setRole(com.ankurshala.backend.entity.Role.STUDENT);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals(uniqueEmail, savedUser.getEmail());

        // Find the user
        var foundUser = userRepository.findByEmail(uniqueEmail);
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
    }
}
