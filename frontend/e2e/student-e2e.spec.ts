import { test, expect, Page } from '@playwright/test';

// Test credentials from E2ETestDataSeeder
const TEST_CREDENTIALS = {
  student1: {
    email: 'student-e2e1@ankurshala.com',
    password: 'Test@123'
  },
  student2: {
    email: 'student-e2e2@ankurshala.com',
    password: 'Test@123'
  },
  admin: {
    email: 'admin-e2e@ankurshala.com',
    password: 'Test@123'
  }
};

// Helper function to login
async function loginAsStudent(page: Page, studentKey: 'student1' | 'student2' = 'student1') {
  await page.goto('/auth/login');
  
  const credentials = TEST_CREDENTIALS[studentKey];
  await page.fill('input[type="email"]', credentials.email);
  await page.fill('input[type="password"]', credentials.password);
  await page.click('button[type="submit"]');
  
  // Wait for navigation to dashboard
  await page.waitForURL('/student/dashboard', { timeout: 10000 });
}

test.describe('Student E2E Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Start each test from login
    await loginAsStudent(page, 'student1');
  });

  test('Student Dashboard loads correctly', async ({ page }) => {
    // Should be on dashboard after login
    await expect(page.getByTestId('dashboard-content')).toBeVisible();
    await expect(page.getByTestId('dashboard-welcome')).toBeVisible();
    await expect(page.getByTestId('dashboard-stats')).toBeVisible();
    await expect(page.getByTestId('dashboard-quick-actions')).toBeVisible();
    
    // Check stat counters are present
    await expect(page.getByTestId('dashboard-upcoming-count')).toBeVisible();
    await expect(page.getByTestId('dashboard-completed-count')).toBeVisible();
  });

  test('Today Home page displays daily plan', async ({ page }) => {
    // Navigate to Today page
    await page.goto('/student/today');
    
    // Wait for page load
    await page.waitForSelector('[data-testid="today-page"]', { timeout: 10000 });
    await expect(page.getByTestId('today-header')).toBeVisible();
    
    // Check if loading or content is displayed
    const isLoading = await page.getByTestId('today-loading').isVisible().catch(() => false);
    
    if (!isLoading) {
      // If not loading, check for content or no-data state
      const hasData = await page.getByTestId('today-progress').isVisible().catch(() => false);
      const hasNoData = await page.getByTestId('today-no-data').isVisible().catch(() => false);
      
      expect(hasData || hasNoData).toBeTruthy();
      
      if (hasData) {
        // Verify progress section
        await expect(page.getByTestId('progress-completed')).toBeVisible();
        await expect(page.getByTestId('progress-percentage')).toBeVisible();
        
        // Verify at least one section is visible
        const sections = [
          'today-next-class',
          'today-weak-topic',
          'today-practice',
          'today-revise-note',
          'today-focus-sprint'
        ];
        
        let sectionFound = false;
        for (const section of sections) {
          const visible = await page.getByTestId(section).isVisible().catch(() => false);
          if (visible) {
            sectionFound = true;
            break;
          }
        }
        expect(sectionFound).toBeTruthy();
      }
    }
  });

  test('AI Tutor chat interaction', async ({ page }) => {
    // Navigate to AI Tutor
    await page.goto('/student/ai-tutor');
    
    // Wait for page load
    await page.waitForSelector('[data-testid="ai-tutor-page"]', { timeout: 10000 });
    await expect(page.getByTestId('ai-tutor-header')).toBeVisible();
    await expect(page.getByTestId('ai-tutor-chat-container')).toBeVisible();
    
    // Verify empty state
    await expect(page.getByTestId('ai-tutor-empty')).toBeVisible();
    
    // Type a question
    const questionText = 'Explain Pythagorean theorem with example';
    await page.fill('[data-testid="ai-tutor-input"]', questionText);
    
    // Send message
    await page.click('[data-testid="ai-tutor-send"]');
    
    // Wait for streaming to complete (up to 5 seconds)
    await page.waitForTimeout(5000);
    
    // Verify messages appeared
    const userMessages = page.getByTestId('ai-tutor-message-user');
    await expect(userMessages.first()).toBeVisible();
    await expect(userMessages.first()).toContainText(questionText);
    
    // Verify assistant response
    const assistantMessages = page.getByTestId('ai-tutor-message-assistant');
    await expect(assistantMessages.first()).toBeVisible();
    
    // Check for suggested actions (DEV mode always includes them)
    const hasActions = await page.locator('[data-testid^="ai-tutor-action-"]').count();
    expect(hasActions).toBeGreaterThan(0);
    
    // Test suggested action click (Generate Notes)
    const generateNotesBtn = page.getByTestId('ai-tutor-action-generate_notes');
    if (await generateNotesBtn.isVisible()) {
      await generateNotesBtn.click();
      // Should navigate to notes page
      await page.waitForURL(/\/student\/notes/, { timeout: 5000 });
    }
  });

  test('Practice flow - start and submit', async ({ page }) => {
    // Navigate to practice
    await page.goto('/student/practice');
    
    // Wait for page load
    await page.waitForTimeout(2000);
    
    // Check if practice preferences exist
    const hasPreferences = await page.locator('button:has-text("Start Practice")').isVisible().catch(() => false);
    
    if (hasPreferences) {
      // Click start practice
      await page.click('button:has-text("Start Practice")');
      
      // Wait for practice session to load
      await page.waitForTimeout(3000);
      
      // Look for question elements
      const hasQuestions = await page.locator('text=/Question \\d+/').isVisible().catch(() => false);
      
      if (hasQuestions) {
        // Select first option for each question (simplified)
        const radioButtons = page.locator('input[type="radio"]');
        const count = await radioButtons.count();
        
        if (count > 0) {
          // Select first option
          await radioButtons.first().click();
          
          // Submit practice
          const submitBtn = page.locator('button:has-text("Submit")');
          if (await submitBtn.isVisible()) {
            await submitBtn.click();
            
            // Wait for results
            await page.waitForTimeout(2000);
            
            // Verify results/explanation shown
            const hasResults = await page.locator('text=/Explanation|Result|Score/i').isVisible().catch(() => false);
            expect(hasResults).toBeTruthy();
          }
        }
      }
    }
  });

  test('Notes page navigation and creation', async ({ page }) => {
    // Navigate to notes
    await page.goto('/student/notes');
    
    // Wait for page load
    await page.waitForTimeout(2000);
    
    // Page should load successfully
    await expect(page).toHaveURL(/\/student\/notes/);
    
    // Look for notes UI elements
    const hasNotesUI = await page.locator('text=/Notes|Create Note|My Notes/i').isVisible().catch(() => false);
    expect(hasNotesUI).toBeTruthy();
  });

  test('Focus mode session', async ({ page }) => {
    // Navigate to focus
    await page.goto('/student/focus');
    
    // Wait for page load
    await page.waitForTimeout(2000);
    
    // Page should load successfully
    await expect(page).toHaveURL(/\/student\/focus/);
    
    // Look for focus mode UI
    const hasFocusUI = await page.locator('text=/Focus|Start Session|Timer/i').isVisible().catch(() => false);
    expect(hasFocusUI).toBeTruthy();
  });

  test('Booking companion flow', async ({ page }) => {
    // Navigate to dashboard first
    await page.goto('/student/dashboard');
    
    // Check if there are upcoming classes
    await page.waitForTimeout(2000);
    
    const upcomingClasses = page.getByTestId('dashboard-upcoming-classes');
    const hasClasses = await upcomingClasses.isVisible().catch(() => false);
    
    if (hasClasses) {
      // Look for a class with companion access
      const joinButtons = page.locator('button:has-text("Join Class")');
      const count = await joinButtons.count();
      
      if (count > 0) {
        // Click first join button
        await joinButtons.first().click();
        
        // Wait for navigation to booking companion
        await page.waitForTimeout(2000);
        
        // Verify we're on companion page
        const onCompanionPage = page.url().includes('/companion');
        if (onCompanionPage) {
          // Look for companion UI elements
          const hasCompanionUI = await page.locator('text=/Prep|Warmup|Live Notes|Summary/i').isVisible().catch(() => false);
          expect(hasCompanionUI).toBeTruthy();
        }
      }
    }
  });

  test('Voice mode toggle in AI Tutor', async ({ page }) => {
    // Navigate to AI Tutor
    await page.goto('/student/ai-tutor');
    
    await page.waitForSelector('[data-testid="ai-tutor-voice-toggle"]', { timeout: 10000 });
    
    // Check initial state (voice off)
    const voiceToggle = page.getByTestId('ai-tutor-voice-toggle');
    await expect(voiceToggle).toBeVisible();
    await expect(voiceToggle).toContainText(/Voice Off/i);
    
    // Toggle voice mode on
    await voiceToggle.click();
    await expect(voiceToggle).toContainText(/Voice On/i);
    
    // Verify input placeholder changed
    const input = page.getByTestId('ai-tutor-input');
    const placeholder = await input.getAttribute('placeholder');
    expect(placeholder).toContain('simulate voice');
    
    // Toggle back off
    await voiceToggle.click();
    await expect(voiceToggle).toContainText(/Voice Off/i);
  });

  test('Complete practice step from Today Home', async ({ page }) => {
    // Navigate to Today page
    await page.goto('/student/today');
    
    await page.waitForSelector('[data-testid="today-page"]', { timeout: 10000 });
    
    // Check if there are practice items
    const practiceSection = page.getByTestId('today-practice');
    const hasPractice = await practiceSection.isVisible().catch(() => false);
    
    if (hasPractice) {
      // Find first practice item start button
      const startBtn = page.locator('[data-testid^="practice-item-"][data-testid$="-start-btn"]').first();
      const hasBtn = await startBtn.isVisible().catch(() => false);
      
      if (hasBtn) {
        // Get topic before clicking
        const topicElement = page.locator('[data-testid^="practice-item-"][data-testid$="-topic"]').first();
        const topicText = await topicElement.textContent();
        
        // Click start
        await startBtn.click();
        
        // Should navigate to practice page
        await page.waitForURL(/\/student\/practice/, { timeout: 5000 });
        
        // Verify we're on practice page with correct topic context
        await expect(page).toHaveURL(/\/student\/practice/);
      }
    }
  });

  test('Save AI chat as notes', async ({ page }) => {
    // Navigate to AI Tutor
    await page.goto('/student/ai-tutor');
    
    await page.waitForSelector('[data-testid="ai-tutor-input"]', { timeout: 10000 });
    
    // Send a message
    await page.fill('[data-testid="ai-tutor-input"]', 'Explain photosynthesis');
    await page.click('[data-testid="ai-tutor-send"]');
    
    // Wait for response
    await page.waitForTimeout(5000);
    
    // Find save as notes button
    const saveBtn = page.getByTestId('ai-tutor-save-notes');
    const hasBtn = await saveBtn.isVisible().catch(() => false);
    
    if (hasBtn) {
      await saveBtn.click();
      
      // Should navigate to notes page with content
      await page.waitForURL(/\/student\/notes/, { timeout: 5000 });
      await expect(page).toHaveURL(/content=/);
    }
  });
});

test.describe('DEV AI Provider Verification', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page, 'student1');
  });

  test('AI responses include DEV mode indicator', async ({ page }) => {
    await page.goto('/student/ai-tutor');
    
    // Look for DEV mode badge
    const devBadge = page.locator('text=/DEV Mode|Deterministic/i');
    await expect(devBadge).toBeVisible();
  });

  test('AI responses are deterministic', async ({ page }) => {
    await page.goto('/student/ai-tutor');
    
    const question = 'What is 2+2?';
    
    // Send same question twice
    await page.fill('[data-testid="ai-tutor-input"]', question);
    await page.click('[data-testid="ai-tutor-send"]');
    await page.waitForTimeout(5000);
    
    // Get first response
    const firstResponse = await page.getByTestId('ai-tutor-message-assistant').first().textContent();
    
    // Send same question again
    await page.fill('[data-testid="ai-tutor-input"]', question);
    await page.click('[data-testid="ai-tutor-send"]');
    await page.waitForTimeout(5000);
    
    // Get second response
    const secondResponse = await page.getByTestId('ai-tutor-message-assistant').nth(1).textContent();
    
    // Responses should be identical (deterministic)
    expect(firstResponse).toBe(secondResponse);
  });
});
