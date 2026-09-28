package es.guardiacivil.diligencias.organizacion.repository;

import es.guardiacivil.diligencias.organizacion.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    Optional<Unidad> findByCodunidad(String codunidad);
    Optional<Unidad> findByCodigo(String codigo);
}
