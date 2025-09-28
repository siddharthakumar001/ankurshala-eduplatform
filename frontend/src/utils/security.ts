/**
 * Security utilities for input validation, sanitization, and security checks
 */

interface ValidationResult {
  isValid: boolean
  errors: string[]
  sanitizedValue?: any
}

class SecurityUtils {
  /**
   * Sanitize HTML content to prevent XSS attacks
   */
  static sanitizeHtml(input: string): string {
    if (typeof input !== 'string') return ''
    
    return input
      .replace(/[<>]/g, '') // Remove potential HTML tags
      .replace(/javascript:/gi, '') // Remove javascript: protocol
      .replace(/on\w+=/gi, '') // Remove event handlers
      .replace(/data:/gi, '') // Remove data: protocol
      .replace(/vbscript:/gi, '') // Remove vbscript: protocol
      .replace(/file:/gi, '') // Remove file: protocol
      .replace(/ftp:/gi, '') // Remove ftp: protocol
      .trim()
  }

  /**
   * Sanitize SQL input to prevent injection attacks
   */
  static sanitizeSql(input: string): string {
    if (typeof input !== 'string') return ''
    
    return input
      .replace(/['"]/g, '') // Remove quotes
      .replace(/;/g, '') // Remove semicolons
      .replace(/--/g, '') // Remove SQL comments
      .replace(/\/\*/g, '') // Remove block comment start
      .replace(/\*\//g, '') // Remove block comment end
      .replace(/union/gi, '') // Remove UNION keyword
      .replace(/select/gi, '') // Remove SELECT keyword
      .replace(/insert/gi, '') // Remove INSERT keyword
      .replace(/update/gi, '') // Remove UPDATE keyword
      .replace(/delete/gi, '') // Remove DELETE keyword
      .replace(/drop/gi, '') // Remove DROP keyword
      .replace(/create/gi, '') // Remove CREATE keyword
      .replace(/alter/gi, '') // Remove ALTER keyword
      .replace(/exec/gi, '') // Remove EXEC keyword
      .replace(/execute/gi, '') // Remove EXECUTE keyword
      .trim()
  }

  /**
   * Validate email format
   */
  static validateEmail(email: string): ValidationResult {
    const errors: string[] = []
    
    if (!email || typeof email !== 'string') {
      errors.push('Email is required')
      return { isValid: false, errors }
    }

    const sanitizedEmail = this.sanitizeHtml(email.toLowerCase())
    
    if (sanitizedEmail.length === 0) {
      errors.push('Email cannot be empty')
      return { isValid: false, errors }
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(sanitizedEmail)) {
      errors.push('Please enter a valid email address')
      return { isValid: false, errors }
    }

    if (sanitizedEmail.length > 254) {
      errors.push('Email address is too long')
      return { isValid: false, errors }
    }

    return { 
      isValid: true, 
      errors: [], 
      sanitizedValue: sanitizedEmail 
    }
  }

  /**
   * Validate password strength
   */
  static validatePassword(password: string): ValidationResult {
    const errors: string[] = []
    
    if (!password || typeof password !== 'string') {
      errors.push('Password is required')
      return { isValid: false, errors }
    }

    if (password.length < 8) {
      errors.push('Password must be at least 8 characters long')
    }

    if (password.length > 128) {
      errors.push('Password is too long (maximum 128 characters)')
    }

    if (!/[A-Z]/.test(password)) {
      errors.push('Password must contain at least one uppercase letter')
    }

    if (!/[a-z]/.test(password)) {
      errors.push('Password must contain at least one lowercase letter')
    }

    if (!/[0-9]/.test(password)) {
      errors.push('Password must contain at least one number')
    }

    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
      errors.push('Password must contain at least one special character')
    }

    // Check for common weak passwords
    const commonPasswords = [
      'password', '123456', '123456789', 'qwerty', 'abc123',
      'password123', 'admin', 'letmein', 'welcome', 'monkey'
    ]
    
    if (commonPasswords.includes(password.toLowerCase())) {
      errors.push('Password is too common, please choose a stronger password')
    }

    return { 
      isValid: errors.length === 0, 
      errors, 
      sanitizedValue: password 
    }
  }

  /**
   * Validate and sanitize text input
   */
  static validateText(text: string, options: {
    minLength?: number
    maxLength?: number
    required?: boolean
    allowHtml?: boolean
  } = {}): ValidationResult {
    const { minLength = 0, maxLength = 1000, required = false, allowHtml = false } = options
    const errors: string[] = []
    
    if (required && (!text || typeof text !== 'string')) {
      errors.push('This field is required')
      return { isValid: false, errors }
    }

    if (!text || typeof text !== 'string') {
      return { isValid: true, errors: [], sanitizedValue: '' }
    }

    const sanitizedText = allowHtml ? text.trim() : this.sanitizeHtml(text.trim())
    
    if (required && sanitizedText.length === 0) {
      errors.push('This field is required')
      return { isValid: false, errors }
    }

    if (sanitizedText.length < minLength) {
      errors.push(`Text must be at least ${minLength} characters long`)
    }

    if (sanitizedText.length > maxLength) {
      errors.push(`Text must not exceed ${maxLength} characters`)
    }

    return { 
      isValid: errors.length === 0, 
      errors, 
      sanitizedValue: sanitizedText 
    }
  }

  /**
   * Validate numeric input
   */
  static validateNumber(value: any, options: {
    min?: number
    max?: number
    required?: boolean
    integer?: boolean
  } = {}): ValidationResult {
    const { min, max, required = false, integer = false } = options
    const errors: string[] = []
    
    if (required && (value === null || value === undefined || value === '')) {
      errors.push('This field is required')
      return { isValid: false, errors }
    }

    if (value === null || value === undefined || value === '') {
      return { isValid: true, errors: [], sanitizedValue: null }
    }

    const numValue = Number(value)
    
    if (isNaN(numValue)) {
      errors.push('Please enter a valid number')
      return { isValid: false, errors }
    }

    if (integer && !Number.isInteger(numValue)) {
      errors.push('Please enter a whole number')
    }

    if (min !== undefined && numValue < min) {
      errors.push(`Value must be at least ${min}`)
    }

    if (max !== undefined && numValue > max) {
      errors.push(`Value must not exceed ${max}`)
    }

    return { 
      isValid: errors.length === 0, 
      errors, 
      sanitizedValue: numValue 
    }
  }

  /**
   * Validate URL format
   */
  static validateUrl(url: string): ValidationResult {
    const errors: string[] = []
    
    if (!url || typeof url !== 'string') {
      errors.push('URL is required')
      return { isValid: false, errors }
    }

    const sanitizedUrl = this.sanitizeHtml(url.trim())
    
    if (sanitizedUrl.length === 0) {
      errors.push('URL cannot be empty')
      return { isValid: false, errors }
    }

    try {
      const urlObj = new URL(sanitizedUrl)
      
      // Only allow HTTP and HTTPS protocols
      if (!['http:', 'https:'].includes(urlObj.protocol)) {
        errors.push('Only HTTP and HTTPS URLs are allowed')
      }
      
      // Check for suspicious patterns
      if (urlObj.hostname.includes('..') || urlObj.hostname.includes('//')) {
        errors.push('Invalid URL format')
      }
      
    } catch {
      errors.push('Please enter a valid URL')
    }

    return { 
      isValid: errors.length === 0, 
      errors, 
      sanitizedValue: sanitizedUrl 
    }
  }

  /**
   * Validate file upload
   */
  static validateFile(file: File, options: {
    maxSize?: number // in bytes
    allowedTypes?: string[]
    required?: boolean
  } = {}): ValidationResult {
    const { maxSize = 10 * 1024 * 1024, allowedTypes = [], required = false } = options // 10MB default
    const errors: string[] = []
    
    if (required && !file) {
      errors.push('File is required')
      return { isValid: false, errors }
    }

    if (!file) {
      return { isValid: true, errors: [], sanitizedValue: null }
    }

    if (file.size > maxSize) {
      errors.push(`File size must not exceed ${Math.round(maxSize / 1024 / 1024)}MB`)
    }

    if (allowedTypes.length > 0 && !allowedTypes.includes(file.type)) {
      errors.push(`File type must be one of: ${allowedTypes.join(', ')}`)
    }

    // Check for suspicious file names
    const suspiciousPatterns = [
      /\.exe$/i, /\.bat$/i, /\.cmd$/i, /\.scr$/i, /\.pif$/i,
      /\.com$/i, /\.vbs$/i, /\.js$/i, /\.jar$/i, /\.php$/i
    ]
    
    if (suspiciousPatterns.some(pattern => pattern.test(file.name))) {
      errors.push('File type not allowed for security reasons')
    }

    return { 
      isValid: errors.length === 0, 
      errors, 
      sanitizedValue: file 
    }
  }

  /**
   * Generate CSRF token
   */
  static generateCsrfToken(): string {
    const array = new Uint8Array(32)
    crypto.getRandomValues(array)
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('')
  }

  /**
   * Validate CSRF token
   */
  static validateCsrfToken(token: string, expectedToken: string): boolean {
    if (!token || !expectedToken) return false
    return token === expectedToken
  }

  /**
   * Rate limiting helper
   */
  static createRateLimiter(maxRequests: number, windowMs: number) {
    const requests = new Map<string, { count: number; resetTime: number }>()
    
    return (identifier: string): boolean => {
      const now = Date.now()
      const windowStart = now - windowMs
      
      // Clean up old entries
      Array.from(requests.entries()).forEach(([key, value]) => {
        if (value.resetTime < windowStart) {
          requests.delete(key)
        }
      })
      
      const current = requests.get(identifier)
      
      if (!current) {
        requests.set(identifier, { count: 1, resetTime: now })
        return true
      }
      
      if (current.resetTime < windowStart) {
        requests.set(identifier, { count: 1, resetTime: now })
        return true
      }
      
      if (current.count >= maxRequests) {
        return false
      }
      
      current.count++
      return true
    }
  }

  /**
   * Check if request is from a suspicious source
   */
  static isSuspiciousRequest(userAgent: string, referer: string): boolean {
    const suspiciousPatterns = [
      /bot/i, /crawler/i, /spider/i, /scraper/i,
      /curl/i, /wget/i, /python/i, /java/i
    ]
    
    return suspiciousPatterns.some(pattern => 
      pattern.test(userAgent) || pattern.test(referer)
    )
  }
}

export default SecurityUtils
