package es.guardiacivil.diligencias.organizacion.repository;

import es.guardiacivil.diligencias.organizacion.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

    // Para buscar un Ã¡rea especÃ­fica de una unidad concreta
    Optional<Area> findByUnidadIdAndCodigo(Long unidadId, String codigo);

    // Para sacar todas las Ã¡reas activas de una unidad (ej: para rellenar un desplegable)
    List<Area> findByUnidadIdAndActivaTrue(Long unidadId);
}