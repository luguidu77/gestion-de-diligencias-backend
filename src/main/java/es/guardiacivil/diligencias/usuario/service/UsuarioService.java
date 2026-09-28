package es.guardiacivil.diligencias.usuario.service;

import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import es.guardiacivil.diligencias.organizacion.entity.Unidad;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.organizacion.repository.UnidadRepository;
import es.guardiacivil.diligencias.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UnidadRepository unidadRepository;
    private final AuthenticatedUserService authenticatedUserService;

    /**
     * Obtiene el perfil de base de datos del usuario autenticado.
     * Si el usuario no existe aÃºn en la BD, lo crea automÃ¡ticamente (aprovisionamiento JIT).
     * Se ha eliminado REQUIRES_NEW para evitar Deadlocks por inaniciÃ³n del Connection Pool (HikariCP).
     * IMPORTANTE: Este mÃ©todo NUNCA debe ser llamado desde una transacciÃ³n de solo lectura (@Transactional(readOnly=true))
     * porque fallarÃ­a al intentar insertar el usuario. Para accesos de lectura, usar AuthenticatedUserService.
     */
    @Transactional
    public Usuario obtenerUsuarioAutenticado() {
        var identity = authenticatedUserService.getAuthenticatedUser();
        String tip = identity.username().toUpperCase();
        String nombre = identity.displayName();
        String empleo = identity.rank();
        return usuarioRepository.findByTip(tip)
                .orElseGet(() -> registrarUsuarioNuevo(tip, nombre, empleo, identity.unitCode()));
    }

    private Usuario registrarUsuarioNuevo(String tip, String nombre, String empleo, String codigoUnidadJwt) {
        if (codigoUnidadJwt == null || codigoUnidadJwt.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("El token JWT no contiene una unidad vÃ¡lida.");
        }
        String codigoUnidad = codigoUnidadJwt;

        Unidad unidadAsignada = unidadRepository.findByCodigo(codigoUnidad)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "La unidad del token no está registrada: " + codigoUnidad));

        Usuario nuevoUsuario = Usuario.builder()
                .keycloakSubject(java.util.UUID.randomUUID()) // placeholder till token provides it
                .tip(tip)
                .nombre(nombre != null ? nombre : "Agente " + tip)
                .empleo(empleo != null ? empleo : "Guardia Civil") // Guardamos el empleo (rango)
                .unidad(unidadAsignada)
                .activo(true)
                .build();

        return usuarioRepository.save(nuevoUsuario);
    }
}
