package es.guardiacivil.diligencias.documento.dto;

import java.time.Instant;

public record DocumentoResponse(
        Long id,
        String nombre,
        String mimeType,
        Long tamanoBytes,
        Instant fechaSubida,
        String subidoPor,
        String destino,
        String estado
) {}
