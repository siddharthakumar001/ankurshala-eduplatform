# Authentication and Security

**Last Updated:** October 7, 2025

---

## Authentication Flow

### Login Process
1. User submits email and password to `/api/auth/signin`
2. Backend validates credentials
3. Backend generates JWT token
4. Frontend stores JWT in httpOnly cookie
5. Cookie automatically sent with subsequent requests

### Session Management
- JWT expires in 24 hours (configurable)
- Session persists across page refreshes
- Auto-logout on token expiration
- Secure httpOnly cookies prevent XSS attacks

---

## Role-Based Access Control (RBAC)

### User Roles
- **ADMIN** - Full system access
- **TEACHER** - Course and student management
- **STUDENT** - Limited to own profile and enrolled courses

### Permission Matrix

| Feature | ADMIN | TEACHER | STUDENT |
|---------|-------|---------|---------|
| Dashboard | ✅ | ✅ | ❌ |
| Student Management | ✅ | View Only | Own Profile |
| Content Management | ✅ | ✅ | View Only |
| User Management | ✅ | ❌ | ❌ |

---

## Security Best Practices

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### API Security
- JWT authentication on all protected routes
- CORS configured for allowed origins
- CSRF protection enabled
- Rate limiting on authentication endpoints

### Frontend Security
- httpOnly cookies prevent JavaScript access
- Secure cookies in production (HTTPS only)
- Route guards on protected pages
- Automatic redirect to login on 401

---

## Default Credentials

**Admin Account:**
- Email: `siddhartha@ankurshala.com`
- Password: `Maza@123`

**⚠️ Change these credentials in production!**

---

## Security Configurations

### Backend (Spring Security)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT authentication
    // CORS configuration
    // Method-level security
    // Password encoding (BCrypt)
}
```

### Frontend (Next.js)
- API proxy routes with cookie forwarding
- Automatic token refresh
- Client-side route protection

---

**See Also:**
- [SETUP_AND_DEPLOYMENT.md](./SETUP_AND_DEPLOYMENT.md) for environment setup
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) for auth issues
