# ═══════════════════════════════════════════════════════════
# Dockerfile - World Cup Cards Backend
# Build: multi-stage con Gradle directo (sin wrapper JAR)
# ═══════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────
FROM gradle:8.12-jdk21 AS builder

WORKDIR /app

# Cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN gradle dependencies --no-daemon --quiet 2>/dev/null || true

# Build
COPY src src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Run ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8080
USER appuser

ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
