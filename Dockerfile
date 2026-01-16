# ============================================
# Multi-stage Dockerfile cho Ezami API
# ============================================

# === Stage 1: Build ===
FROM gradle:8.5-jdk21-alpine AS builder

LABEL maintainer="ezami-team"
LABEL description="Ezami API - Spring Boot Application"
LABEL version="1.2.0"

WORKDIR /app

# Install build dependencies
RUN apk add --no-cache curl bash && rm -rf /var/cache/apk/*

# Copy Gradle wrapper và config files trước (tận dụng cache)
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon --stacktrace || true

# Copy source code
COPY src/ src/

# Build application (skip tests để build nhanh hơn)
RUN ./gradlew clean build \
    -x test \
    -x testClasses \
    --no-daemon \
    --stacktrace \
    --info

# Verify JAR files
RUN ls -lah /app/build/libs/*.jar || (echo "ERROR: JAR file not found!" && exit 1)

# Giữ lại đúng bootJar (loại bỏ plain jar để tránh COPY wildcard dính 2 file)
RUN rm -f /app/build/libs/*-plain.jar

# === Stage 2: Runtime ===
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="ezami-team"
LABEL description="Ezami API - Runtime Image"

# Install runtime dependencies
RUN apk add --no-cache curl tzdata && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root user (idempotent + fallback nếu UID/GID bị trùng)
RUN set -eux; \
    if ! getent group appuser >/dev/null 2>&1; then \
      addgroup -S -g 1001 appuser || addgroup -S appuser; \
    fi; \
    if ! id -u appuser >/dev/null 2>&1; then \
      adduser -S -u 1001 -G appuser appuser || adduser -S -G appuser appuser; \
    fi

WORKDIR /app

# Copy boot jar từ builder stage (sau khi đã rm *-plain.jar thì wildcard sẽ chỉ còn 1 file)
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Create necessary directories + permission
RUN mkdir -p /app/uploads/images \
    /app/uploads/avatars \
    /app/uploads/temp \
    /app/logs && \
    chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

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

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
