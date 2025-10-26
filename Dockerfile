# Stage 1: Build using Gradle Wrapper
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Gradle wrapper and settings first for better caching
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./

# Pre-download dependencies (cache layer)
RUN ./gradlew --no-daemon dependencies  --quiet --console=plain || true

# Copy source and build
COPY . .
RUN ./gradlew shadowJar --no-daemon --build-cache --quiet --console=plain

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
