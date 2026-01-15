# === Build stage ===
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
ARG CACHEBUST=2

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

COPY src/ src/

RUN ./gradlew clean build -x test --no-daemon --stacktrace
RUN ls -la /app/build/libs/ || echo "Build folder not found"


# === Runtime stage ===
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1001 appuser || true && useradd -r -u 1001 -g appuser appuser || true
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R appuser:appuse
