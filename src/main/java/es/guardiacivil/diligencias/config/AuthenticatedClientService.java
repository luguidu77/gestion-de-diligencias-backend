package es.guardiacivil.diligencias.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio centralizado para extraer información del cliente técnico autenticado.
 *
 * <p>No contiene lógica específica de Keycloak. Extrae el {@code client_id}
 * del claim {@code azp} (authorized party), que en OAuth2 client_credentials
 * identifica al cliente que solicitó el token.</p>
 *
 * <p>Los roles se extraen directamente de las authorities del contexto de seguridad,
 * quitando el prefijo {@code ROLE_} que añade {@link JwtAuthConverter}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedClientService {

    /**
     * Construye un {@link AuthenticatedClient} a partir del JWT del contexto de seguridad actual.
     *
     * @return El cliente técnico autenticado.
     * @throws IllegalStateException si no hay JWT en el contexto.
     */
    public AuthenticatedClient getAuthenticatedClient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No hay JWT autenticado en el contexto de seguridad");
        }

        Jwt jwt = jwtAuth.getToken();

        // El claim 'azp' (authorized party) identifica al cliente OAuth2 en client_credentials
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null) {
            // Fallback al claim 'client_id' (algunos IdPs lo usan en su lugar)
            clientId = jwt.getClaimAsString("client_id");
        }
        if (clientId == null) {
            // Ultimo fallback: usar el subject
            clientId = jwt.getSubject();
            log.warn("No se encontró claim 'azp' ni 'client_id' en el JWT. Usando sub='{}' como clientId", clientId);
        }

        // Los roles ya los extrae JwtAuthConverter y los pone en las authorities
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)) // Eliminar prefijo ROLE_
                .collect(Collectors.toUnmodifiableSet());

        return new AuthenticatedClient(clientId, roles);
    }
}
