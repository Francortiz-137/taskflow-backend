# ================
# STAGE 1 — BUILD
# ================
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon || true

COPY . .
RUN ./gradlew clean bootJar --no-daemon


# ==================
# STAGE 2 — RUNTIME
# ==================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
