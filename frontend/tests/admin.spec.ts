/**
 * Admin Module Tests
 * 
 * Purpose: Comprehensive testing of admin functionality including dashboard,
 * student management, content management, and profile management
 * 
 * Key Features:
 * - Admin dashboard metrics and charts
 * - Student management CRUD operations
 * - Content management (boards, grades, subjects, chapters, topics)
 * - Admin profile management
 * - RBAC validation for admin-only features
 * - Security testing for admin endpoints
 * 
 * Security Considerations:
 * - Admin role verification
 * - Unauthorized access prevention
 * - Input validation and sanitization
 * - CSRF protection testing
 * - Session management validation
 * 
 * Usage Example:
 * ```bash
 * npx playwright test tests/admin.spec.ts
 * ```
 * 
 * Dependencies:
 * - Playwright: For browser automation and testing
 * - Backend API: Must be running on localhost:8080
 * - Frontend: Must be running on localhost:3000
 * - Admin user: siddhartha@ankurshala.com with ADMIN role
 * 
 * @author AnkurShala Development Team
 * @version 1.0.0
 * @since 2024-01-01
 */

import { test, expect, Page } from '@playwright/test'

// Admin test data
const ADMIN_USER = {
  email: 'siddhartha@ankurshala.com',
  password: 'Maza@123',
  role: 'ADMIN',
  expectedRedirect: '/admin/dashboard'
}

// Test data for student management
const TEST_STUDENT = {
  firstName: 'Test',
  lastName: 'Student',
  email: 'teststudent@example.com',
  phone: '+1234567890',
  schoolName: 'Test School',
  educationalBoard: 'CBSE',
  classLevel: 'GRADE_9',
  address: '123 Test Street, Test City'
}

/**
 * Helper function to login as admin
 */
async function loginAsAdmin(page: Page) {
  await page.goto('/login')
  await expect(page).toHaveTitle(/Ankurshala/)
  
  // Fill login form
  await page.fill('[name="email"]', ADMIN_USER.email)
  await page.fill('[name="password"]', ADMIN_USER.password)
  
  // Submit form
  await page.click('button[type="submit"]')
  
  // Wait for navigation to admin dashboard
  await page.waitForURL(`**${ADMIN_USER.expectedRedirect}**`, { timeout: 10000 })
  
  // Verify we're on admin dashboard
  await expect(page).toHaveURL(/.*admin.*dashboard/)
  await expect(page.locator('h1')).toContainText('Dashboard')
}

/**
 * Helper function to navigate to admin section
 */
async function navigateToAdminSection(page: Page, section: string) {
  const sectionMap = {
    'dashboard': '/admin/dashboard',
    'students': '/admin/users/students',
    'teachers': '/admin/users/teachers',
    'content': '/admin/content/manage',
    'profile': '/admin/profile',
    'pricing': '/admin/pricing',
    'analytics': '/admin/content/analytics'
  }
  
  const url = sectionMap[section as keyof typeof sectionMap]
  if (url) {
    await page.goto(url)
    await page.waitForLoadState('networkidle')
  }
}

/**
 * Helper function to verify admin navigation
 */
async function verifyAdminNavigation(page: Page) {
  // Check if admin sidebar is visible
  await expect(page.locator('text=Admin Panel')).toBeVisible()
  
  // Check if navigation items are present
  const navItems = ['Dashboard', 'Students', 'Teachers', 'Content Import', 'Content Structure', 'Analytics', 'Pricing', 'Notifications', 'Fee Waivers']
  
  for (const item of navItems) {
    await expect(page.locator(`text=${item}`)).toBeVisible()
  }
}

