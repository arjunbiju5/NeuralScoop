# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/news-aggregator-1.0-SNAPSHOT.jar app.jar

# Expose the port (Render sets PORT env variable)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
