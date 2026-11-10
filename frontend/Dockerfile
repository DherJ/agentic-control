FROM node:24-alpine AS build
WORKDIR /src
COPY frontend/package*.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /src/dist /usr/share/nginx/html/agent-workspace
COPY infra/nginx/frontend.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
