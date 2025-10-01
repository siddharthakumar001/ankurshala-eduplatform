import { test, expect, Page } from '@playwright/test'

// Test configuration
const BASE_URL = 'http://localhost:3000'
const API_URL = 'http://localhost:8080/api'

// Test data
const TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  },
  student: {
    email: 'student1@ankurshala.com',
    password: 'Maza@123',
    role: 'STUDENT'
  },
  teacher: {
    email: 'teacher1@ankurshala.com',
    password: 'Maza@123',
    role: 'TEACHER'
  }
}

// Helper functions
async function login(page: Page, user: typeof TEST_USERS.admin) {
  await page.goto('/login')
  await page.fill('input[name="email"]', user.email)
  await page.fill('input[name="password"]', user.password)
  await page.click('button[type="submit"]')
  await page.waitForLoadState('networkidle')
}

async function logout(page: Page) {
  // Try multiple logout methods
  try {
    // Method 1: Look for logout button in session manager
    const logoutButton = page.locator('button:has-text("Logout")').first()
    if (await logoutButton.isVisible()) {
      await logoutButton.click()
      await page.waitForLoadState('networkidle')
      return
    }
  } catch (error) {
    console.log('Logout button not found in session manager')
  }

  try {
    // Method 2: Clear localStorage and redirect
    await page.evaluate(() => {
      localStorage.clear()
      window.location.href = '/login'
    })
    await page.waitForLoadState('networkidle')
  } catch (error) {
    console.log('Failed to logout via localStorage clear')
  }
}

async function waitForRedirect(page: Page, expectedPath: string, timeout = 10000) {
  await page.waitForURL(`**${expectedPath}`, { timeout })
}

