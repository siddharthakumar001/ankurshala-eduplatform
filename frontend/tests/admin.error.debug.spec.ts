import { test, expect } from '@playwright/test'

test.describe('Admin Error Debug', () => {
  test('capture console errors on admin page', async ({ page }) => {
    const consoleErrors: string[] = []
    
    // Listen for console errors
    page.on('console', msg => {
      if (msg.type() === 'error') {
        consoleErrors.push(msg.text())
      }
    })
    
    // Navigate to login page
    await page.goto('/login')
    
    // Login as admin
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')
    
    // Wait for redirect to admin profile
    await page.waitForURL('/admin/profile')
    
    // Navigate to admin dashboard
    await page.goto('/admin')
    
    // Wait for page to load
    await page.waitForLoadState('networkidle')
    
    // Log console errors
    console.log('Console errors:', consoleErrors)
    
    // Check if there are any error messages on the page
    const errorText = await page.locator('text=Unhandled Runtime Error').count()
    console.log('Error elements on page:', errorText)
    
    // Check if admin layout is now working
    const asideElements = await page.locator('aside').count()
    const headerElements = await page.locator('header').count()
    console.log('Aside elements:', asideElements)
    console.log('Header elements:', headerElements)
    
    // Check if we can see the dashboard content
    const dashboardTitle = await page.locator('h1').textContent()
    console.log('Dashboard title:', dashboardTitle)
    
    // Take screenshot
    await page.screenshot({ path: 'debug-admin-error.png' })
  })
})
