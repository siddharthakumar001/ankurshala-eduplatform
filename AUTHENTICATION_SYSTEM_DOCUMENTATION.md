# AnkurShala Authentication System Documentation

## Overview

The AnkurShala authentication system provides comprehensive user authentication, session management, and security features for the educational platform. It supports multiple user roles (Admin, Teacher, Student) with role-based access control.

## Architecture

### Backend (Spring Boot 3)
- **JWT Token Management**: Access tokens (15 min) and refresh tokens (7 days)
- **Spring Security**: Comprehensive security configuration with CSRF protection
- **Role-Based Access Control**: ADMIN, TEACHER, STUDENT roles
- **Session Management**: Stateless JWT-based authentication
- **Security Headers**: XSS protection, CSRF tokens, secure cookies

### Frontend (Next.js 14)
- **Cookie-Based Authentication**: Secure httpOnly cookies for token storage
- **Zustand State Management**: Centralized authentication state
- **Heartbeat System**: Active session management with 5-minute intervals
- **Idle Timeout**: Automatic logout after 45 minutes of inactivity
- **Route Protection**: Middleware-based route guards

## Features

### ✅ Implemented Features

1. **Login/Signin Flow**
   - Email/password authentication
   - Input validation and sanitization
   - Error handling with user-friendly messages
   - Automatic redirect based on user role

2. **Signup Flow**
   - Student registration (`/register-student`)
   - Teacher registration (`/register-teacher`)
   - Form validation and password confirmation
   - Terms and conditions agreement

3. **Session Management**
   - Heartbeat mechanism (5-minute intervals)
   - Idle timeout (45 minutes)
   - Automatic token refresh
   - Session persistence across page refreshes

4. **Logout Flow**
   - Server-side session invalidation
   - Client-side state cleanup
   - Secure cookie removal
   - Redirect to login page

5. **Security Features**
   - XSS protection with input sanitization
   - CSRF protection with tokens
   - Secure httpOnly cookies
   - Password strength validation
   - Rate limiting protection

6. **Error Handling**
   - Network error recovery
   - Server error handling
   - Validation error messages
   - Graceful degradation

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/signin` | User login | No |
| POST | `/api/auth/signup/student` | Student registration | No |
| POST | `/api/auth/signup/teacher` | Teacher registration | No |
| POST | `/api/auth/refresh` | Token refresh | No |
| POST | `/api/auth/logout` | User logout | No |
| POST | `/api/auth/heartbeat` | Session heartbeat | Yes |
| GET | `/api/auth/test` | Test endpoint | No |

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/user/me` | Get current user info | Yes |

## Frontend Components

### Pages
- `/login` - Login page
- `/register-student` - Student registration
- `/register-teacher` - Teacher registration

### Hooks
- `useAuth()` - Main authentication hook with heartbeat
- `useRequireAuth()` - Route protection hook
- `useRequireRole()` - Role-based access control hook

### Stores
- `useAuthStore` - Zustand store for authentication state

### API Client
- `api` - Secure API client with automatic token management
- Cookie-based authentication with CSRF protection

## Security Implementation

### Token Management
```typescript
// Access Token: 15 minutes
// Refresh Token: 7 days
// Heartbeat: Every 5 minutes
// Idle Timeout: 45 minutes
```

### Cookie Security
```typescript
// Secure httpOnly cookies
accessToken: {
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'strict',
  maxAge: 15 * 60, // 15 minutes
  path: '/',
}
```

### Input Sanitization
```typescript
// XSS Protection
const sanitizedValue = value
  .replace(/[<>]/g, '')
  .replace(/javascript:/gi, '')
  .replace(/on\w+=/gi, '')
  .trim()
```

## Testing

### Test Coverage
- **Unit Tests**: Individual component testing
- **Integration Tests**: API integration testing
- **E2E Tests**: Complete user flow testing
- **Regression Tests**: Ensure no breaking changes
- **Security Tests**: XSS, CSRF, input validation
- **Performance Tests**: Load and stress testing

### Test Commands
```bash
# Run all authentication tests
npm run test:auth:all

# Run specific test suites
npm run test:auth:regression
npm run test:auth:comprehensive
npm run test:auth:integration

# Run comprehensive test suite
./scripts/test-auth-comprehensive.sh
```

