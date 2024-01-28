# Use the official OpenJDK 17 image as a parent image
FROM openjdk:17-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/git-info-fetcher-0.0.1-SNAPSHOT.jar /app/git-info-fetcher.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java","-jar","git-info-fetcher.jar"]
