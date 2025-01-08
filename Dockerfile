# Stage 1: Build Stage
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory in the container
WORKDIR /home/ec2-user

# Copy the Maven wrapper .jar file into the container
COPY mvnw ./
COPY mvnw.cmd ./
COPY .mvn ./

# Copy the project files and build the application
COPY pom.xml ./
COPY src/main ./src
RUN mvn clean install

# Stage 2: Runtime Stage
FROM openjdk:17

# Set the working directory in the container
WORKDIR /home/ec2-user

# Copy the compiled JAR file from the build stage
COPY --from=build home/ec2-user/.mvn/wrapper/maven-wrapper.jar ./

# Expose the port that the backend will run on
EXPOSE 3000

# Health check endpoint (add appropriate logic in your application)
HEALTHCHECK --interval=30s --timeout=10s CMD curl --fail http://localhost:3000/health || exit 1

# Command to run your Java application
CMD ["java", "-jar", "maven-wrapper.jar"]