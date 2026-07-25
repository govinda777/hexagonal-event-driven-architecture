# ==========================================
# Stage 1: Build the application with Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the artifact
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the application on JRE 21
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the builder stage
COPY --from=builder /app/target/execution-flow-service-1.0.0-SNAPSHOT.jar app.jar

# Expose the server port
EXPOSE 8080

# Configure JVM environment variables for container optimization
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -Xms512m -Xmx1024m"

# Execute the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
