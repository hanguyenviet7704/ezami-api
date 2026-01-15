# === Build stage ===
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
# Force rebuild - Fixed FK constraint violation in diagnostic answer (Jan 7, 2026)
ARG CACHEBUST=4

# Copy Gradle wrapper and config files
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Build application
RUN ./gradlew clean build -x test --no-daemon --stacktrace
RUN ls -la /app/build/libs/ || echo "Build folder not found"


# === Runtime stage ===
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1001 appuser || true && useradd -r -u 1001 -g appuser appuser || true
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Create uploads directory for user-uploaded files
RUN mkdir -p /app/uploads && chown -R appuser:appuser /app

USER appuser
EXPOSE 8080

# Create uploads subdirectories
RUN mkdir -p /app/uploads/images /app/uploads/avatars /app/uploads/temp

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
