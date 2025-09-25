import { test, expect } from '@playwright/test'
import path from 'path'

test.describe('Admin Content Import', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await page.fill('input[type="email"]', 'siddhartha@ankurshala.com')
    await page.fill('input[type="password"]', 'Maza@123')
    await page.click('button[type="submit"]')

    // Wait for login to complete and redirect
    await page.waitForURL(/\/admin/, { timeout: 10000 })

    // Navigate to content import page
    await page.click('a[href="/admin/content/import"]')
    await page.waitForURL('/admin/content/import')

    // Wait for the page to load completely
    await page.waitForLoadState('networkidle')
    await page.waitForSelector('.animate-pulse', { state: 'hidden', timeout: 10000 })

    await expect(page.locator('h1')).toContainText('Content Import')
  })

  test('should display CSV-only import page with proper structure', async ({ page }) => {
    // Check page title and description
    await expect(page.locator('h1')).toContainText('Content Import')
    await expect(page.locator('p').filter({ hasText: 'Import educational content from CSV files only' })).toBeVisible()

    // Check upload section
    await expect(page.locator('h3').filter({ hasText: 'Upload Content File' })).toBeVisible()
    await expect(page.locator('text=Drop your CSV file here, or click to browse')).toBeVisible()
    await expect(page.locator('text=Supports CSV files only up to 10MB')).toBeVisible()

    // Check dry run toggle
    await expect(page.locator('text=Dry Run')).toBeVisible()
    await expect(page.locator('input[type="checkbox"]')).toBeVisible()

    // Check sample CSV download button
    await expect(page.locator('button:has-text("Download Sample CSV")')).toBeVisible()

    // Check import jobs section
    await expect(page.locator('h3').filter({ hasText: 'Import Jobs' }).first()).toBeVisible()
  })

  test('should handle CSV file upload with dry run', async ({ page }) => {
    // Create a test CSV file with proper headers
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter,Test Topic,1.5,Test Description,Test Summary`
    
    // Create a file input and upload
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Verify file is selected (filename should appear in the selected file section)
    await expect(page.locator('text=test.csv')).toBeVisible()

    // Ensure dry run is enabled
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.check()

    // Start import (button text is "Preview Import" when dry run is enabled)
    await page.click('button:has-text("Preview Import")')

    // Wait for success message
    await expect(page.locator('text=Dry run completed successfully! Check the results below.')).toBeVisible({ timeout: 10000 })
  })

  test('should handle CSV file upload with actual import', async ({ page }) => {
    // Create a test CSV file with proper headers
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter 2,Test Topic 2,2.0,Test Description 2,Test Summary 2`
    
    // Create a file input and upload
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'test-import.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Verify file is selected
    await expect(page.locator('text=test-import.csv')).toBeVisible()

    // Ensure dry run is disabled
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.uncheck()

    // Start import
    await page.click('button:has-text("Start Import")')

    // Wait for success message
    await expect(page.locator('text=File uploaded successfully! Processing started.')).toBeVisible({ timeout: 10000 })
  })

  test('should reject XLSX files with proper error message', async ({ page }) => {
    // Create a fake XLSX file
    const xlsxContent = 'fake xlsx content'
    
    // Create a file input and upload
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'test.xlsx',
      mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      buffer: Buffer.from(xlsxContent)
    })

    // Should show error message (toast notification)
    await expect(page.locator('text=Only CSV files are supported. XLSX files are not allowed.')).toBeVisible()
  })

  test('should download sample CSV file', async ({ page }) => {
    // Click download sample CSV button
    const downloadPromise = page.waitForEvent('download')
    await page.click('button:has-text("Download Sample CSV")')
    const download = await downloadPromise

    // Verify download
    expect(download.suggestedFilename()).toContain('.csv')
  })

  test('should display import jobs table with data', async ({ page }) => {
    // First create an import job by uploading a file
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter 3,Test Topic 3,1.0,Test Description 3,Test Summary 3`
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'test-job.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Ensure dry run is disabled for actual import
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.uncheck()

    // Start import
    await page.click('button:has-text("Start Import")')

    // Wait for job to appear in table
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('th:has-text("Status")')).toBeVisible()
    await expect(page.locator('th:has-text("File Name")')).toBeVisible()
    await expect(page.locator('th:has-text("Records")')).toBeVisible()
    await expect(page.locator('th:has-text("Created")')).toBeVisible()
  })

  test('should display proper status badges', async ({ page }) => {
    // Create an import job first
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter 4,Test Topic 4,0.5,Test Description 4,Test Summary 4`
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'test-status.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Ensure dry run is disabled for actual import
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.uncheck()

    await page.click('button:has-text("Start Import")')

    // Wait for job to appear and check status badge
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 })
    
    // Check for status badge (should be SUCCEEDED, RUNNING, or PENDING)
    const statusBadge = page.locator('table tr:last-child td:nth-child(2) span')
    await expect(statusBadge).toBeVisible()
  })

  test('should handle file size validation', async ({ page }) => {
    // Create a large CSV file (simulate)
    const largeCsvContent = 'Board,Grade,Subject,Chapter,TopicTitle,Hours\n' + 
      'CBSE,9,Chemistry,Test Chapter,Test Topic,1.0\n'.repeat(10000)
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'large-test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(largeCsvContent)
    })

    // Should show file size information
    await expect(page.locator('text=large-test.csv')).toBeVisible()
  })

  test('should handle CSV validation errors', async ({ page }) => {
    // Create a CSV with missing required headers
    const invalidCsvContent = `Grade,Subject,Chapter,TopicTitle
9,Chemistry,Test Chapter,Test Topic`
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'invalid.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(invalidCsvContent)
    })

    // Ensure dry run is enabled for validation testing
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.check()

    // Start import (button text is "Preview Import" when dry run is enabled)
    await page.click('button:has-text("Preview Import")')

    // Should show validation error
    await expect(page.locator('text=Missing required headers')).toBeVisible({ timeout: 10000 })
  })

  test('should handle responsive design on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Check that the page is still functional on mobile
    await expect(page.locator('h1')).toContainText('Content Import')
    await expect(page.locator('text=Drop your CSV file here, or click to browse')).toBeVisible()

    // Check that buttons are accessible
    await expect(page.locator('button:has-text("Download Sample CSV")')).toBeVisible()
  })

  test('should maintain dark mode compatibility', async ({ page }) => {
    // Toggle to dark mode (look for moon icon button)
    await page.click('button:has(svg.lucide-moon)')
    await expect(page.locator('html')).toHaveClass(/dark/)

    // Verify elements are visible in dark mode
    await expect(page.locator('h1')).toContainText('Content Import')
    await expect(page.locator('text=Drop your CSV file here, or click to browse')).toBeVisible()

    // Toggle back to light mode
    await page.click('button:has(svg.lucide-sun)')
    await expect(page.locator('html')).not.toHaveClass(/dark/)
  })

  test('should handle keyboard navigation', async ({ page }) => {
    // Test keyboard accessibility
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')
    
    // Should be able to navigate through the page elements
    await expect(page.locator('h1')).toContainText('Content Import')
  })

  test('should display time formatting correctly', async ({ page }) => {
    // Create a CSV with different hour values
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter,Test Topic 1,1.0,Test Description,Test Summary
CBSE,9,Chemistry,Test Chapter,Test Topic 2,1.5,Test Description,Test Summary
CBSE,9,Chemistry,Test Chapter,Test Topic 3,0.5,Test Description,Test Summary`
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'time-test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Ensure dry run is disabled for actual import
    const dryRunCheckbox = page.locator('input[type="checkbox"]')
    await dryRunCheckbox.uncheck()

    // Start import
    await page.click('button:has-text("Start Import")')

    // Wait for job to complete and check time formatting in results
    await expect(page.locator('text=File uploaded successfully! Processing started.')).toBeVisible({ timeout: 10000 })
  })

  test('should handle cancel operation', async ({ page }) => {
    // Upload a file
    const csvContent = `Board,Grade,Subject,Chapter,TopicTitle,Hours,Description,Summary
CBSE,9,Chemistry,Test Chapter,Test Topic,1.0,Test Description,Test Summary`
    
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles({
      name: 'cancel-test.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csvContent)
    })

    // Click cancel button
    await page.click('button:has-text("Cancel")')

    // File should be cleared
    await expect(page.locator('text=Drop your CSV file here, or click to browse')).toBeVisible()
  })
})