import { test, expect } from '@playwright/test';

test.describe('Student Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Login as student
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');
    await expect(page).toHaveURL(/\/student\/dashboard/);
  });

  test('should display student dashboard correctly', async ({ page }) => {
    // Should see dashboard elements
    await expect(page.locator('[data-testid="welcome-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="quick-stats"]')).toBeVisible();
    await expect(page.locator('[data-testid="recent-bookings"]')).toBeVisible();
    await expect(page.locator('[data-testid="upcoming-classes"]')).toBeVisible();
  });

  test('should show quick stats', async ({ page }) => {
    // Should see stats cards
    await expect(page.locator('[data-testid="total-classes-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="completed-classes-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="pending-classes-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="wallet-balance-stat"]')).toBeVisible();
  });

  test('should navigate to topics page', async ({ page }) => {
    // Click on topics navigation
    await page.click('[data-testid="topics-nav-link"]');
    
    // Should navigate to topics page
    await expect(page).toHaveURL('/student/topics');
    await expect(page.locator('[data-testid="topics-header"]')).toBeVisible();
  });

  test('should navigate to bookings page', async ({ page }) => {
    // Click on bookings navigation
    await page.click('[data-testid="bookings-nav-link"]');
    
    // Should navigate to bookings page
    await expect(page).toHaveURL('/student/bookings');
    await expect(page.locator('[data-testid="bookings-header"]')).toBeVisible();
  });

  test('should navigate to profile page', async ({ page }) => {
    // Click on profile navigation
    await page.click('[data-testid="profile-nav-link"]');
    
    // Should navigate to profile page
    await expect(page).toHaveURL('/student/profile');
    await expect(page.locator('[data-testid="profile-header"]')).toBeVisible();
  });

  test('should show notifications', async ({ page }) => {
    // Click on notification bell
    await page.click('[data-testid="notification-bell"]');
    
    // Should show notification dropdown
    await expect(page.locator('[data-testid="notification-dropdown"]')).toBeVisible();
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

  test('should display teacher dashboard correctly', async ({ page }) => {
    // Should see dashboard elements
    await expect(page.locator('[data-testid="welcome-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="teacher-stats"]')).toBeVisible();
    await expect(page.locator('[data-testid="pending-requests"]')).toBeVisible();
    await expect(page.locator('[data-testid="upcoming-classes"]')).toBeVisible();
  });

  test('should show teacher stats', async ({ page }) => {
    // Should see teacher-specific stats
    await expect(page.locator('[data-testid="total-students-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="completed-classes-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="pending-requests-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="earnings-stat"]')).toBeVisible();
  });

  test('should navigate to availability page', async ({ page }) => {
    // Click on availability navigation
    await page.click('[data-testid="availability-nav-link"]');
    
    // Should navigate to availability page
    await expect(page).toHaveURL('/teacher/availability');
    await expect(page.locator('[data-testid="availability-header"]')).toBeVisible();
  });

  test('should navigate to students page', async ({ page }) => {
    // Click on students navigation
    await page.click('[data-testid="students-nav-link"]');
    
    // Should navigate to students page
    await expect(page).toHaveURL('/teacher/students');
    await expect(page.locator('[data-testid="students-header"]')).toBeVisible();
  });

  test('should navigate to earnings page', async ({ page }) => {
    // Click on earnings navigation
    await page.click('[data-testid="earnings-nav-link"]');
    
    // Should navigate to earnings page
    await expect(page).toHaveURL('/teacher/earnings');
    await expect(page.locator('[data-testid="earnings-header"]')).toBeVisible();
  });
});

test.describe('Admin Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'admin@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');
    await expect(page).toHaveURL(/\/admin\/dashboard/);
  });

  test('should display admin dashboard correctly', async ({ page }) => {
    // Should see admin dashboard elements
    await expect(page.locator('[data-testid="admin-welcome"]')).toBeVisible();
    await expect(page.locator('[data-testid="system-stats"]')).toBeVisible();
    await expect(page.locator('[data-testid="recent-activities"]')).toBeVisible();
    await expect(page.locator('[data-testid="pending-approvals"]')).toBeVisible();
  });

  test('should show system statistics', async ({ page }) => {
    // Should see system-wide stats
    await expect(page.locator('[data-testid="total-users-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="total-teachers-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="total-students-stat"]')).toBeVisible();
    await expect(page.locator('[data-testid="total-bookings-stat"]')).toBeVisible();
  });

  test('should navigate to users management', async ({ page }) => {
    // Click on users navigation
    await page.click('[data-testid="users-nav-link"]');
    
    // Should navigate to users page
    await expect(page).toHaveURL('/admin/users');
    await expect(page.locator('[data-testid="users-header"]')).toBeVisible();
  });

  test('should navigate to bookings management', async ({ page }) => {
    // Click on bookings navigation
    await page.click('[data-testid="bookings-nav-link"]');
    
    // Should navigate to bookings page
    await expect(page).toHaveURL('/admin/bookings');
    await expect(page.locator('[data-testid="bookings-header"]')).toBeVisible();
  });

  test('should navigate to reports page', async ({ page }) => {
    // Click on reports navigation
    await page.click('[data-testid="reports-nav-link"]');
    
    // Should navigate to reports page
    await expect(page).toHaveURL('/admin/reports');
    await expect(page.locator('[data-testid="reports-header"]')).toBeVisible();
  });
});

test.describe('Cross-Role Navigation', () => {
  test('should prevent unauthorized access', async ({ page }) => {
    // Try to access admin page without admin role
    await page.goto('/admin/dashboard');
    
    // Should redirect to login or show unauthorized message
    await expect(page).toHaveURL(/\/login|\/unauthorized/);
  });

  test('should show appropriate navigation based on role', async ({ page }) => {
    // Login as student
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');

    // Should not see admin navigation
    await expect(page.locator('[data-testid="admin-nav-link"]')).not.toBeVisible();
    
    // Should see student navigation
    await expect(page.locator('[data-testid="topics-nav-link"]')).toBeVisible();
    await expect(page.locator('[data-testid="bookings-nav-link"]')).toBeVisible();
  });
});

test.describe('Dashboard Performance', () => {
  test('should load dashboard quickly', async ({ page }) => {
    const startTime = Date.now();
    
    // Login and navigate to dashboard
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');
    
    // Wait for dashboard to load
    await expect(page.locator('[data-testid="welcome-message"]')).toBeVisible();
    
    const loadTime = Date.now() - startTime;
    
    // Should load within 3 seconds
    expect(loadTime).toBeLessThan(3000);
  });

  test('should handle dashboard data loading states', async ({ page }) => {
    // Login and navigate to dashboard
    await page.goto('/login');
    await page.fill('[data-testid="email-input"]', 'teststudent@example.com');
    await page.fill('[data-testid="password-input"]', 'SecurePass123!');
    await page.click('[data-testid="submit-button"]');

    // Should show loading states initially
    await expect(page.locator('[data-testid="loading-skeleton"]')).toBeVisible();
    
    // Should hide loading states when data loads
    await expect(page.locator('[data-testid="loading-skeleton"]')).not.toBeVisible();
    await expect(page.locator('[data-testid="quick-stats"]')).toBeVisible();
  });
});
