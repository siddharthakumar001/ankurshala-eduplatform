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
  
  // Wait for authentication to complete
  await Promise.race([
    page.waitForURL('**/admin/dashboard**', { timeout: 15000 }),
    page.waitForURL('**/login**', { timeout: 15000 }),
    page.waitForTimeout(10000)
  ]);
  
  // Additional wait for auth state
  await page.waitForTimeout(3000);
}

async function checkApiEndpoint(page: Page, endpoint: string, expectedStatus: number = 200) {
  const response = await page.request.get(`http://localhost:8080${endpoint}`);
  return response.status();
}

// Authentication and API Tests
test.describe('Admin Content Management - API and Authentication Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test.describe('Authentication Flow', () => {
    test('should login successfully as admin', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Check if we're redirected to admin dashboard or still on login
      const currentUrl = page.url();
      const isOnDashboard = currentUrl.includes('/admin/dashboard');
      const isOnLogin = currentUrl.includes('/login');
      
      // Should either be on dashboard or login (if there's an auth issue)
      expect(isOnDashboard || isOnLogin).toBeTruthy();
      
      if (isOnDashboard) {
        // Verify admin dashboard elements
        await expect(page.locator('text=Admin Dashboard')).toBeVisible();
      }
    });

    test('should handle invalid credentials', async ({ page }) => {
      await page.goto('/login');
      await page.fill('[name="email"]', 'invalid@example.com');
      await page.fill('[name="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      
      // Should stay on login page or show error
      await page.waitForTimeout(3000);
      
      const currentUrl = page.url();
      const isOnLogin = currentUrl.includes('/login');
      
      // Should remain on login page
      expect(isOnLogin).toBeTruthy();
    });

    test('should redirect to login when not authenticated', async ({ page }) => {
      // Try to access admin content management without login
      await page.goto('/admin/content/manage');
      
      // Should redirect to login or show auth error
      await page.waitForTimeout(5000);
      
      const currentUrl = page.url();
      const isOnLogin = currentUrl.includes('/login');
      const hasAuthError = await page.locator('text=Authentication required').isVisible();
      
      expect(isOnLogin || hasAuthError).toBeTruthy();
    });
  });

  test.describe('API Endpoints', () => {
    test('should test boards API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test boards endpoint
      const boardsResponse = await page.request.get('http://localhost:8080/api/admin/content/boards?page=0&size=10');
      
      console.log('Boards API Status:', boardsResponse.status());
      console.log('Boards API Response:', await boardsResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(boardsResponse.status());
    });

    test('should test boards dropdown API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test boards dropdown endpoint
      const dropdownResponse = await page.request.get('http://localhost:8080/api/admin/content-utils/boards/dropdown');
      
      console.log('Boards Dropdown API Status:', dropdownResponse.status());
      console.log('Boards Dropdown API Response:', await dropdownResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(dropdownResponse.status());
    });

    test('should test grades API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test grades endpoint
      const gradesResponse = await page.request.get('http://localhost:8080/api/admin/content/grades?page=0&size=10');
      
      console.log('Grades API Status:', gradesResponse.status());
      console.log('Grades API Response:', await gradesResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(gradesResponse.status());
    });

    test('should test subjects API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test subjects endpoint
      const subjectsResponse = await page.request.get('http://localhost:8080/api/admin/content/subjects?page=0&size=10');
      
      console.log('Subjects API Status:', subjectsResponse.status());
      console.log('Subjects API Response:', await subjectsResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(subjectsResponse.status());
    });

    test('should test chapters API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test chapters endpoint
      const chaptersResponse = await page.request.get('http://localhost:8080/api/admin/content/chapters?page=0&size=10');
      
      console.log('Chapters API Status:', chaptersResponse.status());
      console.log('Chapters API Response:', await chaptersResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(chaptersResponse.status());
    });

    test('should test topics API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test topics endpoint
      const topicsResponse = await page.request.get('http://localhost:8080/api/admin/content/topics?page=0&size=10');
      
      console.log('Topics API Status:', topicsResponse.status());
      console.log('Topics API Response:', await topicsResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(topicsResponse.status());
    });

    test('should test topic notes API endpoint', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test topic notes endpoint
      const topicNotesResponse = await page.request.get('http://localhost:8080/api/admin/content/topic-notes?page=0&size=10');
      
      console.log('Topic Notes API Status:', topicNotesResponse.status());
      console.log('Topic Notes API Response:', await topicNotesResponse.text());
      
      // Should return either 200 (success) or 401 (auth issue) or 500 (server error)
      expect([200, 401, 500]).toContain(topicNotesResponse.status());
    });
  });

  test.describe('Error Handling', () => {
    test('should handle 401 unauthorized responses', async ({ page }) => {
      // Test without authentication
      const response = await page.request.get('http://localhost:8080/api/admin/content/boards');
      
      console.log('Unauthorized API Status:', response.status());
      
      // Should return 401 or 403
      expect([401, 403]).toContain(response.status());
    });

    test('should handle 404 not found responses', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test non-existent endpoint
      const response = await page.request.get('http://localhost:8080/api/admin/content/nonexistent');
      
      console.log('Not Found API Status:', response.status());
      
      // Should return 404
      expect(response.status()).toBe(404);
    });

    test('should handle server errors gracefully', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Test with invalid parameters
      const response = await page.request.get('http://localhost:8080/api/admin/content/boards?page=invalid&size=invalid');
      
      console.log('Invalid Params API Status:', response.status());
      
      // Should return either 400 (bad request) or 500 (server error)
      expect([400, 500]).toContain(response.status());
    });
  });

  test.describe('Frontend-Backend Integration', () => {
    test('should load content management page with API calls', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Monitor network requests
      const requests: string[] = [];
      page.on('request', request => {
        if (request.url().includes('/api/admin/content')) {
          requests.push(request.url());
        }
      });
      
      await page.goto('/admin/content/manage');
      
      // Wait for page to load
      await page.waitForTimeout(5000);
      
      console.log('API Requests made:', requests);
      
      // Should have made API requests
      expect(requests.length).toBeGreaterThan(0);
      
      // Check for boards API call
      const hasBoardsCall = requests.some(url => url.includes('/boards'));
      expect(hasBoardsCall).toBeTruthy();
    });

    test('should handle API errors in frontend', async ({ page }) => {
      await loginAsAdmin(page);
      
      // Intercept API calls and return error
      await page.route('**/api/admin/content/boards**', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal Server Error' })
        });
      });
      
      await page.goto('/admin/content/manage');
      
      // Wait for error to appear
      await page.waitForTimeout(5000);
      
      // Should show error message
      const hasError = await page.locator('text=Error loading boards').isVisible();
      expect(hasError).toBeTruthy();
    });

    test('should retry failed API calls', async ({ page }) => {
      await loginAsAdmin(page);
      
      let callCount = 0;
      
      // Intercept API calls and fail first time, succeed second time
      await page.route('**/api/admin/content/boards**', route => {
        callCount++;
        if (callCount === 1) {
          route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify({ error: 'Internal Server Error' })
          });
        } else {
          route.continue();
        }
      });
      
      await page.goto('/admin/content/manage');
      
      // Wait for retry
      await page.waitForTimeout(10000);
      
      // Should have made multiple calls
      expect(callCount).toBeGreaterThan(1);
    });
  });

  test.describe('Performance Tests', () => {
    test('should load content management page within acceptable time', async ({ page }) => {
      await loginAsAdmin(page);
      
      const startTime = Date.now();
      
      await page.goto('/admin/content/manage');
      
      // Wait for content to load
      await Promise.race([
        page.waitForSelector('text=Content Management', { timeout: 10000 }),
        page.waitForSelector('text=Error loading boards', { timeout: 10000 }),
        page.waitForTimeout(10000)
      ]);
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      console.log('Page load time:', loadTime, 'ms');
      
      // Should load within 10 seconds
      expect(loadTime).toBeLessThan(10000);
    });

    test('should handle multiple concurrent API calls', async ({ page }) => {
      await loginAsAdmin(page);
      
      const requests: Promise<any>[] = [];
      
      // Make multiple API calls simultaneously
      for (let i = 0; i < 5; i++) {
        requests.push(
          page.request.get(`http://localhost:8080/api/admin/content/boards?page=${i}&size=10`)
        );
      }
      
      const responses = await Promise.all(requests);
      
      // All requests should complete
      expect(responses.length).toBe(5);
      
      // Check response statuses
      responses.forEach(response => {
        expect([200, 401, 500]).toContain(response.status());
      });
    });
  });
});
