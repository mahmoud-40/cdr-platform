# =============================================
# Optimized Multi-stage Docker Build
# =============================================

# Stage 1: Dependencies cache
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force

# Stage 2: Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --include=dev
COPY . .
RUN npm run build && npm prune --production

# Stage 3: Runtime optimized
FROM nginx:1.25-alpine AS runtime

# Remove default nginx content and install curl
RUN rm -rf /usr/share/nginx/html/* && \
    apk --no-cache add curl

# Copy built application
COPY --from=build /app/dist /usr/share/nginx/html

# Copy optimized nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"] 