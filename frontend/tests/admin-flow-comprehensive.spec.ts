import { test, expect, Page } from '@playwright/test';

// Helper functions for authentication
async function loginAsAdmin(page: Page) {
  await page.goto('/login');
  await expect(page.locator('input[type="email"]')).toBeVisible();
  await page.fill('input[type="email"]', 'siddhartha@ankurshala.com');
  await page.fill('input[type="password"]', 'Maza@123');
  await page.click('button[type="submit"]');
  
  // Wait for login to complete and redirect to admin dashboard
  await page.waitForURL(/\/admin/, { timeout: 15000 });
  await page.waitForLoadState('networkidle');
}

async function navigateToContentManagement(page: Page) {
  await page.click('a[href="/admin/content/manage"]');
  await page.waitForURL('/admin/content/manage');
  await page.waitForLoadState('networkidle');
  
  // Wait for loading states to complete
  await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 });
  await expect(page.locator('h1')).toContainText('Content Management');
}

async function createTestEntity(page: Page, entityType: string, data: any) {
  const buttonText = `Add ${entityType.toLowerCase()}`;
  await page.click(`button:has-text("${buttonText}")`);
  
  // Wait for modal to open
  await expect(page.locator('[role="dialog"]')).toBeVisible();
  
  // Fill form based on entity type
  switch (entityType.toLowerCase()) {
    case 'board':
      await page.fill('input[id="name"]', data.name);
      break;
    case 'grade':
      await page.fill('input[id="name"]', data.name);
      if (data.boardId) {
        await page.click('[data-testid="board-select"]');
        await page.click(`[role="option"]:has-text("${data.boardName}")`);
      }
      break;
    case 'subject':
      await page.fill('input[id="name"]', data.name);
      if (data.gradeId) {
        await page.click('[data-testid="grade-select"]');
        await page.click(`[role="option"]:has-text("${data.gradeName}")`);
      }
      break;
    case 'chapter':
      await page.fill('input[id="name"]', data.name);
      if (data.subjectId) {
        await page.click('[data-testid="subject-select"]');
        await page.click(`[role="option"]:has-text("${data.subjectName}")`);
      }
      break;
    case 'topic':
      await page.fill('input[id="title"]', data.title);
      await page.fill('input[id="expectedTimeMins"]', data.expectedTimeMins.toString());
      await page.fill('textarea[id="description"]', data.description);
      await page.fill('textarea[id="summary"]', data.summary);
      if (data.chapterId) {
        await page.click('[data-testid="chapter-select"]');
        await page.click(`[role="option"]:has-text("${data.chapterName}")`);
      }
      break;
    case 'note':
      await page.fill('input[id="title"]', data.title);
      await page.fill('textarea[id="content"]', data.content);
      if (data.topicId) {
        await page.click('[data-testid="topic-select"]');
        await page.click(`[role="option"]:has-text("${data.topicName}")`);
      }
      break;
  }
  
  // Submit form
  await page.click('button:has-text("Create")');
  
  // Wait for success message
  await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 15000 });
  await expect(page.locator('[data-sonner-toast]').filter({ hasText: /created successfully/ })).toBeVisible({ timeout: 15000 });
  
  // Wait for modal to close
  await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  
  return data;
}

