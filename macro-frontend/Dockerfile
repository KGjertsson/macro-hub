# Build the static bundle
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Backend base URL baked into the bundle; empty means same origin, proxied by nginx below
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Serve it with nginx
FROM nginx:1.27-alpine
# Where nginx proxies /macro-analyzer/ to (scheme://host:port, no path); the default is the
# compose service. The resolver is read from /etc/resolv.conf at startup (Docker: 127.0.0.11).
ENV BACKEND_URL=http://macro-analyzer:8080 \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
