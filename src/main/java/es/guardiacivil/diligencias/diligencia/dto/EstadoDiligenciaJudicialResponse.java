package es.guardiacivil.diligencias.diligencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * DTO de respuesta para la consulta de estado de diligencias por parte de la aplicación judicial.
 *
 * <p>Solo contiene información estrictamente necesaria.
 * No incluye datos personales, auditoría interna, nodeIds de Alfresco
 * ni detalles operativos del expediente policial.</p>
 */
@Schema(description = "Información mínima y pública sobre el estado de unas diligencias policiales para la administración de justicia")
public record EstadoDiligenciaJudicialResponse(

        @Schema(description = "Referencia del órgano judicial vinculado", example = "NIG-27028-43-2026")
        String referenciaJudicial,

        @Schema(description = "Referencia oficial de las diligencias policiales", example = "2026/PJ-LUG/0014")
        String referenciaPolicial,

        @Schema(description = "Estado funcional de la instrucción policial", example = "ACTIVO", allowableValues = {"ACTIVO", "COMPLETADO", "PENDIENTE"})
        String estado,

        @Schema(description = "Fecha y hora UTC del último movimiento registrados en las diligencias", example = "2026-07-24T08:30:00Z")
        Instant fechaUltimaActualizacion,

        @Schema(description = "Fecha de remisión formal al juzgado (si ya ha sido enviada)", example = "2026-07-24T09:00:00Z")
        Instant fechaRemision,

        @Schema(description = "Número o código de registro de entrada/remisión en el órgano judicial", example = "REG-JUZ-2026-0041")
        String referenciaRemision
) {}
