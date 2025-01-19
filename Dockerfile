FROM maven:3.8.6-openjdk:17-oracle AS build
ADD . /src
WORKDIR /src
RUN mvn package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java","-jar","faces_recognition-0.0.1-SNAPSHOT.jar"]