import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { toast } from 'sonner'

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    // Get token from localStorage (in production, use httpOnly cookies)
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
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
        // Attempt to refresh token
        const refreshToken = typeof window !== 'undefined' ? localStorage.getItem('refreshToken') : null
        
        if (refreshToken) {
          const response = await axios.post(
            `${process.env.NEXT_PUBLIC_API_BASE || 'http://localhost:8080/api'}/auth/refresh`,
            { refreshToken }
          )

          const { accessToken, refreshToken: newRefreshToken } = response.data

          // Update tokens in localStorage
          if (typeof window !== 'undefined') {
            localStorage.setItem('accessToken', accessToken)
            localStorage.setItem('refreshToken', newRefreshToken)
          }

          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return apiClient(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        if (typeof window !== 'undefined') {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
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

// Auth API functions
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
      role: 'STUDENT'
    })
    return response.data
  },

  signupTeacher: async (name: string, email: string, password: string) => {
    const response = await apiClient.post('/auth/signup/teacher', {
      name,
      email,
      password,
      role: 'TEACHER'
    })
    return response.data
  },

  refresh: async (refreshToken: string) => {
    const response = await apiClient.post('/auth/refresh', { refreshToken })
    return response.data
  },

  logout: async (refreshToken: string) => {
    const response = await apiClient.post('/auth/logout', { refreshToken })
    return response.data
  }
}

// Student API functions
export const studentAPI = {
  getProfile: async () => {
    const response = await apiClient.get('/student/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await apiClient.put('/student/profile', profileData)
    return response.data
  },

  getDocuments: async () => {
    const response = await apiClient.get('/student/profile/documents')
    return response.data
  },

  addDocument: async (documentData: any) => {
    const response = await apiClient.post('/student/profile/documents', documentData)
    return response.data
  },

  deleteDocument: async (documentId: number) => {
    const response = await apiClient.delete(`/student/profile/documents/${documentId}`)
    return response.data
  }
}

// Teacher API functions
export const teacherAPI = {
  getProfile: async () => {
    const response = await apiClient.get('/teacher/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await apiClient.put('/teacher/profile', profileData)
    return response.data
  },

  // Qualifications
  getQualifications: async () => {
    const response = await apiClient.get('/teacher/profile/qualifications')
    return response.data
  },

  addQualification: async (qualificationData: any) => {
    const response = await apiClient.post('/teacher/profile/qualifications', qualificationData)
    return response.data
  },

  updateQualification: async (id: number, qualificationData: any) => {
    const response = await apiClient.put(`/teacher/profile/qualifications/${id}`, qualificationData)
    return response.data
  },

  deleteQualification: async (id: number) => {
    const response = await apiClient.delete(`/teacher/profile/qualifications/${id}`)
    return response.data
  },

  // Experiences
  getExperiences: async () => {
    const response = await apiClient.get('/teacher/profile/experiences')
    return response.data
  },

  addExperience: async (experienceData: any) => {
    const response = await apiClient.post('/teacher/profile/experiences', experienceData)
    return response.data
  },

  updateExperience: async (id: number, experienceData: any) => {
    const response = await apiClient.put(`/teacher/profile/experiences/${id}`, experienceData)
    return response.data
  },

  deleteExperience: async (id: number) => {
    const response = await apiClient.delete(`/teacher/profile/experiences/${id}`)
    return response.data
  },

  // Certifications
  getCertifications: async () => {
    const response = await apiClient.get('/teacher/profile/certifications')
    return response.data
  },

  addCertification: async (certificationData: any) => {
    const response = await apiClient.post('/teacher/profile/certifications', certificationData)
    return response.data
  },

  updateCertification: async (id: number, certificationData: any) => {
    const response = await apiClient.put(`/teacher/profile/certifications/${id}`, certificationData)
    return response.data
  },

  deleteCertification: async (id: number) => {
    const response = await apiClient.delete(`/teacher/profile/certifications/${id}`)
    return response.data
  },

  // Bank Details
  getBankDetails: async () => {
    const response = await apiClient.get('/teacher/profile/bank-details')
    return response.data
  },

  updateBankDetails: async (bankData: any) => {
    const response = await apiClient.put('/teacher/profile/bank-details', bankData)
    return response.data
  },

  // Documents
  getDocuments: async () => {
    const response = await apiClient.get('/teacher/profile/documents')
    return response.data
  },

  addDocument: async (documentData: any) => {
    const response = await apiClient.post('/teacher/profile/documents', documentData)
    return response.data
  },

  updateDocument: async (id: number, documentData: any) => {
    const response = await apiClient.put(`/teacher/profile/documents/${id}`, documentData)
    return response.data
  },

  deleteDocument: async (id: number) => {
    const response = await apiClient.delete(`/teacher/profile/documents/${id}`)
    return response.data
  }
}

// Admin API functions
export const adminAPI = {
  getProfile: async () => {
    const response = await apiClient.get('/admin/profile')
    return response.data
  },

  updateProfile: async (profileData: any) => {
    const response = await apiClient.put('/admin/profile', profileData)
    return response.data
  }
}

export const userAPI = {
  getCurrentUser: async () => {
    const response = await apiClient.get('/user/me')
    return response.data
  }
}
