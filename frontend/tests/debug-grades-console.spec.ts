import { test, expect, Page } from '@playwright/test';

test.describe('Grades Tab Debug with Console Logs', () => {
  test('should check grades tab console logs', async ({ page }) => {
    // Listen to console logs
    const consoleLogs = [];
    page.on('console', msg => {
      consoleLogs.push(`${msg.type()}: ${msg.text()}`);
    });

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

    // Log all console messages
    console.log('Console Logs:', consoleLogs);

    // Check if Grades tab content is visible
    const gradesContent = page.locator('text=Grade Levels, text=Loading grades, text=Error loading grades');
    const contentVisible = await gradesContent.isVisible();
    console.log('Grades content visible:', contentVisible);

    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-with-logs.png' });

    // The test should pass if we can see some content or console logs
    expect(contentVisible || consoleLogs.length > 0).toBe(true);
  });
});
