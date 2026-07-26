# ==============================================================
#  STAGE 1: Build the JAR (uses full JDK + Maven)
# ==============================================================
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# 1. Copy Maven wrapper and pom.xml FIRST (for layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 2. Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 3. Copy the actual source code and build the JAR
COPY src src
RUN ./mvnw package -DskipTests -B

# ==============================================================
#  STAGE 2: Run the JAR (uses slim JRE)
# ==============================================================
FROM eclipse-temurin:25-jre-alpine

# 4. Security: Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# 5. Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# 6. Expose the port
EXPOSE 8080

# 7. Start the application
ENTRYPOINT ["java", "-Xmx350m", "-Xss512k", "-jar", "app.jar"]