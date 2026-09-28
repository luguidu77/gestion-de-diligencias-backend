package es.guardiacivil.diligencias.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("SYSTEM");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Retorna el preferred_username (que es el TIP del usuario en la configuración de Keycloak)
            // o el subject si no estuviera disponible
            return Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                    .or(() -> Optional.ofNullable(jwt.getSubject()));
        }

        return Optional.of(authentication.getName());
    }
}
