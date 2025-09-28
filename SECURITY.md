# Security Implementation Guide

## Overview
This document outlines the comprehensive security implementation for the AnkurShala Education Platform, including JWT token management, authentication guards, session management, and security utilities.

## 🔐 Authentication & Authorization

### JWT Token Management (`/utils/auth.ts`)
- **Automatic Token Validation**: Checks token expiration and validity
- **Heartbeat Mechanism**: Refreshes tokens every 5 minutes when user is active
- **Idle Timeout**: Automatically logs out users after 45 minutes of inactivity
- **Token Refresh**: Automatically refreshes tokens before expiration
- **Secure Storage**: Tokens stored in localStorage with proper validation

### Authentication Guard (`/components/AuthGuard.tsx`)
- **Route Protection**: Protects admin routes from unauthorized access
- **Role-Based Access**: Supports role-based access control (ADMIN, USER, etc.)
- **Automatic Redirects**: Redirects unauthorized users to login page
- **Loading States**: Shows loading indicators during authentication checks

### Session Management (`/components/SessionManager.tsx`)
- **Session Monitoring**: Tracks user activity and session status
- **Token Expiration Display**: Shows time remaining until token expires
- **Automatic Logout**: Handles session expiration gracefully
- **Session Info Display**: Optional session status widget

## 🛡️ Security Features

### Secure API Client (`/utils/api.ts`)
- **Automatic Token Injection**: Adds JWT tokens to all authenticated requests
- **Request Sanitization**: Sanitizes all request data to prevent XSS
- **Security Headers**: Adds comprehensive security headers to all requests
- **Timeout Protection**: Implements request timeouts to prevent hanging requests
- **Error Handling**: Proper error handling with automatic token refresh
- **File Upload Security**: Secure file upload with progress tracking

### Input Validation & Sanitization (`/utils/security.ts`)
- **XSS Prevention**: Sanitizes HTML content to prevent cross-site scripting
- **SQL Injection Prevention**: Sanitizes SQL inputs
- **Email Validation**: Comprehensive email format validation
- **Password Strength**: Validates password strength requirements
- **File Upload Validation**: Validates file types, sizes, and names
- **URL Validation**: Validates and sanitizes URLs
- **CSRF Protection**: Generates and validates CSRF tokens
- **Rate Limiting**: Implements rate limiting for API requests

### Next.js Security Configuration (`/next.config.js`)
- **Security Headers**: Comprehensive security headers for all responses
- **CSP (Content Security Policy)**: Prevents XSS attacks
- **HSTS**: HTTP Strict Transport Security for HTTPS enforcement
- **Frame Options**: Prevents clickjacking attacks
- **Content Type Options**: Prevents MIME type sniffing
- **Permissions Policy**: Restricts browser features

### Middleware Security (`/middleware.ts`)
- **Route Protection**: Protects sensitive routes at the server level
- **Token Validation**: Validates JWT tokens in middleware
- **Security Headers**: Adds security headers to all responses
- **Redirect Logic**: Handles authentication redirects
- **Static File Protection**: Protects against directory traversal

## 🔒 Login & Authentication Flow

### Login Page (`/app/login/page.tsx`)
- **Secure Form Handling**: Sanitizes all form inputs
- **Password Visibility Toggle**: Secure password input with visibility control
- **Remember Me**: Optional persistent login
- **Error Handling**: Comprehensive error handling and display
- **Security Indicators**: Shows security features to users

### Unauthorized Page (`/app/unauthorized/page.tsx`)
- **Access Denied Handling**: Proper handling of unauthorized access
- **Navigation Options**: Provides safe navigation options
- **Security Messaging**: Clear security-related messaging

## 🚀 Implementation Details

### Token Lifecycle
1. **Login**: User logs in and receives access + refresh tokens
2. **Storage**: Tokens stored securely in localStorage
3. **Validation**: Every request validates token expiration
4. **Refresh**: Tokens automatically refreshed before expiration
5. **Heartbeat**: Active sessions maintained with 5-minute heartbeat
6. **Idle Detection**: 45-minute idle timeout triggers logout
7. **Logout**: Secure logout clears all tokens and redirects

### Security Headers
```javascript
// Applied to all responses
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'...
```

### API Security
- **Authentication**: All admin APIs require valid JWT tokens
- **Authorization**: Role-based access control for sensitive operations
- **Input Sanitization**: All inputs sanitized before processing
- **Rate Limiting**: API rate limiting to prevent abuse
- **CORS**: Proper CORS configuration for cross-origin requests

## 🔧 Usage Examples

### Protecting a Page
```tsx
import AuthGuard from '@/components/AuthGuard'

export default function AdminPage() {
  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <div>Admin content here</div>
    </AuthGuard>
  )
}
```

### Using Secure API Client
```tsx
import { api } from '@/utils/api'

// GET request with automatic auth
const response = await api.get('/admin/content/boards')

// POST request with data sanitization
const response = await api.post('/admin/content/boards', {
  name: 'New Board',
  active: true
})
```

### Input Validation
```tsx
import SecurityUtils from '@/utils/security'

// Validate email
const emailResult = SecurityUtils.validateEmail(userInput)
if (!emailResult.isValid) {
  console.error(emailResult.errors)
}

// Sanitize HTML
const cleanHtml = SecurityUtils.sanitizeHtml(userInput)
```

## 🛠️ Configuration

### Environment Variables
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NODE_ENV=development
```

### Security Settings
- **Token Refresh Threshold**: 5 minutes before expiration
- **Heartbeat Interval**: 5 minutes
- **Idle Timeout**: 45 minutes
- **Request Timeout**: 30 seconds
- **Max File Size**: 10MB
- **Rate Limit**: Configurable per endpoint

## 🔍 Security Monitoring

### Logging
- All authentication events logged
- Failed login attempts tracked
- Token refresh events monitored
- Security violations logged

### Error Handling
- Graceful handling of expired tokens
- Automatic redirects for unauthorized access
- User-friendly error messages
- Security event notifications

## 🚨 Security Best Practices

1. **Never store sensitive data in localStorage** (except tokens)
2. **Always validate user input** before processing
3. **Use HTTPS in production** for all communications
4. **Implement proper CORS** configuration
5. **Regular security audits** of authentication flows
6. **Monitor for suspicious activity** and failed login attempts
7. **Keep dependencies updated** for security patches
8. **Use environment variables** for sensitive configuration

## 🔄 Maintenance

### Regular Tasks
- Monitor token expiration patterns
- Review failed authentication attempts
- Update security headers as needed
- Audit user permissions and roles
- Test authentication flows regularly

### Security Updates
- Update JWT libraries regularly
- Review and update security policies
- Monitor for new security vulnerabilities
- Update CSP policies as needed
- Review and update rate limiting rules

This comprehensive security implementation ensures that the AnkurShala Education Platform is protected against common web vulnerabilities while providing a smooth user experience for authenticated users.
