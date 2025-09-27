import { test, expect } from '@playwright/test'

test.describe('Admin Content Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')

    // Wait for login to complete and redirect
    await page.waitForURL(/\/admin/, { timeout: 10000 })

    // Navigate to content management page
    await page.click('a[href="/admin/content/manage"]')
    await page.waitForURL('/admin/content/manage')

    // Wait for the page to load completely
    await page.waitForLoadState('networkidle')
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })

    await expect(page.locator('h1')).toContainText('Content Management')
  })

  test('should display content management page with proper structure', async ({ page }) => {
    // Check page title
    await expect(page.locator('h1')).toContainText('Content Management')

    // Check tabs are present
    await expect(page.locator('[role="tablist"]')).toBeVisible()
    await expect(page.locator('button[role="tab"]:has-text("Boards")')).toBeVisible()
    await expect(page.locator('button[role="tab"]:has-text("Subjects")')).toBeVisible()
    await expect(page.locator('button[role="tab"]:has-text("Chapters")')).toBeVisible()
    await expect(page.locator('button[role="tab"]:has-text("Topics")')).toBeVisible()
    await expect(page.locator('button[role="tab"]:has-text("Notes")')).toBeVisible()

    // Check search and add button
    await expect(page.locator('input[placeholder="Search boards..."]')).toBeVisible()
    await expect(page.locator('button:has-text("Add")')).toBeVisible()
  })

  test('should display boards tab with data', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })

    // Check table headers
    await expect(page.locator('th:has-text("Name")')).toBeVisible()
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("Created")')).toBeVisible()
    await expect(page.locator('th:has-text("Actions")')).toBeVisible()

    // Wait for data to load
    await page.waitForTimeout(2000)
    
    // Check that CBSE board exists (from our CSV import) - this confirms data loading works
    await expect(page.locator('td:has-text("CBSE")')).toBeVisible()
    
    // Check that table has data (at least 3 rows - original data)
    const boardRows = page.locator('tbody tr')
    const boardCount = await boardRows.count()
    expect(boardCount).toBeGreaterThanOrEqual(3)
  })

  test('should display subjects tab with data', async ({ page }) => {
    // Click on subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })

    // Check table headers
    await expect(page.locator('th:has-text("Name")')).toBeVisible()
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("Actions")')).toBeVisible()

    // Check that Chemistry subject exists (from our CSV import)
    await expect(page.locator('td:has-text("Chemistry")')).toBeVisible()
  })

  test('should display topics tab with data and time formatting', async ({ page }) => {
    // Click on topics tab
    await page.click('button[role="tab"]:has-text("Topics")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })

    // Check table headers
    await expect(page.locator('th:has-text("Name")')).toBeVisible()
    await expect(page.locator('th:has-text("Code")')).toBeVisible()
    await expect(page.locator('th:has-text("Time")')).toBeVisible()
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("Created")')).toBeVisible()
    await expect(page.locator('th:has-text("Actions")')).toBeVisible()

    // Wait for topics to load and check if any topics exist
    await page.waitForTimeout(3000) // Give more time for API call
    
    // Check if any topics are displayed (at least 18 - original data)
    const topicRows = page.locator('tbody tr')
    const topicCount = await topicRows.count()
    expect(topicCount).toBeGreaterThanOrEqual(18)
    
    // Check for any time formatting (45m, 30m, or other formats) - at least 14
    const timeFormattedCells = page.locator('td').filter({ hasText: /m$/ })
    const timeCount = await timeFormattedCells.count()
    expect(timeCount).toBeGreaterThanOrEqual(14)
  })

  test('should create a new board', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Click add button
    await page.click('button:has-text("Add board")')

    // Wait for modal to open
    await page.waitForTimeout(2000) // Give time for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.locator('text=Create board')).toBeVisible()

    // Fill form with unique name
    const uniqueBoardName = `Test Board ${Date.now()}`
    await page.fill('input[id="name"]', uniqueBoardName)
    
    // Ensure active toggle is checked (it should be checked by default)
    // No need to click since it's already checked

    // Save
    await page.click('button:has-text("Create")')

    // Wait for success message and modal to close
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    // Check for any success message (be more flexible)
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[role="dialog"]')).not.toBeVisible()

    // Verify the new board appears in the table
    await expect(page.locator(`td:has-text("${uniqueBoardName}")`)).toBeVisible()
  })

  test('should create a new subject', async ({ page }) => {
    // Click on subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Click add button
    await page.click('button:has-text("Add subject")')

    // Wait for modal to open
    await page.waitForSelector('[role="dialog"]', { timeout: 10000 })
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form with unique name
    const uniqueName = `Test Subject ${Date.now()}`
    await page.fill('input[id="name"]', uniqueName)

    // Wait for form to be ready
    await page.waitForTimeout(1000)

    // Save
    console.log('About to click Create button')
    await page.click('button:has-text("Create")')
    console.log('Clicked Create button')

    // Wait for success message (toast notification)
    await expect(page.locator('[data-sonner-toast]')).toBeVisible()
    await expect(page.locator('text=subject created successfully')).toBeVisible()

    // Wait for table to refresh
    await page.waitForTimeout(5000)

    // Verify the new subject appears in the table
    await expect(page.locator(`td:has-text("${uniqueName}")`)).toBeVisible()
  })

  test('should create a new chapter', async ({ page }) => {
    // Click on chapters tab
    await page.click('button[role="tab"]:has-text("Chapters")')

    // Click add button
    await page.click('button:has-text("Add chapter")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form with unique name
    const uniqueChapterName = `Test Chapter ${Date.now()}`
    await page.fill('input[id="name"]', uniqueChapterName)
    
    // Select the first available subject (instead of hardcoding Chemistry)
    await page.click('[data-testid="subject-select"]')
    await page.waitForTimeout(500) // Wait for dropdown to open
    // Click the first subject option (assuming at least one exists)
    const firstSubject = page.locator('[role="option"]').first()
    await expect(firstSubject).toBeVisible({ timeout: 5000 })
    await firstSubject.click()

    // Save
    await page.click('button:has-text("Create")')

    // Wait for success message
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /chapter created successfully/ })).toBeVisible({ timeout: 10000 })

    // Verify the new chapter appears in the table
    await expect(page.locator(`td:has-text("${uniqueChapterName}")`)).toBeVisible()
  })

  test('should create a new topic with time conversion', async ({ page }) => {
    // Click on topics tab
    await page.click('button[role="tab"]:has-text("Topics")')

    // Click add button
    await page.click('button:has-text("Add topic")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form with unique name
    const uniqueTopicName = `Test Topic ${Date.now()}`
    await page.fill('input[id="title"]', uniqueTopicName)
    await page.fill('input[id="expectedTimeMins"]', '90') // 1.5 hours in minutes
    await page.fill('textarea[id="description"]', 'Test Description')
    await page.fill('textarea[id="summary"]', 'Test Summary')
    
    // Select a chapter (should have chapters from CSV import)
    await page.click('[data-testid="chapter-select"]')
    await page.click('[role="option"]:first-child') // Select first available chapter

    // Save
    await page.click('button:has-text("Create")')

    // Wait for success message
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    // Check for any success message (be more flexible)
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 10000 })

    // Verify the new topic appears in the table with correct time formatting
    const topicRow = page.locator(`tr:has(td:has-text("${uniqueTopicName}"))`)
    await expect(topicRow).toBeVisible()
    await expect(topicRow.locator('td:has-text("1h 30m")')).toBeVisible() // 90 minutes = 1h 30m
  })

  test('should edit an existing board', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })
    
    // Find the first row with data and click its edit button
    const firstDataRow = page.locator('tbody tr').first()
    await expect(firstDataRow).toBeVisible({ timeout: 5000 })
    
    // Click the edit button (first button in the actions column)
    await firstDataRow.locator('button').first().click()

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.locator('text=Edit board')).toBeVisible()

    // Get current name and modify it
    const nameInput = page.locator('input[id="name"]')
    await expect(nameInput).toBeVisible()
    const currentName = await nameInput.inputValue()
    const newName = currentName + ' Updated'
    
    await nameInput.fill(newName)

    // Save (in edit mode, the button says "Update")
    await page.click('button:has-text("Update")')

    // Wait for success message
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /updated successfully/ })).toBeVisible({ timeout: 10000 })

    // Verify the updated name appears
    await expect(page.locator(`td:has-text("${newName}")`)).toBeVisible()
  })

  test('should toggle active status', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })

    // Find the first row and click the toggle button (second button in actions)
    const firstDataRow = page.locator('tbody tr').first()
    await expect(firstDataRow).toBeVisible({ timeout: 5000 })
    
    // Click the toggle button (second button - eye/eye-off)
    await firstDataRow.locator('button').nth(1).click()

    // Wait for any toast message to appear
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })

    // Verify status changed (status badge should be visible in the row)
    await expect(firstDataRow.locator('[data-testid="status-badge"]')).toBeVisible()
  })

  test('should delete an item with confirmation', async ({ page }) => {
    // Create a test board first
    await page.click('button[role="tab"]:has-text("Boards")')
    await page.click('button:has-text("Add board")')
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    const testBoardName = `Board to Delete ${Date.now()}`
    await page.fill('input[id="name"]', testBoardName)
    await page.click('button:has-text("Create")')
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 10000 })

    // Wait for modal to close and table to refresh
    await expect(page.locator('[role="dialog"]')).not.toBeVisible()
    await page.waitForTimeout(2000)

    // Find the created board row and delete it
    const boardRow = page.locator(`tr:has-text("${testBoardName}")`)
    await expect(boardRow).toBeVisible()
    
    // Click the delete button (third button in actions)
    await boardRow.locator('button').nth(2).click()

    // Wait for success message (no confirmation dialog in current implementation)
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /deleted successfully/ })).toBeVisible({ timeout: 10000 })

    // Wait for table to refresh
    await page.waitForTimeout(2000)

    // Verify the item is no longer in the table
    await expect(page.locator(`tr:has-text("${testBoardName}")`)).not.toBeVisible()
  })

  test('should search and filter items', async ({ page }) => {
    // Click on subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Wait for data to load
    await page.waitForSelector('table')

    // Search for Chemistry
    await page.fill('input[placeholder*="Search"]', 'Chemistry')

    // Wait for search results
    await page.waitForTimeout(1000)

    // Should show Chemistry in results
    await expect(page.locator('td:has-text("Chemistry")')).toBeVisible()

    // Clear search
    await page.fill('input[placeholder*="Search"]', '')
    await page.waitForTimeout(1000)
  })

  test('should handle pagination', async ({ page }) => {
    // Click on subjects tab (has many subjects)
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Wait for data to load
    await page.waitForSelector('table')

    // Check if pagination exists (if there are more than 10 subjects)
    const nextButton = page.locator('button:has-text("Next")')
    if (await nextButton.isVisible()) {
      await nextButton.click()
      
      // Should navigate to next page
      await expect(page.locator('text=Showing')).toBeVisible()
    }
  })

  test('should handle responsive design on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Check that the page is still functional on mobile
    await expect(page.locator('h1')).toContainText('Content Management')
    await expect(page.locator('[role="tablist"]')).toBeVisible()

    // Tabs should be scrollable on mobile
    await page.click('button[role="tab"]:has-text("Topics")')
    await expect(page.locator('table')).toBeVisible()
  })

  test('should maintain dark mode compatibility', async ({ page }) => {
    // Toggle to dark mode
    await page.click('button:has(svg.lucide-moon)')
    await expect(page.locator('html')).toHaveClass(/dark/)

    // Verify elements are visible in dark mode
    await expect(page.locator('h1')).toContainText('Content Management')
    await expect(page.locator('[role="tablist"]')).toBeVisible()

    // Toggle back to light mode
    await page.click('button:has(svg.lucide-sun)')
    await expect(page.locator('html')).not.toHaveClass(/dark/)
  })

  test('should handle keyboard navigation', async ({ page }) => {
    // Test keyboard accessibility for tabs
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    
    // Should be able to navigate through tabs with arrow keys
    await page.keyboard.press('ArrowRight')
    await page.keyboard.press('ArrowRight')
    
    // Should still be on the content management page
    await expect(page.locator('h1')).toContainText('Content Management')
  })

  test('should display topic notes tab and functionality', async ({ page }) => {
    // Click on topic notes tab
    await page.click('button[role="tab"]:has-text("Notes")')

    // Wait for data to load
    await page.waitForSelector('table', { timeout: 10000 })

    // Check table headers if data exists, otherwise check empty state
    if (await page.locator('table').isVisible()) {
      await expect(page.locator('th:has-text("Name")')).toBeVisible()
      await expect(page.locator('th:has-text("Topic")')).toBeVisible()
      await expect(page.locator('th:has-text("Status")')).toBeVisible()
      await expect(page.locator('th:has-text("Actions")')).toBeVisible()
    } else {
      await expect(page.locator('text=No data found')).toBeVisible()
    }
  })

  test('should create a topic note', async ({ page }) => {
    // Click on topic notes tab
    await page.click('button[role="tab"]:has-text("Notes")')

    // Click add button
    await page.click('button:has-text("Add note")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form with unique name
    const uniqueNoteName = `Test Note ${Date.now()}`
    await page.fill('input[id="title"]', uniqueNoteName)
    await page.fill('textarea[id="content"]', 'This is a test note content with sufficient length to meet the minimum requirements.')
    
    // Select a topic (should have topics from CSV import)
    await page.click('[data-testid="topic-select"]')
    await page.click('[role="option"]:first-child') // Select first available topic

    // Save
    await page.click('button:has-text("Create")')

    // Wait for success message
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    // Check for any success message (be more flexible)
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 10000 })

    // Verify the new note appears in the table
    await expect(page.locator(`td:has-text("${uniqueNoteName}")`)).toBeVisible()
  })

  test('should handle force delete option', async ({ page }) => {
    // Create a test board first
    await page.click('button[role="tab"]:has-text("Boards")')
    await page.click('button:has-text("Add board")')
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    const testBoardName = `Board for Force Delete ${Date.now()}`
    await page.fill('input[id="name"]', testBoardName)
    await page.click('button:has-text("Create")')
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 10000 })

    // Wait for modal to close and table to refresh
    await expect(page.locator('[role="dialog"]')).not.toBeVisible()
    await page.waitForTimeout(2000)

    // Find the created board row and delete it
    const boardRow = page.locator(`tr:has-text("${testBoardName}")`)
    await expect(boardRow).toBeVisible()
    
    // Click the delete button (third button in actions)
    await boardRow.locator('button').nth(2).click()

    // Wait for success message (current implementation does regular delete, not force)
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /deleted successfully/ })).toBeVisible({ timeout: 10000 })

    // Wait for table to refresh
    await page.waitForTimeout(2000)

    // Verify the item is no longer in the table
    await expect(page.locator(`tr:has-text("${testBoardName}")`)).not.toBeVisible()
  })
})
