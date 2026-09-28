package es.guardiacivil.diligencias.diligencia.repository;

import es.guardiacivil.diligencias.diligencia.entity.Actuacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActuacionRepository extends JpaRepository<Actuacion, Long> {
    List<Actuacion> findByDiligenciaIdOrderByNumeroOrdenAsc(Long diligenciaId);
    
    // Para obtener el maximo numero_orden actual en una diligencia
    Actuacion findTopByDiligenciaIdOrderByNumeroOrdenDesc(Long diligenciaId);
}
