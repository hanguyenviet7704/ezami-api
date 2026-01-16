# ============================================
# Multi-stage Dockerfile cho Ezami API
# ============================================

# === Stage 1: Build ===
FROM gradle:8.5-jdk17-alpine AS builder

# Metadata
LABEL maintainer="ezami-team"
LABEL description="Ezami API - Spring Boot Application"
LABEL version="1.2.0"

WORKDIR /app

# Install build dependencies
RUN apk add --no-cache \
    curl \
    bash \
    && rm -rf /var/cache/apk/*

# Copy Gradle wrapper và config files trước (để tận dụng cache)
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cache layer này)
# Sử dụng --no-daemon để tránh background process
RUN ./gradlew dependencies --no-daemon --stacktrace || true

# Copy source code
COPY src/ src/

# Build application (skip tests để build nhanh hơn, tests chạy riêng trong CI/CD)
RUN ./gradlew clean build \
    -x test \
    -x testClasses \
    --no-daemon \
    --stacktrace \
    --info

# Verify JAR file exists
RUN ls -lah /app/build/libs/*.jar || (echo "ERROR: JAR file not found!" && exit 1)

# === Stage 2: Runtime ===
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="ezami-team"
LABEL description="Ezami API - Runtime Image"

# Install runtime dependencies
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S -G appuser appuser

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/uploads/images \
    /app/uploads/avatars \
    /app/uploads/temp \
    /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM Options optimized for containers
ENV JAVA_OPTS="-Xmx512m \
    -Xms256m \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