// Test suite for authentication flows
test.describe('Authentication Integration Tests', () => {
  
  test.describe('Non-logged-in user behavior', () => {
    test('should redirect to home page when accessing root URL', async ({ page }) => {
      await page.goto('/')
      await expect(page).toHaveURL('/')
      await expect(page.locator('h1')).toContainText('Smart Learning, On Demand')
    })

    test('should redirect to login when accessing protected routes', async ({ page }) => {
      const protectedRoutes = ['/admin/dashboard', '/student/profile', '/teacher/profile']
      
      for (const route of protectedRoutes) {
        await page.goto(route)
        await waitForRedirect(page, '/login')
        await expect(page).toHaveURL(/.*\/login.*/)
        
        // Check for redirect message
        const message = page.locator('text=Please login to access this page')
        await expect(message).toBeVisible()
      }
    })

    test('should redirect to login with admin message for admin routes', async ({ page }) => {
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/login')
      await expect(page).toHaveURL(/.*\/login.*/)
      
      const message = page.locator('text=Admin access required')
      await expect(message).toBeVisible()
    })

    test('should allow access to public routes', async ({ page }) => {
      const publicRoutes = ['/login', '/register-student', '/register-teacher', '/unauthorized', '/forbidden']
      
      for (const route of publicRoutes) {
        await page.goto(route)
        await expect(page).toHaveURL(route)
      }
    })
  })

  test.describe('Logged-in user behavior', () => {
    test('should redirect admin to admin dashboard from home page', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/')
      await waitForRedirect(page, '/admin/dashboard')
      await expect(page).toHaveURL('/admin/dashboard')
    })

    test('should redirect student to student profile from home page', async ({ page }) => {
      await login(page, TEST_USERS.student)
      await page.goto('/')
      await waitForRedirect(page, '/student/profile')
      await expect(page).toHaveURL('/student/profile')
    })

    test('should redirect teacher to teacher profile from home page', async ({ page }) => {
      await login(page, TEST_USERS.teacher)
      await page.goto('/')
      await waitForRedirect(page, '/teacher/profile')
      await expect(page).toHaveURL('/teacher/profile')
    })

    test('should redirect to forbidden page when accessing unauthorized routes', async ({ page }) => {
      await login(page, TEST_USERS.student)
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/forbidden')
      await expect(page).toHaveURL('/forbidden')
      await expect(page.locator('h1')).toContainText('Access Forbidden')
    })
  })

  test.describe('Admin dashboard functionality', () => {
    test('should display admin dashboard with metrics', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Check dashboard elements
      await expect(page.locator('h1')).toContainText('Dashboard')
      await expect(page.locator('text=Welcome back, Admin!')).toBeVisible()
      
      // Check metrics cards
      await expect(page.locator('[data-testid="dashboard-metrics"]')).toBeVisible()
      
      // Wait for metrics to load
      await page.waitForSelector('text=Total Students', { timeout: 10000 })
      await expect(page.locator('text=Total Students')).toBeVisible()
      await expect(page.locator('text=Total Teachers')).toBeVisible()
      await expect(page.locator('text=Active Students')).toBeVisible()
      await expect(page.locator('text=Active Teachers')).toBeVisible()
    })

    test('should show session information for admin', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Check for session manager
      await expect(page.locator('text=Session Status')).toBeVisible()
      await expect(page.locator('text=Active')).toBeVisible()
    })
  })

  test.describe('Logout functionality', () => {
    test('should logout user and clear session', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Verify user is logged in
      await expect(page.locator('text=Welcome back, Admin!')).toBeVisible()
      
      // Logout
      await logout(page)
      
      // Verify redirect to login
      await expect(page).toHaveURL(/.*\/login.*/)
      
      // Try to access protected route
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/login')
      await expect(page).toHaveURL(/.*\/login.*/)
    })

    test('should logout from all sessions', async ({ page, context }) => {
      // Create two pages (simulating different tabs)
      const page1 = page
      const page2 = await context.newPage()
      
      // Login on both pages
      await login(page1, TEST_USERS.admin)
      await login(page2, TEST_USERS.admin)
      
      // Verify both are logged in
      await page1.goto('/admin/dashboard')
      await page2.goto('/admin/dashboard')
      await expect(page1.locator('text=Welcome back, Admin!')).toBeVisible()
      await expect(page2.locator('text=Welcome back, Admin!')).toBeVisible()
      
      // Logout from page1
      await logout(page1)
      
      // Verify page1 is logged out
      await expect(page1).toHaveURL(/.*\/login.*/)
      
      // Verify page2 is also logged out (shared session)
      await page2.reload()
      await waitForRedirect(page2, '/login')
      await expect(page2).toHaveURL(/.*\/login.*/)
    })
  })

  test.describe('Session management and heartbeat', () => {
    test('should maintain session with user activity', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Simulate user activity
      await page.click('body')
      await page.waitForTimeout(1000)
      await page.hover('h1')
      await page.waitForTimeout(1000)
      
      // Verify session is still active
      await expect(page.locator('text=Session Status')).toBeVisible()
      await expect(page.locator('text=Active')).toBeVisible()
    })

    test('should show session expiration warning', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Mock token expiration by modifying localStorage
      await page.evaluate(() => {
        const token = localStorage.getItem('accessToken')
        if (token) {
          // Create a token that expires in 4 minutes (within warning threshold)
          const payload = JSON.parse(atob(token.split('.')[1]))
          payload.exp = Math.floor(Date.now() / 1000) + (4 * 60) // 4 minutes
          const newToken = token.split('.')[0] + '.' + btoa(JSON.stringify(payload)) + '.' + token.split('.')[2]
          localStorage.setItem('accessToken', newToken)
        }
      })
      
      // Wait for warning to appear
      await page.waitForSelector('text=Session Expiring Soon', { timeout: 10000 })
      await expect(page.locator('text=Session Expiring Soon')).toBeVisible()
    })

    test('should handle session extension', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      await page.goto('/admin/dashboard')
      
      // Mock token expiration
      await page.evaluate(() => {
        const token = localStorage.getItem('accessToken')
        if (token) {
          const payload = JSON.parse(atob(token.split('.')[1]))
          payload.exp = Math.floor(Date.now() / 1000) + (4 * 60) // 4 minutes
          const newToken = token.split('.')[0] + '.' + btoa(JSON.stringify(payload)) + '.' + token.split('.')[2]
          localStorage.setItem('accessToken', newToken)
        }
      })
      
      // Wait for warning and extend session
      await page.waitForSelector('text=Session Expiring Soon', { timeout: 10000 })
      await page.click('button:has-text("Extend Session")')
      
      // Wait for success message
      await page.waitForSelector('text=Session extended successfully', { timeout: 10000 })
      await expect(page.locator('text=Session extended successfully')).toBeVisible()
    })
  })

  test.describe('Edge cases and error handling', () => {
    test('should handle invalid credentials', async ({ page }) => {
      await page.goto('/login')
      await page.fill('input[name="email"]', 'invalid@example.com')
      await page.fill('input[name="password"]', 'wrongpassword')
      await page.click('button[type="submit"]')
      
      // Check for error message
      await expect(page.locator('text=Invalid email or password')).toBeVisible()
    })

    test('should handle network errors gracefully', async ({ page }) => {
      // Mock network failure
      await page.route('**/api/auth/signin', route => route.abort())
      
      await page.goto('/login')
      await page.fill('input[name="email"]', TEST_USERS.admin.email)
      await page.fill('input[name="password"]', TEST_USERS.admin.password)
      await page.click('button[type="submit"]')
      
      // Check for error message
      await expect(page.locator('text=Login failed')).toBeVisible()
    })

    test('should handle expired token gracefully', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      
      // Mock expired token
      await page.evaluate(() => {
        const token = localStorage.getItem('accessToken')
        if (token) {
          const payload = JSON.parse(atob(token.split('.')[1]))
          payload.exp = Math.floor(Date.now() / 1000) - 3600 // Expired 1 hour ago
          const newToken = token.split('.')[0] + '.' + btoa(JSON.stringify(payload)) + '.' + token.split('.')[2]
          localStorage.setItem('accessToken', newToken)
        }
      })
      
      // Try to access protected route
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/login')
      await expect(page).toHaveURL(/.*\/login.*/)
    })

    test('should handle malformed tokens', async ({ page }) => {
      // Set malformed token
      await page.evaluate(() => {
        localStorage.setItem('accessToken', 'malformed.token')
      })
      
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/login')
      await expect(page).toHaveURL(/.*\/login.*/)
    })
  })

  test.describe('Role-based access control', () => {
    test('should allow admin to access admin routes', async ({ page }) => {
      await login(page, TEST_USERS.admin)
      
      const adminRoutes = ['/admin/dashboard', '/admin/content/manage', '/admin/users/students']
      
      for (const route of adminRoutes) {
        await page.goto(route)
        await expect(page).toHaveURL(route)
      }
    })

    test('should deny student access to admin routes', async ({ page }) => {
      await login(page, TEST_USERS.student)
      
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/forbidden')
      await expect(page).toHaveURL('/forbidden')
    })

    test('should deny teacher access to admin routes', async ({ page }) => {
      await login(page, TEST_USERS.teacher)
      
      await page.goto('/admin/dashboard')
      await waitForRedirect(page, '/forbidden')
      await expect(page).toHaveURL('/forbidden')
    })
  })

  test.describe('Security features', () => {
    test('should clear credentials from URL parameters', async ({ page }) => {
      await page.goto('/login?email=test@example.com&password=test123')
      
      // Check that URL is cleaned
      await expect(page).toHaveURL('/login')
      
      // Check for security warning
      await expect(page.locator('text=Security Notice')).toBeVisible()
    })

    test('should prevent XSS attacks in login form', async ({ page }) => {
      await page.goto('/login')
      
      // Try to inject script
      await page.fill('input[name="email"]', '<script>alert("xss")</script>')
      await page.fill('input[name="password"]', '<script>alert("xss")</script>')
      
      // Check that script is sanitized
      const emailValue = await page.inputValue('input[name="email"]')
      const passwordValue = await page.inputValue('input[name="password"]')
      
      expect(emailValue).not.toContain('<script>')
      expect(passwordValue).not.toContain('<script>')
    })

    test('should enforce HTTPS in production', async ({ page }) => {
      // This test would need to be run in production environment
      // For now, we'll just verify security headers are present
      const response = await page.goto('/')
      const headers = response?.headers()
      
      // Check for security headers
      expect(headers?.['x-frame-options']).toBe('DENY')
      expect(headers?.['x-content-type-options']).toBe('nosniff')
      expect(headers?.['x-xss-protection']).toBe('1; mode=block')
    })
  })
})
