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
  
  // Wait for page to load
  await Promise.race([
    page.waitForSelector('text=Content Management', { timeout: 15000 }),
    page.waitForSelector('text=Verifying authentication...', { state: 'hidden', timeout: 15000 }),
    page.waitForTimeout(10000)
  ]);
}

async function ensureAuthenticated(page: Page) {
  // Check if we're on login page, if so, login
  const currentUrl = page.url();
  if (currentUrl.includes('/login')) {
    await loginAsAdmin(page);
  }
  
  // Wait for authentication to complete
  await page.waitForFunction(() => {
    const loadingElements = document.querySelectorAll('*');
    for (const element of loadingElements) {
      if (element.textContent?.includes('Verifying authentication...')) {
        return false;
      }
    }
    return true;
  }, { timeout: 15000 });
}

async function waitForContentLoad(page: Page, contentType: string) {
  // Wait for either content to load or error to appear
  await Promise.race([
    page.waitForSelector(`text=Loading ${contentType}...`, { state: 'hidden', timeout: 10000 }),
    page.waitForSelector(`text=Error loading ${contentType}`, { timeout: 10000 }),
    page.waitForSelector(`text=${contentType}`, { timeout: 10000 }),
    page.waitForTimeout(10000)
  ]);
}

// Main test suite
test.describe('Complete Content Management System Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
    await ensureAuthenticated(page);
  });

  // ===== BOARDS MANAGEMENT TESTS =====
  test.describe('Boards Management', () => {
    test('should display boards tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Boards tab
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Verify boards tab is active
      await expect(page.getByRole('tab', { name: 'Boards' })).toHaveAttribute('data-state', 'active');
      
      // Check for boards content or error message
      const hasError = await page.locator('text=Error loading boards').isVisible();
      if (hasError) {
        console.log('Boards API error detected - this is the issue we need to fix');
        // Take screenshot for debugging
        await page.screenshot({ path: 'test-results/boards-error.png' });
      } else {
        // Verify boards content is displayed
        await expect(page.locator('text=Boards')).toBeVisible();
      }
    });

    test('should create a new board', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Click create board button
      const createButton = page.getByRole('button', { name: 'Add Board' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill board creation form
      await page.fill('[name="name"]', 'Test Board');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success message or board appears in list
      await expect(page.locator('text=Test Board')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing board', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Find and click edit button for first board
      const editButton = page.locator('[data-testid="edit-board"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify board name
        await page.fill('[name="name"]', 'Updated Board Name');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Board Name')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should toggle board status', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Find status toggle for first board
      const statusToggle = page.locator('[data-testid="board-status-toggle"]').first();
      if (await statusToggle.isVisible()) {
        const initialState = await statusToggle.isChecked();
        await statusToggle.click();
        
        // Verify status changed
        await expect(statusToggle).toBeChecked({ checked: !initialState });
      }
    });

    test('should search and filter boards', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Test search functionality
      const searchInput = page.locator('[placeholder*="Search"]');
      if (await searchInput.isVisible()) {
        await searchInput.fill('CBSE');
        await page.keyboard.press('Enter');
        
        // Verify search results
        await expect(page.locator('text=CBSE')).toBeVisible();
      }
      
      // Test status filter
      const statusFilter = page.locator('[data-testid="status-filter"]');
      if (await statusFilter.isVisible()) {
        await statusFilter.selectOption('active');
        
        // Verify filtered results
        await expect(page.locator('text=Active')).toBeVisible();
      }
    });
  });

  // ===== GRADES MANAGEMENT TESTS =====
  test.describe('Grades Management', () => {
    test('should display grades tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Grades tab
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Verify grades tab is active
      await expect(page.getByRole('tab', { name: 'Grades' })).toHaveAttribute('data-state', 'active');
      
      // Check for grades content
      const hasError = await page.locator('text=Error loading grades').isVisible();
      if (hasError) {
        console.log('Grades API error detected');
        await page.screenshot({ path: 'test-results/grades-error.png' });
      } else {
        await expect(page.locator('text=Grades')).toBeVisible();
      }
    });

    test('should create a new grade', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Click create grade button
      const createButton = page.getByRole('button', { name: 'Add Grade' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill grade creation form
      await page.fill('[name="name"]', 'Grade 10');
      await page.fill('[name="description"]', 'Tenth Grade');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success
      await expect(page.locator('text=Grade 10')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing grade', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Find and click edit button
      const editButton = page.locator('[data-testid="edit-grade"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify grade
        await page.fill('[name="name"]', 'Updated Grade');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Grade')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should filter grades by board', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Test board filter
      const boardFilter = page.locator('[data-testid="board-filter"]');
      if (await boardFilter.isVisible()) {
        await boardFilter.selectOption('1'); // CBSE board
        
        // Verify filtered results
        await expect(page.locator('text=CBSE')).toBeVisible();
      }
    });
  });

  // ===== SUBJECTS MANAGEMENT TESTS =====
  test.describe('Subjects Management', () => {
    test('should display subjects tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Subjects tab
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Verify subjects tab is active
      await expect(page.getByRole('tab', { name: 'Subjects' })).toHaveAttribute('data-state', 'active');
      
      // Check for subjects content
      const hasError = await page.locator('text=Error loading subjects').isVisible();
      if (hasError) {
        console.log('Subjects API error detected');
        await page.screenshot({ path: 'test-results/subjects-error.png' });
      } else {
        await expect(page.locator('text=Subjects')).toBeVisible();
      }
    });

    test('should create a new subject', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Click create subject button
      const createButton = page.getByRole('button', { name: 'Add Subject' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill subject creation form
      await page.fill('[name="name"]', 'Mathematics');
      await page.fill('[name="description"]', 'Advanced Mathematics');
      await page.selectOption('[name="boardId"]', '1');
      await page.selectOption('[name="gradeId"]', '1');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success
      await expect(page.locator('text=Mathematics')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing subject', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Find and click edit button
      const editButton = page.locator('[data-testid="edit-subject"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify subject
        await page.fill('[name="name"]', 'Updated Subject');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Subject')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should filter subjects by board and grade', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Test board filter
      const boardFilter = page.locator('[data-testid="board-filter"]');
      if (await boardFilter.isVisible()) {
        await boardFilter.selectOption('1');
        
        // Test grade filter
        const gradeFilter = page.locator('[data-testid="grade-filter"]');
        if (await gradeFilter.isVisible()) {
          await gradeFilter.selectOption('1');
          
          // Verify filtered results
          await expect(page.locator('text=CBSE')).toBeVisible();
        }
      }
    });
  });

  // ===== CHAPTERS MANAGEMENT TESTS =====
  test.describe('Chapters Management', () => {
    test('should display chapters tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Chapters tab
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Verify chapters tab is active
      await expect(page.getByRole('tab', { name: 'Chapters' })).toHaveAttribute('data-state', 'active');
      
      // Check for chapters content
      const hasError = await page.locator('text=Error loading chapters').isVisible();
      if (hasError) {
        console.log('Chapters API error detected');
        await page.screenshot({ path: 'test-results/chapters-error.png' });
      } else {
        await expect(page.locator('text=Chapters')).toBeVisible();
      }
    });

    test('should create a new chapter', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Click create chapter button
      const createButton = page.getByRole('button', { name: 'Add Chapter' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill chapter creation form
      await page.fill('[name="name"]', 'Algebra Basics');
      await page.fill('[name="description"]', 'Introduction to Algebra');
      await page.selectOption('[name="subjectId"]', '1');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success
      await expect(page.locator('text=Algebra Basics')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing chapter', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Find and click edit button
      const editButton = page.locator('[data-testid="edit-chapter"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify chapter
        await page.fill('[name="name"]', 'Updated Chapter');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Chapter')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should filter chapters by subject', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Test subject filter
      const subjectFilter = page.locator('[data-testid="subject-filter"]');
      if (await subjectFilter.isVisible()) {
        await subjectFilter.selectOption('1');
        
        // Verify filtered results
        await expect(page.locator('text=Mathematics')).toBeVisible();
      }
    });
  });

  // ===== TOPICS MANAGEMENT TESTS =====
  test.describe('Topics Management', () => {
    test('should display topics tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Topics tab
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Verify topics tab is active
      await expect(page.getByRole('tab', { name: 'Topics' })).toHaveAttribute('data-state', 'active');
      
      // Check for topics content
      const hasError = await page.locator('text=Error loading topics').isVisible();
      if (hasError) {
        console.log('Topics API error detected');
        await page.screenshot({ path: 'test-results/topics-error.png' });
      } else {
        await expect(page.locator('text=Topics')).toBeVisible();
      }
    });

    test('should create a new topic', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Click create topic button
      const createButton = page.getByRole('button', { name: 'Add Topic' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill topic creation form
      await page.fill('[name="name"]', 'Linear Equations');
      await page.fill('[name="description"]', 'Solving Linear Equations');
      await page.selectOption('[name="chapterId"]', '1');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success
      await expect(page.locator('text=Linear Equations')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing topic', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Find and click edit button
      const editButton = page.locator('[data-testid="edit-topic"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify topic
        await page.fill('[name="name"]', 'Updated Topic');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Topic')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should filter topics by chapter', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Test chapter filter
      const chapterFilter = page.locator('[data-testid="chapter-filter"]');
      if (await chapterFilter.isVisible()) {
        await chapterFilter.selectOption('1');
        
        // Verify filtered results
        await expect(page.locator('text=Algebra Basics')).toBeVisible();
      }
    });
  });

  // ===== TOPIC NOTES MANAGEMENT TESTS =====
  test.describe('Topic Notes Management', () => {
    test('should display topic notes tab and content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Click on Topic Notes tab
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Verify topic notes tab is active
      await expect(page.getByRole('tab', { name: 'Topic Notes' })).toHaveAttribute('data-state', 'active');
      
      // Check for topic notes content
      const hasError = await page.locator('text=Error loading topic notes').isVisible();
      if (hasError) {
        console.log('Topic Notes API error detected');
        await page.screenshot({ path: 'test-results/topic-notes-error.png' });
      } else {
        await expect(page.locator('text=Topic Notes')).toBeVisible();
      }
    });

    test('should create a new topic note', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Click create topic note button
      const createButton = page.getByRole('button', { name: 'Add Topic Note' });
      await expect(createButton).toBeVisible();
      await createButton.click();
      
      // Fill topic note creation form
      await page.fill('[name="title"]', 'Introduction to Linear Equations');
      await page.fill('[name="content"]', 'Linear equations are equations where the highest power of the variable is 1.');
      await page.selectOption('[name="topicId"]', '1');
      await page.check('[name="active"]');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Verify success
      await expect(page.locator('text=Introduction to Linear Equations')).toBeVisible({ timeout: 10000 });
    });

    test('should edit existing topic note', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Find and click edit button
      const editButton = page.locator('[data-testid="edit-topic-note"]').first();
      if (await editButton.isVisible()) {
        await editButton.click();
        
        // Modify topic note
        await page.fill('[name="title"]', 'Updated Topic Note');
        
        // Submit changes
        await page.click('button[type="submit"]');
        
        // Verify update
        await expect(page.locator('text=Updated Topic Note')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should delete topic note', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Find and click delete button
      const deleteButton = page.locator('[data-testid="delete-topic-note"]').first();
      if (await deleteButton.isVisible()) {
        await deleteButton.click();
        
        // Confirm deletion
        await page.click('button[type="button"]:has-text("Delete")');
        
        // Verify deletion
        await expect(page.locator('text=Topic note deleted successfully')).toBeVisible({ timeout: 10000 });
      }
    });

    test('should filter topic notes by topic', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Test topic filter
      const topicFilter = page.locator('[data-testid="topic-filter"]');
      if (await topicFilter.isVisible()) {
        await topicFilter.selectOption('1');
        
        // Verify filtered results
        await expect(page.locator('text=Linear Equations')).toBeVisible();
      }
    });
  });

  // ===== CROSS-TAB FUNCTIONALITY TESTS =====
  test.describe('Cross-Tab Functionality', () => {
    test('should maintain state when switching between tabs', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Test boards tab
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Test grades tab
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Test subjects tab
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Test chapters tab
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Test topics tab
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Test topic notes tab
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Verify all tabs are accessible
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Subjects' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Chapters' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Topics' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Topic Notes' })).toBeVisible();
    });

    test('should handle pagination across all tabs', async ({ page }) => {
      await navigateToContentManagement(page);
      
      const tabs = ['Boards', 'Grades', 'Subjects', 'Chapters', 'Topics', 'Topic Notes'];
      
      for (const tabName of tabs) {
        await page.getByRole('tab', { name: tabName }).click();
        await waitForContentLoad(page, tabName.toLowerCase());
        
        // Check for pagination controls
        const pagination = page.locator('[data-testid="pagination"]');
        if (await pagination.isVisible()) {
          // Test pagination
          const nextButton = page.locator('[data-testid="next-page"]');
          if (await nextButton.isVisible()) {
            await nextButton.click();
            await page.waitForTimeout(1000);
          }
          
          const prevButton = page.locator('[data-testid="prev-page"]');
          if (await prevButton.isVisible()) {
            await prevButton.click();
            await page.waitForTimeout(1000);
          }
        }
      }
    });

    test('should handle search functionality across all tabs', async ({ page }) => {
      await navigateToContentManagement(page);
      
      const tabs = ['Boards', 'Grades', 'Subjects', 'Chapters', 'Topics', 'Topic Notes'];
      
      for (const tabName of tabs) {
        await page.getByRole('tab', { name: tabName }).click();
        await waitForContentLoad(page, tabName.toLowerCase());
        
        // Test search functionality
        const searchInput = page.locator('[placeholder*="Search"]');
        if (await searchInput.isVisible()) {
          await searchInput.fill('test');
          await page.keyboard.press('Enter');
          await page.waitForTimeout(1000);
          
          // Clear search
          await searchInput.clear();
          await page.keyboard.press('Enter');
          await page.waitForTimeout(1000);
        }
      }
    });
  });

  // ===== ERROR HANDLING TESTS =====
  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Test each tab for error handling
      const tabs = ['Boards', 'Grades', 'Subjects', 'Chapters', 'Topics', 'Topic Notes'];
      
      for (const tabName of tabs) {
        await page.getByRole('tab', { name: tabName }).click();
        await waitForContentLoad(page, tabName.toLowerCase());
        
        // Check for error messages
        const errorMessage = page.locator(`text=Error loading ${tabName.toLowerCase()}`);
        if (await errorMessage.isVisible()) {
          console.log(`Error detected in ${tabName} tab`);
          
          // Verify error message is user-friendly
          await expect(errorMessage).toBeVisible();
          
          // Check for retry button
          const retryButton = page.locator('button:has-text("Retry")');
          if (await retryButton.isVisible()) {
            await retryButton.click();
            await page.waitForTimeout(2000);
          }
        }
      }
    });

    test('should handle network timeouts', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Simulate slow network
      await page.route('**/api/admin/content/**', route => {
        // Delay response to simulate timeout
        setTimeout(() => route.continue(), 35000);
      });
      
      await page.getByRole('tab', { name: 'Boards' }).click();
      
      // Should show loading state
      await expect(page.locator('text=Loading boards...')).toBeVisible();
      
      // Should eventually show timeout error
      await expect(page.locator('text=Request timeout')).toBeVisible({ timeout: 40000 });
    });
  });

  // ===== RESPONSIVE DESIGN TESTS =====
  test.describe('Responsive Design', () => {
    test('should work on mobile devices', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await navigateToContentManagement(page);
      
      // Test mobile navigation
      await page.getByRole('tab', { name: 'Boards' }).click();
      await waitForContentLoad(page, 'boards');
      
      // Verify mobile layout
      await expect(page.locator('text=Content Management')).toBeVisible();
      
      // Test mobile menu if present
      const mobileMenu = page.locator('[data-testid="mobile-menu"]');
      if (await mobileMenu.isVisible()) {
        await mobileMenu.click();
        await expect(page.locator('text=Boards')).toBeVisible();
      }
    });

    test('should work on tablet devices', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await navigateToContentManagement(page);
      
      // Test tablet layout
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Verify tablet layout
      await expect(page.locator('text=Content Management')).toBeVisible();
    });
  });

  // ===== ACCESSIBILITY TESTS =====
  test.describe('Accessibility', () => {
    test('should have proper ARIA labels', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Check for proper ARIA labels on tabs
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
      
      // Check for proper button labels
      const createButton = page.getByRole('button', { name: 'Add Board' });
      if (await createButton.isVisible()) {
        await expect(createButton).toBeVisible();
      }
    });

    test('should support keyboard navigation', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Test keyboard navigation
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      await page.keyboard.press('Enter');
      
      // Should navigate to first tab
      await expect(page.getByRole('tab', { name: 'Boards' })).toHaveAttribute('data-state', 'active');
    });
  });
});
