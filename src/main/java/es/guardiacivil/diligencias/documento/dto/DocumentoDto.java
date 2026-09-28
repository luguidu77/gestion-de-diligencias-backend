package es.guardiacivil.diligencias.documento.dto;

import es.guardiacivil.diligencias.documento.entity.DestinoDocumento;
import es.guardiacivil.diligencias.documento.entity.EstadoAlmacenamiento;
import es.guardiacivil.diligencias.documento.entity.EstadoDocumento;

import java.time.Instant;

public record DocumentoDto(
        Long id,
        String nombreOriginal,
        String tituloDocumento,
        String mimeType,
        Long tamanoBytes,
        String hashSha256,
        DestinoDocumento destino,
        EstadoDocumento estadoDocumento,
        EstadoAlmacenamiento estadoAlmacenamiento,
        Instant fechaCreacion,
        String subidoPor
) {}
