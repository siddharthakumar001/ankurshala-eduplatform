import { test, expect, Page } from '@playwright/test';

test.describe('Admin Content Management - Grades Tab Debug', () => {
  test('should load grades tab and check for errors', async ({ page }) => {
    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(2000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Click Grades tab
    await page.click('text=Grades');
    await page.waitForTimeout(5000);

    // Check for any error messages
    const errorMessages = page.locator('text=Error, text=error, text=Error loading, text=Failed to load');
    const errorCount = await errorMessages.count();
    
    if (errorCount > 0) {
      console.log('Found error messages:');
      for (let i = 0; i < errorCount; i++) {
        const errorText = await errorMessages.nth(i).textContent();
        console.log(`Error ${i}: ${errorText}`);
      }
    }
    
    // Check if the grades content is visible
    const gradesContent = page.locator('text=Grade Levels, text=Loading grades, text=Error loading grades');
    const contentVisible = await gradesContent.isVisible();
    console.log('Grades content visible:', contentVisible);
    
    // Check network requests
    const responses = [];
    page.on('response', response => {
      if (response.url().includes('/api/admin/content/grades')) {
        responses.push({
          url: response.url(),
          status: response.status(),
          statusText: response.statusText()
        });
      }
    });
    
    // Wait a bit more to catch any network requests
    await page.waitForTimeout(2000);
    
    console.log('Grades API responses:', responses);
    
    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-tab-detailed.png' });
    
    // The test should pass if we can see some content (even if it's an error)
    expect(contentVisible).toBe(true);
  });
});
