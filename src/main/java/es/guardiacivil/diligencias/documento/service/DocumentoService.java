package es.guardiacivil.diligencias.documento.service;

import es.guardiacivil.diligencias.usuario.service.UsuarioService;
import es.guardiacivil.diligencias.documento.alfresco.AlfrescoService;
import es.guardiacivil.diligencias.documento.dto.AlfrescoUploadResult;
import es.guardiacivil.diligencias.documento.dto.DocumentoDto;
import es.guardiacivil.diligencias.documento.dto.SubirDocumentoRequest;
import es.guardiacivil.diligencias.diligencia.entity.Actuacion;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.diligencia.repository.ActuacionRepository;
import es.guardiacivil.diligencias.diligencia.repository.DiligenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoPersistenceService documentoPersistenceService;
    private final AlfrescoService alfrescoService;
    private final DiligenciaRepository diligenciaRepository;
    private final ActuacionRepository actuacionRepository;
    private final UsuarioService usuarioService;

    public DocumentoDto subirDocumento(Long diligenciaId, MultipartFile archivo, SubirDocumentoRequest request) {
        Diligencia diligencia = diligenciaRepository.findById(diligenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Diligencia no encontrada"));
        
        Actuacion actuacion = null;
        if (request.actuacionId() != null) {
            actuacion = actuacionRepository.findById(request.actuacionId())
                    .orElseThrow(() -> new IllegalArgumentException("Actuacion no encontrada"));
        }

        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();

        DocumentoMetadata pendiente = documentoPersistenceService.crearRegistroPendiente(diligencia, actuacion, usuario, archivo, request);

        try {
            AlfrescoUploadResult resultado = alfrescoService.subir(archivo, pendiente);
            DocumentoMetadata almacenado = documentoPersistenceService.confirmarAlmacenamiento(pendiente.getId(), resultado);
            return toDto(almacenado);
        } catch (RuntimeException exception) {
            documentoPersistenceService.registrarError(pendiente.getId(), exception);
            throw exception;
        }
    }

    private DocumentoDto toDto(DocumentoMetadata metadata) {
        return new DocumentoDto(
                metadata.getId(),
                metadata.getNombreOriginal(),
                metadata.getTituloDocumento(),
                metadata.getMimeType(),
                metadata.getTamanoBytes(),
                metadata.getHashSha256(),
                metadata.getDestino(),
                metadata.getEstadoDocumento(),
                metadata.getEstadoAlmacenamiento(),
                metadata.getFechaCreacion() != null ? metadata.getFechaCreacion().toInstant(java.time.ZoneOffset.UTC) : null,
                metadata.getSubidoPor() != null ? metadata.getSubidoPor().getTip() : null
        );
    }
}
