# Use an official OpenJDK runtime as a parent image
FROM openjdk:22-jdk

# Copy the application JAR file into the container
COPY target/macro-analyzer-0.0.1-SNAPSHOT.jar /macro-analyzer.jar

# Specify the command to run the JAR file
CMD ["java", "-jar", "macro-analyzer.jar"]
