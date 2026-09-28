package es.guardiacivil.diligencias.requerimiento.dto;

import es.guardiacivil.diligencias.requerimiento.entity.PrioridadRequerimiento;
import es.guardiacivil.diligencias.requerimiento.entity.TipoRequerimientoJudicial;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request para la creación de un requerimiento judicial enviado por el órgano judicial.
 */
@Schema(description = "Datos para la creación de un requerimiento judicial desde la aplicación judicial externa")
public record CrearRequerimientoJudicialRequest(

        @Schema(
                description = "Referencia del órgano judicial (NIG, procedimiento o número de autos)",
                example = "NIG-27028-43-2026",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String referenciaJudicial,

        @Schema(
                description = "Referencia del expediente o diligencia policial a la que se asocia",
                example = "2026/PJ-LUG/0014",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String referenciaPolicial,

        @Schema(
                description = "Tipo de actuación o requerimiento solicitado",
                example = "INFORME",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull TipoRequerimientoJudicial tipo,

        @Schema(
                description = "Descripción detallada del requerimiento judicial emitido",
                example = "Se solicita ampliación de atestado sobre la toma de muestras dactilares.",
                maxLength = 4000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank @Size(max = 4000) String descripcion,

        @Schema(
                description = "Fecha límite concedida por el juzgado para cumplimentar la solicitud (opcional)",
                example = "2026-08-15"
        )
        LocalDate fechaLimite,

        @Schema(
                description = "Nivel de prioridad asignado por el juzgado",
                example = "ALTA",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull PrioridadRequerimiento prioridad,

        @Schema(
                description = "Identificador único generado por el sistema judicial origen (Mecanismo de Idempotencia)",
                example = "REQ-EXT-2026-998811",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String identificadorExterno
) {}
