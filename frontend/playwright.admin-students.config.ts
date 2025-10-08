import { defineConfig, devices } from '@playwright/test';

/**
 * Admin Students Comprehensive Test Configuration
 * Optimized for testing the enhanced AdminStudentsController
 */
export default defineConfig({
  testDir: './tests',
  
  // Test files to run
  testMatch: [
    'admin-students-comprehensive.spec.ts',
    'admin-students-api-comprehensive.spec.ts',
    'admin.students.spec.ts'
  ],
  
  // Run tests in parallel for faster execution
  fullyParallel: true,
  
  // Fail the build on CI if you accidentally left test.only in the source code
  forbidOnly: !!process.env.CI,
  
  // Retry on CI only
  retries: process.env.CI ? 2 : 0,
  
  // Opt out of parallel tests on CI for stability
  workers: process.env.CI ? 1 : undefined,
  
  // Reporter configuration for comprehensive reporting
  reporter: [
    ['html', { outputFolder: 'test-results/html-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/results.xml' }],
    ['list'],
    ['github']
  ],
  
  // Global timeout for each test
  timeout: 60000,
  
  // Shared settings for all projects
  use: {
    // Base URL for the frontend application
    baseURL: 'http://localhost:3000',
    
    // Collect trace when retrying the failed test
    trace: 'on-first-retry',
    
    // Take screenshot on failure
    screenshot: 'only-on-failure',
    
    // Record video on failure
    video: 'retain-on-failure',
    
    // Global timeout for each action
    actionTimeout: 15000,
    
    // Global timeout for navigation
    navigationTimeout: 30000,
    
    // Ignore HTTPS errors
    ignoreHTTPSErrors: true,
    
    // Additional context options
    extraHTTPHeaders: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  },
  
  // Global setup and teardown
  globalSetup: require.resolve('./tests/global-setup.ts'),
  globalTeardown: require.resolve('./tests/global-teardown.ts'),
  
  // Configure projects for major browsers
  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // Additional Chromium-specific settings
        launchOptions: {
          args: ['--disable-web-security', '--disable-features=VizDisplayCompositor']
        }
      },
    },
    
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        // Additional Firefox-specific settings
        launchOptions: {
          firefoxUserPrefs: {
            'security.tls.insecure_fallback_hosts': 'localhost'
          }
        }
      },
    },
    
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        // Additional WebKit-specific settings
        launchOptions: {
          args: ['--disable-web-security']
        }
      },
    },
    
    // Mobile testing
    {
      name: 'Mobile Chrome',
      use: { 
        ...devices['Pixel 5'],
        // Mobile-specific settings
        viewport: { width: 393, height: 851 },
        deviceScaleFactor: 2.75,
        isMobile: true,
        hasTouch: true
      },
    },
    
    {
      name: 'Mobile Safari',
      use: { 
        ...devices['iPhone 12'],
        // Mobile Safari-specific settings
        viewport: { width: 390, height: 844 },
        deviceScaleFactor: 3,
        isMobile: true,
        hasTouch: true
      },
    },
    
    // Tablet testing
    {
      name: 'iPad',
      use: { 
        ...devices['iPad Pro'],
        // Tablet-specific settings
        viewport: { width: 1024, height: 1366 },
        deviceScaleFactor: 2,
        isMobile: true,
        hasTouch: true
      },
    }
  ],
  
  // Run your local dev server before starting the tests
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
    // Additional server options
    env: {
      NODE_ENV: 'test',
      PORT: '3000'
    }
  },
  
  // Test output directory
  outputDir: 'test-results/artifacts',
  
  // Expect timeout
  expect: {
    timeout: 10000
  },
  
  // Note: Keep single `use` block to avoid overriding baseURL.
});
