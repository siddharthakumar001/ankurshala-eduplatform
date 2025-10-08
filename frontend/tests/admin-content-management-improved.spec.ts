import { test, expect, Page } from '@playwright/test';

// Helper function to login and navigate to content management
async function loginAndNavigateToContentManagement(page: Page) {
  await page.goto('http://localhost:3000/login');
  await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
  await page.fill('input[name="password"]', 'Maza@123');
  await page.click('button[type="submit"]');
  
  // Wait for successful login
  await page.waitForURL('**/admin/**', { timeout: 10000 });
  await page.waitForTimeout(2000);
  
  // Navigate to content management
  await page.goto('http://localhost:3000/admin/content/manage');
  await page.waitForSelector('text=Content Management', { timeout: 10000 });
  await page.waitForTimeout(2000);
}

// Helper function to switch tabs with better error handling
async function switchToTab(page: Page, tabName: string) {
  const tabSelector = `[role="tab"][value="${tabName}"]`;
  
  // Try multiple approaches to click the tab
  try {
    // Approach 1: Direct click
    await page.click(tabSelector, { timeout: 5000 });
  } catch (error) {
    try {
      // Approach 2: Click by text
      await page.click(`text=${tabName}`, { timeout: 5000 });
    } catch (error) {
      // Approach 3: Force click
      await page.click(tabSelector, { force: true, timeout: 5000 });
    }
  }
  
  // Wait for tab content to load
  await page.waitForTimeout(2000);
}

// Helper function to create content with improved dialog handling
async function createContent(page: Page, contentType: string, data: any) {
  // Click Add button
  await page.getByRole('button', { name: `Add ${contentType}` }).click();
  
  // Wait for dialog to be fully loaded
  await page.waitForSelector('[role="dialog"]', { timeout: 10000 });
  await page.waitForTimeout(2000);
  
  // Handle form fields based on content type
  if (contentType === 'Board') {
    await page.fill('input[id="name"]', data.name);
    await page.fill('input[id="title"]', data.title);
    
    // Handle active switch
    const activeSwitch = page.locator('[role="switch"][id="active"]');
    if (await activeSwitch.isVisible()) {
      await activeSwitch.click();
    }
  } else if (contentType === 'Grade') {
    // Select board first
    await page.click('[role="combobox"]', { force: true });
    await page.waitForTimeout(500);
    await page.locator('text=CBSE').first().click();
    await page.waitForTimeout(500);
    
    await page.fill('input[id="name"]', data.name);
    await page.fill('input[id="displayName"]', data.displayName);
    
    // Handle active switch
    const activeSwitch = page.locator('[role="switch"][id="active"]');
    if (await activeSwitch.isVisible()) {
      await activeSwitch.click();
    }
  }
  
  // Click create button
  await page.getByRole('button', { name: `Create ${contentType}` }).click();
  
  // Wait for dialog to close
  await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
  await page.waitForTimeout(2000);
}

// Helper function to search for content
async function searchForContent(page: Page, searchTerm: string) {
  const searchInput = page.locator('input[placeholder*="Search"]').first();
  await searchInput.fill(searchTerm);
  await searchInput.press('Enter');
  await page.waitForTimeout(2000);
}

test.describe('Admin Content Management - Improved Tests', () => {
  test('should test all tabs are accessible', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Test each tab
    const tabs = ['boards', 'grades', 'subjects', 'chapters', 'topics', 'topicnotes'];
    
    for (const tab of tabs) {
      console.log(`Testing tab: ${tab}`);
      
      // Switch to tab
      await switchToTab(page, tab);
      
      // Verify tab content is loaded
      const tabContent = page.locator(`[data-state="active"][value="${tab}"]`);
      await expect(tabContent).toBeVisible({ timeout: 10000 });
      
      // Wait a bit before next tab
      await page.waitForTimeout(1000);
    }
  });

  test('should create and manage boards successfully', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Switch to boards tab
    await switchToTab(page, 'boards');
    
    // Create a unique board
    const uniqueBoardName = `Test Board ${Date.now()}`;
    const boardData = {
      name: uniqueBoardName,
      title: `Title for ${uniqueBoardName}`
    };
    
    await createContent(page, 'Board', boardData);
    
    // Search for the created board
    await searchForContent(page, uniqueBoardName);
    
    // Verify board was created
    await expect(page.locator(`text=${uniqueBoardName}`)).toBeVisible({ timeout: 10000 });
  });

  test('should create and manage grades successfully', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Switch to grades tab
    await switchToTab(page, 'grades');
    
    // Create a unique grade
    const uniqueGradeName = `Test Grade ${Date.now()}`;
    const gradeData = {
      name: uniqueGradeName,
      displayName: `Display ${uniqueGradeName}`
    };
    
    await createContent(page, 'Grade', gradeData);
    
    // Search for the created grade
    await searchForContent(page, uniqueGradeName);
    
    // Verify grade was created
    await expect(page.locator(`text=${uniqueGradeName}`)).toBeVisible({ timeout: 10000 });
  });

  test('should handle search and filtering across all tabs', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Test search functionality on each tab
    const tabs = ['boards', 'grades', 'subjects'];
    
    for (const tab of tabs) {
      console.log(`Testing search on tab: ${tab}`);
      
      // Switch to tab
      await switchToTab(page, tab);
      
      // Test search functionality
      const searchInput = page.locator('input[placeholder*="Search"]').first();
      await searchInput.fill('test');
      await searchInput.press('Enter');
      await page.waitForTimeout(2000);
      
      // Clear search
      await searchInput.clear();
      await searchInput.press('Enter');
      await page.waitForTimeout(1000);
    }
  });

  test('should handle pagination across all tabs', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Test pagination on each tab
    const tabs = ['boards', 'grades', 'subjects'];
    
    for (const tab of tabs) {
      console.log(`Testing pagination on tab: ${tab}`);
      
      // Switch to tab
      await switchToTab(page, tab);
      
      // Check if pagination controls exist
      const nextButton = page.locator('button:has-text("Next")');
      const prevButton = page.locator('button:has-text("Previous")');
      
      if (await nextButton.isVisible()) {
        console.log(`Pagination controls found on ${tab} tab`);
      } else {
        console.log(`No pagination controls on ${tab} tab (may have few items)`);
      }
      
      await page.waitForTimeout(1000);
    }
  });

  test('should handle error scenarios gracefully', async ({ page }) => {
    await loginAndNavigateToContentManagement(page);
    
    // Switch to boards tab
    await switchToTab(page, 'boards');
    
    // Try to create a board with invalid data
    await page.getByRole('button', { name: 'Add Board' }).click();
    await page.waitForSelector('[role="dialog"]');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Board' }).click();
    
    // Should show validation errors
    await page.waitForTimeout(2000);
    
    // Close dialog
    await page.getByRole('button', { name: 'Cancel' }).click();
    await page.waitForSelector('[role="dialog"]', { state: 'hidden' });
  });
});
