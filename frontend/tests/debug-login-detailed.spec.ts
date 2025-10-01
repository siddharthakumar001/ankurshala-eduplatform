import { test, expect } from '@playwright/test'

test.describe('Debug Login Flow Detailed', () => {
  test('should debug login process with detailed logging', async ({ page }) => {
    // Listen to console logs
    page.on('console', msg => {
      console.log(`PAGE LOG: ${msg.text()}`)
    })

    // Listen to network requests
    const requests: any[] = []
    const responses: any[] = []
    
    page.on('request', request => {
      requests.push({
        url: request.url(),
        method: request.method(),
        headers: request.headers()
      })
      console.log(`REQUEST: ${request.method()} ${request.url()}`)
    })
    
    page.on('response', response => {
      responses.push({
        url: response.url(),
        status: response.status(),
        statusText: response.statusText()
      })
      console.log(`RESPONSE: ${response.status()} ${response.url()}`)
    })

    // Navigate to login page
    await page.goto('/login')
    console.log('✅ Successfully navigated to login page')

    // Check if form elements are visible
    await expect(page.locator('input[placeholder="Enter your email"]')).toBeVisible()
    await expect(page.locator('input[placeholder="Enter your password"]')).toBeVisible()
    await expect(page.locator('button[type="submit"]')).toBeVisible()
    console.log('✅ Form elements are visible')

    // Fill credentials
    await page.fill('input[placeholder="Enter your email"]', 'student1@ankurshala.com')
    await page.fill('input[placeholder="Enter your password"]', 'Maza@123')
    console.log('✅ Credentials filled')

    // Click submit and wait for navigation
    await Promise.all([
      page.waitForURL('**/student/profile**'),
      page.click('button[type="submit"]')
    ])
    console.log('✅ Submit button clicked and navigation completed')

    // Wait a bit for any redirects
    await page.waitForTimeout(2000)

    // Check current URL
    const currentUrl = page.url()
    console.log(`Current URL after submit: ${currentUrl}`)

    // Check if there are any error messages
    const errorElement = page.locator('[class*="error"], [class*="Error"], .text-red-600, .text-red-400')
    if (await errorElement.count() > 0) {
      const errorText = await errorElement.first().textContent()
      console.log(`ERROR MESSAGE: ${errorText}`)
    }

    // Log all requests and responses
    console.log('REQUESTS:', JSON.stringify(requests, null, 2))
    console.log('RESPONSES:', JSON.stringify(responses, null, 2))

    // For now, just verify we can see the form
    expect(currentUrl).toContain('/student/profile')
  })
})