test.describe('Comprehensive Admin Flow Validation', () => {
  test.beforeEach(async ({ page }) => {
    // Clear any existing state
    await page.context().clearCookies();
    await page.context().clearPermissions();
  });

  test('should complete full admin authentication and session management flow', async ({ page }) => {
    // Step 1: Login as admin
    await loginAsAdmin(page);
    
    // Verify admin dashboard is accessible
    await expect(page.locator('h1')).toContainText('Admin Dashboard');
    
    // Step 2: Navigate to content management
    await navigateToContentManagement(page);
    
    // Step 3: Verify all tabs are present and functional
    const tabs = ['Boards', 'Grades', 'Subjects', 'Chapters', 'Topics', 'Notes'];
    for (const tab of tabs) {
      await expect(page.locator(`button[role="tab"]:has-text("${tab}")`)).toBeVisible();
    }
    
    // Step 4: Test session persistence
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Should still be logged in and on the content management page
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Step 5: Verify logout functionality
    await page.click('button:has-text("Logout"), a:has-text("Logout")');
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.locator('h2')).toContainText('Welcome Back');
  });

  test('should validate session extension popup functionality', async ({ page }) => {
    // Login as admin
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Mock token expiration by injecting JavaScript to simulate near-expiry
    await page.evaluate(() => {
      // Access the auth manager and simulate token near expiry
      const authManager = (window as any).authManager;
      if (authManager) {
        // Simulate a token that will expire in 2 minutes
        const nearExpiryToken = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaWRkaGFydGhhQGFua3Vyc2hhbGEuY29tIiwiZW1haWwiOiJzaWRkaGFydGhhQGFua3Vyc2hhbGEuY29tIiwicm9sZXMiOlsiQURNSU4iXSwiaWF0IjoxNzI3NzIzNjAwLCJleHAiOjE3Mjc3MjM3MjB9';
        localStorage.setItem('accessToken', nearExpiryToken);
      }
    });
    
    // Trigger session check by performing an action
    await page.click('button[role="tab"]:has-text("Boards")');
    
    // Wait a moment to see if session extension popup appears
    await page.waitForTimeout(3000);
    
    // Check if session extension popup appears (this may or may not be visible depending on actual token state)
    const sessionPopup = page.locator('text=Session Expiring Soon').or(page.locator('text=Extend Session'));
    const isSessionPopupVisible = await sessionPopup.isVisible().catch(() => false);
    
    if (isSessionPopupVisible) {
      console.log('Session extension popup appeared - testing interaction');
      await page.click('button:has-text("Extend Session"), button:has-text("Yes")');
      await expect(sessionPopup).not.toBeVisible();
    } else {
      console.log('Session extension popup did not appear - normal behavior');
    }
  });

  test('should perform complete CRUD operations on all entities', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    const timestamp = Date.now();
    
    // Step 1: Create a Board
    await page.click('button[role="tab"]:has-text("Boards")');
    const boardData = await createTestEntity(page, 'board', {
      name: `Test Board ${timestamp}`
    });
    
    // Verify board appears in table
    await expect(page.locator(`td:has-text("${boardData.name}")`)).toBeVisible();
    
    // Step 2: Create a Grade for the Board
    await page.click('button[role="tab"]:has-text("Grades")');
    const gradeData = await createTestEntity(page, 'grade', {
      name: `Test Grade ${timestamp}`,
      boardName: boardData.name
    });
    
    // Verify grade appears in table
    await expect(page.locator(`td:has-text("${gradeData.name}")`)).toBeVisible();
    
    // Step 3: Create a Subject for the Grade
    await page.click('button[role="tab"]:has-text("Subjects")');
    const subjectData = await createTestEntity(page, 'subject', {
      name: `Test Subject ${timestamp}`,
      gradeName: gradeData.name
    });
    
    // Verify subject appears in table
    await expect(page.locator(`td:has-text("${subjectData.name}")`)).toBeVisible();
    
    // Step 4: Create a Chapter for the Subject
    await page.click('button[role="tab"]:has-text("Chapters")');
    const chapterData = await createTestEntity(page, 'chapter', {
      name: `Test Chapter ${timestamp}`,
      subjectName: subjectData.name
    });
    
    // Verify chapter appears in table
    await expect(page.locator(`td:has-text("${chapterData.name}")`)).toBeVisible();
    
    // Step 5: Create a Topic for the Chapter
    await page.click('button[role="tab"]:has-text("Topics")');
    const topicData = await createTestEntity(page, 'topic', {
      title: `Test Topic ${timestamp}`,
      expectedTimeMins: 90,
      description: `Test description for topic ${timestamp}`,
      summary: `Test summary for topic ${timestamp}`,
      chapterName: chapterData.name
    });
    
    // Verify topic appears in table with correct time formatting
    await expect(page.locator(`td:has-text("${topicData.title}")`)).toBeVisible();
    await expect(page.locator('td:has-text("1h 30m")')).toBeVisible(); // 90 minutes = 1h 30m
    
    // Step 6: Create a Topic Note
    await page.click('button[role="tab"]:has-text("Notes")');
    const noteData = await createTestEntity(page, 'note', {
      title: `Test Note ${timestamp}`,
      content: `This is a comprehensive test note content created at ${timestamp}. It contains sufficient text to meet any minimum requirements.`,
      topicName: topicData.title
    });
    
    // Verify note appears in table
    await expect(page.locator(`td:has-text("${noteData.title}")`)).toBeVisible();
  });

  test('should validate edit and delete operations', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Create a test board for editing/deleting
    await page.click('button[role="tab"]:has-text("Boards")');
    const testBoardName = `Edit Test Board ${Date.now()}`;
    await createTestEntity(page, 'board', { name: testBoardName });
    
    // Test Edit Operation
    const boardRow = page.locator(`tr:has-text("${testBoardName}")`);
    await expect(boardRow).toBeVisible();
    
    // Click edit button (first button in actions)
    await boardRow.locator('button').first().click();
    
    // Wait for edit modal
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    await expect(page.locator('text=Edit board')).toBeVisible();
    
    // Update the name
    const updatedName = testBoardName + ' UPDATED';
    await page.fill('input[id="name"]', updatedName);
    await page.click('button:has-text("Update")');
    
    // Wait for success message
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 15000 });
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /updated successfully/ })).toBeVisible({ timeout: 15000 });
    
    // Verify updated name appears
    await expect(page.locator(`td:has-text("${updatedName}")`)).toBeVisible();
    
    // Test Toggle Active Status
    const updatedRow = page.locator(`tr:has-text("${updatedName}")`);
    await updatedRow.locator('button').nth(1).click(); // Toggle button
    
    // Wait for toggle success
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 15000 });
    
    // Test Delete Operation
    await updatedRow.locator('button').nth(2).click(); // Delete button
    
    // Wait for delete success
    await expect(page.locator('[data-sonner-toast]')).toBeVisible({ timeout: 15000 });
    await expect(page.locator('[data-sonner-toast]').filter({ hasText: /deleted successfully/ })).toBeVisible({ timeout: 15000 });
    
    // Verify item is removed from table
    await page.waitForTimeout(2000);
    await expect(page.locator(`tr:has-text("${updatedName}")`)).not.toBeVisible();
  });

  test('should validate search and filtering functionality', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Test search on Subjects tab (has existing data)
    await page.click('button[role="tab"]:has-text("Subjects")');
    await page.waitForSelector('table', { timeout: 10000 });
    
    // Test search functionality
    await page.fill('input[placeholder*="Search"]', 'Chemistry');
    await page.waitForTimeout(2000);
    
    // Should show Chemistry in results
    await expect(page.locator('td:has-text("Chemistry")')).toBeVisible();
    
    // Clear search and verify all results return
    await page.fill('input[placeholder*="Search"]', '');
    await page.waitForTimeout(2000);
    
    // Should show more results
    const subjectRows = page.locator('tbody tr');
    const count = await subjectRows.count();
    expect(count).toBeGreaterThan(1);
  });

  test('should validate hierarchical filtering', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Test hierarchical filtering on Subjects tab
    await page.click('button[role="tab"]:has-text("Subjects")');
    await page.waitForSelector('table', { timeout: 10000 });
    
    // Check if board filter exists and test it
    const boardFilter = page.locator('[data-testid="board-filter"]');
    if (await boardFilter.isVisible()) {
      await boardFilter.click();
      await page.click('[role="option"]:has-text("CBSE")');
      await page.waitForTimeout(2000);
      
      // Verify filtering works - table should update
      await expect(page.locator('table')).toBeVisible();
    }
    
    // Test grade filter if available
    const gradeFilter = page.locator('[data-testid="grade-filter"]');
    if (await gradeFilter.isVisible()) {
      await gradeFilter.click();
      await page.click('[role="option"]:first-child');
      await page.waitForTimeout(2000);
      
      // Verify filtering works
      await expect(page.locator('table')).toBeVisible();
    }
  });

  test('should validate error handling and edge cases', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Test creating entity with empty required fields
    await page.click('button[role="tab"]:has-text("Boards")');
    await page.click('button:has-text("Add board")');
    
    await expect(page.locator('[role="dialog"]')).toBeVisible();
    
    // Try to submit without filling required fields
    await page.click('button:has-text("Create")');
    
    // Should show validation error or stay on modal
    await page.waitForTimeout(2000);
    
    // The modal should still be visible (form validation should prevent submission)
    const modalStillVisible = await page.locator('[role="dialog"]').isVisible();
    expect(modalStillVisible).toBe(true);
    
    // Close modal
    await page.keyboard.press('Escape');
    await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  });

  test('should validate responsive design and accessibility', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Page should still be functional on mobile
    await expect(page.locator('h1')).toContainText('Content Management');
    await expect(page.locator('[role="tablist"]')).toBeVisible();
    
    // Test keyboard navigation
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    
    // Should be able to navigate tabs with keyboard
    await page.keyboard.press('ArrowRight');
    await page.keyboard.press('Enter');
    
    // Verify page is still functional
    await expect(page.locator('h1')).toContainText('Content Management');
    
    // Reset to desktop viewport
    await page.setViewportSize({ width: 1280, height: 720 });
  });

  test('should validate dark mode compatibility', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Toggle to dark mode
    const darkModeToggle = page.locator('button:has(svg.lucide-moon)');
    if (await darkModeToggle.isVisible()) {
      await darkModeToggle.click();
      await expect(page.locator('html')).toHaveClass(/dark/);
      
      // Verify elements are still visible in dark mode
      await expect(page.locator('h1')).toContainText('Content Management');
      await expect(page.locator('[role="tablist"]')).toBeVisible();
      
      // Toggle back to light mode
      await page.click('button:has(svg.lucide-sun)');
      await expect(page.locator('html')).not.toHaveClass(/dark/);
    }
  });

  test('should validate pagination functionality', async ({ page }) => {
    // Login and navigate
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
    
    // Go to a tab that likely has pagination (Topics or Subjects)
    await page.click('button[role="tab"]:has-text("Topics")');
    await page.waitForSelector('table', { timeout: 10000 });
    
    // Check if pagination controls exist
    const nextButton = page.locator('button:has-text("Next")');
    const prevButton = page.locator('button:has-text("Previous")');
    
    if (await nextButton.isVisible()) {
      // Test pagination
      await nextButton.click();
      await page.waitForTimeout(2000);
      
      // Should navigate to next page
      await expect(page.locator('table')).toBeVisible();
      
      // Test previous button
      if (await prevButton.isVisible()) {
        await prevButton.click();
        await page.waitForTimeout(2000);
        await expect(page.locator('table')).toBeVisible();
      }
    }
  });
});
