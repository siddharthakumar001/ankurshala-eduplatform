import { test, expect } from '@playwright/test'

const ADMIN_CREDENTIALS = {
  email: 'siddhartha@ankurshala.com',
  password: 'Maza@123'
}

test.describe('Admin Login and Students Page', () => {
  test.beforeEach(async ({ page }) => {
    // Clear cookies and storage before each test
    await page.context().clearCookies()
    await page.goto('/')
  })

  test('should successfully login as admin and redirect to dashboard', async ({ page }) => {
    console.log('Starting login test...')
    
    // Navigate to login page
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    
    console.log('On login page')

    // Wait for login form to be visible
    await expect(page.locator('input[name="email"]')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('input[name="password"]')).toBeVisible()

    // Fill in login credentials
    console.log('Filling credentials...')
    await page.fill('input[name="email"]', ADMIN_CREDENTIALS.email)
    await page.fill('input[name="password"]', ADMIN_CREDENTIALS.password)

    // Click login button
    console.log('Clicking login button...')
    const loginButton = page.locator('button[type="submit"]').first()
    await loginButton.click()

    // Wait for navigation to complete
    console.log('Waiting for redirect...')
    await page.waitForURL('**/admin/dashboard', { timeout: 15000 })
    
    console.log('Redirected to:', page.url())
    
    // Verify we're on the admin dashboard
    expect(page.url()).toContain('/admin/dashboard')
    
    // Check for dashboard elements
    await expect(page.locator('text=/dashboard|analytics|overview/i').first()).toBeVisible({ timeout: 5000 })
  })

  test('should login and access admin students page successfully', async ({ page }) => {
    console.log('Starting admin students page test...')
    
    // Login first
    await page.goto('/login')
    await page.waitForLoadState('networkidle')
    
    await page.fill('input[name="email"]', ADMIN_CREDENTIALS.email)
    await page.fill('input[name="password"]', ADMIN_CREDENTIALS.password)
    await page.locator('button[type="submit"]').first().click()
    
    await page.waitForURL('**/admin/**', { timeout: 15000 })
    console.log('Logged in successfully')

    // Navigate to students page
    console.log('Navigating to students page...')
    await page.goto('/admin/users/students')
    await page.waitForLoadState('networkidle')
    
    console.log('On students page:', page.url())

    // Wait for the page to load - look for key elements
    await page.waitForTimeout(3000) // Give React time to hydrate
    
    // Check for students page elements (should have table or empty state)
    const hasTable = await page.locator('table').isVisible().catch(() => false)
    const hasEmptyState = await page.locator('text=/no students found/i').isVisible().catch(() => false)
    const hasLoadingState = await page.locator('text=/loading/i').isVisible().catch(() => false)
    
    console.log('Page state:', { hasTable, hasEmptyState, hasLoadingState })
    
    // Should have either table, empty state, or be loading
    expect(hasTable || hasEmptyState || hasLoadingState).toBeTruthy()

    // Check for search and filter controls
    const hasSearchBox = await page.locator('input[placeholder*="Search"]').isVisible().catch(() => false)
    console.log('Has search box:', hasSearchBox)
    
    // Verify no authentication errors
    const hasAuthError = await page.locator('text=/unauthorized|401|not authorized/i').isVisible().catch(() => false)
    expect(hasAuthError).toBeFalsy()
  })

  test('should maintain authentication after page reload', async ({ page }) => {
    console.log('Testing authentication persistence...')
    
    // Login first
    await page.goto('/login')
    await page.fill('input[name="email"]', ADMIN_CREDENTIALS.email)
    await page.fill('input[name="password"]', ADMIN_CREDENTIALS.password)
    await page.locator('button[type="submit"]').first().click()
    
    await page.waitForURL('**/admin/**', { timeout: 15000 })
    console.log('Initial login successful')

    // Reload the page
    console.log('Reloading page...')
    await page.reload()
    await page.waitForLoadState('networkidle')
    
    // Should still be on admin dashboard, not redirected to login
    expect(page.url()).toContain('/admin')
    expect(page.url()).not.toContain('/login')
    
    console.log('Authentication persisted after reload')
  })

  test('should handle API calls to /api/auth/refresh', async ({ page }) => {
    console.log('Testing token refresh...')
    
    // Set up response listener
    const responses: any[] = []
    page.on('response', response => {
      if (response.url().includes('/api/auth/refresh')) {
        responses.push({
          url: response.url(),
          status: response.status(),
          statusText: response.statusText()
        })
        console.log('Refresh API called:', response.status(), response.url())
      }
    })

    // Login
    await page.goto('/login')
    await page.fill('input[name="email"]', ADMIN_CREDENTIALS.email)
    await page.fill('input[name="password"]', ADMIN_CREDENTIALS.password)
    await page.locator('button[type="submit"]').first().click()
    
    await page.waitForURL('**/admin/**', { timeout: 15000 })
    
    // Navigate to students page
    await page.goto('/admin/users/students')
    await page.waitForLoadState('networkidle')
    
    // Wait a bit for any refresh calls
    await page.waitForTimeout(2000)
    
    // Check if refresh was called and it didn't return 401
    const refreshCalls = responses.filter(r => r.url.includes('/api/auth/refresh'))
    console.log('Refresh calls:', refreshCalls)
    
    // If refresh was called, it should not return 401
    refreshCalls.forEach(call => {
      expect(call.status).not.toBe(401)
    })
  })

  test('should redirect unauthenticated users to login', async ({ page }) => {
    console.log('Testing unauthenticated access...')
    
    // Try to access admin page without logging in
    await page.goto('/admin/users/students')
    
    // Should be redirected to login
    await page.waitForURL('**/login**', { timeout: 10000 })
    expect(page.url()).toContain('/login')
    
    console.log('Unauthenticated user redirected to login')
  })
})
