package es.guardiacivil.diligencias.requerimiento.repository;

import es.guardiacivil.diligencias.requerimiento.entity.RequerimientoJudicial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequerimientoJudicialRepository extends JpaRepository<RequerimientoJudicial, Long> {

    Optional<RequerimientoJudicial> findBySistemaOrigenAndIdentificadorExterno(String sistemaOrigen, String identificadorExterno);

    Page<RequerimientoJudicial> findByDiligenciaIdOrderByFechaRecepcionDesc(Long diligenciaId, Pageable pageable);
}
