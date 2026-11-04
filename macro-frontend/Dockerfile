FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Backend base URL baked into the bundle; empty means same origin, proxied by nginx below
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:1.27-alpine
# Where nginx proxies each route to (scheme://host:port, no path); the defaults are the compose services.
# These defaults matter: the template keeps ${X} literally for an unset variable. An empty value
# makes nginx exit at startup. The resolver is read from /etc/resolv.conf (Docker: 127.0.0.11).
ENV MACRO_SERIES_URL=http://macro-series:8083 \
    REGIONAL_STATISTICS_URL=http://regional-statistics:8082 \
    PARLIAMENT_URL=http://parliament:8084 \
    ELECTION_URL=http://election:8085 \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
