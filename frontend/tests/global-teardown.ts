import { FullConfig } from '@playwright/test';

/**
 * Global teardown for Admin Students Test Suite
 * Performs cleanup tasks after all tests complete
 */
async function globalTeardown(config: FullConfig) {
  console.log('🧹 Starting Global Teardown for Admin Students Test Suite');
  
  try {
    // Clean up any test data if needed
    console.log('🧹 Cleaning up test data...');
    
    // Log test completion
    console.log('✅ All tests completed');
    console.log('📊 Check test-results/ directory for detailed reports');
    console.log('🎉 Admin Students Test Suite finished');
    
  } catch (error) {
    console.log('⚠️ Global teardown encountered errors:', error);
  }
}

export default globalTeardown;
