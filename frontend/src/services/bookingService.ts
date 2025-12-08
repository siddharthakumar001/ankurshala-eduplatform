/**
 * Student Booking API Service
 * Provides typed API calls for all booking operations
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

export interface BookingQuoteRequest {
  topicId: number
  date: string // ISO date
  startTime: string // HH:mm format
  teacherId: number
}

export interface BookingQuote {
  expectedMinutes: number
  endTimeISO: string
  bufferOk: boolean
  price: {
    currency: string
    min: number
    max: number
  }
}

export interface CreateBookingRequest {
  topicId: number
  date: string // ISO date
  startTime: string // HH:mm format
  teacherId: number
  notes?: string
}

export interface BookingResponse {
  id: number
  studentId: number
  teacherId: number
  teacherName: string
  topicId: number
  topicTitle: string
  date: string
  startTime: string
  endTime: string
  status: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'RESCHEDULED'
  price: number
  currency: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface RescheduleRequest {
  newDate: string
  newStartTime: string
  reason: string
}

export interface CancelBookingRequest {
  reason: string
}

export interface CalendarEvent {
  id: number
  title: string
  start: string // ISO datetime
  end: string // ISO datetime
  status: string
  type: 'BOOKING' | 'AVAILABILITY'
  teacherName?: string
  topicTitle?: string
}

export interface SessionFeedback {
  rating: number // 1-5
  comment: string
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'LATE'
}

export interface SessionJoinResponse {
  sessionUrl: string
  sessionId: string
  meetingPassword?: string
}

// =========================== API SERVICE CLASS ===========================

class BookingService {
  private baseUrl = '/bookings'

  /**
   * Get booking quote (price and duration estimate)
   */
  async getQuote(request: BookingQuoteRequest): Promise<BookingQuote> {
    const url = `${this.baseUrl}/quote`
    console.log('BookingService: Getting quote:', request)

    try {
      const response = await api.post<BookingQuote>(url, request)
      console.log('BookingService: Quote received:', response.data)
      return response.data
    } catch (error) {
      console.error('BookingService: Quote failed:', error)
      throw error
    }
  }

  /**
   * Create a new booking
   */
  async createBooking(request: CreateBookingRequest): Promise<BookingResponse> {
    const url = `${this.baseUrl}`
    console.log('BookingService: Creating booking:', request)

    try {
      const response = await api.post<BookingResponse>(url, request)
      console.log('BookingService: Booking created:', response.data.id)
      return response.data
    } catch (error) {
      console.error('BookingService: Booking creation failed:', error)
      throw error
    }
  }

  /**
   * Get booking details by ID
   */
  async getBooking(bookingId: number): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}`
    console.log('BookingService: Getting booking:', bookingId)

    try {
      const response = await api.get<BookingResponse>(url)
      console.log('BookingService: Booking loaded:', response.data)
      return response.data
    } catch (error) {
      console.error('BookingService: Booking load failed:', error)
      throw error
    }
  }

  /**
   * Get upcoming bookings
   */
  async getUpcomingBookings(page: number = 0, size: number = 20): Promise<PageResponse<BookingResponse>> {
    const url = `${this.baseUrl}/upcoming?page=${page}&size=${size}`
    console.log('BookingService: Getting upcoming bookings')

    try {
      const response = await api.get<PageResponse<BookingResponse>>(url)
      console.log('BookingService: Found bookings:', response.data.content.length)
      return response.data
    } catch (error) {
      console.error('BookingService: Upcoming bookings load failed:', error)
      throw error
    }
  }

  /**
   * Get booking history
   */
  async getBookingHistory(page: number = 0, size: number = 20): Promise<PageResponse<BookingResponse>> {
    const url = `${this.baseUrl}/history?page=${page}&size=${size}`
    console.log('BookingService: Getting booking history')

    try {
      const response = await api.get<PageResponse<BookingResponse>>(url)
      console.log('BookingService: Found history:', response.data.content.length)
      return response.data
    } catch (error) {
      console.error('BookingService: History load failed:', error)
      throw error
    }
  }

  /**
   * Get calendar events
   */
  async getCalendarEvents(fromDate: string, toDate: string): Promise<CalendarEvent[]> {
    const url = `${this.baseUrl}/calendar?from=${fromDate}&to=${toDate}`
    console.log('BookingService: Getting calendar events:', fromDate, toDate)

    try {
      const response = await api.get<CalendarEvent[]>(url)
      console.log('BookingService: Found events:', response.data.length)
      return response.data
    } catch (error) {
      console.error('BookingService: Calendar load failed:', error)
      throw error
    }
  }

  /**
   * Reschedule a booking
   */
  async rescheduleBooking(bookingId: number, request: RescheduleRequest): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}/reschedule`
    console.log('BookingService: Rescheduling booking:', bookingId)

    try {
      const response = await api.put<BookingResponse>(url, request)
      console.log('BookingService: Booking rescheduled:', response.data)
      return response.data
    } catch (error) {
      console.error('BookingService: Reschedule failed:', error)
      throw error
    }
  }

  /**
   * Cancel a booking
   */
  async cancelBooking(bookingId: number, request: CancelBookingRequest): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}/cancel`
    console.log('BookingService: Cancelling booking:', bookingId)

    try {
      const response = await api.put<BookingResponse>(url, request)
      console.log('BookingService: Booking cancelled:', response.data)
      return response.data
    } catch (error) {
      console.error('BookingService: Cancel failed:', error)
      throw error
    }
  }

  /**
   * Add notes to a booking
   */
  async addNotes(bookingId: number, notes: string): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}/notes`
    console.log('BookingService: Adding notes to booking:', bookingId)

    try {
      const response = await api.post<BookingResponse>(url, { notes })
      console.log('BookingService: Notes added')
      return response.data
    } catch (error) {
      console.error('BookingService: Add notes failed:', error)
      throw error
    }
  }

  /**
   * Bookmark a booking
   */
  async bookmarkBooking(bookingId: number, bookmarked: boolean = true): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}/bookmark`
    console.log('BookingService: Bookmarking:', bookingId, bookmarked)

    try {
      const response = await api.post<BookingResponse>(url, { bookmarked })
      console.log('BookingService: Bookmark updated')
      return response.data
    } catch (error) {
      console.error('BookingService: Bookmark failed:', error)
      throw error
    }
  }

  /**
   * Submit session feedback
   */
  async submitFeedback(bookingId: number, feedback: SessionFeedback): Promise<BookingResponse> {
    const url = `${this.baseUrl}/${bookingId}/feedback`
    console.log('BookingService: Submitting feedback for booking:', bookingId)

    try {
      const response = await api.post<BookingResponse>(url, feedback)
      console.log('BookingService: Feedback submitted')
      return response.data
    } catch (error) {
      console.error('BookingService: Feedback submission failed:', error)
      throw error
    }
  }

  /**
   * Join a session
   */
  async joinSession(bookingId: number): Promise<SessionJoinResponse> {
    const url = `${this.baseUrl}/${bookingId}/join`
    console.log('BookingService: Joining session:', bookingId)

    try {
      const response = await api.post<SessionJoinResponse>(url, {})
      console.log('BookingService: Session URL received')
      return response.data
    } catch (error) {
      console.error('BookingService: Join session failed:', error)
      throw error
    }
  }
}

// Export singleton instance
export const bookingService = new BookingService()
