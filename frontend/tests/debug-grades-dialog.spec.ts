import { test, expect, Page } from '@playwright/test';

test.describe('Grades Dialog Debug', () => {
  test('should debug grades dialog content', async ({ page }) => {
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

    // Click Add Grade button
    await page.getByRole('button', { name: 'Add Grade' }).click();
    await page.waitForSelector('[role="dialog"]');
    await page.waitForTimeout(2000);

    // Debug: Check what's in the dialog
    const dialogContent = await page.textContent('[role="dialog"]');
    console.log('Dialog content:', dialogContent);

    // Debug: Check all input fields in the dialog
    const inputs = page.locator('[role="dialog"] input');
    const inputCount = await inputs.count();
    console.log('Number of inputs in dialog:', inputCount);
    
    for (let i = 0; i < inputCount; i++) {
      const input = inputs.nth(i);
      const id = await input.getAttribute('id');
      const placeholder = await input.getAttribute('placeholder');
      console.log(`Input ${i}: id="${id}", placeholder="${placeholder}"`);
    }

    // Debug: Check all textareas in the dialog
    const textareas = page.locator('[role="dialog"] textarea');
    const textareaCount = await textareas.count();
    console.log('Number of textareas in dialog:', textareaCount);
    
    for (let i = 0; i < textareaCount; i++) {
      const textarea = textareas.nth(i);
      const id = await textarea.getAttribute('id');
      const placeholder = await textarea.getAttribute('placeholder');
      console.log(`Textarea ${i}: id="${id}", placeholder="${placeholder}"`);
    }

    // Take a screenshot
    await page.screenshot({ path: 'debug-grades-dialog.png' });

    // The test should pass if we find any inputs
    expect(inputCount > 0).toBe(true);
  });
});
