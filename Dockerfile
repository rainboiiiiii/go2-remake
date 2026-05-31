FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -e -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/supergo2-server.jar ./app.jar

EXPOSE 10000

ENV SPRING_PROFILES_ACTIVE=prod
ENV GAME_TRANSPORT=websocket

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
