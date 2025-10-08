import { test, expect, Page } from '@playwright/test';

test.describe('Admin Content Management - Board Tests', () => {
  test('should create a board successfully', async ({ page }) => {
    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(2000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Click Boards tab
    await page.getByRole('tab', { name: 'Boards' }).click();
    await page.waitForTimeout(2000);

    // Test 1: Create Board
    const uniqueBoardName = `Test Board ${Date.now()}`;
    
    await page.getByRole('button', { name: 'Add Board' }).click();
    await page.waitForSelector('[role="dialog"]');
    
    await page.fill('input[id="name"]', uniqueBoardName);
    await page.getByRole('button', { name: 'Create Board' }).click();
    
    // Wait for any API calls to complete
    await page.waitForResponse(response => response.url().includes('/api/admin/content/boards') && response.status() === 200, { timeout: 10000 });
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Debug: Check what's on the page
    const allH3s = await page.locator('h3').allTextContents();
    console.log('All h3 elements after creation:', allH3s);
    
    const allText = await page.textContent('body');
    console.log('Page contains board name:', allText?.includes(uniqueBoardName));
    
    // Instead of pagination, try searching for the board
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueBoardName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);
    
    // Check if board appears in search results
    const searchResults = await page.locator('h3').allTextContents();
    console.log('Search results:', searchResults);
    
    const boardInSearch = await page.locator(`text=${uniqueBoardName}`).isVisible();
    console.log('Board found in search:', boardInSearch);

    // Verify board was created (either on current page or found via search)
    await expect(page.locator(`text=${uniqueBoardName}`)).toBeVisible({ timeout: 10000 });
  });

  test('should edit and delete a board successfully', async ({ page }) => {
    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(2000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Click Boards tab
    await page.getByRole('tab', { name: 'Boards' }).click();
    await page.waitForTimeout(2000);

    // Create a board first
    const uniqueBoardName = `Test Board ${Date.now()}`;
    
    await page.getByRole('button', { name: 'Add Board' }).click();
    await page.waitForSelector('[role="dialog"]');
    
    await page.fill('input[id="name"]', uniqueBoardName);
    await page.getByRole('button', { name: 'Create Board' }).click();
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 15000 });
    await page.waitForTimeout(3000);

    // Search for the created board
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueBoardName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);

    // Test Edit Board
    const updatedBoardName = `${uniqueBoardName} Updated`;
    
    // Find the board row and click edit button using aria-label
    const editButton = page.getByRole('button', { name: `Edit ${uniqueBoardName}` });
    await editButton.click();
    
    await page.waitForSelector('[role="dialog"]');
    
    // Update the name
    await page.fill('input[id="edit-name"]', updatedBoardName);
    await page.getByRole('button', { name: 'Update Board' }).click();
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Verify board was updated by searching again
    await searchInput.fill(updatedBoardName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);
    
    await expect(page.locator(`text=${updatedBoardName}`)).toBeVisible({ timeout: 10000 });
    
    // Test Delete Board
    // Set up dialog handler before clicking delete
    page.on('dialog', async dialog => {
      console.log('Dialog message:', dialog.message());
      await dialog.accept();
    });
    
    const deleteButton = page.getByRole('button', { name: `Delete ${updatedBoardName}` });
    await deleteButton.click();
    
    await page.waitForTimeout(2000);

    // Verify board was deleted
    await expect(page.locator(`text=${updatedBoardName}`)).not.toBeVisible();
  });

  test('should filter boards by status', async ({ page }) => {
    // Login
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(2000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    // Click Boards tab
    await page.getByRole('tab', { name: 'Boards' }).click();
    await page.waitForTimeout(2000);

    // Filter by active status
    const statusSelectTrigger = page.locator('[role="combobox"]').first();
    await statusSelectTrigger.click();
    await page.waitForTimeout(500);
    await page.getByRole('option', { name: 'Active' }).first().click();
    await page.waitForTimeout(1000);

    // Verify only active boards are shown (this is a basic check)
    await expect(page.locator('.inline-flex.items-center.rounded-full').first()).toBeVisible();
  });
});
