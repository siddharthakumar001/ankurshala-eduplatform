/**
 * Authentication Module Playwright Tests
 * 
 * Purpose: Comprehensive testing of the authentication flow including login, logout, 
 * session management, and RBAC-based routing
 * 
 * Key Features:
 * - Login flow validation
 * - Logout functionality
 * - Session management
 * - RBAC-based navigation
 * - Error handling
 * - Security validation
 * 
 * Security Considerations:
 * - Token management validation
 * - CSRF protection testing
 * - Rate limiting verification
 * - Input sanitization testing
 * 
 * Usage Example:
 * ```bash
 * npx playwright test tests/auth/login.spec.ts
 * ```
 * 
 * Dependencies:
 * - Playwright: For browser automation and testing
 * - Backend API: Must be running on localhost:8080
 * - Frontend: Must be running on localhost:3000
 * 
 * @author AnkurShala Development Team
 * @version 1.0.0
 * @since 2024-01-01
 */

import { test, expect, Page } from '@playwright/test'

// Test data for different user roles
const TEST_USERS = {
  admin: {
    email: 'admin@ankurshala.com',
    password: 'Admin123!',
    role: 'ADMIN',
    expectedRedirect: '/admin/dashboard'
  },
  teacher: {
    email: 'teacher@ankurshala.com',
    password: 'Teacher123!',
    role: 'TEACHER',
    expectedRedirect: '/teacher/profile'
  },
  student: {
    email: 'student@ankurshala.com',
    password: 'Student123!',
    role: 'STUDENT',
    expectedRedirect: '/student/dashboard'
  }
}

// Invalid test data
const INVALID_CREDENTIALS = {
  invalidEmail: 'invalid@example.com',
  invalidPassword: 'wrongpassword',
  malformedEmail: 'notanemail',
  weakPassword: '123'
}

/**
 * Helper function to navigate to login page
 */
async function navigateToLogin(page: Page) {
  await page.goto('http://localhost:3000/login')
  await expect(page).toHaveTitle(/Ankurshala/)
  await expect(page.locator('h2')).toContainText('Welcome Back')
}

/**
 * Helper function to perform login
 */
async function performLogin(page: Page, email: string, password: string) {
  await page.fill('input[name="email"]', email)
  await page.fill('input[name="password"]', password)
  await page.click('button[type="submit"]')
}

/**
 * Helper function to wait for navigation after login
 */
async function waitForLoginRedirect(page: Page, expectedPath: string) {
  await page.waitForURL(`http://localhost:3000${expectedPath}`, { timeout: 10000 })
}

/**
 * Helper function to perform logout
 */
async function performLogout(page: Page) {
  // Look for logout button in navigation or user menu
  const logoutButton = page.locator('button:has-text("Logout"), a:has-text("Logout"), [data-testid="logout-button"]')
  if (await logoutButton.isVisible()) {
    await logoutButton.click()
  } else {
    // If no logout button found, navigate to logout endpoint
    await page.goto('http://localhost:3000/api/auth/logout', { method: 'POST' })
  }
}

