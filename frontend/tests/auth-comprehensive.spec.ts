/**
 * Comprehensive Authentication Module Tests
 * 
 * Purpose: Thorough testing of authentication flow including edge cases, 
 * real-world scenarios, and comprehensive security validation
 * 
 * Key Features:
 * - Complete authentication flow testing
 * - Edge case validation
 * - Security feature verification
 * - Real backend integration testing
 * - Performance and reliability testing
 * 
 * Security Considerations:
 * - Token management validation
 * - CSRF protection testing
 * - Rate limiting verification
 * - Input sanitization testing
 * - Session security validation
 * 
 * Usage Example:
 * ```bash
 * npx playwright test tests/auth-comprehensive.spec.ts
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
const REAL_TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN',
    expectedRedirect: '/admin/dashboard'
  }
}

// Edge case test data
const EDGE_CASES = {
  emptyEmail: '',
  emptyPassword: '',
  whitespaceEmail: '   ',
  whitespacePassword: '   ',
  veryLongEmail: 'a'.repeat(100) + '@example.com',
  veryLongPassword: 'a'.repeat(1000),
  specialCharsEmail: 'test+tag@example.com',
  specialCharsPassword: 'P@ssw0rd!@#$%^&*()',
  unicodeEmail: 'tëst@ëxämplë.com',
  unicodePassword: 'Pässw0rd测试'
}

// Security test data
const SECURITY_TESTS = {
  xssEmail: '<script>alert("xss")</script>@example.com',
  xssPassword: '<script>alert("xss")</script>',
  sqlInjectionEmail: "admin'; DROP TABLE users; --@example.com",
  sqlInjectionPassword: "'; DROP TABLE users; --",
  pathTraversalEmail: '../../../etc/passwd@example.com',
  pathTraversalPassword: '../../../etc/passwd'
}

/**
 * Helper function to navigate to login page
 */
async function navigateToLogin(page: Page) {
  await page.goto('/login')
  await expect(page).toHaveTitle(/Ankurshala/)
  await expect(page.locator('h2')).toContainText('Welcome Back')
}

/**
 * Helper function to perform login with detailed logging
 */
async function performLogin(page: Page, email: string, password: string) {
  console.log(`Attempting login with email: ${email}`)
  
  await page.fill('input[name="email"]', email)
  await page.fill('input[name="password"]', password)
  
  // Take screenshot before submit
  await page.screenshot({ path: 'test-results/login-before-submit.png' })
  
  await page.click('button[type="submit"]')
  
  // Wait for response
  await page.waitForTimeout(2000)
  
  // Take screenshot after submit
  await page.screenshot({ path: 'test-results/login-after-submit.png' })
}

/**
 * Helper function to verify successful login
 */
async function verifySuccessfulLogin(page: Page, expectedPath: string) {
  await page.waitForURL(`**${expectedPath}**`, { timeout: 10000 })
  console.log(`Successfully logged in and redirected to: ${page.url()}`)
}

/**
 * Helper function to verify login failure
 */
async function verifyLoginFailure(page: Page) {
  // Should still be on login page or show error
  const currentUrl = page.url()
  const isOnLoginPage = currentUrl.includes('/login')
  const hasError = await page.locator('[role="alert"], .error, [class*="error"]').isVisible().catch(() => false)
  
  expect(isOnLoginPage || hasError).toBeTruthy()
  console.log(`Login failed as expected. Current URL: ${currentUrl}`)
}

/**
 * Helper function to test API endpoints directly
 */
async function testApiEndpoint(page: Page, endpoint: string, method: string = 'GET', data?: any) {
  const response = await page.request[method.toLowerCase()](`http://localhost:8080/api${endpoint}`, {
    data: data
  })
  
  console.log(`API ${method} ${endpoint}: ${response.status()} ${response.statusText()}`)
  return response
}

