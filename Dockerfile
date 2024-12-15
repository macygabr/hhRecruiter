# 1. Используем образ с JDK для сборки
FROM openjdk:17-jdk-slim AS build

# 2. Устанавливаем рабочую директорию
WORKDIR /app

# 3. Копируем build.gradle и другие необходимые файлы (например, settings.gradle, если есть)
COPY build.gradle /app/
COPY settings.gradle /app/  # если есть
COPY gradle /app/gradle  # если используется Gradle wrapper

# 4. Копируем исходный код в контейнер
COPY src /app/src/

# 5. Устанавливаем Gradle и собираем проект
RUN apt-get update && apt-get install -y wget && \
    wget https://services.gradle.org/distributions/gradle-7.5-bin.zip -P /tmp && \
    unzip /tmp/gradle-7.5-bin.zip -d /opt && \
    ln -s /opt/gradle-7.5/bin/gradle /usr/bin/gradle && \
    gradle build -x test  # Собираем проект, пропуская тесты

# 6. Запускаем финальный контейнер
FROM openjdk:17-jdk-slim

# 7. Устанавливаем рабочую директорию для финального контейнера
WORKDIR /app

# 8. Копируем JAR файл из первого слоя
COPY --from=build /app/build/libs/hhRecruiter-0.0.1.jar /app/hhRecruiter-0.0.1.jar

# 9. Открываем порт для приложения
EXPOSE 8080

# 10. Устанавливаем точку входа для запуска приложения
ENTRYPOINT ["java", "-jar", "hhRecruiter-0.0.1.jar"]
