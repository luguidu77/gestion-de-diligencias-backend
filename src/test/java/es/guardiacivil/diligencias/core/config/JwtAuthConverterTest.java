package es.guardiacivil.diligencias.core.config;

import es.guardiacivil.diligencias.core.config.SecurityClaimsProperties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthConverterTest {

    private final SecurityClaimsProperties properties = new SecurityClaimsProperties(
            "preferred_username",
            "name",
            "rango",
            "unidad",
            "resource_access.diligencias-backend.roles"
    );

    private final JwtAuthConverter converter = new JwtAuthConverter(properties);

    @Test
    @DisplayName("Extrae ROLE_TRAMITADOR correctamente desde resource_access")
    void extraeRolesCorrectamente() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("agente1");
        when(jwt.getClaims()).thenReturn(Map.of(
                "resource_access", Map.of(
                        "diligencias-backend", Map.of(
                                "roles", List.of("TRAMITADOR", "OTRO_ROL")
                        )
                )
        ));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token).isNotNull();
        assertThat(token.getName()).isEqualTo("agente1");
        
        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
                
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_TRAMITADOR", "ROLE_OTRO_ROL");
    }

    @Test
    @DisplayName("Devuelve autoridades vacías si el claim no existe o la ruta está incompleta")
    void devuelveVacioSiClaimNoExiste() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("agente1");
        when(jwt.getClaims()).thenReturn(Map.of());

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }
    
    @Test
    @DisplayName("Devuelve autoridades vacías si el cliente no está en resource_access")
    void devuelveVacioSiClienteNoExiste() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("agente1");
        when(jwt.getClaims()).thenReturn(Map.of(
                "resource_access", Map.of(
                        "otro-cliente", Map.of("roles", List.of("TRAMITADOR"))
                )
        ));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Devuelve autoridades vacías si roles está vacío")
    void devuelveVacioSiRolesVacio() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("agente1");
        when(jwt.getClaims()).thenReturn(Map.of(
                "resource_access", Map.of(
                        "diligencias-backend", Map.of("roles", List.of())
                )
        ));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }
}
