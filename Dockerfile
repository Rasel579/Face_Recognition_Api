# Используем базовый образ с Maven и JDK
FROM maven:3-openjdk-18-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем исходный код и файл pom.xml
COPY src /app/src
COPY pom.xml /app

# Собираем проект с помощью Maven
RUN mvn clean package -DskipTests

# Используем базовый образ с JRE для запуска приложения
FROM openjdk:11-ea-17-jre-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя и группу для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Указываем путь к JAR-файлу
ARG JAR_FILE=target/*.jar

# Копируем JAR-файл в контейнер
COPY ${JAR_FILE} app.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "/app.jar"]