import { test, expect } from '@playwright/test'

test.describe('Admin Route Debug', () => {
  test('check admin route protection', async ({ page }) => {
    // Navigate to login page
    await page.goto('/login')
    
    // Login as admin
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')
    
    // Wait for redirect to admin profile
    await page.waitForURL('/admin/profile')
    
    // Check if we can access admin profile (should work)
    console.log('Admin profile URL:', page.url())
    const profileTitle = await page.locator('h1').textContent()
    console.log('Profile title:', profileTitle)
    
    // Navigate to admin dashboard
    await page.goto('/admin')
    
    // Check if we can access admin dashboard
    console.log('Admin dashboard URL:', page.url())
    const dashboardTitle = await page.locator('h1').textContent()
    console.log('Dashboard title:', dashboardTitle)
    
    // Check if we're redirected to login
    if (page.url().includes('/login')) {
      console.log('Redirected to login - AdminRoute is blocking access')
    } else {
      console.log('AdminRoute is allowing access')
    }
  })
})
