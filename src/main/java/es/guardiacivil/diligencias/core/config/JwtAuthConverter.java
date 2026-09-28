package es.guardiacivil.diligencias.core.config;

import es.guardiacivil.diligencias.core.config.SecurityClaimsProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Conversor de JWT a {@link AbstractAuthenticationToken} con extracciÃƒÂ³n de roles configurable.
 *
 * <p>La ruta de claims de la que se extraen los roles se configura mediante la propiedad
 * {@code app.security.claims.roles-path}, soportando:</p>
 * <ul>
 *   <li>{@code roles} Ã¢â‚¬â€ claim simple directo en el token</li>
 *   <li>{@code realm_access.roles} Ã¢â‚¬â€ roles de realm de Keycloak</li>
 *   <li>{@code resource_access.diligencias-backend.roles} Ã¢â‚¬â€ roles de cliente de Keycloak</li>
 *   <li>Cualquier ruta anidada separada por punto</li>
 * </ul>
 *
 * <p>Los roles se convierten a {@link GrantedAuthority} con el prefijo {@code ROLE_},
 * que es el formato estÃƒÂ¡ndar de Spring Security para las expresiones SpEL
 * {@code hasRole()} y {@code hasAnyRole()}.</p>
 *
 * <p>Este conversor no contiene lÃƒÂ³gica especÃƒÂ­fica de ningÃƒÂºn proveedor de identidad.
 * Cambiar la fuente de roles se resuelve ÃƒÂºnicamente actualizando {@code CLAIM_ROLES_PATH}
 * en el perfil de configuraciÃƒÂ³n correspondiente.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SecurityClaimsProperties claimsProperties;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoleAuthorities(jwt);

        // El principal del token es el username configurado (preferred_username, samAccountName, etc.)
        String principalClaim = jwt.getClaimAsString(claimsProperties.username());
        String principal = principalClaim != null ? principalClaim : jwt.getSubject();

        return new JwtAuthenticationToken(jwt, authorities, principal);
    }

    /**
     * Navega la ruta configurada en {@code app.security.claims.roles-path} y
     * convierte los roles encontrados en {@link GrantedAuthority} con prefijo {@code ROLE_}.
     */
    private Collection<GrantedAuthority> extractRoleAuthorities(Jwt jwt) {
        String rolesPath = claimsProperties.rolesPath();
        String[] pathParts = rolesPath.split("\\.");
        Object current = jwt.getClaims();

        for (String part : pathParts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                log.debug("Ruta de roles '{}' no encontrada en el JWT en el segmento '{}'",
                        rolesPath, part);
                return Collections.emptySet();
            }
        }

        if (!(current instanceof Collection<?> roleCollection)) {
            log.debug("El claim de roles '{}' no es una colecciÃƒÂ³n", rolesPath);
            return Collections.emptySet();
        }

        Set<GrantedAuthority> authorities = roleCollection.stream()
                .filter(r -> r instanceof String)
                .map(r -> {
                    String roleName = ((String) r).toUpperCase();
                    return (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + roleName);
                })
                .collect(Collectors.toUnmodifiableSet());

        log.debug("Roles extraÃƒÂ­dos del JWT (path '{}'): {}", rolesPath, authorities);
        return authorities;
    }
}