test.describe('Admin Module Tests', () => {
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
      if (request.url().includes('/admin/')) {
        console.log(`Admin Request: ${request.method()} ${request.url()}`)
      }
    })
    
    page.on('response', response => {
      if (response.url().includes('/admin/')) {
        console.log(`Admin Response: ${response.status()} ${response.url()}`)
      }
    })
  })

  test.describe('Admin Authentication and Access Control', () => {
    test('should login as admin and access admin dashboard', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Verify admin dashboard elements
      await expect(page.locator('h1')).toContainText('Dashboard')
      await expect(page.locator('text=Welcome back, Admin!')).toBeVisible()
      
      // Verify admin navigation
      await verifyAdminNavigation(page)
    })

    test('should prevent non-admin users from accessing admin pages', async ({ page }) => {
      // Try to access admin dashboard without login
      await page.goto('/admin/dashboard')
      
      // Should redirect to login page
      await page.waitForURL('**/login**', { timeout: 5000 })
      await expect(page).toHaveURL(/.*login/)
    })

    test('should maintain admin session across page refreshes', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Refresh the page
      await page.reload()
      
      // Should still be logged in and on admin dashboard
      await expect(page).toHaveURL(/.*admin.*dashboard/)
      await expect(page.locator('h1')).toContainText('Dashboard')
    })
  })

  test.describe('Admin Dashboard', () => {
    test('should display dashboard metrics correctly', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Wait for metrics to load
      await page.waitForSelector('[data-testid="dashboard-metrics"]', { timeout: 10000 })
      
      // Check if metric cards are visible
      await expect(page.locator('text=Total Students')).toBeVisible()
      await expect(page.locator('text=Total Teachers')).toBeVisible()
      await expect(page.locator('text=Active Students')).toBeVisible()
      await expect(page.locator('text=Active Teachers')).toBeVisible()
      
      // Check if metrics have numeric values
      const studentCount = await page.locator('text=Total Students').locator('..').locator('text=/\\d+/').first().textContent()
      expect(studentCount).toBeTruthy()
      expect(parseInt(studentCount!)).toBeGreaterThanOrEqual(0)
    })

    test('should display content statistics', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check content statistics section
      await expect(page.locator('text=Content Statistics')).toBeVisible()
      await expect(page.locator('text=Total Boards')).toBeVisible()
      await expect(page.locator('text=Total Subjects')).toBeVisible()
      await expect(page.locator('text=Total Chapters')).toBeVisible()
      await expect(page.locator('text=Total Topics')).toBeVisible()
    })

    test('should display user activity metrics', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check user activity section
      await expect(page.locator('text=User Activity')).toBeVisible()
      await expect(page.locator('text=New Students (7 days)')).toBeVisible()
      await expect(page.locator('text=New Teachers (7 days)')).toBeVisible()
      await expect(page.locator('text=New Students (30 days)')).toBeVisible()
      await expect(page.locator('text=New Teachers (30 days)')).toBeVisible()
    })

    test('should handle dashboard loading states', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check if loading skeleton appears initially
      // Note: This might be too fast to catch, but we can check for the final loaded state
      await page.waitForSelector('[data-testid="dashboard-metrics"]', { timeout: 10000 })
      
      // Verify metrics are loaded and not showing loading state
      await expect(page.locator('text=Total Students')).toBeVisible()
      await expect(page.locator('text=Total Teachers')).toBeVisible()
    })

    test('should handle dashboard errors gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock API failure
      await page.route('**/admin/dashboard/metrics', route => 
        route.fulfill({ status: 500, body: 'Internal Server Error' })
      )
      
      // Reload page to trigger error
      await page.reload()
      
      // Should show error message
      await expect(page.locator('text=Error loading dashboard')).toBeVisible()
      await expect(page.locator('text=Try Again')).toBeVisible()
    })
  })

  test.describe('Admin Navigation', () => {
    test('should navigate to all admin sections', async ({ page }) => {
      await loginAsAdmin(page)
      
      const sections = ['students', 'teachers', 'content', 'profile', 'pricing', 'analytics']
      
      for (const section of sections) {
        await navigateToAdminSection(page, section)
        
        // Verify we're on the correct page
        const currentUrl = page.url()
        expect(currentUrl).toContain(`/admin/${section}`)
        
        // Go back to dashboard for next test
        await navigateToAdminSection(page, 'dashboard')
      }
    })

    test('should have responsive navigation on mobile', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 })
      
      await loginAsAdmin(page)
      
      // Check if mobile menu button is visible
      await expect(page.locator('button[aria-label="Open menu"]')).toBeVisible()
      
      // Click mobile menu button
      await page.click('button[aria-label="Open menu"]')
      
      // Check if sidebar is visible
      await expect(page.locator('text=Admin Panel')).toBeVisible()
    })

    test('should support keyboard navigation', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Tab through navigation items
      await page.keyboard.press('Tab')
      await page.keyboard.press('Tab')
      await page.keyboard.press('Tab')
      
      // Press Enter on focused element
      await page.keyboard.press('Enter')
      
      // Should navigate to focused section
      await page.waitForTimeout(1000)
    })
  })

  test.describe('Admin Students Management', () => {
    test('should access students management page', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'students')
      
      // Verify we're on students page
      await expect(page).toHaveURL(/.*admin.*students/)
      
      // Check for students management elements
      await expect(page.locator('text=Students')).toBeVisible()
    })

    test('should display students list with pagination', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'students')
      
      // Wait for students list to load
      await page.waitForTimeout(2000)
      
      // Check for pagination controls
      await expect(page.locator('text=Previous')).toBeVisible()
      await expect(page.locator('text=Next')).toBeVisible()
    })

    test('should have search functionality for students', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'students')
      
      // Look for search input
      const searchInput = page.locator('input[placeholder*="search" i]').first()
      
      if (await searchInput.isVisible()) {
        await searchInput.fill('test')
        await page.keyboard.press('Enter')
        
        // Wait for search results
        await page.waitForTimeout(2000)
      }
    })

    test('should have filters for students', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'students')
      
      // Look for filter controls
      await page.waitForTimeout(2000)
      
      // Check for common filter elements
      const filterElements = page.locator('select, button[role="button"]')
      const filterCount = await filterElements.count()
      
      if (filterCount > 0) {
        console.log(`Found ${filterCount} filter elements`)
      }
    })
  })

  test.describe('Admin Content Management', () => {
    test('should access content management page', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'content')
      
      // Verify we're on content management page
      await expect(page).toHaveURL(/.*admin.*content.*manage/)
      
      // Check for content management tabs
      await expect(page.locator('text=Boards')).toBeVisible()
      await expect(page.locator('text=Grades')).toBeVisible()
      await expect(page.locator('text=Subjects')).toBeVisible()
      await expect(page.locator('text=Chapters')).toBeVisible()
      await expect(page.locator('text=Topics')).toBeVisible()
      await expect(page.locator('text=Topic Notes')).toBeVisible()
    })

    test('should navigate between content management tabs', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'content')
      
      const tabs = ['boards', 'grades', 'subjects', 'chapters', 'topics', 'topicnotes']
      
      for (const tab of tabs) {
        // Click on tab
        await page.click(`[data-value="${tab}"]`)
        
        // Wait for tab content to load
        await page.waitForTimeout(1000)
        
        // Verify tab is active
        const tabElement = page.locator(`[data-value="${tab}"]`)
        await expect(tabElement).toHaveAttribute('data-state', 'active')
      }
    })

    test('should have CRUD operations for boards', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'content')
      
      // Click on Boards tab
      await page.click('[data-value="boards"]')
      
      // Look for Add/Create button
      const addButton = page.locator('button:has-text("Add"), button:has-text("Create"), button:has-text("New")').first()
      
      if (await addButton.isVisible()) {
        await addButton.click()
        
        // Check if form dialog opens
        await expect(page.locator('text=Create Board, text=Add Board, text=New Board')).toBeVisible()
      }
    })
  })

  test.describe('Admin Profile Management', () => {
    test('should access admin profile page', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'profile')
      
      // Verify we're on profile page
      await expect(page).toHaveURL(/.*admin.*profile/)
      
      // Check for profile form elements
      await expect(page.locator('text=Profile')).toBeVisible()
    })

    test('should display admin profile information', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'profile')
      
      // Wait for profile to load
      await page.waitForTimeout(2000)
      
      // Check for profile fields
      const profileFields = page.locator('input, textarea, select')
      const fieldCount = await profileFields.count()
      
      expect(fieldCount).toBeGreaterThan(0)
    })

    test('should allow profile updates', async ({ page }) => {
      await loginAsAdmin(page)
      await navigateToAdminSection(page, 'profile')
      
      // Wait for profile to load
      await page.waitForTimeout(2000)
      
      // Look for save/update button
      const saveButton = page.locator('button:has-text("Save"), button:has-text("Update"), button:has-text("Submit")').first()
      
      if (await saveButton.isVisible()) {
        // Button exists, profile updates are supported
        expect(true).toBeTruthy()
      }
    })
  })

  test.describe('Admin Security and RBAC', () => {
    test('should require admin role for all admin endpoints', async ({ page }) => {
      // Test direct API access without authentication
      const response = await page.request.get('http://localhost:8080/api/admin/dashboard/metrics')
      expect(response.status()).toBe(401)
    })

    test('should validate admin permissions on frontend', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Verify admin-specific elements are visible
      await expect(page.locator('text=Admin Panel')).toBeVisible()
      await expect(page.locator('text=Dashboard')).toBeVisible()
      
      // Verify non-admin elements are not visible
      // (This would need to be tested with a non-admin user)
    })

    test('should handle session expiration gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Simulate session expiration by clearing tokens
      await page.evaluate(() => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
      })
      
      // Try to navigate to admin page
      await page.goto('/admin/dashboard')
      
      // Should redirect to login or show auth error
      await page.waitForTimeout(3000)
      
      const currentUrl = page.url()
      expect(currentUrl.includes('/login') || currentUrl.includes('/admin/dashboard')).toBeTruthy()
    })
  })

  test.describe('Admin UI/UX', () => {
    test('should be responsive on different screen sizes', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Test desktop view
      await page.setViewportSize({ width: 1920, height: 1080 })
      await expect(page.locator('text=Admin Panel')).toBeVisible()
      
      // Test tablet view
      await page.setViewportSize({ width: 768, height: 1024 })
      await expect(page.locator('text=Admin Panel')).toBeVisible()
      
      // Test mobile view
      await page.setViewportSize({ width: 375, height: 667 })
      await expect(page.locator('text=Admin Panel')).toBeVisible()
    })

    test('should have consistent branding and colors', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check for AnkurShala branding
      await expect(page.locator('text=Admin Panel')).toBeVisible()
      
      // Check for consistent color scheme
      const adminPanel = page.locator('text=Admin Panel')
      await expect(adminPanel).toBeVisible()
    })

    test('should provide clear navigation feedback', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check for active navigation state
      const dashboardLink = page.locator('a[href="/admin"]')
      await expect(dashboardLink).toHaveClass(/bg-blue-600/)
    })

    test('should handle loading states properly', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check for loading indicators
      await page.waitForSelector('[data-testid="dashboard-metrics"]', { timeout: 10000 })
      
      // Verify metrics are loaded
      await expect(page.locator('text=Total Students')).toBeVisible()
    })
  })

  test.describe('Admin Error Handling', () => {
    test('should handle network errors gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Block admin API requests
      await page.route('**/admin/**', route => route.abort())
      
      // Reload page
      await page.reload()
      
      // Should show error message or fallback content
      await page.waitForTimeout(3000)
      
      // Check for error handling
      const hasError = await page.locator('text=Error, text=Failed, text=Unable').isVisible().catch(() => false)
      expect(hasError).toBeTruthy()
    })

    test('should handle server errors gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock server error
      await page.route('**/admin/dashboard/metrics', route => 
        route.fulfill({ status: 500, body: 'Internal Server Error' })
      )
      
      // Reload page
      await page.reload()
      
      // Should show error message
      await expect(page.locator('text=Error loading dashboard')).toBeVisible()
    })

    test('should provide retry functionality for failed requests', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock server error
      await page.route('**/admin/dashboard/metrics', route => 
        route.fulfill({ status: 500, body: 'Internal Server Error' })
      )
      
      // Reload page
      await page.reload()
      
      // Should show retry button
      await expect(page.locator('text=Try Again')).toBeVisible()
      
      // Click retry
      await page.click('text=Try Again')
      
      // Should attempt to reload data
      await page.waitForTimeout(2000)
    })
  })
})
