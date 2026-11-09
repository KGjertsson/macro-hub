# Build the jar first: from macro-backend/, sh ./mvnw clean package
FROM eclipse-temurin:25-jre

COPY target/auth-service-1.0.0.jar /auth-service.jar

EXPOSE 8086

CMD ["java", "-jar", "/auth-service.jar"]
