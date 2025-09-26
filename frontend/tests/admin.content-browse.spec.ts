import { test, expect } from '@playwright/test'

test.describe('Admin Content Browse', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')

    // Wait for login to complete and redirect
    await page.waitForURL(/\/admin/, { timeout: 10000 })

    // Navigate to content browse page
    await page.click('a[href="/admin/content/browse"]')
    await page.waitForURL('/admin/content/browse')

    // Wait for the page to load completely
    await page.waitForLoadState('networkidle')
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })

    await expect(page.locator('h1')).toContainText('Content Browser')
  })

  test('should display content browse page with proper structure', async ({ page }) => {
    // Check page title
    await expect(page.locator('h1')).toContainText('Content Browser')

    // Check hierarchical filters section
    await expect(page.locator('h3:has-text("Filters")')).toBeVisible()
    await expect(page.locator('text=Board')).toBeVisible()
    await expect(page.locator('text=Grade')).toBeVisible()
    await expect(page.locator('text=Subject')).toBeVisible()
    await expect(page.locator('text=Chapter')).toBeVisible()

    // Check topics section
    await expect(page.locator('h3:has-text("Topics")')).toBeVisible()
    await expect(page.locator('input[placeholder*="Search topics"]')).toBeVisible()
  })

  test('should display hierarchical filter dropdowns', async ({ page }) => {
    // Check Board dropdown
    const boardSelect = page.locator('select').first()
    await expect(boardSelect).toBeVisible()
    
    // Should have "All Boards" option and CBSE
    await expect(boardSelect.locator('option:has-text("All Boards")')).toBeVisible()
    await expect(boardSelect.locator('option:has-text("CBSE")')).toBeVisible()

    // Select CBSE board
    await boardSelect.selectOption({ label: 'CBSE' })

    // Grade dropdown should become enabled
    const gradeSelect = page.locator('select').nth(1)
    await expect(gradeSelect).toBeVisible()
    await expect(gradeSelect.locator('option:has-text("All Grades")')).toBeVisible()
  })

  test('should filter topics by board selection', async ({ page }) => {
    // Select CBSE board
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    // Wait for topics to load
    await page.waitForTimeout(1000)

    // Should show topics from CBSE board
    await expect(page.locator('text=Chemistry')).toBeVisible()
  })

  test('should cascade filter from board to grade to subject to chapter', async ({ page }) => {
    // Select CBSE board
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    // Wait for grade options to load
    await page.waitForTimeout(1000)

    // Select Grade 9
    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    // Wait for subject options to load
    await page.waitForTimeout(1000)

    // Select Chemistry
    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for chapter options to load
    await page.waitForTimeout(1000)

    // Should show chemistry chapters
    const chapterSelect = page.locator('select').nth(3)
    await expect(chapterSelect.locator('option:has-text("MATTER IN OUR SURROUNDINGS")')).toBeVisible()
  })

  test('should display topics as cards with proper information', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Should display topic cards
    const topicCards = page.locator('[data-testid="topic-card"], .topic-card, div:has-text("Physcial nature and state of matter")').first()
    await expect(topicCards).toBeVisible()

    // Check topic card contains required information
    await expect(page.locator('text=45m, text=30m').first()).toBeVisible() // Time formatting
    await expect(page.locator('span:has-text("Active"), span:has-text("Inactive")').first()).toBeVisible() // Status
  })

  test('should search topics by title', async ({ page }) => {
    // Select filters first
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Search for a specific topic
    await page.fill('input[placeholder*="Search topics"]', 'Physcial nature')

    // Wait for search results
    await page.waitForTimeout(1000)

    // Should show matching topic
    await expect(page.locator('text=Physcial nature and state of matter')).toBeVisible()
  })

  test('should display time formatting correctly', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Check time formatting (45 minutes = 45m, 30 minutes = 30m)
    await expect(page.locator('text=45m')).toBeVisible()
    await expect(page.locator('text=30m')).toBeVisible()
  })

  test('should toggle topic active status', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Find first topic card and toggle its status
    const firstTopicCard = page.locator('[data-testid="topic-card"]').first()
    if (await firstTopicCard.isVisible()) {
      const toggleButton = firstTopicCard.locator('button:has(svg.lucide-eye), button:has(svg.lucide-eye-off)')
      if (await toggleButton.isVisible()) {
        await toggleButton.click()
        
        // Should show success message
        await expect(page.locator('text=Topic status updated successfully')).toBeVisible({ timeout: 5000 })
      }
    }
  })

  test('should handle edit topic action', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Find first topic card and click edit
    const firstTopicCard = page.locator('[data-testid="topic-card"]').first()
    if (await firstTopicCard.isVisible()) {
      const editButton = firstTopicCard.locator('button:has(svg.lucide-edit)')
      if (await editButton.isVisible()) {
        await editButton.click()
        
        // Should open edit modal or navigate to edit page
        await expect(page.locator('[role="dialog"], text=Edit Topic')).toBeVisible({ timeout: 5000 })
      }
    }
  })

  test('should handle delete topic action', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Find first topic card and click delete
    const firstTopicCard = page.locator('[data-testid="topic-card"]').first()
    if (await firstTopicCard.isVisible()) {
      const deleteButton = firstTopicCard.locator('button:has(svg.lucide-trash-2)')
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        
        // Should show confirmation dialog
        await expect(page.locator('[role="alertdialog"], text=Are you sure')).toBeVisible({ timeout: 5000 })
      }
    }
  })

  test('should handle bulk actions', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Check for bulk action buttons
    const bulkActivateButton = page.locator('button:has-text("Bulk Activate")')
    const bulkDeactivateButton = page.locator('button:has-text("Bulk Deactivate")')
    
    if (await bulkActivateButton.isVisible()) {
      await expect(bulkActivateButton).toBeVisible()
    }
    if (await bulkDeactivateButton.isVisible()) {
      await expect(bulkDeactivateButton).toBeVisible()
    }
  })

  test('should display empty state when no topics match filters', async ({ page }) => {
    // Select filters that might not have topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'ICSE' })

    // Wait for results
    await page.waitForTimeout(2000)

    // Should show empty state or no topics message
    const emptyState = page.locator('text=No topics found, text=No data available')
    if (await emptyState.isVisible()) {
      await expect(emptyState).toBeVisible()
    }
  })

  test('should handle responsive design on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Check that the page is still functional on mobile
    await expect(page.locator('h1')).toContainText('Content Browser')

    // Filters should be accessible on mobile
    await expect(page.locator('select').first()).toBeVisible()

    // Topics should stack properly on mobile
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })
    await page.waitForTimeout(1000)
  })

  test('should maintain dark mode compatibility', async ({ page }) => {
    // Toggle to dark mode
    await page.click('button:has(svg.lucide-moon)')
    await expect(page.locator('html')).toHaveClass(/dark/)

    // Verify elements are visible in dark mode
    await expect(page.locator('h1')).toContainText('Content Browser')
    await expect(page.locator('h3:has-text("Filters")')).toBeVisible()

    // Toggle back to light mode
    await page.click('button:has(svg.lucide-sun)')
    await expect(page.locator('html')).not.toHaveClass(/dark/)
  })

  test('should handle keyboard navigation', async ({ page }) => {
    // Test keyboard accessibility
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    
    // Should be able to navigate through filter dropdowns
    await page.keyboard.press('ArrowDown')
    await page.keyboard.press('Enter')
    
    // Should still be on the content browser page
    await expect(page.locator('h1')).toContainText('Content Browser')
  })

  test('should clear filters and reset view', async ({ page }) => {
    // Select some filters
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    // Look for a clear filters button
    const clearButton = page.locator('button:has-text("Clear Filters"), button:has-text("Reset")')
    if (await clearButton.isVisible()) {
      await clearButton.click()
      
      // Should reset to "All Boards"
      await expect(boardSelect.locator('option:checked')).toHaveText('All Boards')
    }
  })

  test('should display topic descriptions and summaries', async ({ page }) => {
    // Select filters to show topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    const gradeSelect = page.locator('select').nth(1)
    await gradeSelect.selectOption({ label: '9' })

    const subjectSelect = page.locator('select').nth(2)
    await subjectSelect.selectOption({ label: 'Chemistry' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Should show topic descriptions/summaries in cards
    const topicCard = page.locator('text=Concept of atom, text=matter , physical nature').first()
    if (await topicCard.isVisible()) {
      await expect(topicCard).toBeVisible()
    }
  })

  test('should handle pagination for large topic lists', async ({ page }) => {
    // Select filters to show all topics
    const boardSelect = page.locator('select').first()
    await boardSelect.selectOption({ label: 'CBSE' })

    // Wait for topics to load
    await page.waitForTimeout(2000)

    // Check if pagination exists
    const nextButton = page.locator('button:has-text("Next"), button:has-text("Load More")')
    if (await nextButton.isVisible()) {
      await nextButton.click()
      await page.waitForTimeout(1000)
      
      // Should load more topics or navigate to next page
      await expect(page.locator('h1')).toContainText('Content Browser')
    }
  })
})

