package es.guardiacivil.diligencias.requerimiento.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para que el instructor responda a un requerimiento judicial.
 */
public record ResponderRequerimientoRequest(
        @NotBlank String resultado,
        String observaciones
) {}
