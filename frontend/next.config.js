/** @type {import('next').NextConfig} */
const nextConfig = {
  // Only use standalone output for Docker/production builds
  ...(process.env.DOCKER_BUILD === 'true' && { output: 'standalone' }),
  typescript: {
    ignoreBuildErrors: process.env.DOCKER_BUILD === 'true',
  },
  eslint: {
    ignoreDuringBuilds: process.env.DOCKER_BUILD === 'true',
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  },
  experimental: {
    // Enable faster refresh in development
    optimizePackageImports: ['@/components/ui'],
  },
  // Security configurations
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
          {
            key: 'Permissions-Policy',
            value: 'camera=(), microphone=(), geolocation=()',
          },
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=31536000; includeSubDomains; preload',
          },
        ],
      },
      {
        source: '/api/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'no-cache, no-store, must-revalidate',
          },
          {
            key: 'Pragma',
            value: 'no-cache',
          },
          {
            key: 'Expires',
            value: '0',
          },
        ],
      },
    ]
  },
  // API rewrites to proxy backend requests
  async rewrites() {
    // For server-side rewrites, we need to use the actual backend URL
    // In Docker: use internal service name (backend:8080)
    // In development: use localhost:8080
    // BACKEND_URL is only for server-side (Next.js server) communication with backend
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080/api'
    
    console.log('[Next.js Rewrites] Backend URL:', backendUrl)
    
    return [
      // NOTE: Routes for /api/admin/*, /api/student/*, /api/teacher/* are handled by
      // API route handlers in src/app/api/ which properly forward cookies
      // Only rewrite auth and other public endpoints directly to backend
      {
        source: '/api/auth/:path*',
        destination: `${backendUrl}/auth/:path*`,
      },
      {
        source: '/api/user/:path*',
        destination: `${backendUrl}/user/:path*`,
      },
      {
        source: '/api/actuator/:path*',
        destination: `${backendUrl}/actuator/:path*`,
      },
    ]
  },
  // Redirects for security
  async redirects() {
    return [
      {
        source: '/admin',
        destination: '/admin/dashboard',
        permanent: false,
      },
    ]
  },
  // Webpack configuration for security
  webpack: (config, { dev, isServer }) => {
    // Add source map support in development
    if (dev && !isServer) {
      config.devtool = 'cheap-module-source-map'
    }
    
    // Security: Prevent access to sensitive files
    config.module.rules.push({
      test: /\.(env|config)\.(js|ts)$/,
      use: 'ignore-loader',
    })
    
    return config
  },
  // Image optimization security
  images: {
    domains: ['localhost', 'api.ankurshala.com'],
    dangerouslyAllowSVG: false,
    contentSecurityPolicy: "default-src 'self'; script-src 'none'; sandbox;",
  },
  // Compression and optimization
  compress: true,
  poweredByHeader: false,
  // Development server configuration
  ...(process.env.NODE_ENV === 'development' && {
    onDemandEntries: {
      // Period (in ms) where the server will keep pages in the buffer
      maxInactiveAge: 25 * 1000,
      // Number of pages that should be kept simultaneously without being disposed
      pagesBufferLength: 2,
    },
  }),
}

module.exports = nextConfig