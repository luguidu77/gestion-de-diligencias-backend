FROM docker.io/eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew dependencies --configuration runtimeClasspath --no-daemon
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

# Etapa 2: Imagen final ligera de ejecución (Soporta nativamente ARM64 y AMD64)
FROM docker.io/eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/build/libs/diligencias-backend-1.0.0-SNAPSHOT.jar app.jar

ENV TZ=Europe/Madrid
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
