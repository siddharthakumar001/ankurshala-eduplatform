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
    await expect(page.locator('button[role="tab"]:has-text("Topic Notes")')).toBeVisible()

    // Check search and add button
    await expect(page.locator('input[placeholder="Search..."]')).toBeVisible()
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

    // Check that CBSE board exists (from our CSV import)
    await expect(page.locator('td:has-text("CBSE")')).toBeVisible()
    await expect(page.locator('span:has-text("Active")')).toBeVisible()
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
    await expect(page.locator('th:has-text("Title")')).toBeVisible()
    await expect(page.locator('th:has-text("Chapter")')).toBeVisible()
    await expect(page.locator('th:has-text("Subject")')).toBeVisible()
    await expect(page.locator('th:has-text("Time")')).toBeVisible()
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("Actions")')).toBeVisible()

    // Check that topics from our CSV import exist with proper time formatting
    await expect(page.locator('td:has-text("45m")')).toBeVisible() // 0.75 hours = 45 minutes
    await expect(page.locator('td:has-text("30m")')).toBeVisible() // 0.5 hours = 30 minutes
  })

  test('should create a new board', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Click add button
    await page.click('button:has-text("Add board")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.locator('text=Add board')).toBeVisible()

    // Fill form
    await page.fill('input[id="name"]', 'Test Board')
    
    // Ensure active toggle is checked
    const activeSwitch = page.locator('input[id="active"]')
    await activeSwitch.check()

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message and modal to close
    await expect(page.locator('text=Item created successfully')).toBeVisible()
    await expect(page.locator('[role="dialog"]')).not.toBeVisible()

    // Verify the new board appears in the table
    await expect(page.locator('td:has-text("Test Board")')).toBeVisible()
  })

  test('should create a new subject', async ({ page }) => {
    // Click on subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Click add button
    await page.click('button:has-text("Add subject")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form
    await page.fill('input[id="name"]', 'Test Subject')

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Verify the new subject appears in the table
    await expect(page.locator('td:has-text("Test Subject")')).toBeVisible()
  })

  test('should create a new chapter', async ({ page }) => {
    // Click on chapters tab
    await page.click('button[role="tab"]:has-text("Chapters")')

    // Click add button
    await page.click('button:has-text("Add chapter")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form
    await page.fill('input[id="name"]', 'Test Chapter')
    
    // Select a subject (Chemistry should be available)
    await page.selectOption('select[id="subjectId"]', { label: 'Chemistry' })

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Verify the new chapter appears in the table
    await expect(page.locator('td:has-text("Test Chapter")')).toBeVisible()
  })

  test('should create a new topic with time conversion', async ({ page }) => {
    // Click on topics tab
    await page.click('button[role="tab"]:has-text("Topics")')

    // Click add button
    await page.click('button:has-text("Add topic")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form
    await page.fill('input[id="title"]', 'Test Topic')
    await page.fill('input[id="expectedTimeMins"]', '90') // 1.5 hours in minutes
    await page.fill('textarea[id="description"]', 'Test Description')
    await page.fill('textarea[id="summary"]', 'Test Summary')
    
    // Select a chapter (should have chapters from CSV import)
    const chapterSelect = page.locator('select[id="chapterId"]')
    await chapterSelect.selectOption({ index: 1 }) // Select first available chapter

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Verify the new topic appears in the table with correct time formatting
    await expect(page.locator('td:has-text("Test Topic")')).toBeVisible()
    await expect(page.locator('td:has-text("1h 30m")')).toBeVisible() // 90 minutes = 1h 30m
  })

  test('should edit an existing board', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Wait for data to load
    await page.waitForSelector('table')

    // Click edit button for CBSE board
    await page.click('tr:has-text("CBSE") button:has(svg.lucide-edit)')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await expect(page.locator('text=Edit board')).toBeVisible()

    // Modify the name
    await page.fill('input[id="name"]', 'CBSE Updated')

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message
    await expect(page.locator('text=Item updated successfully')).toBeVisible()

    // Verify the updated name appears
    await expect(page.locator('td:has-text("CBSE Updated")')).toBeVisible()
  })

  test('should toggle active status', async ({ page }) => {
    // Click on boards tab
    await page.click('button[role="tab"]:has-text("Boards")')

    // Wait for data to load
    await page.waitForSelector('table')

    // Click the eye/eye-off toggle button for a board
    await page.click('tr:has-text("CBSE") button:has(svg.lucide-eye-off)')

    // Wait for success message
    await expect(page.locator('text=Item deactivated successfully')).toBeVisible()

    // Verify status changed to Inactive
    await expect(page.locator('tr:has-text("CBSE") span:has-text("Inactive")')).toBeVisible()
  })

  test('should delete an item with confirmation', async ({ page }) => {
    // Create a test board first
    await page.click('button[role="tab"]:has-text("Boards")')
    await page.click('button:has-text("Add board")')
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await page.fill('input[id="name"]', 'Board to Delete')
    await page.click('button:has-text("Save")')
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Now delete it
    await page.click('tr:has-text("Board to Delete") button:has(svg.lucide-trash-2)')

    // Wait for confirmation dialog
    await expect(page.locator('[role="alertdialog"]')).toBeVisible()
    await expect(page.locator('text=Are you sure?')).toBeVisible()

    // Confirm deletion
    await page.click('button:has-text("Delete")')

    // Wait for success message
    await expect(page.locator('text=Item deleted successfully')).toBeVisible()
  })

  test('should search and filter items', async ({ page }) => {
    // Click on subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")')

    // Wait for data to load
    await page.waitForSelector('table')

    // Search for Chemistry
    await page.fill('input[placeholder="Search..."]', 'Chemistry')

    // Wait for search results
    await page.waitForTimeout(1000)

    // Should show Chemistry in results
    await expect(page.locator('td:has-text("Chemistry")')).toBeVisible()

    // Clear search
    await page.fill('input[placeholder="Search..."]', '')
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
    await page.click('button[role="tab"]:has-text("Topic Notes")')

    // Wait for data to load
    await page.waitForSelector('table, text=No data found', { timeout: 10000 })

    // Check table headers if data exists, otherwise check empty state
    if (await page.locator('table').isVisible()) {
      await expect(page.locator('th:has-text("Title")')).toBeVisible()
      await expect(page.locator('th:has-text("Topic")')).toBeVisible()
      await expect(page.locator('th:has-text("Status")')).toBeVisible()
      await expect(page.locator('th:has-text("Actions")')).toBeVisible()
    } else {
      await expect(page.locator('text=No data found')).toBeVisible()
    }
  })

  test('should create a topic note', async ({ page }) => {
    // Click on topic notes tab
    await page.click('button[role="tab"]:has-text("Topic Notes")')

    // Click add button
    await page.click('button:has-text("Add note")')

    // Wait for modal to open
    await expect(page.locator('[role="dialog"]')).toBeVisible()

    // Fill form
    await page.fill('input[id="title"]', 'Test Note')
    await page.fill('textarea[id="content"]', 'This is a test note content with sufficient length to meet the minimum requirements.')
    
    // Select a topic (should have topics from CSV import)
    const topicSelect = page.locator('select[id="topicId"]')
    await topicSelect.selectOption({ index: 1 }) // Select first available topic

    // Save
    await page.click('button:has-text("Save")')

    // Wait for success message
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Verify the new note appears in the table
    await expect(page.locator('td:has-text("Test Note")')).toBeVisible()
  })

  test('should handle force delete option', async ({ page }) => {
    // Create a test board first
    await page.click('button[role="tab"]:has-text("Boards")')
    await page.click('button:has-text("Add board")')
    await expect(page.locator('[role="dialog"]')).toBeVisible()
    await page.fill('input[id="name"]', 'Board for Force Delete')
    await page.click('button:has-text("Save")')
    await expect(page.locator('text=Item created successfully')).toBeVisible()

    // Delete it with force option
    await page.click('tr:has-text("Board for Force Delete") button:has(svg.lucide-trash-2)')

    // Wait for confirmation dialog
    await expect(page.locator('[role="alertdialog"]')).toBeVisible()

    // Enable force delete
    await page.click('input[id="force-delete"]')
    await expect(page.locator('text=Force delete (permanent)')).toBeVisible()

    // Confirm deletion
    await page.click('button:has-text("Permanently Delete")')

    // Wait for success message
    await expect(page.locator('text=Item permanently deleted')).toBeVisible()
  })
})

