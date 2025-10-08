/**
 * Simple Login Test
 * 
 * Purpose: Test just the login functionality without redirect checks
 * This will help us verify that the login is working correctly
 */

import { test, expect, Page } from '@playwright/test'

test.describe('Simple Login Test', () => {
  test('should login successfully as admin', async ({ page }) => {
    await page.goto('/login')
    
    // Fill in login form
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[name="password"]', 'Maza@123')
    
    // Click submit
    await page.click('button[type="submit"]')
    
    // Wait for the API call to complete
    await page.waitForResponse(response => 
      response.url().includes('/api/auth/signin') && response.status() === 200
    )
    
    // Check that we get a successful response
    const response = await page.waitForResponse(response => 
      response.url().includes('/api/auth/signin')
    )
    
    expect(response.status()).toBe(200)
    
    const responseData = await response.json()
    console.log('Login response:', responseData)
    
    // Verify the response contains user data
    expect(responseData.success).toBe(true)
    expect(responseData.data).toBeDefined()
    expect(responseData.data.email).toBe('siddhartha@ankurshala.com')
    expect(responseData.data.role).toBe('ADMIN')
    
    console.log('✅ Login test passed!')
  })
})
