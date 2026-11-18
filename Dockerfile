# ==========================================
# Multi-stage Dockerfile for Railway.app
# Builds React Frontend + Spring Boot Backend
# ==========================================

# 1. Build React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci || npm install

COPY frontend/ ./
RUN npm run build

# 2. Build Spring Boot Backend with embedded static frontend
FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder
WORKDIR /app

COPY backend/pom.xml ./backend/
RUN mvn -f backend/pom.xml dependency:go-offline -B || true

COPY backend/src ./backend/src

# Copy built frontend assets into Spring Boot's static directory
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/

# Package executable JAR
RUN mvn -f backend/pom.xml clean package -DskipTests

# 3. Production Runtime Container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data /app/clips

COPY --from=backend-builder /app/backend/target/*.jar app.jar

ENV PORT=10000
ENV MONITORED_FOLDER=/app/clips
ENV AUTO_DELETE_ON_PUBLISH=true
ENV YOUTUBE_REDIRECT_URI=https://youtube-publishing-agent-1.onrender.com/api/youtube/oauth2/callback
ENV DB_URL="jdbc:h2:file:/app/data/youtube_agent;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"

EXPOSE 10000 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
