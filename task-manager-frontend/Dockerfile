# --------- ETAPE DE BUILD ---------
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build --configuration=production

# --------- ETAPE SERVEUR WEB NGINX ---------
FROM nginx:alpine
# Copie le build Angular dans le serveur Nginx
COPY --from=build /app/dist/task-manager-frontend/browser /usr/share/nginx/html
# Configuration basique Nginx pour laisser Angular gérer le routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
