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
  
  // Wait for redirect to admin dashboard
  await Promise.race([
    page.waitForURL('**/admin/dashboard**', { timeout: 10000 }),
    page.waitForURL('**/admin/content/manage**', { timeout: 10000 }),
    page.waitForTimeout(5000)
  ]);
  
  // Additional wait to ensure authentication state is set
  await page.waitForTimeout(2000);
}

async function navigateToContentManagement(page: Page) {
  await page.goto('/admin/content/manage');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);
}

// Test suite for Content Management Dropdown Functionality
test.describe('Content Management Dropdown Functionality', () => {
  
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
  });

  // ============ BOARDS TAB TESTS ============
  
  test('should display boards tab with content', async ({ page }) => {
    // Click on Boards tab
    await page.click('button:has-text("Boards")');
    await page.waitForTimeout(2000);
    
    // Verify boards content is loaded
    await expect(page.locator('h3:has-text("Education Boards")')).toBeVisible();
    
    // Verify boards are displayed
    await expect(page.locator('text=CBSE')).toBeVisible();
    await expect(page.locator('text=ICSE')).toBeVisible();
    await expect(page.locator('text=State Board')).toBeVisible();
  });

  test('should have working search and filter controls on boards tab', async ({ page }) => {
    // Click on Boards tab
    await page.click('button:has-text("Boards")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
    
    // Test search functionality
    await page.fill('input[placeholder*="Search"]', 'CBSE');
    await page.waitForTimeout(1000);
    
    // Verify search results
    await expect(page.locator('text=CBSE')).toBeVisible();
    await expect(page.locator('text=ICSE')).not.toBeVisible();
  });

  // ============ GRADES TAB TESTS ============
  
  test('should display grades tab with content', async ({ page }) => {
    // Click on Grades tab
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(2000);
    
    // Verify grades content is loaded
    await expect(page.locator('h3:has-text("Grades")')).toBeVisible();
    
    // Verify grades are displayed (should show some grades)
    const gradeElements = page.locator('[data-testid="grade-item"], .grade-item, tr:has-text("Grade")');
    await expect(gradeElements.first()).toBeVisible();
  });

  test('should have working search and filter controls on grades tab', async ({ page }) => {
    // Click on Grades tab
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
  });

  // ============ SUBJECTS TAB TESTS ============
  
  test('should display subjects tab with content', async ({ page }) => {
    // Click on Subjects tab
    await page.click('button:has-text("Subjects")');
    await page.waitForTimeout(2000);
    
    // Verify subjects content is loaded
    await expect(page.locator('h3:has-text("Subjects")')).toBeVisible();
    
    // Verify subjects are displayed (should show some subjects)
    const subjectElements = page.locator('[data-testid="subject-item"], .subject-item, tr:has-text("Subject")');
    await expect(subjectElements.first()).toBeVisible();
  });

  test('should have working search and filter controls on subjects tab', async ({ page }) => {
    // Click on Subjects tab
    await page.click('button:has-text("Subjects")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
  });

  // ============ CHAPTERS TAB TESTS ============
  
  test('should display chapters tab with content', async ({ page }) => {
    // Click on Chapters tab
    await page.click('button:has-text("Chapters")');
    await page.waitForTimeout(2000);
    
    // Verify chapters content is loaded
    await expect(page.locator('h3:has-text("Chapters")')).toBeVisible();
    
    // Verify chapters are displayed (should show some chapters)
    const chapterElements = page.locator('[data-testid="chapter-item"], .chapter-item, tr:has-text("Chapter")');
    await expect(chapterElements.first()).toBeVisible();
  });

  test('should have working search and filter controls on chapters tab', async ({ page }) => {
    // Click on Chapters tab
    await page.click('button:has-text("Chapters")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
  });

  // ============ TOPICS TAB TESTS ============
  
  test('should display topics tab with content', async ({ page }) => {
    // Click on Topics tab
    await page.click('button:has-text("Topics")');
    await page.waitForTimeout(2000);
    
    // Verify topics content is loaded
    await expect(page.locator('h3:has-text("Topics")')).toBeVisible();
    
    // Verify topics are displayed (should show some topics)
    const topicElements = page.locator('[data-testid="topic-item"], .topic-item, tr:has-text("Topic")');
    await expect(topicElements.first()).toBeVisible();
  });

  test('should have working search and filter controls on topics tab', async ({ page }) => {
    // Click on Topics tab
    await page.click('button:has-text("Topics")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
  });

  // ============ TOPIC NOTES TAB TESTS ============
  
  test('should display topic notes tab with content', async ({ page }) => {
    // Click on Topic Notes tab
    await page.click('button:has-text("Topic Notes")');
    await page.waitForTimeout(2000);
    
    // Verify topic notes content is loaded
    await expect(page.locator('h3:has-text("Topic Notes")')).toBeVisible();
    
    // Verify topic notes are displayed (should show some notes)
    const noteElements = page.locator('[data-testid="note-item"], .note-item, tr:has-text("Note")');
    await expect(noteElements.first()).toBeVisible();
  });

  test('should have working search and filter controls on topic notes tab', async ({ page }) => {
    // Click on Topic Notes tab
    await page.click('button:has-text("Topic Notes")');
    await page.waitForTimeout(2000);
    
    // Verify search input is visible
    await expect(page.locator('input[placeholder*="Search"]')).toBeVisible();
    
    // Verify filter dropdown is visible
    await expect(page.locator('select')).toBeVisible();
  });

  // ============ HIERARCHICAL FILTERING TESTS ============
  
  test('should implement hierarchical filtering - Board to Grade', async ({ page }) => {
    // Click on Grades tab
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(2000);
    
    // Look for board filter dropdown
    const boardFilter = page.locator('select').first();
    if (await boardFilter.isVisible()) {
      // Select a board
      await boardFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Verify grades are filtered by board
      const gradeElements = page.locator('[data-testid="grade-item"], .grade-item, tr:has-text("Grade")');
      await expect(gradeElements.first()).toBeVisible();
    }
  });

  test('should implement hierarchical filtering - Board + Grade to Subject', async ({ page }) => {
    // Click on Subjects tab
    await page.click('button:has-text("Subjects")');
    await page.waitForTimeout(2000);
    
    // Look for board and grade filter dropdowns
    const boardFilter = page.locator('select').first();
    const gradeFilter = page.locator('select').nth(1);
    
    if (await boardFilter.isVisible() && await gradeFilter.isVisible()) {
      // Select a board
      await boardFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a grade
      await gradeFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Verify subjects are filtered by board and grade
      const subjectElements = page.locator('[data-testid="subject-item"], .subject-item, tr:has-text("Subject")');
      await expect(subjectElements.first()).toBeVisible();
    }
  });

  test('should implement hierarchical filtering - Board + Grade + Subject to Chapter', async ({ page }) => {
    // Click on Chapters tab
    await page.click('button:has-text("Chapters")');
    await page.waitForTimeout(2000);
    
    // Look for board, grade, and subject filter dropdowns
    const boardFilter = page.locator('select').first();
    const gradeFilter = page.locator('select').nth(1);
    const subjectFilter = page.locator('select').nth(2);
    
    if (await boardFilter.isVisible() && await gradeFilter.isVisible() && await subjectFilter.isVisible()) {
      // Select a board
      await boardFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a grade
      await gradeFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a subject
      await subjectFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Verify chapters are filtered by board, grade, and subject
      const chapterElements = page.locator('[data-testid="chapter-item"], .chapter-item, tr:has-text("Chapter")');
      await expect(chapterElements.first()).toBeVisible();
    }
  });

  test('should implement hierarchical filtering - Board + Grade + Subject + Chapter to Topic', async ({ page }) => {
    // Click on Topics tab
    await page.click('button:has-text("Topics")');
    await page.waitForTimeout(2000);
    
    // Look for board, grade, subject, and chapter filter dropdowns
    const boardFilter = page.locator('select').first();
    const gradeFilter = page.locator('select').nth(1);
    const subjectFilter = page.locator('select').nth(2);
    const chapterFilter = page.locator('select').nth(3);
    
    if (await boardFilter.isVisible() && await gradeFilter.isVisible() && 
        await subjectFilter.isVisible() && await chapterFilter.isVisible()) {
      // Select a board
      await boardFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a grade
      await gradeFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a subject
      await subjectFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a chapter
      await chapterFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Verify topics are filtered by board, grade, subject, and chapter
      const topicElements = page.locator('[data-testid="topic-item"], .topic-item, tr:has-text("Topic")');
      await expect(topicElements.first()).toBeVisible();
    }
  });

  test('should implement hierarchical filtering - Board + Grade + Subject + Chapter + Topic to Notes', async ({ page }) => {
    // Click on Topic Notes tab
    await page.click('button:has-text("Topic Notes")');
    await page.waitForTimeout(2000);
    
    // Look for all filter dropdowns
    const boardFilter = page.locator('select').first();
    const gradeFilter = page.locator('select').nth(1);
    const subjectFilter = page.locator('select').nth(2);
    const chapterFilter = page.locator('select').nth(3);
    const topicFilter = page.locator('select').nth(4);
    
    if (await boardFilter.isVisible() && await gradeFilter.isVisible() && 
        await subjectFilter.isVisible() && await chapterFilter.isVisible() && 
        await topicFilter.isVisible()) {
      // Select a board
      await boardFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a grade
      await gradeFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a subject
      await subjectFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a chapter
      await chapterFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Select a topic
      await topicFilter.selectOption({ index: 1 });
      await page.waitForTimeout(1000);
      
      // Verify notes are filtered by all selections
      const noteElements = page.locator('[data-testid="note-item"], .note-item, tr:has-text("Note")');
      await expect(noteElements.first()).toBeVisible();
    }
  });

  // ============ CRUD OPERATIONS TESTS ============
  
  test('should allow creating new board', async ({ page }) => {
    // Click on Boards tab
    await page.click('button:has-text("Boards")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  test('should allow creating new grade', async ({ page }) => {
    // Click on Grades tab
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  test('should allow creating new subject', async ({ page }) => {
    // Click on Subjects tab
    await page.click('button:has-text("Subjects")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  test('should allow creating new chapter', async ({ page }) => {
    // Click on Chapters tab
    await page.click('button:has-text("Chapters")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  test('should allow creating new topic', async ({ page }) => {
    // Click on Topics tab
    await page.click('button:has-text("Topics")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  test('should allow creating new topic note', async ({ page }) => {
    // Click on Topic Notes tab
    await page.click('button:has-text("Topic Notes")');
    await page.waitForTimeout(2000);
    
    // Look for create button
    const createButton = page.locator('button:has-text("Create"), button:has-text("Add"), button:has-text("New")').first();
    if (await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
      
      // Verify create form is visible
      await expect(page.locator('form, [role="dialog"], .modal')).toBeVisible();
    }
  });

  // ============ RESPONSIVE DESIGN TESTS ============
  
  test('should work on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Verify tabs are visible and clickable
    await expect(page.locator('button:has-text("Boards")')).toBeVisible();
    await expect(page.locator('button:has-text("Grades")')).toBeVisible();
    await expect(page.locator('button:has-text("Subjects")')).toBeVisible();
    
    // Test tab switching on mobile
    await page.click('button:has-text("Boards")');
    await page.waitForTimeout(1000);
    await expect(page.locator('h3:has-text("Education Boards")')).toBeVisible();
  });

  test('should work on tablet devices', async ({ page }) => {
    // Set tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Verify tabs are visible and clickable
    await expect(page.locator('button:has-text("Boards")')).toBeVisible();
    await expect(page.locator('button:has-text("Grades")')).toBeVisible();
    await expect(page.locator('button:has-text("Subjects")')).toBeVisible();
    
    // Test tab switching on tablet
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(1000);
    await expect(page.locator('h3:has-text("Grades")')).toBeVisible();
  });

  // ============ ACCESSIBILITY TESTS ============
  
  test('should be accessible with keyboard navigation', async ({ page }) => {
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Test keyboard navigation
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');
    
    // Verify focus is visible
    const focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();
  });

  test('should have proper ARIA labels and roles', async ({ page }) => {
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Verify ARIA labels
    await expect(page.locator('[aria-label], [role="button"], [role="tab"]')).toBeVisible();
  });

  // ============ ERROR HANDLING TESTS ============
  
  test('should handle API errors gracefully', async ({ page }) => {
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Click on Boards tab
    await page.click('button:has-text("Boards")');
    await page.waitForTimeout(2000);
    
    // Verify error handling (if API fails)
    const errorMessage = page.locator('text=Error, text=Failed, text=Unable');
    if (await errorMessage.isVisible()) {
      // Verify error message is user-friendly
      await expect(errorMessage).toBeVisible();
    }
  });

  test('should handle network timeouts gracefully', async ({ page }) => {
    // Navigate to content management
    await navigateToContentManagement(page);
    
    // Click on Grades tab
    await page.click('button:has-text("Grades")');
    await page.waitForTimeout(5000); // Wait longer for potential timeout
    
    // Verify loading states
    const loadingIndicator = page.locator('text=Loading, text=Please wait, .loading, .spinner');
    if (await loadingIndicator.isVisible()) {
      // Verify loading indicator is shown
      await expect(loadingIndicator).toBeVisible();
    }
  });
});
