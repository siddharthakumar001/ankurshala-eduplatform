# syntax=docker/dockerfile:1.6
# Multi-stage build for production-grade Next.js application

############################
# Stage 1: Dependencies
############################
FROM node:20-alpine AS deps
LABEL com.ankurshala=1

WORKDIR /app
COPY package.json package-lock.json* ./

# Clear npm cache and install production dependencies
RUN npm cache clean --force && \
    npm ci --only=production --no-audit --no-fund && \
    npm cache clean --force

############################
# Stage 2: Builder
############################
FROM node:20-alpine AS builder
LABEL com.ankurshala=1

WORKDIR /app

# Copy package files and install all dependencies
COPY package.json package-lock.json* ./

# Clear npm cache and install all dependencies
RUN npm cache clean --force && \
    npm ci --no-audit --no-fund && \
    npm cache clean --force

# Copy source code
COPY . .

# Ensure public directory exists
RUN mkdir -p public

# Set build-time environment variables
ARG NEXT_PUBLIC_API_URL=https://ankurshala.com/api
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV DOCKER_BUILD=true

# Build the application
RUN npm run build

############################
# Stage 3: Runner (Production)
############################
FROM node:20-alpine AS runner
LABEL com.ankurshala=1

# Create non-root user for security
RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

WORKDIR /app

# Copy production dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules

# Copy built application from builder stage
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

# Handle public directory safely
RUN mkdir -p ./public
COPY --from=builder /app/public ./public

# Change ownership to non-root user
RUN chown -R nextjs:nodejs /app

# Switch to non-root user
USER nextjs

# Environment variables
ENV NODE_ENV=production \
    PORT=3000 \
    HOSTNAME="0.0.0.0"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

# Expose port
EXPOSE 3000

# Start the application
CMD ["node", "server.js"]