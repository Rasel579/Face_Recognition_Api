FROM openjdk:17-jdk-slim AS build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:resolve

EXPOSE 8080
ENTRYPOINT ["java","-jar","faces_recognition-0.0.1-SNAPSHOT.jar"]