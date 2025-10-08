import { test, expect, Page } from '@playwright/test';

// Test data for Grades
const TEST_DATA = {
  grade: {
    displayName: `Test Grade ${Date.now()}`,
    description: 'Test Grade Description',
    active: true
  }
};

test.describe('Admin Content Management - Grade Tests', () => {
  test('should create a grade successfully', async ({ page }) => {
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
    
    // Click Grades tab and wait for content to load
    await page.getByRole('tab', { name: 'Grades' }).click();
    await page.waitForSelector('text=Grade Levels', { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Create a grade
    const uniqueGradeName = `Test Grade ${Date.now()}`;
    
    await page.getByRole('button', { name: 'Add Grade' }).click({ force: true });
    await page.waitForSelector('[role="dialog"]');
    await page.waitForTimeout(2000);
    
    // Debug: Check dialog content
    const dialogContent = await page.textContent('[role="dialog"]');
    console.log('Dialog content:', dialogContent);
    
    // Debug: Check if form fields are present
    const allInputs = page.locator('input');
    const inputCount = await allInputs.count();
    console.log('Total inputs on page:', inputCount);
    
    const dialogInputs = page.locator('[role="dialog"] input');
    const dialogInputCount = await dialogInputs.count();
    console.log('Inputs in dialog:', dialogInputCount);
    
    // Select a board first (required for grades) - use keyboard navigation
    await page.click('[role="combobox"]', { force: true });
    await page.waitForTimeout(500);
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    await page.waitForTimeout(500);
    
    // Wait for form to be fully rendered
    await page.waitForSelector('input[id="name"]', { timeout: 10000 });
    await page.waitForTimeout(1000);
    
    // Fill grade form - use ID selectors
    await page.fill('input[id="name"]', uniqueGradeName, { force: true });
    await page.fill('input[id="displayName"]', uniqueGradeName, { force: true });
    
    // Handle active status switch
    const label = page.locator('label[for="active"]');
    if (await label.isVisible()) {
      await label.click();
    } else {
      const switchElement = page.locator('[role="switch"][id="active"]');
      await switchElement.click();
    }
    
    await page.getByRole('button', { name: 'Create Grade' }).click();
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Search for the created grade
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueGradeName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);

    // Verify grade was created
    await expect(page.locator(`text=${uniqueGradeName}`)).toBeVisible({ timeout: 10000 });
  });

  test('should edit and delete a grade successfully', async ({ page }) => {
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
    
    // Click Grades tab and wait for content to load
    await page.getByRole('tab', { name: 'Grades' }).click();
    await page.waitForSelector('text=Grade Levels', { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Create a grade first
    const uniqueGradeName = `Test Grade ${Date.now()}`;
    
    await page.getByRole('button', { name: 'Add Grade' }).click({ force: true });
    await page.waitForSelector('[role="dialog"]');
    
    // Select a board first (required for grades) - use keyboard navigation
    await page.click('[role="combobox"]', { force: true });
    await page.waitForTimeout(500);
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    await page.waitForTimeout(500);
    
    // Wait for form to be fully rendered
    await page.waitForSelector('input[id="name"]', { timeout: 10000 });
    await page.waitForTimeout(1000);
    
    // Fill grade form - use ID selectors
    await page.fill('input[id="name"]', uniqueGradeName, { force: true });
    await page.fill('input[id="displayName"]', uniqueGradeName, { force: true });
    
    // Handle active status switch
    const label = page.locator('label[for="active"]');
    if (await label.isVisible()) {
      await label.click();
    } else {
      const switchElement = page.locator('[role="switch"][id="active"]');
      await switchElement.click();
    }
    
    await page.getByRole('button', { name: 'Create Grade' }).click();
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Search for the created grade
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueGradeName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);

    // Test Edit Grade
    const updatedGradeName = `${uniqueGradeName} Updated`;
    
    // Find the grade row and click edit button using aria-label
    const editButton = page.getByRole('button', { name: `Edit ${uniqueGradeName}` });
    await editButton.click();
    
    await page.waitForSelector('[role="dialog"]');
    
    // Update the name
    await page.fill('input[id="edit-displayName"]', updatedGradeName);
    await page.getByRole('button', { name: 'Update Grade' }).click();
    
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Verify grade was updated by searching again
    await searchInput.fill(updatedGradeName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);
    
    await expect(page.locator(`text=${updatedGradeName}`)).toBeVisible({ timeout: 10000 });
    
    // Test Delete Grade
    // Set up dialog handler before clicking delete
    page.on('dialog', async dialog => {
      console.log('Dialog message:', dialog.message());
      await dialog.accept();
    });
    
    const deleteButton = page.getByRole('button', { name: `Delete ${updatedGradeName}` });
    await deleteButton.click();
    
    await page.waitForTimeout(2000);

    // Verify grade was deleted
    await expect(page.locator(`text=${updatedGradeName}`)).not.toBeVisible();
  });

  test('should search grades successfully', async ({ page }) => {
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
    
    // Click Grades tab and wait for content to load
    await page.getByRole('tab', { name: 'Grades' }).click();
    await page.waitForSelector('text=Grade Levels', { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Search for existing grade
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill('Grade 8');
    await searchInput.press('Enter');
    await page.waitForTimeout(1000);

    // Verify search results
    await expect(page.locator('h3').filter({ hasText: 'Grade 8' })).toBeVisible();
  });

  test('should filter grades by status', async ({ page }) => {
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
    
    // Click Grades tab and wait for content to load
    await page.getByRole('tab', { name: 'Grades' }).click();
    await page.waitForSelector('text=Grade Levels', { timeout: 10000 });
    await page.waitForTimeout(2000);

    // Test status filter
    await page.click('[role="combobox"]');
    await page.waitForTimeout(500);
    await page.getByRole('option', { name: 'Active' }).first().click();
    await page.waitForTimeout(1000);

    // Verify only active grades are shown
    const inactiveGrades = page.locator('text=Inactive');
    await expect(inactiveGrades).not.toBeVisible();
  });
});
