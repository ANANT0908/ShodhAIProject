# Stage 1: build
FROM node:18-alpine AS builder
WORKDIR /app

# copy package files first for better caching
COPY package*.json ./
RUN npm ci --prefer-offline --no-audit

COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Stage 2: production image
FROM node:18-alpine
WORKDIR /app
ENV NODE_ENV=production

COPY --from=builder /app/package.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.js ./next.config.js

EXPOSE 3000
CMD ["npx", "next", "start", "-p", "3000"]
# Stage 1: build
FROM node:18-alpine AS builder
WORKDIR /app

# copy package files first for better caching
COPY package*.json ./
RUN npm ci --prefer-offline --no-audit

COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# Stage 2: production image
FROM node:18-alpine
WORKDIR /app
ENV NODE_ENV=production

COPY --from=builder /app/package.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/next.config.js ./next.config.js

EXPOSE 3000
CMD ["npx", "next", "start", "-p", "3000"]
