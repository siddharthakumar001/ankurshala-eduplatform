import { test, expect, Page } from '@playwright/test';

test.describe('Grades Tab Debug', () => {
  test('should verify grades tab loads and shows existing grades', async ({ page }) => {
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

    // Check if grades are displayed
    const gradeElements = page.locator('h3');
    const gradeCount = await gradeElements.count();
    console.log('Number of h3 elements (grades):', gradeCount);
    
    for (let i = 0; i < gradeCount; i++) {
      const text = await gradeElements.nth(i).textContent();
      console.log(`Grade ${i}:`, text);
    }

    // Verify that at least one grade is visible (Grade 7 or Grade 8)
    const grade7Visible = await page.locator('text=Grade 7').isVisible();
    const grade8Visible = await page.locator('text=Grade 8').isVisible();
    
    console.log('Grade 7 visible:', grade7Visible);
    console.log('Grade 8 visible:', grade8Visible);
    
    // At least one grade should be visible
    expect(grade7Visible || grade8Visible).toBe(true);
    
    // Test search functionality
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill('Grade 8');
    await searchInput.press('Enter');
    await page.waitForTimeout(1000);
    
    // Verify search results
    const searchResults = page.locator('h3').filter({ hasText: 'Grade 8' });
    const searchCount = await searchResults.count();
    console.log('Search results count:', searchCount);
    
    if (searchCount > 0) {
      await expect(searchResults.first()).toBeVisible();
      console.log('✅ Grades search is working');
    } else {
      console.log('❌ Grades search not working - no results found');
    }
  });
});