package es.guardiacivil.diligencias.usuario.service;

import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import es.guardiacivil.diligencias.core.config.SecurityClaimsProperties;

/**
 * Servicio de seguridad que construye la identidad interna normalizada ({@link AuthenticatedUser})
 * del usuario autenticado en la peticiÃ³n HTTP actual.
 *
 * <p><strong>Responsabilidad Ãºnica:</strong> leer el JWT del {@code SecurityContextHolder},
 * navegar los claims configurados en {@link SecurityClaimsProperties} y devolver
 * un {@link AuthenticatedUser} independiente del proveedor de identidad.</p>
 *
 * <p>Los controladores, servicios y el aspecto de auditorÃ­a deben llamar a
 * {@link #getAuthenticatedUser()} en lugar de leer claims directamente del JWT.</p>
 *
 * <p>El cambio de proveedor (Keycloak â†’ SSO corporativo) solo requiere ajustar
 * las propiedades {@code app.security.claims.*} en el perfil correspondiente.</p>
 */
@Component
@Slf4j
public class AuthenticatedUserService {

    private final SecurityClaimsProperties claims;

    public AuthenticatedUserService(SecurityClaimsProperties claims) {
        this.claims = claims;
    }

    /**
     * Construye y devuelve la identidad normalizada del usuario autenticado.
     *
     * @return {@link AuthenticatedUser} con los datos del usuario extraÃ­dos del JWT.
     * @throws IllegalStateException si no existe un JWT vÃ¡lido en el contexto de seguridad.
     */
    public AuthenticatedUser getAuthenticatedUser() {
        Jwt jwt = resolveJwt();

        String username    = jwt.getClaimAsString(claims.username());
        String displayName = claims.displayName() != null ? jwt.getClaimAsString(claims.displayName()) : null;
        String rank        = claims.rank()        != null ? jwt.getClaimAsString(claims.rank())        : null;
        String unitCode    = claims.unit()        != null ? jwt.getClaimAsString(claims.unit())        : null;
        Set<String> roles  = extractRoles(jwt);

        return new AuthenticatedUser(username, displayName, rank, unitCode, roles);
    }

    /**
     * Indica si el usuario autenticado tiene el rol SUPERADMIN.
     * Atajo de conveniencia que evita llamar a {@code getAuthenticatedUser().isSuperadmin()}.
     */
    public boolean isSuperadmin() {
        return getAuthenticatedUser().isSuperadmin();
    }

    /**
     * Devuelve el cÃ³digo de unidad del usuario autenticado.
     * Atajo de conveniencia para componentes que solo necesitan la unidad.
     */
    public String getUnitCode() {
        return getAuthenticatedUser().unitCode();
    }

    /**
     * Devuelve el username del usuario autenticado.
     * Atajo de conveniencia para componentes que solo necesitan el username.
     */
    public String getUsername() {
        return getAuthenticatedUser().username();
    }

    // -------------------------------------------------------------------------
    // MÃ©todos privados
    // -------------------------------------------------------------------------

    /**
     * Obtiene el objeto JWT del contexto de seguridad actual.
     *
     * @throws IllegalStateException si no existe un principal JWT vÃ¡lido.
     */
    private Jwt resolveJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException(
                    "No existe un JWT vÃ¡lido en el contexto de seguridad. "
                  + "La peticiÃ³n debe estar autenticada con un token Bearer.");
        }
        return jwt;
    }

    /**
     * Navega la ruta configurada en {@code claims.rolesPath()} sobre el mapa de claims del JWT
     * y devuelve el conjunto de roles como {@code Set<String>}.
     *
     * <p>Soporta rutas anidadas separadas por punto, por ejemplo:</p>
     * <ul>
     *   <li>{@code roles} â€” claim directo en el token</li>
     *   <li>{@code realm_access.roles} â€” roles de realm de Keycloak</li>
     *   <li>{@code resource_access.diligencias-backend.roles} â€” roles de cliente de Keycloak</li>
     * </ul>
     *
     * <p>Si la ruta no existe o el valor no es una colecciÃ³n, devuelve {@code Set.of()}
     * sin lanzar excepciÃ³n.</p>
     */
    private Set<String> extractRoles(Jwt jwt) {
        String[] pathParts = claims.rolesPath().split("\\.");
        Object current = jwt.getClaims();

        for (String part : pathParts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                log.debug("Ruta de roles '{}' no encontrada en el JWT en el segmento '{}'",
                        claims.rolesPath(), part);
                return Set.of();
            }
        }

        if (current instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .collect(Collectors.toUnmodifiableSet());
        }

        log.debug("El claim de roles '{}' no es una colecciÃ³n (tipo: {})",
                claims.rolesPath(), current == null ? "null" : current.getClass().getSimpleName());
        return Set.of();
    }
}
