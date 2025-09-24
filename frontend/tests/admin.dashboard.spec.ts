import { test, expect } from '@playwright/test'

test.describe('Admin Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page
    await page.goto('/login')

    // Login as admin
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')

    // Wait for redirect to admin dashboard
    await page.waitForURL('/admin')
  })

  test('should display dashboard metrics cards', async ({ page }) => {
    // Wait for the dashboard to load
    await page.waitForSelector('[data-testid="dashboard-metrics"]', { timeout: 10000 })

    // Check that all metric cards are visible
    await expect(page.locator('text=Total Students')).toBeVisible()
    await expect(page.locator('text=Total Teachers')).toBeVisible()
    await expect(page.locator('text=New Students (30 days)')).toBeVisible()
    await expect(page.locator('text=New Teachers (30 days)')).toBeVisible()

    // Check that the cards have numeric values
    const studentCard = page.locator('text=Total Students').locator('..').locator('text=/\\d+/').first()
    const teacherCard = page.locator('text=Total Teachers').locator('..').locator('text=/\\d+/').first()
    
    await expect(studentCard).toBeVisible()
    await expect(teacherCard).toBeVisible()
  })

  test('should display registration trends chart', async ({ page }) => {
    // Wait for the chart section to load
    await page.waitForSelector('text=Registration Trends (Last 30 Days)', { timeout: 10000 })

    // Check that the chart container is visible
    const chartContainer = page.locator('text=Registration Trends (Last 30 Days)').locator('..').locator('svg')
    await expect(chartContainer).toBeVisible()

    // Check that the chart has the expected elements
    await expect(chartContainer.locator('text=Students')).toBeVisible()
    await expect(chartContainer.locator('text=Teachers')).toBeVisible()
  })

  test('should display recent activity section', async ({ page }) => {
    // Wait for the recent activity section
    await page.waitForSelector('text=Recent Activity', { timeout: 10000 })

    // Check that the section is visible
    await expect(page.locator('text=Recent Activity')).toBeVisible()
    await expect(page.locator('text=New Students (30 days)')).toBeVisible()
    await expect(page.locator('text=New Teachers (30 days)')).toBeVisible()
  })

  test('should display course status section', async ({ page }) => {
    // Wait for the course status section
    await page.waitForSelector('text=Course Status', { timeout: 10000 })

    // Check that the section is visible
    await expect(page.locator('text=Course Status')).toBeVisible()
    await expect(page.locator('text=Active Courses')).toBeVisible()
    await expect(page.locator('text=Completed Courses')).toBeVisible()
  })

  test('should handle loading state', async ({ page }) => {
    // Navigate to dashboard and check for loading skeletons
    await page.goto('/admin')
    
    // Check that loading skeletons are visible initially
    const loadingSkeletons = page.locator('.animate-pulse')
    await expect(loadingSkeletons.first()).toBeVisible()
  })

  test('should handle error state gracefully', async ({ page }) => {
    // Mock API failure
    await page.route('**/api/admin/dashboard/metrics', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' })
      })
    })

    await page.route('**/api/admin/dashboard/series', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' })
      })
    })

    // Navigate to dashboard
    await page.goto('/admin')

    // Check that error message is displayed
    await expect(page.locator('text=Failed to load dashboard metrics')).toBeVisible()
    
    // Check that retry button is available
    await expect(page.locator('button:has-text("Retry")')).toBeVisible()
  })

  test('should refresh data when retry button is clicked', async ({ page }) => {
    // Mock initial API failure
    let apiCallCount = 0
    await page.route('**/api/admin/dashboard/metrics', route => {
      apiCallCount++
      if (apiCallCount === 1) {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal Server Error' })
        })
      } else {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            totalStudents: 150,
            totalTeachers: 25,
            newStudentsLast30Days: 12,
            newTeachersLast30Days: 3,
            activeCourses: 0,
            completedCourses: 0
          })
        })
      }
    })

    await page.route('**/api/admin/dashboard/series', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          { date: '2024-01-01', students: 5, teachers: 2 },
          { date: '2024-01-02', students: 3, teachers: 1 }
        ])
      })
    })

    // Navigate to dashboard
    await page.goto('/admin')

    // Wait for error state
    await expect(page.locator('text=Failed to load dashboard metrics')).toBeVisible()

    // Click retry button
    await page.click('button:has-text("Retry")')

    // Wait for success state
    await expect(page.locator('text=Total Students')).toBeVisible()
    await expect(page.locator('text=150')).toBeVisible()
  })

  test('should display last updated timestamp', async ({ page }) => {
    // Wait for dashboard to load
    await page.waitForSelector('text=Last updated:', { timeout: 10000 })

    // Check that timestamp is displayed
    await expect(page.locator('text=Last updated:')).toBeVisible()
    
    // Check that timestamp shows current time format
    const timestamp = page.locator('text=Last updated:').locator('..').locator('text=/\\d{1,2}:\\d{2}/')
    await expect(timestamp).toBeVisible()
  })

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Wait for dashboard to load
    await page.waitForSelector('text=Total Students', { timeout: 10000 })

    // Check that metric cards are still visible on mobile
    await expect(page.locator('text=Total Students')).toBeVisible()
    await expect(page.locator('text=Total Teachers')).toBeVisible()

    // Check that chart is still visible
    const chartContainer = page.locator('text=Registration Trends (Last 30 Days)').locator('..').locator('svg')
    await expect(chartContainer).toBeVisible()
  })

  test('should support dark mode toggle', async ({ page }) => {
    // Wait for dashboard to load
    await page.waitForSelector('text=Total Students', { timeout: 10000 })

    // Find and click dark mode toggle
    const darkModeToggle = page.locator('button[data-testid="dark-mode-toggle"]')
    if (await darkModeToggle.isVisible()) {
      await darkModeToggle.click()
      
      // Check that dark mode is applied
      const body = page.locator('body')
      await expect(body).toHaveClass(/dark/)
    }
  })
})
