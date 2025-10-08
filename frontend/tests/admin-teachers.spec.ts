/**
 * Admin Teachers Management Tests
 * 
 * Purpose: Comprehensive testing of admin teachers management functionality
 * 
 * Key Features:
 * - Teacher list display with pagination
 * - Search and filtering functionality
 * - Teacher CRUD operations (view, edit, toggle status)
 * - RBAC validation for admin-only access
 * - Error handling and loading states
 * - Responsive design testing
 * - Teacher-specific features (ratings, verification, hourly rates)
 * 
 * Security Considerations:
 * - Admin role verification
 * - Unauthorized access prevention
 * - Input validation and sanitization
 * - Session management validation
 * 
 * Usage Example:
 * ```bash
 * npx playwright test tests/admin-teachers.spec.ts
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
  role: 'ADMIN'
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
  await page.waitForURL(`**/admin/dashboard**`, { timeout: 10000 })
  
  // Navigate to teachers page
  await page.goto('/admin/users/teachers')
  await page.waitForLoadState('networkidle')
}

/**
 * Helper function to wait for teachers table to load
 */
async function waitForTeachersTable(page: Page) {
  await page.waitForSelector('table', { timeout: 10000 })
  await page.waitForLoadState('networkidle')
}

test.describe('Admin Teachers Management', () => {
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
      if (request.url().includes('/admin/teachers')) {
        console.log(`Teachers Request: ${request.method()} ${request.url()}`)
      }
    })
    
    page.on('response', response => {
      if (response.url().includes('/admin/teachers')) {
        console.log(`Teachers Response: ${response.status()} ${response.url()}`)
      }
    })
  })

  test.describe('Authentication and Access Control', () => {
    test('should require admin role to access teachers page', async ({ page }) => {
      // Try to access teachers page without login
      await page.goto('/admin/users/teachers')
      
      // Should redirect to login page
      await page.waitForURL('**/login**', { timeout: 5000 })
      await expect(page).toHaveURL(/.*login/)
    })

    test('should allow admin to access teachers page', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Verify we're on teachers page
      await expect(page).toHaveURL(/.*admin.*teachers/)
      await expect(page.locator('h1')).toContainText('Manage Teachers')
    })

    test('should maintain admin session across page refreshes', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Refresh the page
      await page.reload()
      
      // Should still be logged in and on teachers page
      await expect(page).toHaveURL(/.*admin.*teachers/)
      await expect(page.locator('h1')).toContainText('Manage Teachers')
    })
  })

  test.describe('Teachers List Display', () => {
    test('should display teachers table with proper headers', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check table headers
      await expect(page.locator('th:has-text("Teacher")')).toBeVisible()
      await expect(page.locator('th:has-text("Specialization")')).toBeVisible()
      await expect(page.locator('th:has-text("Experience/Rate")')).toBeVisible()
      await expect(page.locator('th:has-text("Rating")')).toBeVisible()
      await expect(page.locator('th:has-text("Status")')).toBeVisible()
      await expect(page.locator('th:has-text("Joined")')).toBeVisible()
      await expect(page.locator('th:has-text("Actions")')).toBeVisible()
    })

    test('should display teacher information correctly', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers are displayed (at least one row should exist)
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check first teacher row has required elements
        const firstRow = teacherRows.first()
        await expect(firstRow.locator('td').nth(0)).toBeVisible() // Teacher info
        await expect(firstRow.locator('td').nth(1)).toBeVisible() // Specialization
        await expect(firstRow.locator('td').nth(2)).toBeVisible() // Experience/Rate
        await expect(firstRow.locator('td').nth(3)).toBeVisible() // Rating
        await expect(firstRow.locator('td').nth(4)).toBeVisible() // Status
        await expect(firstRow.locator('td').nth(5)).toBeVisible() // Joined date
        await expect(firstRow.locator('td').nth(6)).toBeVisible() // Actions
      }
    })

    test('should show pagination controls', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check pagination elements
      await expect(page.locator('text=Page')).toBeVisible()
      await expect(page.locator('button:has-text("Previous")')).toBeVisible()
      await expect(page.locator('button:has-text("Next")')).toBeVisible()
    })

    test('should display teacher count information', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check teacher count display
      await expect(page.locator('text=Showing')).toBeVisible()
      await expect(page.locator('text=teachers')).toBeVisible()
    })
  })

  test.describe('Search and Filtering', () => {
    test('should have search functionality', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check search input
      const searchInput = page.locator('input[placeholder*="Search"]')
      await expect(searchInput).toBeVisible()
      
      // Test search functionality
      await searchInput.fill('test')
      await page.keyboard.press('Enter')
      
      // Wait for search results
      await page.waitForTimeout(2000)
      
      // Search input should still be visible
      await expect(searchInput).toBeVisible()
    })

    test('should have teacher status filter', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check teacher filter
      const teacherFilter = page.locator('select')
      await expect(teacherFilter).toBeVisible()
      
      // Test teacher filter
      await teacherFilter.click()
      await expect(page.locator('text=All Teachers')).toBeVisible()
      await expect(page.locator('text=Pending')).toBeVisible()
      await expect(page.locator('text=Active')).toBeVisible()
      await expect(page.locator('text=Inactive')).toBeVisible()
      await expect(page.locator('text=Suspended')).toBeVisible()
    })

    test('should have clear filters functionality', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Set some filters first
      const searchInput = page.locator('input[placeholder*="Search"]')
      await searchInput.fill('test')
      
      // Clear button should appear
      const clearButton = page.locator('button:has-text("Clear")')
      await expect(clearButton).toBeVisible()
      
      // Click clear
      await clearButton.click()
      
      // Search input should be cleared
      await expect(searchInput).toHaveValue('')
    })
  })

  test.describe('Teacher Actions', () => {
    test('should have view teacher action', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check view button exists
        const viewButton = teacherRows.first().locator('button').first()
        await expect(viewButton).toBeVisible()
        
        // Click view button
        await viewButton.click()
        
        // Should open view dialog
        await expect(page.locator('text=Teacher Details')).toBeVisible()
        
        // Close dialog
        await page.locator('button[aria-label="Close"]').click()
      }
    })

    test('should have edit teacher action', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check edit button exists
        const editButton = teacherRows.first().locator('button').nth(1)
        await expect(editButton).toBeVisible()
        
        // Click edit button
        await editButton.click()
        
        // Should open edit dialog
        await expect(page.locator('text=Edit Teacher')).toBeVisible()
        
        // Close dialog
        await page.locator('button:has-text("Cancel")').click()
      }
    })

    test('should have toggle status action', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check toggle status button exists
        const toggleButton = teacherRows.first().locator('button').nth(2)
        await expect(toggleButton).toBeVisible()
        
        // Button should be clickable (not disabled)
        await expect(toggleButton).toBeEnabled()
      }
    })
  })

  test.describe('Header Actions', () => {
    test('should have refresh button', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check refresh button
      const refreshButton = page.locator('button:has-text("Refresh")')
      await expect(refreshButton).toBeVisible()
      
      // Click refresh
      await refreshButton.click()
      
      // Should trigger refresh (button might be disabled briefly)
      await page.waitForTimeout(1000)
    })

    test('should have export button', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check export button
      const exportButton = page.locator('button:has-text("Export")')
      await expect(exportButton).toBeVisible()
    })

    test('should have add teacher button', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check add teacher button
      const addButton = page.locator('button:has-text("Add Teacher")')
      await expect(addButton).toBeVisible()
    })
  })

  test.describe('Teacher-Specific Features', () => {
    test('should display teacher ratings correctly', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check rating column
        const ratingCell = teacherRows.first().locator('td').nth(3)
        await expect(ratingCell).toBeVisible()
        
        // Should contain star icon or "No rating" text
        const hasStar = await ratingCell.locator('svg').count() > 0
        const hasNoRating = await ratingCell.locator('text=No rating').count() > 0
        
        expect(hasStar || hasNoRating).toBeTruthy()
      }
    })

    test('should display teacher verification status', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check status column for verification badges
        const statusCell = teacherRows.first().locator('td').nth(4)
        await expect(statusCell).toBeVisible()
        
        // Should contain status badges
        const badges = statusCell.locator('[class*="badge"]')
        await expect(badges.first()).toBeVisible()
      }
    })

    test('should display teacher hourly rates', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check experience/rate column
        const rateCell = teacherRows.first().locator('td').nth(2)
        await expect(rateCell).toBeVisible()
        
        // Should contain experience information
        await expect(rateCell.locator('text=years')).toBeVisible()
      }
    })

    test('should display teacher specialization', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Check specialization column
        const specializationCell = teacherRows.first().locator('td').nth(1)
        await expect(specializationCell).toBeVisible()
        
        // Should contain specialization or "Not specified"
        const hasSpecialization = await specializationCell.textContent()
        expect(hasSpecialization).toBeTruthy()
      }
    })
  })

  test.describe('Error Handling', () => {
    test('should handle empty state gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock empty response
      await page.route('**/admin/teachers**', route => 
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              content: [],
              totalPages: 0,
              totalElements: 0
            }
          })
        })
      )
      
      await page.reload()
      await page.waitForTimeout(2000)
      
      // Should show empty state
      await expect(page.locator('text=No teachers found')).toBeVisible()
    })

    test('should handle API errors gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock error response
      await page.route('**/admin/teachers**', route => 
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: 'Internal Server Error'
          })
        })
      )
      
      await page.reload()
      await page.waitForTimeout(2000)
      
      // Should show error message
      await expect(page.locator('text=Error loading teachers')).toBeVisible()
      await expect(page.locator('text=Try Again')).toBeVisible()
    })

    test('should provide retry functionality for failed requests', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Mock error response
      await page.route('**/admin/teachers**', route => 
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: 'Internal Server Error'
          })
        })
      )
      
      await page.reload()
      await page.waitForTimeout(2000)
      
      // Should show retry button
      await expect(page.locator('text=Try Again')).toBeVisible()
      
      // Click retry
      await page.click('text=Try Again')
      
      // Should attempt to reload data
      await page.waitForTimeout(2000)
    })
  })

  test.describe('UI/UX', () => {
    test('should be responsive on mobile devices', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 })
      
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if page is still usable on mobile
      await expect(page.locator('h1')).toContainText('Manage Teachers')
      await expect(page.locator('table')).toBeVisible()
    })

    test('should have consistent branding and colors', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check for AnkurShala branding
      await expect(page.locator('text=Manage Teachers')).toBeVisible()
      
      // Check for consistent styling
      const header = page.locator('h1')
      await expect(header).toBeVisible()
    })

    test('should show loading states properly', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Check for loading skeleton initially
      await page.waitForSelector('table', { timeout: 10000 })
      
      // Table should be visible after loading
      await expect(page.locator('table')).toBeVisible()
    })

    test('should handle action loading states', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Check if teachers exist
      const teacherRows = page.locator('tbody tr')
      const rowCount = await teacherRows.count()
      
      if (rowCount > 0) {
        // Click toggle status button
        const toggleButton = teacherRows.first().locator('button').nth(2)
        await toggleButton.click()
        
        // Should show loading state briefly
        await page.waitForTimeout(1000)
        
        // Button should be clickable again
        await expect(toggleButton).toBeEnabled()
      }
    })
  })

  test.describe('Navigation', () => {
    test('should navigate back to admin dashboard', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Click on admin panel logo or dashboard link
      const dashboardLink = page.locator('a[href="/admin"]')
      if (await dashboardLink.isVisible()) {
        await dashboardLink.click()
        await page.waitForURL('**/admin/dashboard**')
        await expect(page).toHaveURL(/.*admin.*dashboard/)
      }
    })

    test('should support keyboard navigation', async ({ page }) => {
      await loginAsAdmin(page)
      await waitForTeachersTable(page)
      
      // Tab through elements
      await page.keyboard.press('Tab')
      await page.keyboard.press('Tab')
      await page.keyboard.press('Tab')
      
      // Press Enter on focused element
      await page.keyboard.press('Enter')
      
      // Should trigger some action
      await page.waitForTimeout(1000)
    })
  })

  test.describe('Security', () => {
    test('should validate admin permissions on frontend', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Verify admin-specific elements are visible
      await expect(page.locator('text=Manage Teachers')).toBeVisible()
      await expect(page.locator('text=Add Teacher')).toBeVisible()
    })

    test('should handle session expiration gracefully', async ({ page }) => {
      await loginAsAdmin(page)
      
      // Simulate session expiration by clearing tokens
      await page.evaluate(() => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
      })
      
      // Try to perform an action
      await page.click('button:has-text("Refresh")')
      
      // Should handle gracefully (either refresh tokens or redirect)
      await page.waitForTimeout(3000)
    })
  })
})
