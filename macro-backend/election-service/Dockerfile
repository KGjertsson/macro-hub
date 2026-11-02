# Build the jar first: from macro-backend/, sh ./mvnw clean package
FROM eclipse-temurin:25-jre

COPY target/election-service-1.0.0.jar /election-service.jar

EXPOSE 8085

CMD ["java", "-jar", "/election-service.jar"]
