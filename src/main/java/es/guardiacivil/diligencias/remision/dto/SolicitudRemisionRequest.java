package es.guardiacivil.diligencias.remision.dto;

import es.guardiacivil.diligencias.remision.entity.TipoRemision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudRemisionRequest(
    @NotBlank String destino,
    @NotNull TipoRemision tipoRemision,
    String observaciones
) {}
