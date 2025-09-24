# syntax=docker/dockerfile:1.6
ARG APP_LABEL=com.ankurshala=1

FROM node:20 AS build
LABEL com.ankurshala=1
WORKDIR /app
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
RUN npm run build

FROM node:20
LABEL com.ankurshala=1
WORKDIR /app
COPY --from=build /app/.next ./.next
COPY --from=build /app/package*.json ./
RUN npm ci --only=production
EXPOSE 3000
CMD ["npm", "start"]