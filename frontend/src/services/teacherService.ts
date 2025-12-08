/**
 * Public Teacher Search API Service
 * Provides typed API calls for teacher search, availability, and profiles
 */

import { api } from '@/utils/api'

// =========================== TYPE DEFINITIONS ===========================

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

export interface TeacherReview {
  studentName: string
  rating: number
  comment: string
  date: string
}

export interface TeacherProfile {
  id: number
  name: string
  email: string
  profilePictureUrl?: string
  bio?: string
  rating: number
  totalRatings: number
  hourlyRate: number
  teacherCategory: string // STANDARD, PREMIUM, PLATINUM
  languages: string[]
  specializations: string[]
  yearsOfExperience?: number
  verified: boolean
  currentlyAvailable: boolean
  completedSessions: number
  qualifications?: string
  responseRate: number
  responseTime: string
  recentReviews: TeacherReview[]
}

export interface TeacherSearchRequest {
  subjectId?: number
  chapterId?: number
  topicId?: number
  date?: string // ISO date format
  timeSlot?: 'MORNING' | 'AFTERNOON' | 'EVENING'
  minRating?: number
  maxHourlyRate?: number
  languages?: string[]
  teacherCategory?: 'STANDARD' | 'PREMIUM' | 'PLATINUM'
  verifiedOnly?: boolean
  availableNow?: boolean
  search?: string
}

export interface TimeSlot {
  startTime: string // ISO datetime
  endTime: string // ISO datetime
  available: boolean
  status: 'AVAILABLE' | 'BOOKED' | 'BLOCKED'
}

export interface TeacherAvailability {
  teacherId: number
  teacherName: string
  available: boolean
  availableSlots: TimeSlot[]
  message: string
}

export interface TeacherAvailabilityRequest {
  teacherId: number
  date: string // ISO date format
  durationMinutes?: number
}

// =========================== API SERVICE CLASS ===========================

class TeacherService {
  private baseUrl = '/public/teachers'

  /**
   * Search for teachers with filters
   */
  async searchTeachers(
    request: TeacherSearchRequest,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'rating',
    sortDir: 'asc' | 'desc' = 'desc'
  ): Promise<PageResponse<TeacherProfile>> {
    const searchParams = new URLSearchParams()
    searchParams.append('page', page.toString())
    searchParams.append('size', size.toString())
    searchParams.append('sortBy', sortBy)
    searchParams.append('sortDir', sortDir)

    const url = `${this.baseUrl}/search?${searchParams}`
    console.log('TeacherService: Searching teachers with criteria:', request)

    try {
      const response = await api.post<PageResponse<TeacherProfile>>(url, request)
      console.log('TeacherService: Found teachers:', response.data.content.length)
      return response.data
    } catch (error) {
      console.error('TeacherService: Search failed:', error)
      throw error
    }
  }

  /**
   * Check teacher availability for a specific date
   */
  async checkAvailability(
    request: TeacherAvailabilityRequest
  ): Promise<TeacherAvailability> {
    const url = `${this.baseUrl}/availability`
    console.log('TeacherService: Checking availability:', request)

    try {
      const response = await api.post<TeacherAvailability>(url, request)
      console.log('TeacherService: Availability:', response.data.available)
      return response.data
    } catch (error) {
      console.error('TeacherService: Availability check failed:', error)
      throw error
    }
  }

  /**
   * Get teacher profile details
   */
  async getTeacherProfile(teacherId: number): Promise<TeacherProfile> {
    const url = `${this.baseUrl}/${teacherId}/profile`
    console.log('TeacherService: Getting profile for teacher:', teacherId)

    try {
      const response = await api.get<TeacherProfile>(url)
      console.log('TeacherService: Profile loaded:', response.data.name)
      return response.data
    } catch (error) {
      console.error('TeacherService: Profile load failed:', error)
      throw error
    }
  }

  /**
   * Get top-rated teachers
   */
  async getTopRatedTeachers(limit: number = 10): Promise<TeacherProfile[]> {
    const response = await this.searchTeachers(
      { minRating: 4.0, verifiedOnly: true },
      0,
      limit,
      'rating',
      'desc'
    )
    return response.content
  }

  /**
   * Get available teachers for immediate booking
   */
  async getAvailableNow(limit: number = 20): Promise<TeacherProfile[]> {
    const response = await this.searchTeachers(
      { availableNow: true, verifiedOnly: true },
      0,
      limit,
      'rating',
      'desc'
    )
    return response.content
  }
}

// Export singleton instance
export const teacherService = new TeacherService()
