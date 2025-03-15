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
FROM openjdk-18-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из этапа сборки
COPY --from=build /app/target/my-app.jar /app/my-app.jar

# Открываем порт, который использует приложение
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "my-app.jar"]