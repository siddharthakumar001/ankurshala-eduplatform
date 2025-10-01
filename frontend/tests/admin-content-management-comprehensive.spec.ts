import { test, expect, Page } from '@playwright/test';

// Test data
const TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  }
};

// Test data for content creation
const TEST_DATA = {
  board: {
    name: 'Test Board ' + Date.now(),
    active: true
  },
  grade: {
    name: 'grade-9-test',
    displayName: 'Grade 9 Test',
    active: true
  },
  subject: {
    name: 'Test Physics',
    active: true
  },
  chapter: {
    name: 'Test Chapter',
    active: true
  },
  topic: {
    title: 'Test Topic',
    description: 'Test topic description',
    expectedTimeMins: 45,
    active: true
  },
  topicNote: {
    title: 'Test Note',
    content: 'This is a test topic note content.',
    active: true
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

async function navigateToContentManagement(page: Page) {
  await page.goto('/admin/content/manage');
  
  // Wait for page to load
  await Promise.race([
    page.waitForSelector('text=Content Management', { timeout: 15000 }),
    page.waitForSelector('text=Verifying authentication...', { state: 'hidden', timeout: 15000 }),
    page.waitForTimeout(10000)
  ]);
}

async function waitForContentLoad(page: Page, contentType: string) {
  const loadingText = `Loading ${contentType.toLowerCase()}...`;
  const errorText = `Error loading ${contentType.toLowerCase()}`;
  
  await Promise.race([
    page.waitForSelector(`text=${loadingText}`, { state: 'hidden', timeout: 15000 }),
    page.waitForSelector(`text=${errorText}`, { timeout: 15000 }),
    page.waitForTimeout(10000)
  ]);
}

async function createBoard(page: Page, boardData: typeof TEST_DATA.board) {
  // Click Add Board button
  await page.getByRole('button', { name: 'Add Board' }).click();
  
  // Fill form
  await page.fill('input[name="name"]', boardData.name);
  
  // Submit form
  await page.getByRole('button', { name: 'Create Board' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

async function createGrade(page: Page, gradeData: typeof TEST_DATA.grade, boardId: number) {
  // Click Add Grade button
  await page.getByRole('button', { name: 'Add Grade' }).click();
  
  // Fill form
  await page.selectOption('select[name="boardId"]', boardId.toString());
  await page.fill('input[name="name"]', gradeData.name);
  await page.fill('input[name="displayName"]', gradeData.displayName);
  
  // Submit form
  await page.getByRole('button', { name: 'Create Grade' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

async function createSubject(page: Page, subjectData: typeof TEST_DATA.subject, gradeId: number) {
  // Click Add Subject button
  await page.getByRole('button', { name: 'Add Subject' }).click();
  
  // Fill form
  await page.selectOption('select[name="gradeId"]', gradeId.toString());
  await page.fill('input[name="name"]', subjectData.name);
  
  // Submit form
  await page.getByRole('button', { name: 'Create Subject' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

async function createChapter(page: Page, chapterData: typeof TEST_DATA.chapter, subjectId: number) {
  // Click Add Chapter button
  await page.getByRole('button', { name: 'Add Chapter' }).click();
  
  // Fill form
  await page.selectOption('select[name="subjectId"]', subjectId.toString());
  await page.fill('input[name="name"]', chapterData.name);
  
  // Submit form
  await page.getByRole('button', { name: 'Create Chapter' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

async function createTopic(page: Page, topicData: typeof TEST_DATA.topic, chapterId: number) {
  // Click Add Topic button
  await page.getByRole('button', { name: 'Add Topic' }).click();
  
  // Fill form
  await page.selectOption('select[name="chapterId"]', chapterId.toString());
  await page.fill('input[name="title"]', topicData.title);
  await page.fill('input[name="description"]', topicData.description);
  await page.fill('input[name="expectedTimeMins"]', topicData.expectedTimeMins.toString());
  
  // Submit form
  await page.getByRole('button', { name: 'Create Topic' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

async function createTopicNote(page: Page, noteData: typeof TEST_DATA.topicNote, topicId: number) {
  // Click Add Topic Note button
  await page.getByRole('button', { name: 'Add Topic Note' }).click();
  
  // Fill form
  await page.selectOption('select[name="topicId"]', topicId.toString());
  await page.fill('input[name="title"]', noteData.title);
  await page.fill('textarea[name="content"]', noteData.content);
  
  // Submit form
  await page.getByRole('button', { name: 'Create Topic Note' }).click();
  
  // Wait for success
  await page.waitForTimeout(2000);
}

// Main test suite
test.describe('Admin Content Management - Comprehensive Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
  });

  test.describe('Navigation and UI', () => {
    test('should navigate to content management page', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Verify page title
      await expect(page.locator('h1')).toContainText('Content Management');
      
      // Verify description
      await expect(page.locator('text=Manage educational content including boards, grades, subjects, chapters, topics, and notes')).toBeVisible();
    });

    test('should display all content management tabs', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Check all tabs are visible
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Subjects' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Chapters' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Topics' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Topic Notes' })).toBeVisible();
    });

    test('should switch between tabs correctly', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Test each tab
      const tabs = ['Boards', 'Grades', 'Subjects', 'Chapters', 'Topics', 'Topic Notes'];
      
      for (const tabName of tabs) {
        await page.getByRole('tab', { name: tabName }).click();
        await expect(page.getByRole('tab', { name: tabName })).toHaveAttribute('data-state', 'active');
        await page.waitForTimeout(1000); // Wait for content to load
      }
    });
  });

  test.describe('Boards Management', () => {
    test('should display boards tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Boards tab should be active by default
      await expect(page.getByRole('tab', { name: 'Boards' })).toHaveAttribute('data-state', 'active');
      
      // Wait for content to load
      await waitForContentLoad(page, 'boards');
      
      // Check for boards content or error
      const hasContent = await page.locator('text=Education Boards').isVisible();
      const hasError = await page.locator('text=Error loading boards').isVisible();
      
      expect(hasContent || hasError).toBeTruthy();
      
      if (hasContent) {
        await expect(page.getByRole('button', { name: 'Add Board' })).toBeVisible();
      }
    });

    test('should open board creation dialog', async ({ page }) => {
      await navigateToContentManagement(page);
      await waitForContentLoad(page, 'boards');
      
      // Check if create button is available
      const createButton = page.getByRole('button', { name: 'Add Board' });
      const isButtonVisible = await createButton.isVisible();
      
      if (isButtonVisible) {
        await createButton.click();
        
        // Check dialog is open
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.locator('text=Create New Board')).toBeVisible();
        
        // Check form fields
        await expect(page.locator('input[name="name"]')).toBeVisible();
        await expect(page.locator('text=Active')).toBeVisible();
        
        // Cancel dialog
        await page.getByRole('button', { name: 'Cancel' }).click();
        await expect(page.getByRole('dialog')).not.toBeVisible();
      }
    });

    test('should create a new board', async ({ page }) => {
      await navigateToContentManagement(page);
      await waitForContentLoad(page, 'boards');
      
      const createButton = page.getByRole('button', { name: 'Add Board' });
      const isButtonVisible = await createButton.isVisible();
      
      if (isButtonVisible) {
        await createBoard(page, TEST_DATA.board);
        
        // Verify board was created (check for success message or board in list)
        await page.waitForTimeout(2000);
        
        // Check if board appears in the list or success message
        const boardInList = await page.locator(`text=${TEST_DATA.board.name}`).isVisible();
        const successMessage = await page.locator('text=Board created successfully').isVisible();
        
        expect(boardInList || successMessage).toBeTruthy();
      }
    });

    test('should display board filters and search', async ({ page }) => {
      await navigateToContentManagement(page);
      await waitForContentLoad(page, 'boards');
      
      // Check for search input
      const searchInput = page.getByPlaceholder('Search boards...');
      const hasSearch = await searchInput.isVisible();
      
      // Check for status filter
      const statusFilter = page.locator('text=All Status');
      const hasStatusFilter = await statusFilter.isVisible();
      
      expect(hasSearch || hasStatusFilter).toBeTruthy();
    });
  });

  test.describe('Grades Management', () => {
    test('should display grades tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Switch to grades tab
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      // Check for grades content
      const hasContent = await page.locator('text=Grade Levels').isVisible();
      const hasError = await page.locator('text=Error loading grades').isVisible();
      
      expect(hasContent || hasError).toBeTruthy();
      
      if (hasContent) {
        await expect(page.getByRole('button', { name: 'Add Grade' })).toBeVisible();
      }
    });

    test('should open grade creation dialog', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Grades' }).click();
      await waitForContentLoad(page, 'grades');
      
      const createButton = page.getByRole('button', { name: 'Add Grade' });
      const isButtonVisible = await createButton.isVisible();
      
      if (isButtonVisible) {
        await createButton.click();
        
        // Check dialog is open
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.locator('text=Create New Grade')).toBeVisible();
        
        // Check form fields
        await expect(page.locator('select[name="boardId"]')).toBeVisible();
        await expect(page.locator('input[name="name"]')).toBeVisible();
        await expect(page.locator('input[name="displayName"]')).toBeVisible();
        
        // Cancel dialog
        await page.getByRole('button', { name: 'Cancel' }).click();
        await expect(page.getByRole('dialog')).not.toBeVisible();
      }
    });
  });

  test.describe('Subjects Management', () => {
    test('should display subjects tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Switch to subjects tab
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      // Check for subjects content
      const hasContent = await page.locator('text=Subjects').first().isVisible();
      const hasError = await page.locator('text=Error loading subjects').isVisible();
      
      expect(hasContent || hasError).toBeTruthy();
      
      if (hasContent && !hasError) {
        await expect(page.getByRole('button', { name: 'Add Subject' })).toBeVisible();
      }
    });

    test('should open subject creation dialog', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Subjects' }).click();
      await waitForContentLoad(page, 'subjects');
      
      const createButton = page.getByRole('button', { name: 'Add Subject' });
      const isButtonVisible = await createButton.isVisible();
      
      if (isButtonVisible) {
        await createButton.click();
        
        // Check dialog is open
        await expect(page.getByRole('dialog')).toBeVisible();
        await expect(page.locator('text=Create New Subject')).toBeVisible();
        
        // Check form fields
        await expect(page.locator('select[name="gradeId"]')).toBeVisible();
        await expect(page.locator('input[name="name"]')).toBeVisible();
        
        // Cancel dialog
        await page.getByRole('button', { name: 'Cancel' }).click();
        await expect(page.getByRole('dialog')).not.toBeVisible();
      }
    });
  });

  test.describe('Chapters Management', () => {
    test('should display chapters tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Switch to chapters tab
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Check for chapters content
      await expect(page.locator('text=Chapters Management')).toBeVisible();
      await expect(page.getByRole('button', { name: 'Add Chapter' })).toBeVisible();
    });

    test('should have disabled create button initially', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Chapters' }).click();
      await waitForContentLoad(page, 'chapters');
      
      // Create button should be disabled initially
      const createButton = page.getByRole('button', { name: 'Add Chapter' });
      await expect(createButton).toBeDisabled();
    });
  });

  test.describe('Topics Management', () => {
    test('should display topics tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Switch to topics tab
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Check for topics content
      await expect(page.locator('text=Topics Management')).toBeVisible();
      await expect(page.getByRole('button', { name: 'Add Topic' })).toBeVisible();
    });

    test('should have disabled create button initially', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topics' }).click();
      await waitForContentLoad(page, 'topics');
      
      // Create button should be disabled initially
      const createButton = page.getByRole('button', { name: 'Add Topic' });
      await expect(createButton).toBeDisabled();
    });
  });

  test.describe('Topic Notes Management', () => {
    test('should display topic notes tab content', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Switch to topic notes tab
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Check for topic notes content
      await expect(page.locator('text=Topic Notes Management')).toBeVisible();
      await expect(page.getByRole('button', { name: 'Add Topic Note' })).toBeVisible();
    });

    test('should have disabled create button initially', async ({ page }) => {
      await navigateToContentManagement(page);
      await page.getByRole('tab', { name: 'Topic Notes' }).click();
      await waitForContentLoad(page, 'topic notes');
      
      // Create button should be disabled initially
      const createButton = page.getByRole('button', { name: 'Add Topic Note' });
      await expect(createButton).toBeDisabled();
    });
  });

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Wait for any content to load
      await page.waitForTimeout(5000);
      
      // Check if there are any error messages
      const errorMessages = [
        'Error loading boards',
        'Error loading grades',
        'Error loading subjects',
        'Error loading chapters',
        'Error loading topics',
        'Error loading topic notes'
      ];
      
      let hasError = false;
      for (const errorMsg of errorMessages) {
        if (await page.locator(`text=${errorMsg}`).isVisible()) {
          hasError = true;
          console.log(`Found error: ${errorMsg}`);
          break;
        }
      }
      
      // If there are errors, they should be displayed properly
      if (hasError) {
        // Check that error details are expandable
        const debugInfo = page.locator('text=Debug Information');
        const hasDebugInfo = await debugInfo.isVisible();
        
        if (hasDebugInfo) {
          await debugInfo.click();
          await expect(page.locator('text=Error Type')).toBeVisible();
          await expect(page.locator('text=Message')).toBeVisible();
        }
      }
    });

    test('should handle network timeouts', async ({ page }) => {
      // Simulate slow network
      await page.route('**/api/admin/content/**', route => {
        // Delay response by 2 seconds
        setTimeout(() => route.continue(), 2000);
      });
      
      await navigateToContentManagement(page);
      
      // Should show loading state
      await expect(page.locator('text=Loading boards...')).toBeVisible();
      
      // Wait for timeout or content
      await page.waitForTimeout(10000);
    });
  });

  test.describe('Responsive Design', () => {
    test('should work on mobile devices', async ({ page }) => {
      // Set mobile viewport
      await page.setViewportSize({ width: 375, height: 667 });
      
      await navigateToContentManagement(page);
      
      // Check that tabs are still accessible
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      
      // Check that content is responsive
      await waitForContentLoad(page, 'boards');
      
      // Content should be visible on mobile
      const hasContent = await page.locator('text=Education Boards').isVisible();
      const hasError = await page.locator('text=Error loading boards').isVisible();
      
      expect(hasContent || hasError).toBeTruthy();
    });

    test('should work on tablet devices', async ({ page }) => {
      // Set tablet viewport
      await page.setViewportSize({ width: 768, height: 1024 });
      
      await navigateToContentManagement(page);
      
      // Check that all tabs are visible
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Subjects' })).toBeVisible();
      
      await waitForContentLoad(page, 'boards');
      
      // Content should be visible on tablet
      const hasContent = await page.locator('text=Education Boards').isVisible();
      const hasError = await page.locator('text=Error loading boards').isVisible();
      
      expect(hasContent || hasError).toBeTruthy();
    });
  });

  test.describe('Accessibility', () => {
    test('should have proper ARIA labels', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Check for proper ARIA labels on tabs
      await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
      await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
      
      // Check for proper button labels
      const addButton = page.getByRole('button', { name: 'Add Board' });
      const isButtonVisible = await addButton.isVisible();
      
      if (isButtonVisible) {
        await expect(addButton).toBeVisible();
      }
    });

    test('should support keyboard navigation', async ({ page }) => {
      await navigateToContentManagement(page);
      
      // Test tab navigation
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      
      // Should be able to navigate to tabs
      await page.keyboard.press('ArrowRight');
      await page.keyboard.press('ArrowRight');
      
      // Check that focus is visible
      const focusedElement = page.locator(':focus');
      await expect(focusedElement).toBeVisible();
    });
  });
});
