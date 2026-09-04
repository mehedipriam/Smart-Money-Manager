# ---- Build stage: produce the static Vite bundle ----
FROM node:22-alpine AS build
WORKDIR /app

# Dependency install cached in its own layer, separate from source changes.
COPY package.json package-lock.json* ./
RUN npm ci

COPY . .

# Baked into the built JS at compile time (Vite env vars aren't readable at container
# runtime). Defaults to a same-origin relative path — nginx.conf below proxies it to the
# backend service, so the browser never needs to know the backend's actual host/port and
# no cross-origin request (or CORS config) is involved.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ---- Serve stage: static files behind nginx, with an API reverse proxy ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# 127.0.0.1, not localhost: this image's nginx listens on the IPv4 wildcard only, but
# "localhost" can resolve to ::1 first inside the container — the loopback IP sidesteps that.
HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=5 \
    CMD wget -qO- http://127.0.0.1/ >/dev/null 2>&1 || exit 1
