FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY system.properties .

RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod,cloud", "-jar", "/app.jar"]