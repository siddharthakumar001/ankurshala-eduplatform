import { test, expect } from '@playwright/test'

test('Quick login validation', async ({ page }) => {
  // Go to login page
  await page.goto('http://localhost:3000/login')
  
  // Wait for page to be ready
  await page.waitForSelector('input[name="email"]', { timeout: 10000 })
  
  console.log('✅ Login page loaded')
  
  // Fill credentials
  await page.fill('input[name="email"]', 'siddhartha@ankurshala.com')
  await page.fill('input[name="password"]', 'Maza@123')
  
  console.log('✅ Credentials filled')
  
  // Click login
  await page.click('button[type="submit"]')
  
  console.log('✅ Login button clicked, waiting for redirect...')
  
  // Wait for redirect (increased timeout)
  await page.waitForURL('**/admin/**', { timeout: 20000 })
  
  console.log('✅ Redirected to:', page.url())
  
  // Verify we're on an admin page
  expect(page.url()).toContain('/admin')
  
  console.log('✅✅✅ LOGIN TEST PASSED! ✅✅✅')
})
