# Build the jar first: from macro-backend/, sh ./mvnw clean package
FROM eclipse-temurin:25-jre

COPY target/macro-series-service-1.0.0.jar /macro-series-service.jar

EXPOSE 8083

CMD ["java", "-jar", "/macro-series-service.jar"]
