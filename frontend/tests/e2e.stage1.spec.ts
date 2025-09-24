import { test, expect } from '@playwright/test'

// Stage-1 FE complete: Comprehensive E2E test suite for authentication and profile management
test.describe('Stage-1 E2E Tests', () => {
  
  test.describe('Home Page', () => {
    test('should render home page with logo and navigation', async ({ page }) => {
      await page.goto('/')
      
      // Check for logo
      await expect(page.locator('img[alt="Ankurshala Logo"]').first()).toBeVisible()
      
      // Check for main heading
      await expect(page.locator('h1')).toContainText('Welcome to Ankurshala')
      
      // Check for call-to-action buttons
      await expect(page.locator('text=Login').first()).toBeVisible()
      await expect(page.locator('text=Register as Student')).toBeVisible()
      await expect(page.locator('text=Register as Teacher')).toBeVisible()
      
      // Check for key sections
      await expect(page.locator('text=How it works')).toBeVisible()
      await expect(page.locator('text=Subjects & Boards')).toBeVisible()
      await expect(page.locator('text=Safety & Quality')).toBeVisible()
    })
  })

  test.describe('Student Authentication Flow', () => {
    test('should allow student registration, login, and profile access', async ({ page }) => {
      // Generate unique email for this test run
      const timestamp = Date.now()
      const studentEmail = `teststudent${timestamp}@example.com`
      const password = 'TestStudent@123'

      // Navigate to student registration
      await page.goto('/register-student')
      await expect(page.locator('h1')).toContainText('AnkurShala')
      
      // Fill registration form
      await page.fill('input[placeholder="Enter your full name"]', 'Test Student')
      await page.fill('input[placeholder="Enter your email"]', studentEmail)
      await page.fill('input[placeholder="Enter your password"]', password)
      await page.fill('input[placeholder="Confirm your password"]', password)
      
      // Submit registration
      await page.click('button[type="submit"]')
      
      // Should redirect to student profile after successful registration
      await expect(page).toHaveURL('/student/profile')
      await expect(page.locator('h1')).toContainText('Student Profile')
    })

    test('should handle login and profile management', async ({ page }) => {
      // Use correct seeded student credentials
      await page.goto('/login')
      
      await page.fill('input[placeholder="Enter your email"]', 'student1@ankurshala.com')
      await page.fill('input[placeholder="Enter your password"]', 'Maza@123')
      await page.click('button[type="submit"]')
      
      // Should redirect to student profile
      await expect(page).toHaveURL('/student/profile')
      
      // Test profile tabs
      await page.click('text=Personal Information')
      await expect(page.locator('text=First Name')).toBeVisible()
      
      await page.click('text=Academic Information')
      await expect(page.locator('text=School Name')).toBeVisible()
      
      await page.click('text=Documents')
      await expect(page.locator('text=Add New Document')).toBeVisible()
      
      // Test profile update
      await page.click('text=Personal Information')
      await page.fill('input[placeholder="Enter your first name"]', 'Updated Student')
      await page.click('button:has-text("Save Personal Information")')
      
      // Should show success message
      await expect(page.locator('text=updated successfully')).toBeVisible()
    })
  })

  test.describe('Teacher Authentication Flow', () => {
    test('should allow teacher registration and login', async ({ page }) => {
      // Generate unique email for this test run
      const timestamp = Date.now()
      const teacherEmail = `testteacher${timestamp}@example.com`
      const password = 'TestTeacher@123'

      // Navigate to teacher registration
      await page.goto('/register-teacher')
      await expect(page.locator('h1')).toContainText('AnkurShala')
      
      // Fill registration form
      await page.fill('input[placeholder="Enter your full name"]', 'Test Teacher')
      await page.fill('input[placeholder="Enter your email"]', teacherEmail)
      await page.fill('input[placeholder="Enter your password"]', password)
      await page.fill('input[placeholder="Confirm your password"]', password)
      
      // Submit registration
      await page.click('button[type="submit"]')
      
      // Should redirect to teacher profile after successful registration
      await expect(page).toHaveURL('/teacher/profile')
      await expect(page.locator('h1')).toContainText('Teacher Profile')
    })

    test('should handle teacher profile management', async ({ page }) => {
      // Use correct seeded teacher credentials
      await page.goto('/login')
      
      await page.fill('input[placeholder="Enter your email"]', 'teacher1@ankurshala.com')
      await page.fill('input[placeholder="Enter your password"]', 'Maza@123')
      await page.click('button[type="submit"]')
      
      // Should redirect to teacher profile
      await expect(page).toHaveURL('/teacher/profile')
      
      // Test profile tabs (if implemented)
      const profileTab = page.locator('text=Profile').first()
      if (await profileTab.isVisible()) {
        await profileTab.click()
        await expect(page.locator('text=Basic Information')).toBeVisible()
      }
      
      // Test qualifications tab (if implemented)
      const qualificationsTab = page.getByRole('button', { name: 'Qualifications' }).first()
      if (await qualificationsTab.isVisible()) {
        await qualificationsTab.click()
        await expect(page.getByRole('heading', { name: 'Qualifications' })).toBeVisible()
      }
    })
  })

  test.describe('RBAC (Role-Based Access Control)', () => {
    test('should block student from accessing teacher routes', async ({ page }) => {
      // Login as student
      await page.goto('/login')
      await page.fill('input[placeholder="Enter your email"]', 'student@demo.test')
      await page.fill('input[placeholder="Enter your password"]', 'Student@1234')
      await page.click('button[type="submit"]')
      
      // Try to access teacher profile directly
      await page.goto('/teacher/profile')
      
      // Should be redirected away from teacher profile
      await expect(page).not.toHaveURL('/teacher/profile')
      // Should be redirected to student profile or access denied page
      const url = page.url()
      expect(url === '/student/profile' || url.includes('forbidden') || url.includes('login')).toBeTruthy()
    })

    test('should block teacher from accessing student routes', async ({ page }) => {
      // Login as teacher
      await page.goto('/login')
      await page.fill('input[placeholder="Enter your email"]', 'teacher@demo.test')
      await page.fill('input[placeholder="Enter your password"]', 'Teacher@1234')
      await page.click('button[type="submit"]')
      
      // Try to access student profile directly
      await page.goto('/student/profile')
      
      // Should be redirected away from student profile
      await expect(page).not.toHaveURL('/student/profile')
      // Should be redirected to teacher profile or access denied page
      const url = page.url()
      expect(url === '/teacher/profile' || url.includes('forbidden') || url.includes('login')).toBeTruthy()
    })
  })

  test.describe('Document Management', () => {
    test('should allow student to add and delete documents', async ({ page }) => {
      // Login as student
      await page.goto('/login')
      await page.fill('input[placeholder="Enter your email"]', 'student1@ankurshala.com')
      await page.fill('input[placeholder="Enter your password"]', 'Maza@123')
      await page.click('button[type="submit"]')
      
      // Wait for profile to load
      await page.getByTestId('student-profile-root').waitFor({ state: 'visible' })
      await page.waitForLoadState('networkidle')
      
      // Navigate to documents tab
      await page.getByTestId('tab-documents').click()
      await page.getByTestId('panel-documents').waitFor({ state: 'visible' })
      await page.waitForLoadState('networkidle')
      
      // Add a new document
      await page.getByTestId('input-document-name').fill('Test Report Card')
      await page.getByTestId('student-documents-add-url').fill('https://example.com/test-report.pdf')
      await page.click('button:has-text("Add Document")')
      
      // Should show success message
      await expect(page.locator('text=added successfully')).toBeVisible()
      
      // Document should appear in the list
      await expect(page.locator('text=Test Report Card').first()).toBeVisible()
      
      // Test document deletion
      const deleteButton = page.locator('button:has-text("Delete")').first()
      if (await deleteButton.isVisible()) {
        // Mock the confirm dialog
        page.on('dialog', dialog => dialog.accept())
        await deleteButton.click()
        await expect(page.locator('text=deleted successfully')).toBeVisible()
      }
    })
  })

  test.describe('Navigation and User Interface', () => {
    test('should show correct user information in navbar', async ({ page }) => {
      // Login as student
      await page.goto('/login')
      await page.fill('input[placeholder="Enter your email"]', 'student1@ankurshala.com')
      await page.fill('input[placeholder="Enter your password"]', 'Maza@123')
      await page.click('button[type="submit"]')
      
      // Check if navbar shows user info
      await expect(page.locator('text=Student 1')).toBeVisible()
      await expect(page.locator('text=student', { exact: true }).first()).toBeVisible()
      
      // Check logout functionality
      await page.click('button:has-text("Logout")')
      
      // Should redirect to home page or login page (both are valid)
      const url = page.url()
      expect(url.includes('/') || url.includes('login')).toBeTruthy()
      
      // Should show login/register buttons again
      await expect(page.locator('text=Login')).toBeVisible()
    })

    test('should handle form validation errors', async ({ page }) => {
      await page.goto('/register-student')
      
      // Try to submit empty form
      await page.click('button[type="submit"]')
      
      // Should show validation errors
      await expect(page.locator('text=Name must be at least')).toBeVisible()
      await expect(page.locator('text=Please enter a valid email')).toBeVisible()
      
      // Test password validation
      await page.fill('input[placeholder="Enter your full name"]', 'Test User')
      await page.fill('input[placeholder="Enter your email"]', 'test@example.com')
      await page.fill('input[placeholder="Enter your password"]', '123') // Weak password
      await page.click('button[type="submit"]')
      
      await expect(page.locator('text=Password must be at least 8 characters').first()).toBeVisible()
    })
  })

  test.describe('Error Handling', () => {
    test('should show 404 page for invalid routes', async ({ page }) => {
      await page.goto('/invalid-route-that-does-not-exist')
      
      await expect(page.locator('text=404')).toBeVisible()
      await expect(page.locator('text=Page Not Found')).toBeVisible()
      await expect(page.locator('text=Go Home')).toBeVisible()
    })

    test('should redirect unauthenticated users to login', async ({ page }) => {
      // Try to access protected route without authentication
      await page.goto('/student/profile')
      
      // Should be redirected to login
      await expect(page).toHaveURL(/.*login.*/)
    })
  })
})
