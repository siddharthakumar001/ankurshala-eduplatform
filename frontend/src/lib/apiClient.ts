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
        // Refresh failed - don't redirect immediately, let the component handle it
        // Only redirect if it's an auth endpoint
        if (typeof window !== 'undefined' && originalRequest.url?.includes('/auth/')) {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      }
    }

    // Handle other errors - don't show toast for 401s as they're handled above
    if (error.response?.status === 401) {
      // Don't show error toast for 401s - let components handle it
      return Promise.reject(error)
    } else if (error.response?.status >= 500) {
      toast.error('Server error. Please try again later.')
    } else if (error.response?.status === 403) {
      toast.error('Access denied. You do not have permission to perform this action.')
    } else if (error.response?.status === 404) {
      // Don't show error for 404s - might be expected (e.g., no profile yet)
      console.log('Resource not found:', originalRequest.url)
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

  // Response interceptor for protected client - don't redirect on 401
  protectedClient.interceptors.response.use(
    (response) => response,
    async (error) => {
      // Don't redirect on 401 - let components handle authentication
      // Only log the error for debugging
      if (error.response?.status === 401) {
        console.log('Protected API 401 error:', error.config?.url)
        // Don't redirect - let the component handle it
      } else if (error.response?.status >= 500) {
        toast.error('Server error. Please try again later.')
      } else if (error.response?.status === 403) {
        toast.error('Access denied.')
      }
      
      return Promise.reject(error)
    }
  )

  return protectedClient
}

export const protectedAPI = createProtectedClient()

// Student API functions - using protected client for direct backend calls
export const studentAPI = {
  // Profile Management (5 endpoints)
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
  },

  // Study List Management (7 endpoints)
  getStudyList: async () => {
    const response = await protectedAPI.get('/student/study-list')
    return response.data
  },

  getStudyListSummary: async () => {
    const response = await protectedAPI.get('/student/study-list/summary')
    return response.data
  },

  addToStudyList: async (topicId: number) => {
    const response = await protectedAPI.post('/student/study-list', { topicId })
    return response.data
  },

  updateStudyListStatus: async (id: number, status: string) => {
    const response = await protectedAPI.put(`/student/study-list/${id}/status`, { status })
    return response.data
  },

  updateStudyListNote: async (id: number, notes: string) => {
    const response = await protectedAPI.put(`/student/study-list/${id}/note`, { notes })
    return response.data
  },

  removeFromStudyList: async (id: number) => {
    const response = await protectedAPI.delete(`/student/study-list/${id}`)
    return response.data
  },

  getTopicProgress: async (topicId: number) => {
    const response = await protectedAPI.get(`/student/study-list/topic/${topicId}/progress`)
    return response.data
  },

  // Booking Management (13 endpoints)
  getBookings: async (status?: string) => {
    const params = status ? { status } : {}
    const response = await protectedAPI.get('/student/bookings', { params })
    return response.data
  },

  getBookingById: async (id: number) => {
    const response = await protectedAPI.get(`/student/bookings/${id}`)
    return response.data
  },

  createBooking: async (bookingData: any) => {
    const response = await protectedAPI.post('/student/bookings', bookingData)
    return response.data
  },

  updateBooking: async (id: number, bookingData: any) => {
    const response = await protectedAPI.put(`/student/bookings/${id}`, bookingData)
    return response.data
  },

  cancelBooking: async (id: number, reason: string) => {
    const response = await protectedAPI.post(`/student/bookings/${id}/cancel`, { reason })
    return response.data
  },

  rescheduleBooking: async (id: number, newStartTime: string, newEndTime: string) => {
    const response = await protectedAPI.post(`/student/bookings/${id}/reschedule`, {
      newStartTime,
      newEndTime
    })
    return response.data
  },

  confirmBooking: async (id: number) => {
    const response = await protectedAPI.post(`/student/bookings/${id}/confirm`)
    return response.data
  },

  getUpcomingBookings: async () => {
    const response = await protectedAPI.get('/student/bookings/upcoming')
    return response.data
  },

  getBookingHistory: async () => {
    const response = await protectedAPI.get('/student/bookings/history')
    return response.data
  },

  getPendingBookings: async () => {
    const response = await protectedAPI.get('/student/bookings/pending')
    return response.data
  },

  searchAvailableSlots: async (teacherId: number, date: string) => {
    const response = await protectedAPI.get('/student/bookings/available-slots', {
      params: { teacherId, date }
    })
    return response.data
  },

  getBookingsForTopic: async (topicId: number) => {
    const response = await protectedAPI.get(`/student/bookings/topic/${topicId}`)
    return response.data
  },

  addBookingNote: async (id: number, note: string) => {
    const response = await protectedAPI.post(`/student/bookings/${id}/notes`, { note })
    return response.data
  },

  // Dashboard (2 endpoints)
  getDashboard: async () => {
    const response = await protectedAPI.get('/student/dashboard')
    return response.data
  },

  getRecentActivity: async () => {
    const response = await protectedAPI.get('/student/dashboard/activity')
    return response.data
  },

  // Notifications (5 endpoints)
  getNotifications: async () => {
    const response = await protectedAPI.get('/student/notifications')
    return response.data
  },

  getUnreadNotifications: async () => {
    const response = await protectedAPI.get('/student/notifications/unread')
    return response.data
  },

  markAsRead: async (id: number) => {
    const response = await protectedAPI.put(`/student/notifications/${id}/read`)
    return response.data
  },

  markAllAsRead: async () => {
    const response = await protectedAPI.put('/student/notifications/read-all')
    return response.data
  },

  deleteNotification: async (id: number) => {
    const response = await protectedAPI.delete(`/student/notifications/${id}`)
    return response.data
  },

  getNotificationSettings: async () => {
    const response = await protectedAPI.get('/student/notifications/settings')
    return response.data
  },

  updateNotificationSettings: async (settings: any) => {
    const response = await protectedAPI.put('/student/notifications/settings', settings)
    return response.data
  },

  // Payment (endpoints exist but not in main flow)
  getPayments: async () => {
    const response = await protectedAPI.get('/student/payments')
    return response.data
  },

  getPaymentById: async (id: number) => {
    const response = await protectedAPI.get(`/student/payments/${id}`)
    return response.data
  },

  initiatePayment: async (bookingId: number, amount: number) => {
    const response = await protectedAPI.post('/student/payments/initiate', { bookingId, amount })
    return response.data
  },

  // Session Management (endpoints exist but not in main flow)
  getSessions: async () => {
    const response = await protectedAPI.get('/student/sessions')
    return response.data
  },

  getSessionById: async (id: number) => {
    const response = await protectedAPI.get(`/student/sessions/${id}`)
    return response.data
  },

  completeSession: async (id: number, feedback: string, rating: number) => {
    const response = await protectedAPI.post(`/student/sessions/${id}/complete`, { feedback, rating })
    return response.data
  }
}

