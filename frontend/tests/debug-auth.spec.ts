import { test, expect, Page } from '@playwright/test';

test.describe('Authentication Debug', () => {
  test('should check authentication state after login', async ({ page }) => {
    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(3000);
    
    // Check localStorage for tokens
    const accessToken = await page.evaluate(() => localStorage.getItem('accessToken'));
    const refreshToken = await page.evaluate(() => localStorage.getItem('refreshToken'));
    const user = await page.evaluate(() => localStorage.getItem('user'));
    
    console.log('Access Token:', accessToken ? 'Present' : 'Missing');
    console.log('Refresh Token:', refreshToken ? 'Present' : 'Missing');
    console.log('User:', user ? 'Present' : 'Missing');
    
    if (accessToken) {
      console.log('Access Token (first 50 chars):', accessToken.substring(0, 50) + '...');
    }
    
    // Check if authManager is working
    const authState = await page.evaluate(() => {
      // @ts-ignore
      return window.authManager ? {
        isAuthenticated: window.authManager.isAuthenticated(),
        hasToken: !!window.authManager.getToken(),
        token: window.authManager.getToken()?.substring(0, 50) + '...'
      } : null;
    });
    
    console.log('Auth Manager State:', authState);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Check network requests
    const apiCalls = [];
    page.on('request', request => {
      if (request.url().includes('/api/admin/content/')) {
        const headers = request.headers();
        apiCalls.push({
          url: request.url(),
          hasAuth: !!headers['authorization'],
          authHeader: headers['authorization']?.substring(0, 50) + '...'
        });
      }
    });
    
    // Click Grades tab to trigger API call
    await page.click('text=Grades');
    await page.waitForTimeout(3000);
    
    console.log('API Calls:', apiCalls);
    
    // Take a screenshot
    await page.screenshot({ path: 'debug-auth-state.png' });
    
    // Verify authentication
    expect(accessToken).toBeTruthy();
    expect(authState?.isAuthenticated).toBe(true);
  });
});
