# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

COPY --from=build /app/target/stock-tax-calculator-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]