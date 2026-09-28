package es.guardiacivil.diligencias.diligencia.repository;

import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiligenciaRepository extends JpaRepository<Diligencia, Long> {
    
    @EntityGraph(attributePaths = {"unidad", "agenteInstructor", "agenteSecretario"})
    Optional<Diligencia> findByNumeroExpediente(String numeroExpediente);
    Page<Diligencia> findByUnidad_Codigo(String codigo, Pageable pageable);
}
