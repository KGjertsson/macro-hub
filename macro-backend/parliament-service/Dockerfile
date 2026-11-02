# Build the jar first: from macro-backend/, sh ./mvnw clean package
FROM eclipse-temurin:25-jre

COPY target/parliament-service-1.0.0.jar /parliament-service.jar

EXPOSE 8084

CMD ["java", "-jar", "/parliament-service.jar"]
