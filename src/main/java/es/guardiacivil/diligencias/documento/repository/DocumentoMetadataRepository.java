package es.guardiacivil.diligencias.documento.repository;

import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoMetadataRepository extends JpaRepository<DocumentoMetadata, Long> {

    Optional<DocumentoMetadata> findByAlfrescoNodeId(String alfrescoNodeId);

    Page<DocumentoMetadata> findByDiligenciaId(Long diligenciaId, Pageable pageable);

    List<DocumentoMetadata> findByActuacionId(Long actuacionId);

    boolean existsByHashSha256(String hashSha256);
}
