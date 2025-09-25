import { test, expect } from '@playwright/test'

test.describe('Admin Debug', () => {
  test('debug admin page rendering', async ({ page }) => {
    // Navigate to login page
    await page.goto('/login')
    
    // Login as admin
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')
    
    // Wait for redirect to admin dashboard (not profile)
    await page.waitForURL('/admin')
    
    // Check what's on the admin dashboard page
    console.log('Admin dashboard page loaded')
    const dashboardContent = await page.content()
    console.log('Dashboard page has aside:', await page.locator('aside').count())
    console.log('Dashboard page has header:', await page.locator('header').count())
    
    // Navigate to admin dashboard
    await page.goto('/admin')
    
    // Wait for page to load
    await page.waitForLoadState('networkidle')
    
    // Take screenshot
    await page.screenshot({ path: 'debug-admin-page.png' })
    
    // Log the page content
    const content = await page.content()
    console.log('Page content:', content.substring(0, 1000))
    
    // Check what elements are present
    const asideElements = await page.locator('aside').count()
    const headerElements = await page.locator('header').count()
    const h1Elements = await page.locator('h1').count()
    
    console.log(`Aside elements: ${asideElements}`)
    console.log(`Header elements: ${headerElements}`)
    console.log(`H1 elements: ${h1Elements}`)
    
    // Check if AdminLayout is rendered
    const adminLayoutElements = await page.locator('[data-testid="admin-layout"]').count()
    console.log(`AdminLayout elements: ${adminLayoutElements}`)
    
    // Check if there are any error messages
    const errorElements = await page.locator('[data-testid="error"]').count()
    console.log(`Error elements: ${errorElements}`)
    
    // Check the current URL
    console.log('Current URL:', page.url())
    
    // Check if we're redirected
    if (page.url().includes('/login')) {
      console.log('Redirected to login page')
    }
  })
})
