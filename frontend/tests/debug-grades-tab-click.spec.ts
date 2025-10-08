import { test, expect, Page } from '@playwright/test';

test.describe('Grades Tab Click Debug', () => {
  test('should check if grades tab click is registered', async ({ page }) => {
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
    
    // Wait for initial load
    await page.waitForTimeout(2000);
    
    // Check initial state
    console.log('Initial console logs:', consoleLogs.filter(log => log.includes('activeTab')));
    
    // Try different ways to click the Grades tab
    console.log('Trying to click Grades tab...');
    
    // Method 1: Click by text
    await page.click('text=Grades');
    await page.waitForTimeout(2000);
    
    // Method 2: Click by role
    await page.getByRole('tab', { name: 'Grades' }).click();
    await page.waitForTimeout(2000);
    
    // Method 3: Click by data attribute
    await page.click('[data-state="inactive"][value="grades"]');
    await page.waitForTimeout(2000);
    
    // Check final state
    console.log('Final console logs:', consoleLogs.filter(log => log.includes('activeTab') || log.includes('Tab changing')));
    
    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-tab-click.png' });

    // The test should pass if we see tab change logs
    const tabChangeLogs = consoleLogs.filter(log => log.includes('Tab changing'));
    expect(tabChangeLogs.length > 0).toBe(true);
  });
});
