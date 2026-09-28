package es.guardiacivil.diligencias.remision.service;

import es.guardiacivil.diligencias.usuario.service.UsuarioService;

import es.guardiacivil.diligencias.remision.dto.RemisionDocumentoResponse;
import es.guardiacivil.diligencias.remision.dto.SolicitudRemisionRequest;
import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;
import es.guardiacivil.diligencias.remision.entity.EstadoRemision;
import es.guardiacivil.diligencias.remision.entity.RemisionDocumento;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.documento.repository.DocumentoMetadataRepository;
import es.guardiacivil.diligencias.remision.repository.RemisionDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemisionDocumentoService {

    private final RemisionDocumentoRepository remisionRepository;
    private final DocumentoMetadataRepository documentoRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public RemisionDocumentoResponse solicitarRemision(Long documentoId, SolicitudRemisionRequest request) {
        DocumentoMetadata documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();

        RemisionDocumento remision = RemisionDocumento.builder()
                .documento(documento)
                .destino(request.destino())
                .sistemaDestino("SISTEMA_EXTERNO_GENERICO")
                .destinatario(request.destino())
                .estado(EstadoRemision.PENDIENTE)
                .tipoRemision(request.tipoRemision())
                .solicitadaPor(usuario.getTip())
                .fechaSolicitud(Instant.now())
                .numeroIntentos(0)
                .observaciones(request.observaciones())
                .claveIdempotencia(UUID.randomUUID().toString())
                .build();

        RemisionDocumento saved = remisionRepository.save(remision);
        return toDto(saved);
    }

    private RemisionDocumentoResponse toDto(RemisionDocumento remision) {
        return new RemisionDocumentoResponse(
                remision.getId(),
                remision.getDocumento().getId(),
                remision.getEstado().name(),
                remision.getTipoRemision().name(),
                remision.getDestino(),
                remision.getSolicitadaPor(),
                remision.getEjecutadaPor(),
                remision.getFechaSolicitud(),
                remision.getFechaEnvio(),
                remision.getReferenciaExterna(),
                remision.getObservaciones(),
                remision.getMensajeError()
        );
    }
}
