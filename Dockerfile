FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Bust Render Docker cache and verify we are building with Java 11.
ARG CACHE_BUST=20260531-1
RUN echo "cache-bust=${CACHE_BUST}" \
    && java -version \
    && mvn -version \
    && mvn -e -DskipTests clean package \
    && MAJOR=$(javap -verbose -classpath target/classes com.go2super.Go2SuperApplication | grep "major version" | head -1 | awk '{print $3}') \
    && echo "Detected class major version: ${MAJOR}" \
    && test "${MAJOR}" = "55"

FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

COPY --from=build /app/target/supergo2-server.jar ./app.jar

EXPOSE 10000

ENV SPRING_PROFILES_ACTIVE=prod
ENV GAME_TRANSPORT=websocket

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
