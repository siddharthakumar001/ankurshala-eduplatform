import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should allow student to sign up and login', async ({ page }) => {
    // Navigate to student signup page
    await page.goto('/register-student');

    // Fill in student signup form
    await page.fill('[data-testid="name-input"]', 'Test Student');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.selectOption('[data-testid="board-select"]', 'CBSE');
    await page.fill('[data-testid="grade-input"]', '10');
    await page.fill('[data-testid="language-input"]', 'English');
    await page.fill('[data-testid="school-input"]', 'Test School');
    await page.fill('[data-testid="dob-input"]', '2005-05-15');
    await page.fill('[data-testid="pincode-input"]', '110001');
    await page.fill('[data-testid="guardian-name-input"]', 'Guardian Name');
    await page.fill('[data-testid="guardian-contact-input"]', '9876543210');

    // Submit the form
    await page.click('[data-testid="submit-button"]');

    // Should redirect to student dashboard
    await expect(page).toHaveURL('/student/dashboard');
    await expect(page.locator('[data-testid="welcome-message"]')).toContainText('Welcome, Test Student');
  });

  test('should allow teacher to sign up and login', async ({ page }) => {
    // Navigate to teacher signup page
    await page.goto('/register-teacher');

    // Fill in teacher signup form
    await page.fill('[data-testid="name-input"]', 'Test Teacher');
    await page.fill('[data-testid="email-input"]', 'testteacher@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.fill('[data-testid="bio-input"]', 'Experienced mathematics teacher with 10 years of experience');
    await page.fill('[data-testid="years-experience-input"]', '10');
    await page.fill('[data-testid="hourly-rate-input"]', '500');
    
    // Add languages
    await page.click('[data-testid="add-language-button"]');
    await page.fill('[data-testid="language-input-0"]', 'English');
    
    // Add categories
    await page.click('[data-testid="add-category-button"]');
    await page.selectOption('[data-testid="category-select-0"]', 'STANDARD');

    // Submit the form
    await page.click('[data-testid="submit-button"]');

    // Should redirect to teacher dashboard
    await expect(page).toHaveURL('/teacher/dashboard');
    await expect(page.locator('[data-testid="welcome-message"]')).toContainText('Welcome, Test Teacher');
  });

  test('should show validation errors for invalid input', async ({ page }) => {
    await page.goto('/register-student');

    // Try to submit empty form
    await page.click('[data-testid="submit-button"]');

    // Should show validation errors
    await expect(page.locator('[data-testid="name-error"]')).toBeVisible();
    await expect(page.locator('[data-testid="email-error"]')).toBeVisible();
    await expect(page.locator('[data-testid="password-error"]')).toBeVisible();
    await expect(page.locator('[data-testid="board-error"]')).toBeVisible();
    await expect(page.locator('[data-testid="grade-error"]')).toBeVisible();
  });

  test('should allow existing user to login', async ({ page }) => {
    // Navigate to login page
    await page.goto('/login');

    // Fill in login form
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');

    // Submit the form
    await page.click('[data-testid="submit-button"]');

    // Should redirect to appropriate dashboard based on role
    await expect(page).toHaveURL(/\/student\/dashboard|\/teacher\/dashboard|\/admin\/dashboard/);
  });

  test('should show error for invalid login credentials', async ({ page }) => {
    await page.goto('/login');

    // Fill in invalid credentials
    await page.fill('[data-testid="email-input"]', 'invalid@example.com');
    await page.fill('[data-testid="password-input"]', 'wrongpassword');

    // Submit the form
    await page.click('[data-testid="submit-button"]');

    // Should show error message
    await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="error-message"]')).toContainText('Invalid email or password');
  });

  test('should allow user to logout', async ({ page }) => {
    // First login
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');

    // Wait for dashboard to load
    await expect(page).toHaveURL(/\/student\/dashboard|\/teacher\/dashboard|\/admin\/dashboard/);

    // Click logout button
    await page.click('[data-testid="logout-button"]');

    // Should redirect to login page
    await expect(page).toHaveURL('/login');
  });
});

