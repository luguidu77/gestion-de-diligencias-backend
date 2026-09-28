package es.guardiacivil.diligencias.remision.repository;

import es.guardiacivil.diligencias.remision.entity.EstadoRemision;
import es.guardiacivil.diligencias.remision.entity.RemisionDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RemisionDocumentoRepository extends JpaRepository<RemisionDocumento, Long> {

    List<RemisionDocumento> findByDocumentoId(Long documentoId);

    Optional<RemisionDocumento> findBySistemaDestinoAndReferenciaExterna(String sistemaDestino, String referenciaExterna);

    Optional<RemisionDocumento> findByClaveIdempotencia(String claveIdempotencia);

    @Query("SELECT r FROM RemisionDocumento r WHERE r.estado IN :estados AND (r.proximoReintento IS NULL OR r.proximoReintento <= :ahora)")
    List<RemisionDocumento> findRemisionesParaReintentar(@Param("estados") List<EstadoRemision> estados, @Param("ahora") Instant ahora);
}
