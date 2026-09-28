package es.guardiacivil.diligencias.batch.repository;

import es.guardiacivil.diligencias.batch.entity.SincronizacionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SincronizacionBatchRepository extends JpaRepository<SincronizacionBatch, Long> {
    Optional<SincronizacionBatch> findByNombreProceso(String nombreProceso);
}
