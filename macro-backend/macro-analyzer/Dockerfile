# Use the official maven image as a parent image
FROM maven:3.9.5-amazoncorretto-21-al2023 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file and download the dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy the project source and build it
COPY src ./src/
RUN mvn package

# Use the official openjdk image as a parent image
FROM amazoncorretto:21.0.1-al2023

# Set the working directory in the container to /app
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/macro-analyzer.jar /app

# Command to run the application
CMD ["java", "-jar", "macro-analyzer.jar"]
