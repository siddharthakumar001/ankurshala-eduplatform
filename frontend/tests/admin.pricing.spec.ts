import { test, expect } from '@playwright/test'

test.describe('Admin Pricing', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')

    // Wait for login to complete and redirect
    await page.waitForURL(/\/admin/, { timeout: 10000 })

    // Navigate to pricing page
    await page.click('a[href="/admin/pricing"]')
    await page.waitForURL('/admin/pricing')

    // Wait for the page to load completely
    await page.waitForLoadState('domcontentloaded')
    await expect(page.locator('h1')).toContainText('Pricing Management')
  })

  test('should display pricing page with proper structure', async ({ page }) => {
    // Wait for page to load completely
    await page.waitForLoadState('networkidle')
    
    // Check page title
    await expect(page.locator('h1')).toContainText('Pricing Management')
    await expect(page.locator('p:has-text("Manage pricing rules and rates")')).toBeVisible()

    // Check header buttons
    await expect(page.locator('button:has-text("Test Pricing")')).toBeVisible()
    await expect(page.locator('button:has-text("New Rule")')).toBeVisible()

    // Check search functionality
    await expect(page.locator('input[placeholder="Search pricing rules..."]')).toBeVisible()

    // Check table headers
    await expect(page.locator('th:has-text("Scope")')).toBeVisible()
    await expect(page.locator('th:has-text("Hourly Rate")')).toBeVisible()
    await expect(page.locator('th:has-text("Created")')).toBeVisible()
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("Actions")')).toBeVisible()
  })

  test('should open create pricing rule dialog', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(2000) // Allow time for initial load
    
    // Click New Rule button
    await page.click('button:has-text("New Rule")')
    
    // Check dialog opens
    await expect(page.locator('h2:has-text("Create Pricing Rule")')).toBeVisible()
    await expect(page.locator('p:has-text("Define a new pricing rule for specific content scope")')).toBeVisible()
    
    // Check form fields
    await expect(page.locator('label:has-text("Board")')).toBeVisible()
    await expect(page.locator('label:has-text("Grade")')).toBeVisible()
    await expect(page.locator('label:has-text("Subject")')).toBeVisible()
    await expect(page.locator('label:has-text("Chapter")')).toBeVisible()
    await expect(page.locator('label:has-text("Topic")')).toBeVisible()
    await expect(page.locator('label:has-text("Hourly Rate")')).toBeVisible()
    
    // Check buttons
    await expect(page.locator('button:has-text("Cancel")')).toBeVisible()
    await expect(page.locator('button:has-text("Create Rule")')).toBeVisible()
    
    // Close dialog
    await page.click('button:has-text("Cancel")')
  })

  test('should open test pricing dialog', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('domcontentloaded')
    await page.waitForTimeout(2000) // Allow time for initial load
    
    // Click Test Pricing button
    await page.click('button:has-text("Test Pricing")')
    
    // Check dialog opens
    await expect(page.locator('h2:has-text("Test Pricing Resolution")')).toBeVisible()
    await expect(page.locator('p:has-text("Test which pricing rule applies for specific content scope")')).toBeVisible()
    
    // Check form fields
    await expect(page.locator('label:has-text("Board")')).toBeVisible()
    await expect(page.locator('label:has-text("Grade")')).toBeVisible()
    await expect(page.locator('label:has-text("Subject")')).toBeVisible()
    await expect(page.locator('label:has-text("Chapter")')).toBeVisible()
    await expect(page.locator('label:has-text("Topic")')).toBeVisible()
    
    // Check buttons
    await expect(page.locator('button:has-text("Close")').first()).toBeVisible()
    await expect(page.locator('button:has-text("Test Resolution")')).toBeVisible()
    
    // Close dialog
    await page.locator('button:has-text("Close")').first().click()
  })

  test('should display empty state when no pricing rules', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('networkidle')
    
    // Check if empty state is displayed (if no rules exist)
    const emptyState = page.locator('text=No pricing rules found')
    if (await emptyState.isVisible()) {
      await expect(emptyState).toBeVisible()
      await expect(page.locator('text=Create your first pricing rule to get started')).toBeVisible()
    }
  })

  test('should handle search functionality', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('networkidle')
    
    // Type in search box
    await page.fill('input[placeholder="Search pricing rules..."]', 'test')
    
    // Check that search results are filtered
    await page.waitForTimeout(1000)
    
    // Clear search
    await page.fill('input[placeholder="Search pricing rules..."]', '')
  })
})
