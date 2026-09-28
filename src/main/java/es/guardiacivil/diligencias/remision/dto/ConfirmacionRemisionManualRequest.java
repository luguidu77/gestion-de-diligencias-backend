package es.guardiacivil.diligencias.remision.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record ConfirmacionRemisionManualRequest(
    @NotBlank String referenciaExterna,
    Instant fechaEnvio,
    String observaciones
) {}
