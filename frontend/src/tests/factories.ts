// Stage-1 FE complete: Test utilities and data factories
export interface DemoUser {
  email: string
  password: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
}

export interface TestData {
  admin: DemoUser
  students: DemoUser[]
  teachers: DemoUser[]
}

// Demo credentials for testing
export const DEMO_CREDENTIALS: TestData = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  },
  students: [
    { email: 'student1@ankurshala.com', password: 'Maza@123', role: 'STUDENT' },
    { email: 'student2@ankurshala.com', password: 'Maza@123', role: 'STUDENT' },
    { email: 'student3@ankurshala.com', password: 'Maza@123', role: 'STUDENT' },
    { email: 'student4@ankurshala.com', password: 'Maza@123', role: 'STUDENT' },
    { email: 'student5@ankurshala.com', password: 'Maza@123', role: 'STUDENT' }
  ],
  teachers: [
    { email: 'teacher1@ankurshala.com', password: 'Maza@123', role: 'TEACHER' },
    { email: 'teacher2@ankurshala.com', password: 'Maza@123', role: 'TEACHER' },
    { email: 'teacher3@ankurshala.com', password: 'Maza@123', role: 'TEACHER' },
    { email: 'teacher4@ankurshala.com', password: 'Maza@123', role: 'TEACHER' },
    { email: 'teacher5@ankurshala.com', password: 'Maza@123', role: 'TEACHER' }
  ]
}

// Utility functions for test data generation
export const TestFactories = {
  // Generate random student data
  generateStudentData: (index: number) => ({
    firstName: `Student${index}`,
    lastName: 'Test',
    email: `student${index}@ankurshala.com`,
    schoolName: `Test School ${index}`,
    educationalBoard: ['CBSE', 'ICSE', 'IB', 'CAMBRIDGE', 'STATE_BOARD'][index % 5],
    classLevel: ['GRADE_7', 'GRADE_8', 'GRADE_9', 'GRADE_10', 'GRADE_11', 'GRADE_12'][index % 6]
  }),

  // Generate random teacher data
  generateTeacherData: (index: number) => ({
    firstName: `Teacher${index}`,
    lastName: 'Test',
    email: `teacher${index}@ankurshala.com`,
    specialization: ['Mathematics', 'Physics', 'Chemistry', 'Biology', 'English'][index % 5],
    hourlyRate: 500 + (index * 100),
    yearsOfExperience: 5 + index
  }),

  // Generate random document data
  generateDocumentData: (type: string) => ({
    documentName: `Test ${type} Document`,
    documentUrl: `https://example.com/test-${type.toLowerCase()}.pdf`,
    documentType: type.toUpperCase()
  }),

  // Generate random address data
  generateAddressData: (type: 'PERMANENT' | 'CURRENT') => ({
    addressLine1: `${type} Address Line 1`,
    addressLine2: 'Near Test Location',
    city: 'New Delhi',
    state: 'Delhi',
    zipCode: '110001',
    country: 'India',
    addressType: type
  })
}

// Test helper functions
export const TestHelpers = {
  // Wait for API response
  waitForApiResponse: async (page: any, url: string, timeout = 5000) => {
    return page.waitForResponse((response: any) => response.url().includes(url), { timeout })
  },

  // Clear all storage
  clearStorage: async (page: any) => {
    await page.evaluate(() => {
      localStorage.clear()
      sessionStorage.clear()
    })
  },

  // Mock confirm dialog
  mockConfirmDialog: (page: any, accept = true) => {
    page.on('dialog', (dialog: any) => {
      if (accept) dialog.accept()
      else dialog.dismiss()
    })
  },

  // Generate random string
  randomString: (length = 8) => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
  }
}

// Test configuration
export const TEST_CONFIG = {
  baseUrl: 'http://localhost:3000',
  apiBaseUrl: 'http://localhost:8080/api',
  timeout: 30000,
  retries: 2,
  screenshotPath: './test-results/screenshots',
  tracePath: './test-results/traces',
  videoPath: './test-results/videos'
}
