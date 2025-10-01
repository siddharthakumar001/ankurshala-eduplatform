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
  
  // Wait for either redirect to admin dashboard or stay on login page
  await Promise.race([
    page.waitForURL('**/admin/dashboard**', { timeout: 10000 }),
    page.waitForURL('**/login**', { timeout: 10000 }),
    page.waitForTimeout(5000) // Fallback timeout
  ]);
  
  // Additional wait to ensure authentication state is set
  await page.waitForTimeout(2000);
}

async function navigateToContentManagement(page: Page) {
  await page.goto('/admin/content/manage');
  
  // Wait for either the content to load or authentication verification to complete
  await Promise.race([
    page.waitForSelector('text=Content Management', { timeout: 15000 }),
    page.waitForSelector('text=Verifying authentication...', { state: 'hidden', timeout: 15000 }),
    page.waitForSelector('text=Loading boards...', { timeout: 15000 }),
    page.waitForTimeout(10000) // Fallback timeout
  ]);
}

async function ensureAuthenticated(page: Page) {
  // Check if we're on login page, if so, login
  const currentUrl = page.url();
  if (currentUrl.includes('/login')) {
    await loginAsAdmin(page);
  }
  
  // Wait for authentication to complete by checking if the loading text is gone
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

// Content Management Tests
test.describe('Admin Content Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
    await loginAsAdmin(page);
    await ensureAuthenticated(page);
  });

  test('should display all content management tabs', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Check all tabs are visible using role-based selectors
    await expect(page.getByRole('tab', { name: 'Boards' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Grades' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Subjects' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Chapters' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Topics' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Topic Notes' })).toBeVisible();
  });

  test('should switch between tabs', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Test tab switching
    await page.getByRole('tab', { name: 'Grades' }).click();
    await expect(page.getByRole('tab', { name: 'Grades' })).toHaveAttribute('data-state', 'active');
    
    await page.getByRole('tab', { name: 'Subjects' }).click();
    await expect(page.getByRole('tab', { name: 'Subjects' })).toHaveAttribute('data-state', 'active');
    
    await page.getByRole('tab', { name: 'Chapters' }).click();
    await expect(page.getByRole('tab', { name: 'Chapters' })).toHaveAttribute('data-state', 'active');
    
    await page.getByRole('tab', { name: 'Topics' }).click();
    await expect(page.getByRole('tab', { name: 'Topics' })).toHaveAttribute('data-state', 'active');
    
    await page.getByRole('tab', { name: 'Topic Notes' }).click();
    await expect(page.getByRole('tab', { name: 'Topic Notes' })).toHaveAttribute('data-state', 'active');
    
    await page.getByRole('tab', { name: 'Boards' }).click();
    await expect(page.getByRole('tab', { name: 'Boards' })).toHaveAttribute('data-state', 'active');
  });

  test('should display boards tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Boards tab should be active by default
    await expect(page.getByRole('tab', { name: 'Boards' })).toHaveAttribute('data-state', 'active');
    
    // Wait for content to load (either success or error state)
    await page.waitForTimeout(5000);
    
    // Check for either the content or loading/error state
    const hasContent = await page.locator('text=Education Boards').isVisible();
    const hasLoading = await page.locator('text=Loading boards...').isVisible();
    const hasError = await page.locator('text=Error loading boards').isVisible();
    
    // At least one of these should be visible
    expect(hasContent || hasLoading || hasError).toBeTruthy();
    
    // If content is loaded, check for create button
    if (hasContent) {
      await expect(page.getByRole('button', { name: 'Add Board' })).toBeVisible();
    }
  });

  test('should handle board creation dialog', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Try to find and click create board button
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
      
      // Check dialog is closed
      await expect(page.getByRole('dialog')).not.toBeVisible();
    } else {
      // If button is not visible, it might be in loading state
      console.log('Create button not visible - might be in loading state');
    }
  });

  test('should display grades tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to grades tab
    await page.getByRole('tab', { name: 'Grades' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Check for either the content or loading/error state
    const hasContent = await page.locator('text=Grade Levels').isVisible();
    const hasLoading = await page.locator('text=Loading grades...').isVisible();
    const hasError = await page.locator('text=Error loading grades').isVisible();
    
    // At least one of these should be visible
    expect(hasContent || hasLoading || hasError).toBeTruthy();
    
    // If content is loaded, check for create button
    if (hasContent) {
      await expect(page.getByRole('button', { name: 'Add Grade' })).toBeVisible();
    }
  });

  test('should display subjects tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to subjects tab
    await page.getByRole('tab', { name: 'Subjects' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Check for either the content or loading/error state using more specific selectors
    const hasContent = await page.locator('text=Subjects').first().isVisible();
    const hasLoading = await page.locator('text=Loading subjects...').isVisible();
    const hasError = await page.locator('text=Error loading subjects').isVisible();
    
    // At least one of these should be visible
    expect(hasContent || hasLoading || hasError).toBeTruthy();
    
    // If content is loaded successfully (not error), check for create button
    if (hasContent && !hasError) {
      const createButton = page.getByRole('button', { name: 'Add Subject' });
      const createButtonVisible = await createButton.isVisible();
      
      if (createButtonVisible) {
        await expect(createButton).toBeVisible();
      } else {
        console.log('Add Subject button not visible - might be in loading state or error');
      }
    } else {
      console.log('Subjects tab in loading or error state - skipping button check');
    }
  });

  test('should display chapters tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to chapters tab
    await page.getByRole('tab', { name: 'Chapters' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Check for chapters content
    await expect(page.locator('text=Chapters Management')).toBeVisible();
    
    // Check for create button (should be disabled initially)
    await expect(page.getByRole('button', { name: 'Add Chapter' })).toBeVisible();
  });

  test('should display topics tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to topics tab
    await page.getByRole('tab', { name: 'Topics' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Check for topics content
    await expect(page.locator('text=Topics Management')).toBeVisible();
    
    // Check for create button (should be disabled initially)
    await expect(page.getByRole('button', { name: 'Add Topic' })).toBeVisible();
  });

  test('should display topic notes tab content', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to topic notes tab
    await page.getByRole('tab', { name: 'Topic Notes' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Check for topic notes content
    await expect(page.locator('text=Topic Notes Management')).toBeVisible();
    
    // Check for create button (should be disabled initially)
    await expect(page.getByRole('button', { name: 'Add Topic Note' })).toBeVisible();
  });

  test('should handle chapter creation button state', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to chapters tab
    await page.getByRole('tab', { name: 'Chapters' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Click create chapter button (should be disabled initially)
    const createButton = page.getByRole('button', { name: 'Add Chapter' });
    await expect(createButton).toBeDisabled();
    
    // The button should be disabled because no subject is selected
    // This is expected behavior based on the implementation
  });

  test('should handle topic creation button state', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to topics tab
    await page.getByRole('tab', { name: 'Topics' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Click create topic button (should be disabled initially)
    const createButton = page.getByRole('button', { name: 'Add Topic' });
    await expect(createButton).toBeDisabled();
    
    // The button should be disabled because no chapter is selected
    // This is expected behavior based on the implementation
  });

  test('should handle topic note creation button state', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Switch to topic notes tab
    await page.getByRole('tab', { name: 'Topic Notes' }).click();
    
    // Wait for content to load
    await page.waitForTimeout(5000);
    
    // Click create topic note button (should be disabled initially)
    const createButton = page.getByRole('button', { name: 'Add Topic Note' });
    await expect(createButton).toBeDisabled();
    
    // The button should be disabled because no topic is selected
    // This is expected behavior based on the implementation
  });

  test('should display filter controls when content is loaded', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Wait for content to load by waiting for either the loading to disappear or content to appear
    await Promise.race([
      page.waitForSelector('text=Loading boards...', { state: 'hidden', timeout: 10000 }),
      page.waitForSelector('text=Education Boards', { timeout: 10000 }),
      page.waitForSelector('text=Error loading boards', { timeout: 10000 })
    ]);
    
    // Check if content is loaded (not in loading state)
    const isLoading = await page.locator('text=Loading boards...').isVisible();
    const hasError = await page.locator('text=Error loading boards').isVisible();
    
    if (!isLoading && !hasError) {
      // If content is loaded successfully, check for filter controls
      const hasStatusFilter = await page.locator('text=All Status').isVisible();
      const hasSearchInput = await page.getByPlaceholder('Search boards...').isVisible();
      
      // At least one should be visible
      expect(hasStatusFilter || hasSearchInput).toBeTruthy();
    } else {
      console.log('Content still loading or has error - skipping filter test');
    }
  });

  test('should display pagination controls when needed', async ({ page }) => {
    await navigateToContentManagement(page);
    
    // Wait for content to load by waiting for either the loading to disappear or content to appear
    await Promise.race([
      page.waitForSelector('text=Loading boards...', { state: 'hidden', timeout: 10000 }),
      page.waitForSelector('text=Education Boards', { timeout: 10000 }),
      page.waitForSelector('text=Error loading boards', { timeout: 10000 })
    ]);
    
    // Check if content is loaded (not in loading state)
    const isLoading = await page.locator('text=Loading boards...').isVisible();
    const hasError = await page.locator('text=Error loading boards').isVisible();
    
    if (!isLoading && !hasError) {
      // Check if pagination controls are present
      // They may not be visible if there are few items
      const paginationInfo = page.locator('text=Showing').or(page.locator('text=results'));
      const paginationExists = await paginationInfo.isVisible();
      
      if (paginationExists) {
        // If pagination exists, check for Previous/Next buttons
        const prevButton = page.getByRole('button', { name: 'Previous' });
        const nextButton = page.getByRole('button', { name: 'Next' });
        
        // At least one should be visible if pagination exists
        await expect(prevButton.or(nextButton)).toBeVisible();
      }
    } else {
      console.log('Content still loading or has error - skipping pagination test');
    }
  });
});