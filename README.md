# Gestión de Diligencias Backend

API Spring Boot 4.1.1 con Java 25 y Gradle 9.7.1. PostgreSQL 16 se ejecuta en Docker Compose para el desarrollo local. El repositorio contiene únicamente el backend.

## Compilar y probar en Windows

Instala JDK 25 y Docker Desktop. En IntelliJ selecciona JDK 25 para el proyecto y para Gradle. Desde PowerShell, en la raíz del repositorio:

```powershell
.\gradlew.bat clean test bootJar
```

Las pruebas de integración usan Testcontainers y requieren Docker Desktop arrancado. Gradle se descarga automáticamente con el wrapper incluido; no hace falta instalarlo en Windows.

## Arranque local con Docker

Utiliza el archivo de entorno local que ya contiene `DILIGENCIAS_DB_PASSWORD`. Este archivo debe quedar fuera de Git. Si todavía no existe, crea uno con una contraseña propia:

```powershell
'DILIGENCIAS_DB_PASSWORD=CAMBIA_ESTA_CLAVE' | Set-Content -Encoding ascii .env.local
docker build -f Containerfile -t diligencias-backend:local .
docker compose --env-file .env.local -f compose.local.yaml -f compose.backend.local.yaml up -d
docker compose --env-file .env.local -f compose.local.yaml -f compose.backend.local.yaml ps
```

La base se publica en `127.0.0.1:5434` (base `diligencias`, usuario `diligencias_user`) y la API en `127.0.0.1:8081`. Flyway aplica automáticamente las migraciones; V13 adapta la secuencia existente de Spring Batch 6 sin borrar datos. El endpoint `/api/diligencias` debe devolver `401` si se llama sin token JWT. Los valores OIDC y Alfresco de Compose son provisionales y no proporcionan autenticación ni gestor documental funcional.

Para detener los contenedores sin eliminar los datos:

```powershell
docker compose --env-file .env.local -f compose.local.yaml -f compose.backend.local.yaml down
```
