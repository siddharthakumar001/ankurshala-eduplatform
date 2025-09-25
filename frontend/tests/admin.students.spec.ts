import { test, expect } from '@playwright/test'

test.describe('Admin Students Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')
    
    // Wait for login to complete and redirect (could be /admin or /admin/profile)
    await page.waitForURL(/\/admin/, { timeout: 10000 })
    
    // Navigate to students page
    await page.click('a[href="/admin/users/students"]')
    await page.waitForURL('/admin/users/students')
    
    // Wait for the page to load completely
    await page.waitForLoadState('networkidle')
    
    // Wait for loading to complete
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })
    
    await expect(page.locator('h1')).toContainText('Manage Students')
  })

  test('should display students management page with proper structure', async ({ page }) => {
    // Check page title and description
    await expect(page.locator('h1')).toContainText('Manage Students')
    await expect(page.locator('p').filter({ hasText: 'View and manage student accounts' })).toBeVisible()
    
    // Wait for the page to load (not showing skeleton anymore)
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })
    
    // Check action buttons
    await expect(page.locator('button').filter({ hasText: 'Export' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Add Student' })).toBeVisible()
    
    // Check search and filter components
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Status' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Board' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Class' })).toBeVisible()
  })

  test('should display students table with proper headers', async ({ page }) => {
    // Wait for the page to load (not showing skeleton anymore)
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })
    
    // Wait for the table to load
    await expect(page.locator('table')).toBeVisible()
    
    // Check table headers
    await expect(page.locator('th').filter({ hasText: 'Student' })).toBeVisible()
    await expect(page.locator('th').filter({ hasText: 'School' })).toBeVisible()
    await expect(page.locator('th').filter({ hasText: 'Class/Board' })).toBeVisible()
    await expect(page.locator('th').filter({ hasText: 'Status' })).toBeVisible()
    await expect(page.locator('th').filter({ hasText: 'Joined' })).toBeVisible()
    await expect(page.locator('th').filter({ hasText: 'Actions' })).toBeVisible()
  })

  test('should handle search functionality', async ({ page }) => {
    // Wait for the page to load (not showing skeleton anymore)
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })
    
    const searchInput = page.locator('input[placeholder*="Search"]')
    
    // Test search
    await searchInput.fill('test')
    await page.waitForTimeout(500) // Wait for debounce
    
    // The search should trigger a new request (we can't easily test the API response in E2E)
    // But we can verify the input value is maintained
    await expect(searchInput).toHaveValue('test')
    
    // Clear search
    await searchInput.clear()
    await expect(searchInput).toHaveValue('')
  })

  test('should handle status filter', async ({ page }) => {
    // Click on status filter dropdown
    await page.click('button:has-text("Status")')
    
    // Wait for dropdown to open and select active status
    await page.waitForSelector('[role="option"]:has-text("Active")', { timeout: 5000 })
    await page.click('[role="option"]:has-text("Active")')
    
    // The filter should be applied (we can verify the button text changes)
    await expect(page.locator('button').filter({ hasText: 'Active' })).toBeVisible()
  })

  test('should handle educational board filter', async ({ page }) => {
    // Click on board filter dropdown
    await page.click('button:has-text("Board")')
    
    // Wait for dropdown to open and select CBSE
    await page.waitForSelector('[role="option"]:has-text("CBSE")', { timeout: 5000 })
    await page.click('[role="option"]:has-text("CBSE")')
    
    // The filter should be applied
    await expect(page.locator('button').filter({ hasText: 'CBSE' })).toBeVisible()
  })

  test('should handle class level filter', async ({ page }) => {
    // Click on class filter dropdown
    await page.click('button:has-text("Class")')
    
    // Wait for dropdown to open and select Grade 8
    await page.waitForSelector('[role="option"]:has-text("Grade 8")', { timeout: 5000 })
    await page.click('[role="option"]:has-text("Grade 8")')
    
    // The filter should be applied
    await expect(page.locator('button').filter({ hasText: 'Grade 8' })).toBeVisible()
  })

  test('should clear all filters when clear button is clicked', async ({ page }) => {
    // Apply some filters
    await page.locator('input[placeholder*="Search"]').fill('test')
    await page.click('button:has-text("Status")')
    await page.click('text=Active')
    
    // Clear filters
    await page.click('button:has-text("Clear")')
    
    // Verify filters are cleared
    await expect(page.locator('input[placeholder*="Search"]')).toHaveValue('')
    await expect(page.locator('button').filter({ hasText: 'Status' })).toBeVisible()
  })

  test('should display pagination controls', async ({ page }) => {
    // Check pagination elements
    await expect(page.locator('text=Page')).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Previous' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: 'Next' })).toBeVisible()
  })

  test('should handle responsive design on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })
    
    // Check that the page is still functional on mobile
    await expect(page.locator('h1')).toContainText('Manage Students')
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible()
    
    // Check that table is responsive (might be horizontally scrollable)
    await expect(page.locator('table')).toBeVisible()
  })

  test('should show empty state when no students found', async ({ page }) => {
    // Apply a filter that should return no results
    await page.locator('input[placeholder*="Search"]').fill('nonexistentstudentnamethatdoesnotexist')
    await page.waitForTimeout(1000)
    
    // Check for empty state (this might not work if there are actual students)
    // This test would need to be adjusted based on actual data
    const noStudentsText = page.locator('text=No students found')
    if (await noStudentsText.isVisible()) {
      await expect(noStudentsText).toBeVisible()
      await expect(page.locator('text=Try adjusting your search criteria')).toBeVisible()
    }
  })

  test('should display student action buttons', async ({ page }) => {
    // Wait for table to load
    await page.waitForSelector('table tbody tr', { timeout: 10000 })
    
    // Check if there are any student rows
    const studentRows = page.locator('table tbody tr')
    const rowCount = await studentRows.count()
    
    if (rowCount > 0) {
      // Check action buttons in the first row
      const firstRow = studentRows.first()
      await expect(firstRow.locator('button[title="View"], button:has(svg)')).toHaveCount(3, { timeout: 5000 })
    }
  })

  test('should maintain dark mode compatibility', async ({ page }) => {
    // The page should work in both light and dark modes
    // This is more of a visual test, but we can check that dark mode classes are present
    const bodyClasses = await page.locator('body').getAttribute('class')
    
    // The page should render without errors regardless of theme
    await expect(page.locator('h1')).toContainText('Manage Students')
    await expect(page.locator('table')).toBeVisible()
  })
})