// Public Content Discovery API (10 endpoints - no auth required)
export const contentAPI = {
  // Cascading dropdown endpoints
  getBoards: async () => {
    const response = await protectedAPI.get('/content/boards')
    return response.data
  },

  getGradesByBoard: async (boardId: number) => {
    const response = await protectedAPI.get(`/content/grades/by-board/${boardId}`)
    return response.data
  },

  getSubjectsByGrade: async (gradeId: number) => {
    const response = await protectedAPI.get(`/content/subjects/by-grade/${gradeId}`)
    return response.data
  },

  getChaptersBySubject: async (subjectId: number) => {
    const response = await protectedAPI.get(`/content/chapters/by-subject/${subjectId}`)
    return response.data
  },

  getTopicsByChapter: async (chapterId: number) => {
    const response = await protectedAPI.get(`/content/topics/by-chapter/${chapterId}`)
    return response.data
  },

  // Individual entity details
  getBoardById: async (boardId: number) => {
    const response = await protectedAPI.get(`/content/boards/${boardId}`)
    return response.data
  },

  getGradeById: async (gradeId: number) => {
    const response = await protectedAPI.get(`/content/grades/${gradeId}`)
    return response.data
  },

  getSubjectById: async (subjectId: number) => {
    const response = await protectedAPI.get(`/content/subjects/${subjectId}`)
    return response.data
  },

  getChapterById: async (chapterId: number) => {
    const response = await protectedAPI.get(`/content/chapters/${chapterId}`)
    return response.data
  },

  getTopicById: async (topicId: number) => {
    const response = await protectedAPI.get(`/content/topics/${topicId}`)
    return response.data
  },

  // Personalized content for students
  getSubjectsByBoardAndGrade: async (board: string, grade: string) => {
    const response = await protectedAPI.get(`/content/subjects/by-board-grade?board=${encodeURIComponent(board)}&grade=${encodeURIComponent(grade)}`)
    return response.data
  },

  getBoardByName: async (boardName: string) => {
    const response = await protectedAPI.get(`/content/boards/by-name?boardName=${encodeURIComponent(boardName)}`)
    return response.data
  },

  getGradeByName: async (boardId: number, gradeName: string) => {
    const response = await protectedAPI.get(`/content/grades/by-name?boardId=${boardId}&gradeName=${encodeURIComponent(gradeName)}`)
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
