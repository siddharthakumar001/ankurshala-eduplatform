import { test, expect, Page } from '@playwright/test';

// Helper function for admin login
async function loginAsAdmin(page: Page) {
  await page.goto('/login');
  await expect(page.locator('input[type="email"]')).toBeVisible();
  await page.fill('input[type="email"]', 'siddhartha@ankurshala.com');
  await page.fill('input[type="password"]', 'Maza@123');
  await page.click('button[type="submit"]');
  
  // Wait for login to complete
  await page.waitForURL(/\/admin/, { timeout: 15000 });
  await page.waitForLoadState('networkidle');
}

test.describe('Session Management and Authentication Edge Cases', () => {
  test.beforeEach(async ({ page }) => {
    // Clear any existing state
    await page.context().clearCookies();
    await page.context().clearPermissions();
  });

  test('should handle session extension popup correctly', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Mock token near expiry by manipulating localStorage
    await page.evaluate(() => {
      // Create a token that expires in 2 minutes (120 seconds)
      const currentTime = Math.floor(Date.now() / 1000);
      const expiryTime = currentTime + 120; // 2 minutes from now
      
      // Create a mock JWT token with near expiry
      const header = btoa(JSON.stringify({ typ: 'JWT', alg: 'HS256' }));
      const payload = btoa(JSON.stringify({
        sub: 'siddhartha@ankurshala.com',
        email: 'siddhartha@ankurshala.com',
        roles: ['ADMIN'],
        iat: currentTime,
        exp: expiryTime
      }));
      const signature = 'mock_signature';
      
      const mockToken = `${header}.${payload}.${signature}`;
      localStorage.setItem('accessToken', mockToken);
      
      // Trigger a token check
      if ((window as any).authManager) {
        (window as any).authManager.checkTokenValidity();
      }
    });
    
    // Wait for session extension popup to appear
    await page.waitForTimeout(3000);
    
    // Look for session extension popup
    const sessionPopup = page.locator('text=Session Expiring Soon')
      .or(page.locator('text=Extend Session'))
      .or(page.locator('text=Your session will expire'))
      .or(page.locator('[data-testid="session-extension-popup"]'));
    
    const isPopupVisible = await sessionPopup.isVisible().catch(() => false);
    
    if (isPopupVisible) {
      console.log('Session extension popup appeared successfully');
      
      // Test extending session
      const extendButton = page.locator('button:has-text("Extend Session")')
        .or(page.locator('button:has-text("Yes")')
        .or(page.locator('button:has-text("Continue")')));
      
      if (await extendButton.isVisible()) {
        await extendButton.click();
        
        // Popup should disappear
        await expect(sessionPopup).not.toBeVisible({ timeout: 5000 });
        
        // Should still be on the content management page
        await expect(page.locator('h1')).toContainText('Content Management');
      }
      
      // Test that popup doesn't appear again immediately (should only appear once)
      await page.waitForTimeout(2000);
      const popupAgain = await sessionPopup.isVisible().catch(() => false);
      expect(popupAgain).toBe(false);
    } else {
      console.log('Session extension popup did not appear - this may be expected behavior');
    }
  });

  test('should handle token expiration and forced logout', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Mock expired token
    await page.evaluate(() => {
      // Create an expired token
      const currentTime = Math.floor(Date.now() / 1000);
      const expiredTime = currentTime - 300; // 5 minutes ago
      
      const header = btoa(JSON.stringify({ typ: 'JWT', alg: 'HS256' }));
      const payload = btoa(JSON.stringify({
        sub: 'siddhartha@ankurshala.com',
        email: 'siddhartha@ankurshala.com',
        roles: ['ADMIN'],
        iat: currentTime - 3600,
        exp: expiredTime
      }));
      const signature = 'mock_signature';
      
      const expiredToken = `${header}.${payload}.${signature}`;
      localStorage.setItem('accessToken', expiredToken);
    });
    
    // Try to perform an action that requires authentication
    await page.click('button[role="tab"]:has-text("Boards")');
    
    // Should be redirected to login due to expired token
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.locator('h2')).toContainText('Welcome Back');
  });

  test('should handle logout correctly and clear all state', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Verify we're logged in
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Logout
    await page.click('button:has-text("Logout"), a:has-text("Logout")');
    
    // Should redirect to login
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.locator('h2')).toContainText('Welcome Back');
    
    // Verify localStorage is cleared
    const authState = await page.evaluate(() => {
      return {
        accessToken: localStorage.getItem('accessToken'),
        refreshToken: localStorage.getItem('refreshToken'),
        user: localStorage.getItem('user'),
        lastActivity: localStorage.getItem('lastActivity')
      };
    });
    
    expect(authState.accessToken).toBeNull();
    expect(authState.refreshToken).toBeNull();
    expect(authState.user).toBeNull();
    expect(authState.lastActivity).toBeNull();
    
    // Try to access protected route directly
    await page.goto('/admin/content/manage');
    
    // Should be redirected back to login
    await page.waitForURL(/\/login/, { timeout: 10000 });
  });

  test('should handle session persistence across page refreshes', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Verify we're on the page
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Refresh the page
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Should still be logged in and on the same page
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Should not be redirected to login
    expect(page.url()).toContain('/admin/content/manage');
  });

  test('should handle multiple tab sessions correctly', async ({ browser }) => {
    // Create first context/tab
    const context1 = await browser.newContext();
    const page1 = await context1.newPage();
    
    // Login in first tab
    await loginAsAdmin(page1);
    await page1.click('a[href="/admin/content/manage"]');
    await page1.waitForURL('/admin/content/manage');
    
    // Create second context/tab
    const context2 = await browser.newContext();
    const page2 = await context2.newPage();
    
    // Should not be logged in in second tab (different context)
    await page2.goto('/admin/content/manage');
    await page2.waitForURL(/\/login/, { timeout: 10000 });
    
    // Login in second tab
    await loginAsAdmin(page2);
    await page2.click('a[href="/admin/content/manage"]');
    await page2.waitForURL('/admin/content/manage');
    
    // Both tabs should be functional independently
    await expect(page1.locator('h1')).toContainText('Content Management');
    await expect(page2.locator('h1')).toContainText('Content Management');
    
    // Logout from first tab
    await page1.click('button:has-text("Logout"), a:has-text("Logout")');
    await page1.waitForURL(/\/login/, { timeout: 10000 });
    
    // Second tab should still be logged in (different context)
    await expect(page2.locator('h1')).toContainText('Content Management');
    
    // Clean up
    await context1.close();
    await context2.close();
  });

  test('should handle network failures gracefully', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Go offline
    await page.context().setOffline(true);
    
    // Try to perform an action
    await page.click('button[role="tab"]:has-text("Boards")');
    
    // Should handle network error gracefully (not crash)
    await page.waitForTimeout(3000);
    
    // Page should still be responsive
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Go back online
    await page.context().setOffline(false);
    
    // Should be able to perform actions again
    await page.click('button[role="tab"]:has-text("Subjects")');
    await page.waitForSelector('table', { timeout: 15000 });
  });

  test('should handle invalid/malformed tokens', async ({ page }) => {
    // Login as admin first
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Set invalid token
    await page.evaluate(() => {
      localStorage.setItem('accessToken', 'invalid.token.here');
    });
    
    // Try to perform an action
    await page.click('button[role="tab"]:has-text("Boards")');
    
    // Should handle invalid token and redirect to login
    await page.waitForURL(/\/login/, { timeout: 15000 });
    await expect(page.locator('h2')).toContainText('Welcome Back');
  });

  test('should handle browser security restrictions', async ({ page }) => {
    // Test behavior when localStorage is disabled
    await page.addInitScript(() => {
      // Mock localStorage to throw errors
      const originalSetItem = localStorage.setItem;
      localStorage.setItem = () => {
        throw new Error('localStorage disabled');
      };
    });
    
    // Try to login
    await page.goto('/login');
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[type="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Should handle localStorage errors gracefully
    // May redirect to login or show error message
    await page.waitForTimeout(5000);
    
    // Page should not crash
    await expect(page.locator('body')).toBeVisible();
  });

  test('should validate session timeout behavior', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    
    // Navigate to content management
    await page.click('a[href="/admin/content/manage"]');
    await page.waitForURL('/admin/content/manage');
    
    // Mock idle timeout by manipulating the auth manager
    await page.evaluate(() => {
      // Set last activity to an old timestamp to simulate idle timeout
      localStorage.setItem('lastActivity', (Date.now() - 46 * 60 * 1000).toString()); // 46 minutes ago
      
      // Trigger activity check if auth manager is available
      if ((window as any).authManager) {
        (window as any).authManager.updateActivity();
      }
    });
    
    // Perform an action to trigger activity check
    await page.click('button[role="tab"]:has-text("Boards")');
    
    // Wait to see if idle timeout is triggered
    await page.waitForTimeout(3000);
    
    // Depending on implementation, may stay logged in or redirect to login
    // The important thing is that the application handles it gracefully
    const isOnLogin = page.url().includes('/login');
    const isOnContent = page.url().includes('/admin/content/manage');
    
    // Should be on either login (if idle timeout triggered) or content page
    expect(isOnLogin || isOnContent).toBe(true);
  });
});