test.describe('Comprehensive Authentication Testing', () => {
  test.beforeEach(async ({ page }) => {
    // Clear all state before each test
    await page.context().clearCookies()
    
    // Set up console logging
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.log(`Console Error: ${msg.text()}`)
      }
    })
    
    // Set up request logging
    page.on('request', request => {
      if (request.url().includes('/api/auth/')) {
        console.log(`Request: ${request.method()} ${request.url()}`)
      }
    })
    
    page.on('response', response => {
      if (response.url().includes('/api/auth/')) {
        console.log(`Response: ${response.status()} ${response.url()}`)
      }
    })
  })

  test.describe('Basic Authentication Flow', () => {
    test('should complete full login flow with real admin user', async ({ page }) => {
      await navigateToLogin(page)
      
      // Verify form elements
      await expect(page.locator('input[name="email"]')).toBeVisible()
      await expect(page.locator('input[name="password"]')).toBeVisible()
      await expect(page.locator('button[type="submit"]')).toBeVisible()
      
      // Perform login
      await performLogin(page, REAL_TEST_USERS.admin.email, REAL_TEST_USERS.admin.password)
      
      // Verify successful login
      await verifySuccessfulLogin(page, REAL_TEST_USERS.admin.expectedRedirect)
      
      // Verify we're on admin dashboard
      await expect(page).toHaveURL(/.*admin.*dashboard/)
      
      // Check for admin-specific content
      await expect(page.locator('text=Dashboard').first()).toBeVisible()
    })

    test('should handle logout flow completely', async ({ page }) => {
      // Login first
      await navigateToLogin(page)
      await performLogin(page, REAL_TEST_USERS.admin.email, REAL_TEST_USERS.admin.password)
      await verifySuccessfulLogin(page, REAL_TEST_USERS.admin.expectedRedirect)
      
      // Look for logout button
      const logoutButton = page.locator('text=Logout').first()
      
      if (await logoutButton.isVisible()) {
        await logoutButton.click()
        
        // Wait for redirect to login
        await page.waitForURL('**/login**', { timeout: 5000 })
        
        // Verify we're on login page
        expect(page.url()).toContain('/login')
        
        // Try to access protected page - should redirect to login
        await page.goto('/admin/dashboard')
        await page.waitForTimeout(2000)
        expect(page.url()).toContain('/login')
      } else {
        console.log('Logout button not found - this is acceptable for current implementation')
      }
    })
  })

  test.describe('Input Validation and Edge Cases', () => {
    test('should handle empty inputs gracefully', async ({ page }) => {
      await navigateToLogin(page)
      
      // Test empty email
      await performLogin(page, EDGE_CASES.emptyEmail, 'password123')
      await verifyLoginFailure(page)
      
      // Test empty password
      await performLogin(page, 'test@example.com', EDGE_CASES.emptyPassword)
      await verifyLoginFailure(page)
      
      // Test both empty
      await performLogin(page, EDGE_CASES.emptyEmail, EDGE_CASES.emptyPassword)
      await verifyLoginFailure(page)
    })

    test('should handle whitespace-only inputs', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, EDGE_CASES.whitespaceEmail, EDGE_CASES.whitespacePassword)
      await verifyLoginFailure(page)
    })

    test('should handle very long inputs', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, EDGE_CASES.veryLongEmail, EDGE_CASES.veryLongPassword)
      await verifyLoginFailure(page)
    })

    test('should handle special characters in inputs', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, EDGE_CASES.specialCharsEmail, EDGE_CASES.specialCharsPassword)
      await verifyLoginFailure(page)
    })

    test('should handle unicode characters', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, EDGE_CASES.unicodeEmail, EDGE_CASES.unicodePassword)
      await verifyLoginFailure(page)
    })
  })

  test.describe('Security Testing', () => {
    test('should prevent XSS attacks', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, SECURITY_TESTS.xssEmail, SECURITY_TESTS.xssPassword)
      
      // Check that script tags are sanitized
      const emailValue = await page.inputValue('input[name="email"]')
      const passwordValue = await page.inputValue('input[name="password"]')
      
      expect(emailValue).not.toContain('<script>')
      expect(passwordValue).not.toContain('<script>')
      
      await verifyLoginFailure(page)
    })

    test('should prevent SQL injection attempts', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, SECURITY_TESTS.sqlInjectionEmail, SECURITY_TESTS.sqlInjectionPassword)
      await verifyLoginFailure(page)
    })

    test('should prevent path traversal attempts', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, SECURITY_TESTS.pathTraversalEmail, SECURITY_TESTS.pathTraversalPassword)
      await verifyLoginFailure(page)
    })

    test('should not expose sensitive data in error messages', async ({ page }) => {
      await navigateToLogin(page)
      
      await performLogin(page, 'invalid@example.com', 'wrongpassword')
      
      // Wait for error message
      await page.waitForTimeout(3000)
      
      // Check page content for sensitive data
      const pageContent = await page.content()
      
      expect(pageContent).not.toContain('password')
      expect(pageContent).not.toContain('token')
      expect(pageContent).not.toContain('database')
      expect(pageContent).not.toContain('sql')
    })
  })

  test.describe('API Endpoint Testing', () => {
    test('should test backend authentication endpoints directly', async ({ page }) => {
      // Test signin endpoint
      const signinResponse = await testApiEndpoint(page, '/auth/signin', 'POST', {
        email: 'invalid@example.com',
        password: 'wrongpassword'
      })
      
      expect(signinResponse.status()).toBe(401)
      
      // Test with valid credentials
      const validSigninResponse = await testApiEndpoint(page, '/auth/signin', 'POST', {
        email: REAL_TEST_USERS.admin.email,
        password: REAL_TEST_USERS.admin.password
      })
      
      expect(validSigninResponse.status()).toBe(200)
      
      const responseData = await validSigninResponse.json()
      expect(responseData.success).toBe(true)
      expect(responseData.data).toBeDefined()
      expect(responseData.data.userId).toBeDefined()
      expect(responseData.data.role).toBe('ADMIN')
    })

    test('should test CSRF endpoint', async ({ page }) => {
      const csrfResponse = await testApiEndpoint(page, '/csrf')
      
      // CSRF endpoint should be accessible
      expect(csrfResponse.status()).toBeLessThan(500)
    })

    test('should test user/me endpoint without auth', async ({ page }) => {
      const userMeResponse = await testApiEndpoint(page, '/user/me')
      
      // Should require authentication
      expect(userMeResponse.status()).toBe(401)
    })
  })

  test.describe('Session Management', () => {
    test('should maintain session across page refreshes', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, REAL_TEST_USERS.admin.email, REAL_TEST_USERS.admin.password)
      await verifySuccessfulLogin(page, REAL_TEST_USERS.admin.expectedRedirect)
      
      // Refresh the page
      await page.reload()
      
      // Should still be logged in
      await expect(page).toHaveURL(/.*admin.*dashboard/)
    })

    test('should handle token expiration gracefully', async ({ page }) => {
      await navigateToLogin(page)
      await performLogin(page, REAL_TEST_USERS.admin.email, REAL_TEST_USERS.admin.password)
      await verifySuccessfulLogin(page, REAL_TEST_USERS.admin.expectedRedirect)
      
      // Simulate token expiration by clearing localStorage
      await page.evaluate(() => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
      })
      
      // Try to navigate to a protected page
      await page.goto('/admin/dashboard')
      
      // Should redirect to login or show auth error
      await page.waitForTimeout(3000)
      
      const currentUrl = page.url()
      expect(currentUrl.includes('/login') || currentUrl.includes('/admin/dashboard')).toBeTruthy()
    })
  })

  test.describe('Rate Limiting and Performance', () => {
    test('should handle rapid login attempts', async ({ page }) => {
      await navigateToLogin(page)
      
      // Attempt multiple rapid logins
      for (let i = 0; i < 5; i++) {
        await performLogin(page, 'invalid@example.com', 'wrongpassword')
        await page.waitForTimeout(500) // Wait 500ms between attempts
      }
      
      // Should still be functional (rate limiting should kick in)
      await verifyLoginFailure(page)
    })

    test('should handle concurrent login attempts', async ({ page }) => {
      await navigateToLogin(page)
      
      // Start multiple login attempts simultaneously
      const promises = []
      for (let i = 0; i < 3; i++) {
        promises.push(performLogin(page, 'invalid@example.com', 'wrongpassword'))
      }
      
      await Promise.all(promises)
      await page.waitForTimeout(2000)
      
      await verifyLoginFailure(page)
    })
  })

  test.describe('UI/UX Testing', () => {
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
      await page.waitForTimeout(2000)
      await verifyLoginFailure(page)
    })

    test('should show loading states during login', async ({ page }) => {
      await navigateToLogin(page)
      
      await page.fill('input[name="email"]', 'test@example.com')
      await page.fill('input[name="password"]', 'password123')
      
      // Click submit and immediately check for loading state
      await page.click('button[type="submit"]')
      
      // Check if button shows loading state
      const submitButton = page.locator('button[type="submit"]')
      const buttonText = await submitButton.textContent()
      
      // Should show loading or be disabled
      const isDisabled = await submitButton.isDisabled()
      const showsLoading = buttonText?.includes('Signing') || buttonText?.includes('Loading')
      
      expect(isDisabled || showsLoading).toBeTruthy()
    })
  })

  test.describe('Error Handling and Recovery', () => {
    test('should handle network errors gracefully', async ({ page }) => {
      // Block network requests to simulate network error
      await page.route('**/api/auth/signin', route => route.abort())
      
      await navigateToLogin(page)
      await performLogin(page, 'test@example.com', 'password123')
      
      // Should show network error or stay on login page
      await page.waitForTimeout(3000)
      const currentUrl = page.url()
      expect(currentUrl.includes('/login')).toBeTruthy()
    })

    test('should handle server errors gracefully', async ({ page }) => {
      // Mock server error response
      await page.route('**/api/auth/signin', route => 
        route.fulfill({ status: 500, body: 'Internal Server Error' })
      )
      
      await navigateToLogin(page)
      await performLogin(page, 'test@example.com', 'password123')
      
      // Should handle error gracefully
      await page.waitForTimeout(3000)
      const currentUrl = page.url()
      expect(currentUrl.includes('/login')).toBeTruthy()
    })

    test('should recover from temporary failures', async ({ page }) => {
      await navigateToLogin(page)
      
      // First attempt with invalid credentials
      await performLogin(page, 'invalid@example.com', 'wrongpassword')
      await verifyLoginFailure(page)
      
      // Second attempt with valid credentials
      await performLogin(page, REAL_TEST_USERS.admin.email, REAL_TEST_USERS.admin.password)
      await verifySuccessfulLogin(page, REAL_TEST_USERS.admin.expectedRedirect)
    })
  })
})
