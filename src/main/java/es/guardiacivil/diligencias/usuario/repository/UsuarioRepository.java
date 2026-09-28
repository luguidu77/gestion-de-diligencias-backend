package es.guardiacivil.diligencias.usuario.repository;

import es.guardiacivil.diligencias.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByKeycloakSubject(UUID keycloakSubject);
    Optional<Usuario> findByTip(String tip);
}