### Test Files
- `tests/auth.spec.ts` - Basic authentication tests
- `tests/auth-comprehensive.spec.ts` - Comprehensive testing
- `tests/auth-regression.spec.ts` - Regression testing
- `tests/auth-integration-comprehensive.spec.ts` - Integration testing

## Configuration

### Environment Variables
```bash
# Backend
JWT_SECRET=your-super-secret-jwt-key
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ankurshala
DB_USERNAME=ankur
DB_PASSWORD=password

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:8080/api
BACKEND_URL=http://localhost:8080/api
```

### Security Configuration
```yaml
# application.yml
app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey}
    access-token-expiration: 900  # 15 minutes
    refresh-token-expiration: 604800  # 7 days
```

## Usage Examples

### Login Flow
```typescript
import { useAuth } from '@/hooks/useAuth'

function LoginComponent() {
  const { login, isAuthenticated } = useAuth()
  
  const handleLogin = async (email: string, password: string) => {
    try {
      const response = await api.post('/auth/signin', { email, password })
      if (response.data.success) {
        login(response.data.data)
      }
    } catch (error) {
      // Handle error
    }
  }
}
```

### Route Protection
```typescript
import { useRequireAuth } from '@/hooks/useAuth'

function ProtectedPage() {
  const { isAuthenticated, isLoading } = useRequireAuth()
  
  if (isLoading) return <Loading />
  if (!isAuthenticated) return <Redirect to="/login" />
  
  return <PageContent />
}
```

### Role-Based Access
```typescript
import { useRequireRole } from '@/hooks/useAuth'

function AdminPage() {
  const { hasRequiredRole } = useRequireRole(['ADMIN'])
  
  if (!hasRequiredRole) return <Unauthorized />
  
  return <AdminContent />
}
```

## Monitoring and Logging

### Backend Logging
- Authentication events (login, logout, signup)
- Security events (failed attempts, token refresh)
- Business operations (user management)
- Error tracking and debugging

### Frontend Logging
- User activity tracking
- Session management events
- Error handling and recovery
- Performance monitoring

## Troubleshooting

### Common Issues

1. **Login Not Working**
   - Check backend service is running
   - Verify database connection
   - Check JWT secret configuration

2. **Session Expiring Too Quickly**
   - Verify heartbeat is working
   - Check idle timeout configuration
   - Ensure user activity is being tracked

3. **CSRF Token Errors**
   - Verify CSRF endpoint is accessible
   - Check cookie configuration
   - Ensure proper headers are sent

4. **Token Refresh Issues**
   - Check refresh token validity
   - Verify token expiration times
   - Ensure proper error handling

### Debug Commands
```bash
# Check backend health
curl http://localhost:8080/api/actuator/health

# Test authentication endpoint
curl -X POST http://localhost:8080/api/auth/test

# Check frontend
curl http://localhost:3000/login
```

## Future Enhancements

### Planned Features
- [ ] Two-factor authentication (2FA)
- [ ] Social login (Google, Microsoft)
- [ ] Password reset functionality
- [ ] Account verification via email
- [ ] Advanced session management
- [ ] Audit logging and compliance
- [ ] Multi-device session management
- [ ] Biometric authentication support

### Security Improvements
- [ ] Advanced rate limiting
- [ ] IP-based access control
- [ ] Device fingerprinting
- [ ] Suspicious activity detection
- [ ] Enhanced password policies
- [ ] Security headers optimization

## Contributing

### Development Guidelines
1. Follow security best practices
2. Write comprehensive tests
3. Document all changes
4. Use TypeScript for type safety
5. Follow the existing code structure
6. Test all authentication flows

### Testing Requirements
- All new features must have tests
- Maintain 100% test coverage for auth flows
- Run regression tests before deployment
- Include security testing for new features

## Support

For issues or questions regarding the authentication system:
1. Check the troubleshooting section
2. Review the test results
3. Check the logs for errors
4. Contact the development team

---

**Last Updated**: $(date)
**Version**: 1.0.0
**Maintainer**: AnkurShala Development Team
