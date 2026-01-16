# === Build stage ===
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
ARG CACHEBUST=2

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew && sed -i 's/\r$//' gradlew

# Download dependencies (warm cache)
RUN ./gradlew dependencies --no-daemon || true

COPY src/ src/

# Build ONLY bootJar (Spring Boot) để tránh sinh ra nhiều jar (vd: *-plain.jar)
RUN ./gradlew clean bootJar -x test --no-daemon --stacktrace

RUN ls -la /app/build/libs/ || echo "Build folder not found"


# === Runtime stage ===
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -g 1001 appuser || true && useradd -r -u 1001 -g appuser appuser || true

WORKDIR /app

# Copy đúng 1 file jar (đã set tên app.jar trong build.gradle - xem note bên dưới)
COPY --from=builder /app/build/libs/app.jar /app/app.jar

# Create uploads directories and set permissions
RUN mkdir -p /app/uploads/images /app/uploads/avatars /app/uploads/temp \
  && chown -R appuser:appuser /app/uploads

USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