test.describe('Booking Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Login as student
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');
    await expect(page).toHaveURL(/\/student\/dashboard/);
  });

  test('should allow student to browse topics and request booking', async ({ page }) => {
    // Navigate to topics page
    await page.goto('/student/topics');

    // Should see list of topics
    await expect(page.locator('[data-testid="topic-card"]')).toHaveCount.greaterThan(0);

    // Click on a topic
    await page.click('[data-testid="topic-card"]:first-child');

    // Should see topic details and booking form
    await expect(page.locator('[data-testid="topic-title"]')).toBeVisible();
    await expect(page.locator('[data-testid="booking-form"]')).toBeVisible();

    // Fill in booking details
    await page.fill('[data-testid="preferred-time-input"]', '2024-12-25T10:00');
    await page.selectOption('[data-testid="teacher-category-select"]', 'STANDARD');
    await page.fill('[data-testid="notes-input"]', 'Need help with this topic');

    // Submit booking request
    await page.click('[data-testid="request-booking-button"]');

    // Should show success message
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('Booking request submitted');
  });

  test('should show booking quote before requesting', async ({ page }) => {
    await page.goto('/student/topics');
    await page.click('[data-testid="topic-card"]:first-child');

    // Fill in booking details
    await page.fill('[data-testid="preferred-time-input"]', '2024-12-25T10:00');
    await page.selectOption('[data-testid="teacher-category-select"]', 'STANDARD');

    // Click get quote button
    await page.click('[data-testid="get-quote-button"]');

    // Should show quote details
    await expect(page.locator('[data-testid="quote-duration"]')).toBeVisible();
    await expect(page.locator('[data-testid="quote-price"]')).toBeVisible();
    await expect(page.locator('[data-testid="quote-end-time"]')).toBeVisible();
  });
});

test.describe('Teacher Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Login as teacher
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'testteacher@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');
    await expect(page).toHaveURL(/\/teacher\/dashboard/);
  });

  test('should show pending booking requests', async ({ page }) => {
    // Should see pending requests
    await expect(page.locator('[data-testid="pending-requests"]')).toBeVisible();
    
    // Should show request details
    await expect(page.locator('[data-testid="request-card"]')).toHaveCount.greaterThan(0);
  });

  test('should allow teacher to accept booking request', async ({ page }) => {
    // Click on a pending request
    await page.click('[data-testid="request-card"]:first-child');

    // Should see request details
    await expect(page.locator('[data-testid="request-details"]')).toBeVisible();

    // Accept the request
    await page.click('[data-testid="accept-button"]');

    // Should show success message
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="success-message"]')).toContainText('Booking accepted');
  });

  test('should allow teacher to set availability', async ({ page }) => {
    // Navigate to availability page
    await page.goto('/teacher/availability');

    // Should see availability form
    await expect(page.locator('[data-testid="availability-form"]')).toBeVisible();

    // Add availability slot
    await page.selectOption('[data-testid="weekday-select"]', '1'); // Monday
    await page.fill('[data-testid="start-time-input"]', '09:00');
    await page.fill('[data-testid="end-time-input"]', '17:00');
    await page.selectOption('[data-testid="timezone-select"]', 'Asia/Kolkata');

    // Save availability
    await page.click('[data-testid="save-availability-button"]');

    // Should show success message
    await expect(page.locator('[data-testid="success-message"]')).toBeVisible();
  });
});

test.describe('Responsive Design', () => {
  test('should work on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    // Navigate to login page
    await page.goto('/login');

    // Should see mobile-optimized layout
    await expect(page.locator('[data-testid="mobile-menu-button"]')).toBeVisible();
    
    // Login form should be visible and functional
    await expect(page.locator('[data-testid="email-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="password-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="submit-button"]')).toBeVisible();
  });

  test('should work on tablet devices', async ({ page }) => {
    // Set tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });

    // Navigate to student dashboard
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');

    // Should see tablet-optimized layout
    await expect(page.locator('[data-testid="sidebar"]')).toBeVisible();
    await expect(page.locator('[data-testid="main-content"]')).toBeVisible();
  });
});
