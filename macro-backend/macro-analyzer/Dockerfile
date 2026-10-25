# Use an official Java 25 runtime as a parent image
FROM eclipse-temurin:25-jre

# Copy the application JAR file into the container
COPY target/macro-analyzer-1.0.0.jar /macro-analyzer.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the necessary port
EXPOSE 8080

# Specify the command to run the JAR file
CMD ["java", "-jar", "macro-analyzer.jar"]
