import { test, expect, Page } from '@playwright/test';

// Test data
const TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  }
};

test.describe('Debug Login Flow', () => {
  test('should debug login process step by step', async ({ page }) => {
    // Enable console logging
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', error => console.log('PAGE ERROR:', error.message));
    
    // Navigate to login page
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    
    // Check if we're on the login page
    await expect(page).toHaveURL(/.*\/login/);
    console.log('✅ Successfully navigated to login page');
    
    // Check if form elements exist
    const emailInput = page.locator('[name="email"]');
    const passwordInput = page.locator('[name="password"]');
    const submitButton = page.locator('button[type="submit"]');
    
    await expect(emailInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
    await expect(submitButton).toBeVisible();
    console.log('✅ Form elements are visible');
    
    // Fill in credentials
    await emailInput.fill(TEST_USERS.admin.email);
    await passwordInput.fill(TEST_USERS.admin.password);
    console.log('✅ Credentials filled');
    
    // Intercept network requests to see what's happening
    const requests: any[] = [];
    const responses: any[] = [];
    
    page.on('request', request => {
      if (request.url().includes('/api/')) {
        requests.push({
          url: request.url(),
          method: request.method(),
          headers: request.headers()
        });
        console.log('REQUEST:', request.method(), request.url());
      }
    });
    
    page.on('response', response => {
      if (response.url().includes('/api/')) {
        responses.push({
          url: response.url(),
          status: response.status(),
          statusText: response.statusText()
        });
        console.log('RESPONSE:', response.status(), response.url());
      }
    });
    
    // Click submit button
    await submitButton.click();
    console.log('✅ Submit button clicked');
    
    // Wait a bit to see what happens
    await page.waitForTimeout(3000);
    
    // Check current URL
    const currentUrl = page.url();
    console.log('Current URL after submit:', currentUrl);
    
    // Check for any error messages
    const errorElement = page.locator('[class*="error"], [class*="red"]');
    if (await errorElement.count() > 0) {
      const errorText = await errorElement.textContent();
      console.log('ERROR MESSAGE:', errorText);
    }
    
    // Check for any success indicators
    const successElement = page.locator('[class*="success"], [class*="green"]');
    if (await successElement.count() > 0) {
      const successText = await successElement.textContent();
      console.log('SUCCESS MESSAGE:', successText);
    }
    
    // Log all network activity
    console.log('REQUESTS:', requests);
    console.log('RESPONSES:', responses);
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'test-results/debug-login.png' });
    
    // For now, just verify we can see the form
    expect(currentUrl).toContain('/login');
  });
});
