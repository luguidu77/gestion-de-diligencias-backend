package es.guardiacivil.diligencias.documento.alfresco;

import es.guardiacivil.diligencias.documento.dto.AlfrescoUploadResult;
import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class AlfrescoService {

    public AlfrescoUploadResult subir(MultipartFile archivo, DocumentoMetadata pendiente) {
        log.info("Subiendo documento a Alfresco: {}", archivo.getOriginalFilename());
        // Simulación de subida real
        String nodeId = UUID.randomUUID().toString();
        return new AlfrescoUploadResult(nodeId, pendiente.getHashSha256());
    }
}
