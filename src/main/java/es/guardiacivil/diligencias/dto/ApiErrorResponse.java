package es.guardiacivil.diligencias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * DTO estándar de respuesta de error devuelto por la API cuando ocurre una excepción.
 */
@Schema(description = "Estructura estándar de respuesta para errores de la API REST")
public record ApiErrorResponse(

        @Schema(description = "Fecha y hora en que ocurrió el error (ISO-8601 UTC)", example = "2026-07-24T09:50:00Z")
        Instant timestamp,

        @Schema(description = "Código de estado HTTP devuelto", example = "401")
        int status,

        @Schema(description = "Nombre o descripción corta del estado HTTP", example = "Unauthorized")
        String error,

        @Schema(description = "Mensaje detallado explicando el motivo del error o rechazo", example = "El token JWT no es válido o ha expirado")
        String message,

        @Schema(description = "Ruta o URI de la petición que generó el error", example = "/api/expedientes")
        String path
) {}
