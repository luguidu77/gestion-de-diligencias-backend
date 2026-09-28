package es.guardiacivil.diligencias.documento.dto;

import es.guardiacivil.diligencias.documento.entity.DestinoDocumento;
import es.guardiacivil.diligencias.documento.entity.EstadoDocumento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubirDocumentoRequest(
    @Size(max = 200) String tituloDocumento,
    @NotNull DestinoDocumento destino,
    @NotNull EstadoDocumento estadoDocumento,
    Long actuacionId
) {}
