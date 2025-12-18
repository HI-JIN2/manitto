# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files first for better caching
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradle ./gradle
COPY gradlew .
COPY app/build.gradle.kts ./app/

# Copy source code
COPY app/src ./app/src

# Build the application
RUN chmod +x gradlew && ./gradlew :app:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar
COPY --from=build /app/app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
