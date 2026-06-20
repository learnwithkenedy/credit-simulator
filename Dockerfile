# =============================================================================
# Credit Simulator — Multi-stage Dockerfile
# Stage 1: Build with Maven
# Stage 2: Lean JRE runtime image
# =============================================================================

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy POM and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Runtime stage ----
FROM bellsoft/liberica-openjre-alpine:17

LABEL maintainer="credit-simulator"
LABEL description="Vehicle Loan Monthly Installment Calculator"
LABEL version="1.0.0"

WORKDIR /app

# Copy only the JAR
COPY --from=build /app/target/credit_simulator.jar .

# Copy sample input file
COPY file_inputs.txt .

# Set environment variable placeholder for API URL
ENV CREDIT_API_URL="https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c"

# Expose no ports (console app)
ENTRYPOINT ["java", "-jar", "credit_simulator.jar"]
