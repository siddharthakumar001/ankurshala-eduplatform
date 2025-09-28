import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// Define protected routes that require authentication
const protectedRoutes = [
  '/admin',
  '/dashboard',
  '/profile',
  '/settings'
]

// Define admin-only routes
const adminRoutes = [
  '/admin/users',
  '/admin/content',
  '/admin/settings',
  '/admin/reports'
]

// Define public routes that don't require authentication
const publicRoutes = [
  '/',
  '/login',
  '/register',
  '/forgot-password',
  '/unauthorized',
  '/api/auth'
]

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Skip middleware for static files and API routes (except auth)
  if (
    pathname.startsWith('/_next/') ||
    pathname.startsWith('/static/') ||
    pathname.startsWith('/favicon.ico') ||
    (pathname.startsWith('/api/') && !pathname.startsWith('/api/auth'))
  ) {
    return NextResponse.next()
  }

  // Get token from cookies or headers
  const token = request.cookies.get('accessToken')?.value || 
                request.headers.get('authorization')?.replace('Bearer ', '')

  // Check if route is public
  const isPublicRoute = publicRoutes.some(route => pathname.startsWith(route))
  
  // Check if route is protected
  const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route))
  
  // Check if route is admin-only
  const isAdminRoute = adminRoutes.some(route => pathname.startsWith(route))

  // If accessing public route, allow access
  if (isPublicRoute) {
    return NextResponse.next()
  }

  // If accessing protected route without token, redirect to login
  if (isProtectedRoute && !token) {
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('redirect', pathname)
    return NextResponse.redirect(loginUrl)
  }

  // If accessing admin route, we'll let the AuthGuard component handle role checking
  // since we can't easily decode JWT tokens in middleware without additional setup
  if (isAdminRoute && !token) {
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('redirect', pathname)
    return NextResponse.redirect(loginUrl)
  }

  // Add security headers to all responses
  const response = NextResponse.next()
  
  // Security headers
  response.headers.set('X-Frame-Options', 'DENY')
  response.headers.set('X-Content-Type-Options', 'nosniff')
  response.headers.set('X-XSS-Protection', '1; mode=block')
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin')
  response.headers.set('Permissions-Policy', 'camera=(), microphone=(), geolocation=()')
  
  // CSP header for additional security
  response.headers.set(
    'Content-Security-Policy',
    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' http://localhost:8080 https://api.ankurshala.com;"
  )

  return response
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}
