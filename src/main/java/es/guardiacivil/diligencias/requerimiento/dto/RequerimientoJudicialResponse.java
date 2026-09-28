package es.guardiacivil.diligencias.requerimiento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO de respuesta para representar un requerimiento judicial.
 */
@Schema(description = "Detalle y estado de un requerimiento judicial tramitado")
public record RequerimientoJudicialResponse(

        @Schema(description = "Identificador único interno del requerimiento", example = "105")
        Long id,

        @Schema(description = "Referencia del órgano judicial", example = "NIG-27028-43-2026")
        String referenciaJudicial,

        @Schema(description = "Referencia del expediente policial asociado", example = "2026/PJ-LUG/0014")
        String referenciaPolicial,

        @Schema(description = "Tipo de requerimiento", example = "INFORME")
        String tipo,

        @Schema(description = "Descripción de la solicitud judicial", example = "Solicitud de informe dactilar")
        String descripcion,

        @Schema(description = "Prioridad asignada", example = "NORMAL")
        String prioridad,

        @Schema(description = "Estado actual en la máquina de estados", example = "ASIGNADO")
        String estado,

        @Schema(description = "Fecha límite para la contestación", example = "2026-08-15")
        LocalDate fechaLimite,

        @Schema(description = "Fecha y hora de recepción en el sistema", example = "2026-07-24T08:00:00Z")
        Instant fechaRecepcion,

        @Schema(description = "Fecha y hora de asignación al instructor policial", example = "2026-07-24T08:05:00Z")
        Instant fechaAsignacion,

        @Schema(description = "TIP del agente instructor asignado (si aplica)", example = "I12345")
        String instructorTip,

        @Schema(description = "Código de la unidad policial responsable", example = "PJ-LUG-01")
        String unidadResponsable,

        @Schema(description = "Resumen o resultado emitido por el instructor al responder", example = "Diligencia cumplimentada y adjunta.")
        String resultado,

        @Schema(description = "Observaciones internas o notas de tramitación", example = "Pendiente de firma por el capitán de la unidad")
        String observaciones
) {}
