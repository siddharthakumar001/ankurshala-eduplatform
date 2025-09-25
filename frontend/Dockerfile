# syntax=docker/dockerfile:1.6
# Multi-stage build for production-grade Next.js application

############################
# Stage 1: Dependencies
############################
FROM node:20-alpine AS deps
LABEL com.ankurshala=1
WORKDIR /app
COPY package.json package-lock.json* ./
RUN --mount=type=cache,target=/root/.npm npm ci --only=production

############################
# Stage 2: Builder
############################
FROM node:20-alpine AS builder
LABEL com.ankurshala=1
WORKDIR /app

COPY package.json package-lock.json* ./
RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .

# Build-time public API URL (must be passed by --build-arg)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080/api
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

RUN npm run build

############################
# Stage 3: Runner
############################
FROM node:20-alpine AS runner
LABEL com.ankurshala=1

# Non-root user
RUN addgroup --system --gid 1001 nodejs && adduser --system --uid 1001 nextjs
WORKDIR /app

# Copy runtime bits
COPY --from=deps /app/node_modules ./node_modules
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

RUN chown -R nextjs:nodejs /app
USER root && apk add --no-cache wget && su nextjs

ENV NODE_ENV=production \
    PORT=3000 \
    HOSTNAME="0.0.0.0"

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

EXPOSE 3000
USER nextjs
CMD ["node", "server.js"]
