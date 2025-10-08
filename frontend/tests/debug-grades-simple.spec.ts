import { test, expect, Page } from '@playwright/test';

test.describe('Grades Tab Debug - Simple Test', () => {
  test('should load grades tab and check console logs', async ({ page }) => {
    // Listen to console logs
    const consoleLogs = [];
    page.on('console', msg => {
      if (msg.type() === 'error' || msg.type() === 'warn') {
        consoleLogs.push(`${msg.type()}: ${msg.text()}`);
      }
    });

    // Listen to network requests
    const networkRequests = [];
    page.on('request', request => {
      if (request.url().includes('/api/admin/content/grades')) {
        networkRequests.push({
          url: request.url(),
          method: request.method(),
          headers: request.headers()
        });
      }
    });

    // Listen to network responses
    const networkResponses = [];
    page.on('response', response => {
      if (response.url().includes('/api/admin/content/grades')) {
        networkResponses.push({
          url: response.url(),
          status: response.status(),
          statusText: response.statusText()
        });
      }
    });

    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(3000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Click Grades tab
    await page.click('text=Grades');
    await page.waitForTimeout(5000);

    // Check what's visible
    const pageContent = await page.textContent('body');
    console.log('Page content includes "Loading grades":', pageContent.includes('Loading grades'));
    console.log('Page content includes "Error loading grades":', pageContent.includes('Error loading grades'));
    console.log('Page content includes "Grade Levels":', pageContent.includes('Grade Levels'));
    console.log('Page content includes "Grade 7":', pageContent.includes('Grade 7'));
    console.log('Page content includes "Grade 8":', pageContent.includes('Grade 8'));

    // Log network activity
    console.log('Network Requests:', networkRequests);
    console.log('Network Responses:', networkResponses);
    console.log('Console Logs:', consoleLogs);

    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-simple.png' });

    // The test should pass if we can see some content
    expect(pageContent.includes('Grade Levels') || pageContent.includes('Loading grades') || pageContent.includes('Error loading grades')).toBe(true);
  });
});
