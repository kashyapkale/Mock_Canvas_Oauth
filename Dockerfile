# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/fake_oauth_canvas-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render will set PORT env var)
EXPOSE 8457

# Run the application
# Spring Boot will automatically use PORT env var (set by Render)
# If PORT is not set, it defaults to 8457 from application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]

