# ============================================
# Stage 1: Dependencies (Cached Layer)
# ============================================
FROM node:20-alpine AS dependencies

WORKDIR /app

# Copy only package files for better layer caching
COPY package.json package-lock.json ./

# Install dependencies with optimization flags
RUN npm ci --prefer-offline --no-audit --progress=false && \
    npm cache clean --force

# ============================================
# Stage 2: Builder
# ============================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy node_modules from dependencies stage
COPY --from=dependencies /app/node_modules ./node_modules

# Copy package files
COPY package.json package-lock.json ./

# Copy source code
COPY . .

# Build with production optimizations
ENV NODE_ENV=production
ENV NODE_OPTIONS="--max-old-space-size=2048"

RUN npm run build:prod

# Optional: Remove source maps (uncomment if you don't need them)
# RUN find dist -name "*.map" -type f -delete

# Create a minimal file list for copying
RUN cd dist && \
    find . -type f > /tmp/files.txt

# ============================================
# Stage 3: Nginx Runtime (Optimized)
# ============================================
FROM nginx:1.27-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Remove default nginx config and files
RUN rm -rf /usr/share/nginx/html/* && \
    rm /etc/nginx/conf.d/default.conf

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Set proper permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Create optimized nginx configuration
RUN cat > /etc/nginx/nginx.conf <<'EOF'
user nginx;
worker_processes auto;
worker_rlimit_nofile 65535;

error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    keepalive_requests 100;
    types_hash_max_size 2048;
    server_tokens off;

    client_body_buffer_size 16K;
    client_header_buffer_size 1k;
    client_max_body_size 8m;
    large_client_header_buffers 4 16k;

    client_body_timeout 12;
    client_header_timeout 12;
    send_timeout 10;

    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/rss+xml
        font/truetype
        font/opentype
        application/vnd.ms-fontobject
        image/svg+xml;
    gzip_min_length 256;
    gzip_disable "msie6";

    include /etc/nginx/conf.d/*.conf;
}
EOF

# Create optimized site configuration
RUN cat > /etc/nginx/conf.d/default.conf <<'EOF'
# Upstream for API Gateway
upstream api_gateway {
    server api-gateway:8080;
    keepalive 32;
}

proxy_cache_path /var/cache/nginx/api levels=1:2 keys_zone=api_cache:10m max_size=100m inactive=60m use_temp_path=off;

server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline' 'unsafe-eval'" always;
    add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;

    location ~* \.(jpg|jpeg|png|gif|ico|svg|webp)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    location ~* \.(css|js)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    location ~* \.(woff|woff2|ttf|otf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
        add_header Access-Control-Allow-Origin *;
    }

    location /api/ {
        proxy_pass http://api_gateway/api/;
        
        # Proxy headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
        
        # Dont cache by default (customize per endpoint if needed)
        proxy_no_cache 1;
        proxy_cache_bypass 1;
        
        # Optional: Enable caching for specific API endpoints
        # proxy_cache api_cache;
        # proxy_cache_valid 200 5m;
        # proxy_cache_use_stale error timeout updating http_500 http_502 http_503 http_504;
        # proxy_cache_background_update on;
        # proxy_cache_lock on;
    }

    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    location = /favicon.ico {
        log_not_found off;
        access_log off;
    }

    location = /robots.txt {
        log_not_found off;
        access_log off;
    }

    location / {
        try_files $uri $uri/ /index.html;
        
        # No caching for index.html
        location = /index.html {
            add_header Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0";
            expires off;
        }
    }
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }
}
EOF

# Create cache directory
RUN mkdir -p /var/cache/nginx/api && \
    chown -R nginx:nginx /var/cache/nginx

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# Switch to non-root user
USER nginx

EXPOSE 80

# Use nginx in foreground
CMD ["nginx", "-g", "daemon off;"]