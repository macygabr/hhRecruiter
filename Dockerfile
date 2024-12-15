FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/hhRecruiter-0.0.1.jar /app/hhRecruiter-0.0.1.jar
ENTRYPOINT ["java", "-jar", "hhRecruiter-0.0.1.jar"]
EXPOSE 8080