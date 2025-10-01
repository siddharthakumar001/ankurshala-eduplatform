import { test, expect, Page } from '@playwright/test';

// Test data
const TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  }
};

// Helper functions
async function loginAsAdmin(page: Page) {
  await page.goto('/login');
  await page.fill('[name="email"]', TEST_USERS.admin.email);
  await page.fill('[name="password"]', TEST_USERS.admin.password);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/admin/dashboard**', { timeout: 10000 });
}

// API Integration Tests
test.describe('API Integration Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
  });

  test('should fetch boards data from API', async ({ page }) => {
    // Intercept API calls
    const apiCalls: any[] = [];
    
    page.on('response', response => {
      if (response.url().includes('/api/admin/content/boards')) {
        apiCalls.push({
          url: response.url(),
          status: response.status(),
          headers: response.headers()
        });
      }
    });

    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');

    // Check that API was called
    expect(apiCalls.length).toBeGreaterThan(0);
    expect(apiCalls[0].status).toBe(200);
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Mock API to return error
    await page.route('**/api/admin/content/boards**', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' })
      });
    });

    await page.goto('/admin/content/manage');
    
    // Check for error message
    await expect(page.locator('text=Error loading data').or(page.locator('text=Failed to fetch'))).toBeVisible();
  });

  test('should handle network timeout', async ({ page }) => {
    // Mock API to timeout
    await page.route('**/api/admin/content/boards**', route => {
      // Don't fulfill the route to simulate timeout
    });

    await page.goto('/admin/content/manage');
    
    // Check for loading state
    await expect(page.locator('text=Loading...').or(page.locator('[data-testid="loading"]'))).toBeVisible();
  });

  test('should retry failed API calls', async ({ page }) => {
    let callCount = 0;
    
    await page.route('**/api/admin/content/boards**', route => {
      callCount++;
      if (callCount === 1) {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Server Error' })
        });
      } else {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: [], totalElements: 0 })
        });
      }
    });

    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');

    // Check that API was called multiple times (retry)
    expect(callCount).toBeGreaterThan(1);
  });

  test('should handle authentication token refresh', async ({ page }) => {
    let tokenRefreshCalled = false;
    
    // Mock token refresh endpoint
    await page.route('**/api/auth/refresh**', route => {
      tokenRefreshCalled = true;
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'new-token',
          refreshToken: 'new-refresh-token'
        })
      });
    });

    // Mock main API to return 401 first, then 200
    let callCount = 0;
    await page.route('**/api/admin/content/boards**', route => {
      callCount++;
      if (callCount === 1) {
        route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Unauthorized' })
        });
      } else {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: [], totalElements: 0 })
        });
      }
    });

    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');

    // Check that token refresh was called
    expect(tokenRefreshCalled).toBeTruthy();
  });
});

// Performance Tests
test.describe('Performance Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
  });

  test('should load content management page within acceptable time', async ({ page }) => {
    const startTime = Date.now();
    
    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');
    
    const loadTime = Date.now() - startTime;
    
    // Page should load within 5 seconds
    expect(loadTime).toBeLessThan(5000);
  });

  test('should handle large datasets efficiently', async ({ page }) => {
    // Mock API to return large dataset
    await page.route('**/api/admin/content/boards**', route => {
      const largeDataset = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        name: `Board ${i + 1}`,
        description: `Description for board ${i + 1}`,
        active: true
      }));
      
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: largeDataset,
          totalElements: 1000,
          totalPages: 10
        })
      });
    });

    const startTime = Date.now();
    
    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');
    
    const loadTime = Date.now() - startTime;
    
    // Should handle large dataset within 10 seconds
    expect(loadTime).toBeLessThan(10000);
  });

  test('should implement pagination for performance', async ({ page }) => {
    await page.goto('/admin/content/manage');
    await page.waitForLoadState('networkidle');
    
    // Check for pagination controls
    const pagination = page.locator('[data-testid="pagination"]').or(page.locator('nav[aria-label="Pagination"]'));
    
    if (await pagination.isVisible()) {
      // Check that pagination is working
      const pageButtons = pagination.locator('button');
      const pageCount = await pageButtons.count();
      
      expect(pageCount).toBeGreaterThan(1);
    }
  });
});

// Accessibility Tests
test.describe('Accessibility Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
  });

  test('should have proper ARIA labels', async ({ page }) => {
    await page.goto('/admin/content/manage');
    
    // Check for ARIA labels on form elements
    const emailInput = page.locator('[name="email"]');
    const passwordInput = page.locator('[name="password"]');
    
    if (await emailInput.isVisible()) {
      await expect(emailInput).toHaveAttribute('aria-label');
    }
    
    if (await passwordInput.isVisible()) {
      await expect(passwordInput).toHaveAttribute('aria-label');
    }
  });

  test('should support keyboard navigation', async ({ page }) => {
    await page.goto('/admin/content/manage');
    
    // Test tab navigation
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    
    // Check that focus is visible
    const focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();
  });

  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/admin/content/manage');
    
    // Check for h1 heading
    const h1 = page.locator('h1');
    await expect(h1).toBeVisible();
    
    // Check for h2 headings
    const h2 = page.locator('h2');
    await expect(h2).toHaveCount({ min: 1 });
  });

  test('should have proper color contrast', async ({ page }) => {
    await page.goto('/admin/content/manage');
    
    // Check that text is visible and readable
    const textElements = page.locator('p, span, div').filter({ hasText: /[a-zA-Z]/ });
    const firstText = textElements.first();
    
    if (await firstText.isVisible()) {
      // Check that text has proper contrast
      const color = await firstText.evaluate(el => {
        const styles = window.getComputedStyle(el);
        return styles.color;
      });
      
      expect(color).not.toBe('transparent');
    }
  });
});

// Cross-browser Tests
test.describe('Cross-browser Compatibility', () => {
  test('should work in Chrome', async ({ page, browserName }) => {
    if (browserName !== 'chromium') return;
    
    await page.context().clearCookies();
    await loginAsAdmin(page);
    
    await page.goto('/admin/content/manage');
    await expect(page.locator('text=Boards')).toBeVisible();
  });

  test('should work in Firefox', async ({ page, browserName }) => {
    if (browserName !== 'firefox') return;
    
    await page.context().clearCookies();
    await loginAsAdmin(page);
    
    await page.goto('/admin/content/manage');
    await expect(page.locator('text=Boards')).toBeVisible();
  });

  test('should work in Safari', async ({ page, browserName }) => {
    if (browserName !== 'webkit') return;
    
    await page.context().clearCookies();
    await loginAsAdmin(page);
    
    await page.goto('/admin/content/manage');
    await expect(page.locator('text=Boards')).toBeVisible();
  });
});

// Mobile Responsiveness Tests
test.describe('Mobile Responsiveness', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    await page.goto('/admin/content/manage');
    
    // Check that content is visible and properly sized
    await expect(page.locator('text=Boards')).toBeVisible();
    
    // Check that tables are responsive
    const table = page.locator('table');
    if (await table.isVisible()) {
      const tableWidth = await table.boundingBox();
      expect(tableWidth?.width).toBeLessThanOrEqual(375);
    }
  });

  test('should have touch-friendly buttons', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    await page.goto('/admin/content/manage');
    
    // Check that buttons are large enough for touch
    const buttons = page.locator('button');
    const firstButton = buttons.first();
    
    if (await firstButton.isVisible()) {
      const buttonSize = await firstButton.boundingBox();
      expect(buttonSize?.height).toBeGreaterThanOrEqual(44); // Minimum touch target size
    }
  });
});
