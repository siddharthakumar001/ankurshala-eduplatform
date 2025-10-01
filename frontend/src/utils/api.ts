/**
 * Secure API Client with automatic token management
 * Handles authentication, token refresh, and security headers
 */

import { authManager } from '@/utils/auth'

interface RequestOptions extends RequestInit {
  requireAuth?: boolean
  timeout?: number
}

interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data: T
  errors?: string[]
  timestamp?: string
  path?: string
  traceId?: string
}

interface HttpResponse<T = any> {
  data: T
  status: number
  statusText: string
  headers: Headers
}

class SecureApiClient {
  private baseURL: string
  private defaultTimeout: number = 30000 // 30 seconds

  constructor(baseURL?: string) {
    this.baseURL = baseURL || process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'
  }

  /**
   * Sanitize input to prevent XSS attacks
   */
  private sanitizeInput(input: any): any {
    if (typeof input === 'string') {
      return input
        .replace(/[<>]/g, '') // Remove potential HTML tags
        .replace(/javascript:/gi, '') // Remove javascript: protocol
        .replace(/on\w+=/gi, '') // Remove event handlers
        .trim()
    }
    
    if (Array.isArray(input)) {
      return input.map(item => this.sanitizeInput(item))
    }
    
    if (typeof input === 'object' && input !== null) {
      const sanitized: any = {}
      for (const [key, value] of Object.entries(input)) {
        sanitized[key] = this.sanitizeInput(value)
      }
      return sanitized
    }
    
    return input
  }

  /**
   * Add security headers to request
   */
  private addSecurityHeaders(headers: HeadersInit = {}): HeadersInit {
    const securityHeaders = {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
      'X-Content-Type-Options': 'nosniff',
      'X-Frame-Options': 'DENY',
      'X-XSS-Protection': '1; mode=block',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0',
      ...headers
    }

    return securityHeaders
  }

  /**
   * Add authentication headers
   */
  private addAuthHeaders(headers: HeadersInit = {}): HeadersInit {
    const token = authManager.getToken()
    if (token) {
      return {
        ...headers,
        'Authorization': `Bearer ${token}`
      }
    }
    return headers
  }

