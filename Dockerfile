# 1. Используем образ с JDK для сборки
FROM openjdk:17-jdk-slim AS build

# 2. Устанавливаем рабочую директорию
WORKDIR /app

# 3. Копируем gradle wrapper и настройки
COPY gradlew /app/gradlew
COPY gradle /app/gradle

# 4. Копируем исходный код проекта
COPY src /app/src/
COPY settings.gradle.kts /app/settings.gradle.kts

# 5. Даем права на выполнение скрипту gradlew
RUN chmod +x gradlew
RUN chmod +x /app/gradlew

# 6. Собираем проект с помощью Gradle Wrapper
RUN ./gradlew build -x test  # Пропускаем тесты для ускорения сборки

# 7. Запускаем финальный контейнер
FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем скомпилированный .jar файл из первого этапа
COPY --from=build /app/build/libs/hhRecruiter-0.0.1.jar /app/hhRecruiter-0.0.1.jar

# Открываем порт для приложения
EXPOSE 8080

# Точка входа для запуска приложения
ENTRYPOINT ["java", "-jar", "hhRecruiter-0.0.1.jar"]
