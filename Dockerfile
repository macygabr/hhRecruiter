FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY . .
RUN ./gradlew bootJar
CMD ["java", "-jar", "build/libs/hhRecruiter-0.0.1.jar"]