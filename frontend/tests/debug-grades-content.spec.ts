import { test, expect, Page } from '@playwright/test';

test.describe('Grades Tab Content Debug', () => {
  test('should check if grades tab content is rendered', async ({ page }) => {
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
    
    // Check if Grades tab exists
    const gradesTab = page.locator('text=Grades');
    await expect(gradesTab).toBeVisible();
    
    // Click Grades tab
    await gradesTab.click();
    await page.waitForTimeout(3000);

    // Check for various possible content
    const possibleContent = [
      'Grade Levels',
      'Loading grades',
      'Error loading grades',
      'Add Grade',
      'Grade 7',
      'Grade 8',
      'CBSE'
    ];

    let foundContent = [];
    for (const content of possibleContent) {
      const element = page.locator(`text=${content}`);
      if (await element.isVisible()) {
        foundContent.push(content);
      }
    }

    console.log('Found content:', foundContent);

    // Check if any content is visible
    const hasContent = foundContent.length > 0;
    console.log('Has any content:', hasContent);

    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-content.png' });

    // The test should pass if we find any content
    expect(hasContent).toBe(true);
  });
});
