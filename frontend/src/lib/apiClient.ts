import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { toast } from 'sonner'

// Create axios instance for Next.js API routes (for auth)
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Include cookies in requests
})

// Request interceptor - cookies are automatically included
apiClient.interceptors.request.use(
  (config) => {
    // Cookies are automatically included with withCredentials: true
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle token refresh
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // If 401 and not already retried
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        // Attempt to refresh token via Next.js API route
        const response = await apiClient.post('/auth/refresh')
        
        if (response.data.success) {
          // Retry original request - cookies are automatically included
          return apiClient(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        if (typeof window !== 'undefined') {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      }
    }

    // Handle other errors
    if (error.response?.status >= 500) {
      toast.error('Server error. Please try again later.')
    } else if (error.response?.status === 403) {
      toast.error('Access denied. You do not have permission to perform this action.')
    } else if (error.response?.status === 404) {
      toast.error('Resource not found.')
    }

    return Promise.reject(error)
  }
)

export default apiClient

// Auth API functions - using Next.js API routes for security
export const authAPI = {
  signin: async (email: string, password: string) => {
    const response = await apiClient.post('/auth/signin', { email, password })
    return response.data
  },

  signupStudent: async (name: string, email: string, password: string) => {
    const response = await apiClient.post('/auth/signup/student', {
      name,
      email,
      password,
    })
    return response.data
  },

  signupTeacher: async (name: string, email: string, password: string) => {
    const response = await apiClient.post('/auth/signup/teacher', {
      name,
      email,
      password,
    })
    return response.data
  },

  refresh: async () => {
    const response = await apiClient.post('/auth/refresh')
    return response.data
  },

  logout: async () => {
    const response = await apiClient.post('/auth/logout')
    return response.data
  }
}

// User API functions
export const userAPI = {
  getCurrentUser: async () => {
    const response = await apiClient.get('/user/me')
    return response.data.data
  }
}

// CSRF API functions
export const csrfAPI = {
  getToken: async () => {
    const response = await apiClient.get('/csrf')
    return response.data.token
  }
}

// Create a separate client for protected backend routes
const createProtectedClient = () => {
  const protectedClient = axios.create({
    // Use relative URLs to go through Next.js proxy
    baseURL: '/api',
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true,
  })

  // Add CSRF token to requests for state-changing operations
  protectedClient.interceptors.request.use(async (config) => {
    // Only add CSRF token for state-changing operations
    if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
      try {
        // Get CSRF token from Next.js API route
        const csrfResponse = await apiClient.get('/csrf')
        const csrfToken = csrfResponse.data.token
        if (csrfToken) {
          config.headers['X-CSRF-TOKEN'] = csrfToken
        }
      } catch (error) {
        console.warn('Failed to fetch CSRF token:', error)
        // Fallback to cookie if API call fails
        const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='))?.split('=')[1]
        if (csrfToken) {
          config.headers['X-CSRF-TOKEN'] = csrfToken
        }
      }
    }
    return config
  })

  return protectedClient
}

export const protectedAPI = createProtectedClient()

// Student API functions - using protected client for direct backend calls
export const studentAPI = {
  getProfile: async () => {
    const response = await protectedAPI.get('/student/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await protectedAPI.put('/student/profile', profileData)
    return response.data
  },

  getDocuments: async () => {
    const response = await protectedAPI.get('/student/profile/documents')
    return response.data
  },

  addDocument: async (documentData: any) => {
    const response = await protectedAPI.post('/student/profile/documents', documentData)
    return response.data
  },

  deleteDocument: async (documentId: number) => {
    const response = await protectedAPI.delete(`/student/profile/documents/${documentId}`)
    return response.data
  }
}

// Teacher API functions - using protected client for direct backend calls
export const teacherAPI = {
  getProfile: async () => {
    const response = await protectedAPI.get('/teacher/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await protectedAPI.put('/teacher/profile', profileData)
    return response.data
  },

  // Qualifications
  getQualifications: async () => {
    const response = await protectedAPI.get('/teacher/profile/qualifications')
    return response.data
  },

  addQualification: async (qualificationData: any) => {
    const response = await protectedAPI.post('/teacher/profile/qualifications', qualificationData)
    return response.data
  },

  updateQualification: async (id: number, qualificationData: any) => {
    const response = await protectedAPI.put(`/teacher/profile/qualifications/${id}`, qualificationData)
    return response.data
  },

  deleteQualification: async (id: number) => {
    const response = await protectedAPI.delete(`/teacher/profile/qualifications/${id}`)
    return response.data
  },

  // Bank Details
  getBankDetails: async () => {
    const response = await protectedAPI.get('/teacher/profile/bank-details')
    return response.data
  },

  updateBankDetails: async (bankData: any) => {
    const response = await protectedAPI.put('/teacher/profile/bank-details', bankData)
    return response.data
  }
}

// Admin API functions - using protected client for direct backend calls
export const adminAPI = {
  getProfile: async () => {
    const response = await protectedAPI.get('/admin/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await protectedAPI.put('/admin/profile', profileData)
    return response.data
  }
}
