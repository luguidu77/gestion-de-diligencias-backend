package es.guardiacivil.diligencias.tarea.repository;

import es.guardiacivil.diligencias.tarea.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByUsuarioId(Long idUsuario);
    List<Tarea> findByUsuarioTip(String tip);
}
