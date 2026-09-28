# Etapa 1: Compilación de la aplicación con Maven y JDK 17
FROM docker.io/maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
# Copiar el pom.xml primero para aprovechar la caché de capas de Podman en las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copiar el código fuente y compilar el archivo JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera de ejecución (Soporta nativamente ARM64 y AMD64)
FROM docker.io/eclipse-temurin:17-jre
WORKDIR /app
# Copiar el JAR resultante de la etapa de construcción
COPY --from=build /build/target/backend-1.0.0-SNAPSHOT.jar app.jar

# Configurar zona horaria UTC / Madrid por defecto e indicar puerto del contenedor
ENV TZ=Europe/Madrid
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