  /**
   * Create timeout promise
   */
  private createTimeoutPromise(timeout: number): Promise<never> {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error(`Request timeout after ${timeout}ms`))
      }, timeout)
    })
  }

  /**
   * Handle API response with standardized response format
   */
  private async handleResponse<T>(response: Response): Promise<HttpResponse<T>> {
    // Check for token expiration
    if (response.status === 401) {
      console.log('Unauthorized response, checking token')
      if (authManager.isAuthenticated()) {
        console.log('Token expired, attempting refresh')
        const refreshed = await authManager.refreshToken()
        if (!refreshed) {
          authManager.forceLogout('Session expired. Please log in again.')
          throw new Error('Authentication failed')
        }
      } else {
        authManager.forceLogout('Please log in to continue')
        throw new Error('Authentication required')
      }
    }

    // Handle other error statuses
    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`
      
      try {
        const errorData = JSON.parse(errorText)
        // Handle standardized error responses
        if (errorData.success === false) {
          errorMessage = errorData.message || errorMessage
          if (errorData.errors && errorData.errors.length > 0) {
            errorMessage += ': ' + errorData.errors.join(', ')
          }
        } else {
          errorMessage = errorData.message || errorData.detail || errorMessage
        }
      } catch {
        // Use default error message if JSON parsing fails
      }
      
      throw new Error(errorMessage)
    }

    // Parse response
    let data: T
    const contentType = response.headers.get('content-type')
    
    if (contentType && contentType.includes('application/json')) {
      const jsonResponse = await response.json()
      
      // Check if it's a standardized API response
      if (jsonResponse && typeof jsonResponse === 'object' && 'success' in jsonResponse) {
        const apiResponse: ApiResponse<T> = jsonResponse
        
        // Handle standardized API responses
        if (apiResponse.success === false) {
          let errorMessage = apiResponse.message || 'An unexpected error occurred'
          if (apiResponse.errors && apiResponse.errors.length > 0) {
            errorMessage += ': ' + apiResponse.errors.join(', ')
          }
          throw new Error(errorMessage)
        }
        
        data = apiResponse.data
      } else {
        // Handle direct responses (like /user/me)
        data = jsonResponse as T
      }
    } else {
      data = await response.text() as unknown as T
    }

    return {
      data,
      status: response.status,
      statusText: response.statusText,
      headers: response.headers
    }
  }

  /**
   * Make authenticated request
   */
  private async makeRequest<T>(
    url: string, 
    options: RequestOptions = {}
  ): Promise<HttpResponse<T>> {
    const {
      requireAuth = true,
      timeout = this.defaultTimeout,
      ...fetchOptions
    } = options

    // Check authentication if required
    if (requireAuth && !authManager.isAuthenticated()) {
      throw new Error('Authentication required')
    }

    // Sanitize request body
    if (fetchOptions.body && typeof fetchOptions.body === 'string') {
      try {
        const parsed = JSON.parse(fetchOptions.body)
        const sanitized = this.sanitizeInput(parsed)
        fetchOptions.body = JSON.stringify(sanitized)
      } catch {
        // If not JSON, sanitize as string
        fetchOptions.body = this.sanitizeInput(fetchOptions.body)
      }
    }

    // Prepare headers
    let headers = this.addSecurityHeaders(fetchOptions.headers)
    if (requireAuth) {
      headers = this.addAuthHeaders(headers)
    }

    // Create full URL
    const fullUrl = url.startsWith('http') ? url : `${this.baseURL}${url}`
    
    // Log request without sensitive data
    const logOptions = { ...fetchOptions }
    if (logOptions.body && typeof logOptions.body === 'string') {
      try {
        const parsed = JSON.parse(logOptions.body)
        if (parsed.password) {
          parsed.password = '[REDACTED]'
        }
        logOptions.body = JSON.stringify(parsed)
      } catch {
        // If not JSON, don't log the body
        logOptions.body = '[REDACTED]'
      }
    }
    
    console.log('API Client: Making request to:', fullUrl)
    console.log('API Client: Request options:', { ...logOptions, headers })

    // Create request with timeout
    const requestPromise = fetch(fullUrl, {
      ...fetchOptions,
      headers,
      credentials: 'include' // Include cookies for CSRF protection
    })

    const timeoutPromise = this.createTimeoutPromise(timeout)

    try {
      const response = await Promise.race([requestPromise, timeoutPromise])
      console.log('API Client: Response status:', response.status, response.statusText)
      return await this.handleResponse<T>(response)
    } catch (error) {
      console.error('API Client: Request failed:', error)
      if (error instanceof Error) {
        throw error
      }
      throw new Error('Network error occurred')
    }
  }

  /**
   * Public API methods
   */

  public async get<T>(url: string, options: RequestOptions = {}): Promise<HttpResponse<T>> {
    return this.makeRequest<T>(url, { ...options, method: 'GET' })
  }

  public async post<T>(url: string, data?: any, options: RequestOptions = {}): Promise<HttpResponse<T>> {
    return this.makeRequest<T>(url, {
      ...options,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public async put<T>(url: string, data?: any, options: RequestOptions = {}): Promise<HttpResponse<T>> {
    return this.makeRequest<T>(url, {
      ...options,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public async patch<T>(url: string, data?: any, options: RequestOptions = {}): Promise<HttpResponse<T>> {
    return this.makeRequest<T>(url, {
      ...options,
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  public async delete<T>(url: string, options: RequestOptions = {}): Promise<HttpResponse<T>> {
    return this.makeRequest<T>(url, { ...options, method: 'DELETE' })
  }

  /**
   * Upload file with progress tracking
   */
  public async uploadFile<T>(
    url: string, 
    file: File, 
    onProgress?: (progress: number) => void,
    options: RequestOptions = {}
  ): Promise<HttpResponse<T>> {
    const formData = new FormData()
    formData.append('file', file)

    const xhr = new XMLHttpRequest()

    return new Promise((resolve, reject) => {
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable && onProgress) {
          const progress = (event.loaded / event.total) * 100
          onProgress(progress)
        }
      })

      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const data = JSON.parse(xhr.responseText)
            resolve({
              data,
              status: xhr.status,
              statusText: xhr.statusText,
              headers: new Headers()
            })
          } catch {
            resolve({
              data: xhr.responseText as unknown as T,
              status: xhr.status,
              statusText: xhr.statusText,
              headers: new Headers()
            })
          }
        } else {
          reject(new Error(`Upload failed: ${xhr.statusText}`))
        }
      })

      xhr.addEventListener('error', () => {
        reject(new Error('Upload failed'))
      })

      xhr.open('POST', `${this.baseURL}${url}`)
      
      // Add auth header
      const token = authManager.getToken()
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`)
      }

      xhr.send(formData)
    })
  }
}

// Create singleton instance
export const apiClient = new SecureApiClient()

// Export convenience methods
export const api = {
  get: <T>(url: string, options?: RequestOptions) => apiClient.get<T>(url, options),
  post: <T>(url: string, data?: any, options?: RequestOptions) => apiClient.post<T>(url, data, options),
  put: <T>(url: string, data?: any, options?: RequestOptions) => apiClient.put<T>(url, data, options),
  patch: <T>(url: string, data?: any, options?: RequestOptions) => apiClient.patch<T>(url, data, options),
  delete: <T>(url: string, options?: RequestOptions) => apiClient.delete<T>(url, options),
  uploadFile: <T>(url: string, file: File, onProgress?: (progress: number) => void, options?: RequestOptions) => 
    apiClient.uploadFile<T>(url, file, onProgress, options)
}
