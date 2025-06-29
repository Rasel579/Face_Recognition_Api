# Используем базовый образ с Maven и JDK
FROM maven:3-openjdk-17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем исходный код и файл pom.xml
COPY src /app/src
COPY pom.xml /app

# Собираем проект с помощью Maven
RUN mvn clean package -DskipTests

# Используем базовый образ с JRE для запуска приложения
FROM amazoncorretto:17.0.14-al2023

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из этапа сборки
COPY --from=build /app/target/faces_recognition-0.0.1-SNAPSHOT.jar /app/faces_recognition-0.0.1-SNAPSHOT.jar

COPY --from=build /app/src/main/resources/ /app/src/main/resources/


# Открываем порт, который использует приложение
EXPOSE 8081

# Команда для запуска приложения
ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-jar", "faces_recognition-0.0.1-SNAPSHOT.jar"]