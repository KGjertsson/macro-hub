# Build the jar first: from macro-backend/, sh ./mvnw clean package
FROM eclipse-temurin:25-jre

COPY target/regional-statistics-service-1.0.0.jar /regional-statistics-service.jar

EXPOSE 8082

# The first full scrape keeps every fetched value in memory until it is stored. There is no -Xmx or container memory
# limit today; if either is added, leave room for that
CMD ["java", "-jar", "/regional-statistics-service.jar"]
