package es.guardiacivil.diligencias.organizacion.repository;

import es.guardiacivil.diligencias.organizacion.entity.UsuarioArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioAreaRepository extends JpaRepository<UsuarioArea, Long> {

    // Devuelve todas las Ã¡reas activas en las que estÃ¡ asignado un usuario concreto
    List<UsuarioArea> findByUsuarioIdAndActivaTrue(Long usuarioId);

    // Devuelve cuÃ¡l es el Ã¡rea principal de un usuario
    Optional<UsuarioArea> findByUsuarioIdAndAreaPrincipalTrueAndActivaTrue(Long usuarioId);
}