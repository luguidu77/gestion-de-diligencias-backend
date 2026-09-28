package es.guardiacivil.diligencias.documento.service;

import es.guardiacivil.diligencias.documento.dto.AlfrescoUploadResult;
import es.guardiacivil.diligencias.documento.dto.SubirDocumentoRequest;
import es.guardiacivil.diligencias.diligencia.entity.Actuacion;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;
import es.guardiacivil.diligencias.documento.entity.EstadoAlmacenamiento;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.documento.repository.DocumentoMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoPersistenceService {

    private final DocumentoMetadataRepository documentoRepository;

    @Transactional
    public DocumentoMetadata crearRegistroPendiente(Diligencia diligencia, Actuacion actuacion, Usuario subidoPor, MultipartFile archivo, SubirDocumentoRequest request) {
        DocumentoMetadata metadata = DocumentoMetadata.builder()
                .diligencia(diligencia)
                .actuacion(actuacion)
                .nombreOriginal(archivo.getOriginalFilename())
                .tituloDocumento(request.tituloDocumento())
                .mimeType(archivo.getContentType())
                .tamanoBytes(archivo.getSize())
                .destino(request.destino())
                .estadoDocumento(request.estadoDocumento())
                .estadoAlmacenamiento(EstadoAlmacenamiento.PENDIENTE)
                .subidoPor(subidoPor)
                .build();
        return documentoRepository.save(metadata);
    }

    @Transactional
    public DocumentoMetadata confirmarAlmacenamiento(Long documentoId, AlfrescoUploadResult resultado) {
        DocumentoMetadata metadata = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        metadata.setAlfrescoNodeId(resultado.alfrescoNodeId());
        metadata.setHashSha256(resultado.hashSha256());
        metadata.setEstadoAlmacenamiento(EstadoAlmacenamiento.ALMACENADO);
        return documentoRepository.save(metadata);
    }

    @Transactional
    public void registrarError(Long documentoId, Exception exception) {
        documentoRepository.findById(documentoId).ifPresent(metadata -> {
            metadata.setEstadoAlmacenamiento(EstadoAlmacenamiento.ERROR);
            log.error("Error al almacenar en Alfresco el documento ID {}: {}", documentoId, exception.getMessage());
            documentoRepository.save(metadata);
        });
    }
}