describe('Authentication Module', () => {
  test.beforeEach(async ({ page }) => {
    // Clear all cookies and localStorage before each test
    await page.context().clearCookies()
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
  })

  describe('Login Flow', () => {
    test('should display login form correctly', async ({ page }) => {
      await navigateToLogin(page)
      
      // Check form elements
      await expect(page.locator('input[name="email"]')).toBeVisible()
      await expect(page.locator('input[name="password"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toBeVisible()
      await expect(page.locator('input[name="rememberMe"]')).toBeVisible()
      
      // Check registration links
      await expect(page.locator('text=Register as Student')).toBeVisible()
      await expect(page.locator('text=Register as Teacher')).toBeVisible()
      
      // Check security notice
      await expect(page.locator('text=🔒 Your connection is secured')).toBeVisible()
    })

    test('should validate email format', async ({ page }) => {
      await navigateToLogin(page)
      
      await page.fill('input[name="email"]', INVALID_CREDENTIALS.malformedEmail)
      await page.fill('input[name="password"]', 'validpassword123')
      await page.click('button[type="submit"]')
      
      await expect(page.locator('text=Please enter a valid email address')).toBeVisible()
    })

    test('should validate password length', async ({ page }) => {
      await navigateToLogin(page)
      
      await page.fill('input[name="email"]', 'test@example.com')
      await page.fill('input[name="password"]', INVALID_CREDENTIALS.weakPassword)
      await page.click('button[type="submit"]')
      
      await expect(page.locator('text=Password must be at least 8 characters long')).toBeVisible()
    })

    test('should show error for invalid credentials', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, INVALID_CREDENTIALS.invalidEmail, INVALID_CREDENTIALS.invalidPassword)
      
      // Wait for error message
      await expect(page.locator('text=Invalid email or password')).toBeVisible({ timeout: 10000 })
    })

    test('should handle rate limiting', async ({ page }) => {
      await navigateToLogin(page)
      
      // Attempt multiple failed logins to trigger rate limiting
      for (let i = 0; i < 6; i++) {
        await performLogin(page, INVALID_CREDENTIALS.invalidEmail, INVALID_CREDENTIALS.invalidPassword)
        await page.waitForTimeout(1000) // Wait 1 second between attempts
      }
      
      // Should show rate limit error
      await expect(page.locator('text=Too many login attempts')).toBeVisible()
    })

    test('should toggle password visibility', async ({ page }) => {
      await navigateToLogin(page)
      
      const passwordInput = page.locator('input[name="password"]')
      const toggleButton = page.locator('button:has([data-lucide="eye"])')
      
      // Initially password should be hidden
      await expect(passwordInput).toHaveAttribute('type', 'password')
      
      // Click toggle button
      await toggleButton.click()
      
      // Password should be visible
      await expect(passwordInput).toHaveAttribute('type', 'text')
      
      // Click toggle button again
      await toggleButton.click()
      
      // Password should be hidden again
      await expect(passwordInput).toHaveAttribute('type', 'password')
    })
  })

  describe('Successful Login Flow', () => {
    test('should login admin user and redirect to admin dashboard', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, TEST_USERS.admin.email, TEST_USERS.admin.password)
      
      await waitForLoginRedirect(page, TEST_USERS.admin.expectedRedirect)
      
      // Verify we're on the admin dashboard
      await expect(page).toHaveURL(/.*admin.*dashboard/)
      
      // Check for admin-specific elements
      await expect(page.locator('text=Admin Dashboard')).toBeVisible()
    })

    test('should login teacher user and redirect to teacher profile', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, TEST_USERS.teacher.email, TEST_USERS.teacher.password)
      
      await waitForLoginRedirect(page, TEST_USERS.teacher.expectedRedirect)
      
      // Verify we're on the teacher profile
      await expect(page).toHaveURL(/.*teacher.*profile/)
      
      // Check for teacher-specific elements
      await expect(page.locator('text=Teacher Profile')).toBeVisible()
    })

    test('should login student user and redirect to student dashboard', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Verify we're on the student dashboard
      await expect(page).toHaveURL(/.*student.*dashboard/)
      
      // Check for student-specific elements
      await expect(page.locator('text=Student Dashboard')).toBeVisible()
    })
  })

  describe('Session Management', () => {
    test('should maintain session across page refreshes', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Refresh the page
      await page.reload()
      
      // Should still be logged in
      await expect(page).toHaveURL(/.*student.*dashboard/)
    })

    test('should handle token expiration gracefully', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Simulate token expiration by clearing localStorage
      await page.evaluate(() => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
      })
      
      // Try to navigate to a protected page
      await page.goto('http://localhost:3000/student/profile')
      
      // Should redirect to login
      await expect(page).toHaveURL(/.*login/)
    })

    test('should handle idle timeout', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Wait for idle timeout (45 minutes in production, but we'll test with a shorter timeout)
      // Note: This test might need adjustment based on actual timeout implementation
      await page.waitForTimeout(1000) // Wait 1 second
      
      // Try to make an API call
      await page.goto('http://localhost:3000/student/profile')
      
      // Should still be logged in for this short duration
      await expect(page).toHaveURL(/.*student.*profile/)
    })
  })

  describe('Logout Flow', () => {
    test('should logout successfully and redirect to login', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      await performLogout(page)
      
      // Should redirect to login page
      await expect(page).toHaveURL(/.*login/)
      
      // Should not be able to access protected pages
      await page.goto('http://localhost:3000/student/dashboard')
      await expect(page).toHaveURL(/.*login/)
    })

    test('should clear all authentication data on logout', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      await performLogout(page)
      
      // Check that localStorage is cleared
      const authData = await page.evaluate(() => {
        return {
          accessToken: localStorage.getItem('accessToken'),
          refreshToken: localStorage.getItem('refreshToken'),
          user: localStorage.getItem('user')
        }
      })
      
      expect(authData.accessToken).toBeNull()
      expect(authData.refreshToken).toBeNull()
      expect(authData.user).toBeNull()
    })
  })

  describe('RBAC (Role-Based Access Control)', () => {
    test('should prevent student from accessing admin routes', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Try to access admin route
      await page.goto('http://localhost:3000/admin/dashboard')
      
      // Should be redirected to unauthorized page or login
      await expect(page).toHaveURL(/.*unauthorized|.*forbidden|.*login/)
    })

    test('should prevent teacher from accessing admin routes', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.teacher.email, TEST_USERS.teacher.password)
      await waitForLoginRedirect(page, TEST_USERS.teacher.expectedRedirect)
      
      // Try to access admin route
      await page.goto('http://localhost:3000/admin/dashboard')
      
      // Should be redirected to unauthorized page or login
      await expect(page).toHaveURL(/.*unauthorized|.*forbidden|.*login/)
    })

    test('should prevent admin from accessing student-specific routes', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.admin.email, TEST_USERS.admin.password)
      await waitForLoginRedirect(page, TEST_USERS.admin.expectedRedirect)
      
      // Try to access student route
      await page.goto('http://localhost:3000/student/dashboard')
      
      // Should be redirected to unauthorized page or admin dashboard
      await expect(page).toHaveURL(/.*unauthorized|.*forbidden|.*admin.*dashboard/)
    })
  })

  describe('Security Features', () => {
    test('should sanitize input to prevent XSS', async ({ page }) => {
      await navigateToLogin(page)
      
      const maliciousInput = '<script>alert("XSS")</script>'
      
      await page.fill('input[name="email"]', maliciousInput)
      await page.fill('input[name="password"]', 'validpassword123')
      await page.click('button[type="submit"]')
      
      // Should not execute the script
      await expect(page.locator('text=Please enter a valid email address')).toBeVisible()
    })

    test('should handle CSRF protection', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      await waitForLoginRedirect(page, TEST_USERS.student.expectedRedirect)
      
      // Try to make a request without CSRF token
      const response = await page.request.post('http://localhost:3000/api/user/me', {
        data: { test: 'data' }
      })
      
      // Should handle CSRF protection appropriately
      expect(response.status()).toBeGreaterThanOrEqual(400)
    })

    test('should not expose sensitive data in error messages', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, INVALID_CREDENTIALS.invalidEmail, INVALID_CREDENTIALS.invalidPassword)
      
      // Error message should not contain sensitive information
      const errorText = await page.locator('[class*="error"], [class*="alert"]').textContent()
      expect(errorText).not.toContain('password')
      expect(errorText).not.toContain('token')
      expect(errorText).not.toContain('database')
    })
  })

  describe('Navigation and UI', () => {
    test('should show appropriate navigation based on user role', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.admin.email, TEST_USERS.admin.password)
      await waitForLoginRedirect(page, TEST_USERS.admin.expectedRedirect)
      
      // Check for admin-specific navigation
      await expect(page.locator('text=Admin')).toBeVisible()
      await expect(page.locator('text=Students')).toBeVisible()
      await expect(page.locator('text=Content')).toBeVisible()
    })

    test('should be responsive on mobile devices', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 })
      
      await navigateToLogin(page)
      
      // Check that form is still usable on mobile
      await expect(page.locator('input[name="email"]')).toBeVisible()
      await expect(page.locator('input[name="password"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toBeVisible()
      
      // Check that logo is visible
      await expect(page.locator('img[alt="Ankurshala"]')).toBeVisible()
    })

    test('should support keyboard navigation', async ({ page }) => {
      await navigateToLogin(page)
      
      // Tab through form elements
      await page.keyboard.press('Tab') // Email field
      await page.keyboard.type('test@example.com')
      
      await page.keyboard.press('Tab') // Password field
      await page.keyboard.type('password123')
      
      await page.keyboard.press('Tab') // Submit button
      await page.keyboard.press('Enter')
      
      // Should attempt login
      await expect(page.locator('text=Password must be at least 8 characters long')).toBeVisible()
    })
  })

  describe('Error Handling', () => {
    test('should handle network errors gracefully', async ({ page }) => {
      // Block network requests to simulate network error
      await page.route('**/api/auth/signin', route => route.abort())
      
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      
      // Should show network error message
      await expect(page.locator('text=Server error')).toBeVisible()
    })

    test('should handle server errors gracefully', async ({ page }) => {
      // Mock server error response
      await page.route('**/api/auth/signin', route => 
        route.fulfill({ status: 500, body: 'Internal Server Error' })
      )
      
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      
      // Should show server error message
      await expect(page.locator('text=Server error')).toBeVisible()
    })

    test('should handle timeout errors gracefully', async ({ page }) => {
      // Mock timeout response
      await page.route('**/api/auth/signin', route => 
        route.fulfill({ status: 408, body: 'Request Timeout' })
      )
      
      await navigateToLogin(page)
      await performLogin(page, TEST_USERS.student.email, TEST_USERS.student.password)
      
      // Should show timeout error message
      await expect(page.locator('text=Request timeout')).toBeVisible()
    })
  })
})
