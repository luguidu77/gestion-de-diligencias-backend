package es.guardiacivil.diligencias.remision.dto;

import java.time.Instant;

public record RemisionDocumentoResponse(
    Long id,
    Long documentoId,
    String estado,
    String tipoRemision,
    String destino,
    String solicitadaPor,
    String ejecutadaPor,
    Instant fechaSolicitud,
    Instant fechaEnvio,
    String referenciaExterna,
    String observaciones,
    String mensajeError
) {}
