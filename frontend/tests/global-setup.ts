import { chromium, FullConfig } from '@playwright/test';

/**
 * Global setup for Admin Students Test Suite
 * Performs initial setup tasks before running tests
 */
async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting Global Setup for Admin Students Test Suite');
  
  // Launch browser for setup tasks
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  try {
    // Test backend connectivity
    console.log('🔍 Testing backend connectivity...');
    const backendResponse = await page.request.get('http://localhost:8080/api/actuator/health');
    
    if (backendResponse.ok()) {
      console.log('✅ Backend is accessible');
    } else {
      console.log('⚠️ Backend health check failed, but continuing with tests');
    }
    
    // Test frontend connectivity
    console.log('🔍 Testing frontend connectivity...');
    const frontendResponse = await page.request.get('http://localhost:3000');
    
    if (frontendResponse.ok()) {
      console.log('✅ Frontend is accessible');
    } else {
      console.log('⚠️ Frontend connectivity check failed, but continuing with tests');
    }
    
    // Verify admin user exists and can authenticate
    console.log('🔍 Verifying admin authentication...');
    const loginResponse = await page.request.post('http://localhost:8080/api/auth/signin', {
      data: {
        email: 'siddhartha@ankurshala.com',
        password: 'Maza@123'
      }
    });
    
    if (loginResponse.ok()) {
      console.log('✅ Admin authentication verified');
    } else {
      console.log('❌ Admin authentication failed - tests may fail');
    }
    
    console.log('✅ Global setup completed successfully');
    
  } catch (error) {
    console.log('⚠️ Global setup encountered errors:', error);
    console.log('Continuing with tests - individual tests will handle their own setup');
  } finally {
    await browser.close();
  }
}

export default globalSetup;
