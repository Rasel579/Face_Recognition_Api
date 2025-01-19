FROM openjdk:17-oracle
EXPOSE 8080
ADD /target/faces_recognition-0.0.1-SNAPSHOT.jar faces_recognition-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","faces_recognition-0.0.1-SNAPSHOT.jar"]