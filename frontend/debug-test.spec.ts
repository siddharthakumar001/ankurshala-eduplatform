import { test, expect } from '@playwright/test'

test.describe('Debug Test', () => {
  test('debug login and navigation', async ({ page }) => {
    // Go to login page
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    
    // Check if login page loaded
    await expect(page.locator('input[type="email"]')).toBeVisible()
    
    // Fill login form
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    
    // Submit form
    await page.click('button[type="submit"]')
    
    // Wait for redirect
    await page.waitForURL(/\/admin/, { timeout: 10000 })
    
    // Check if we're on admin dashboard
    console.log('Current URL:', page.url())
    
    // Try to navigate to content import
    await page.goto('/admin/content/import')
    await page.waitForLoadState('networkidle')
    
    console.log('Content import URL:', page.url())
    
    // Check if h1 exists
    const h1Elements = await page.locator('h1').count()
    console.log('Number of h1 elements:', h1Elements)
    
    if (h1Elements > 0) {
      const h1Text = await page.locator('h1').first().textContent()
      console.log('First h1 text:', h1Text)
    }
    
    // Take a screenshot
    await page.screenshot({ path: 'debug-screenshot.png' })
  })
})
