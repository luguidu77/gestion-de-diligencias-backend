package es.guardiacivil.diligencias.core.security;

import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;

import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;

import es.guardiacivil.diligencias.core.config.Roles;

import es.guardiacivil.diligencias.core.config.SecurityClaimsProperties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios de {@link AuthenticatedUserService}.
 *
 * <p>Validan que el servicio construye correctamente {@link AuthenticatedUser}
 * con distintos proveedores de identidad y configuraciones de claims, sin requerir
 * ninguna infraestructura de Keycloak ni contexto de Spring.</p>
 */
@DisplayName("AuthenticatedUserService — mapeo de JWT a identidad normalizada")
class AuthenticatedUserServiceTest {

    /** Propiedades de claims equivalentes a la configuración de laboratorio (Keycloak 24). */
    private static final SecurityClaimsProperties LAB_CLAIMS = new SecurityClaimsProperties(
            "preferred_username",
            "name",
            "rango",
            "unidad",
            "resource_access.diligencias-backend.roles"
    );

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void autenticarEnContexto(Map<String, Object> claims) {
        Jwt jwt = Jwt.withTokenValue("mock-token-value")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(claims))
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1 — Token de Keycloak de laboratorio con todos los claims presentes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token Keycloak de laboratorio — extrae identidad completa correctamente")
    void tokenKeycloakLaboratorio_extrae_identidad_completa() {
        autenticarEnContexto(Map.of(
                "preferred_username", "lugo_user",
                "name", "Agente Lugo",
                "rango", "Guardia Civil",
                "unidad", "PJ-LUG-01",
                "resource_access", Map.of(
                        "diligencias-backend", Map.of(
                                "roles", List.of("TRAMITADOR")
                        )
                )
        ));

        AuthenticatedUser user = new AuthenticatedUserService(LAB_CLAIMS).getAuthenticatedUser();

        assertThat(user.username()).isEqualTo("lugo_user");
        assertThat(user.displayName()).isEqualTo("Agente Lugo");
        assertThat(user.rank()).isEqualTo("Guardia Civil");
        assertThat(user.unitCode()).isEqualTo("PJ-LUG-01");
        assertThat(user.roles()).containsExactlyInAnyOrder("TRAMITADOR");
        assertThat(user.hasRole("TRAMITADOR")).isTrue();
        assertThat(user.isSuperadmin()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2 — Token con claims equivalentes de SSO corporativo alternativo
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token SSO corporativo con claims distintos — extrae identidad si se remapean propiedades")
    void tokenSSOAlternativo_con_claims_reasignados_extrae_correctamente() {
        // Simula un SSO corporativo que usa nombres de claims distintos a Keycloak
        SecurityClaimsProperties ssoClaims = new SecurityClaimsProperties(
                "samAccountName",   // En vez de preferred_username
                "displayName",      // En vez de name
                "rank",             // En vez de rango
                "unidadDestino",    // En vez de unidad
                "roles"             // Claim directo en vez de ruta anidada
        );

        autenticarEnContexto(Map.of(
                "samAccountName", "jfe_atc",
                "displayName", "J. Fernandez",
                "rank", "Sargento Primero",
                "unidadDestino", "PJ-LUG-01",
                "roles", List.of("ADMIN_UNIDAD")
        ));

        AuthenticatedUser user = new AuthenticatedUserService(ssoClaims).getAuthenticatedUser();

        assertThat(user.username()).isEqualTo("jfe_atc");
        assertThat(user.displayName()).isEqualTo("J. Fernandez");
        assertThat(user.rank()).isEqualTo("Sargento Primero");
        assertThat(user.unitCode()).isEqualTo("PJ-LUG-01");
        assertThat(user.roles()).containsExactlyInAnyOrder("ADMIN_UNIDAD");
        assertThat(user.hasRole("ADMIN_UNIDAD")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3 — Token sin claim de unidad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token sin claim de unidad — unitCode es null, no lanza excepción")
    void tokenSinUnidad_devuelve_unitCode_null_sin_excepcion() {
        autenticarEnContexto(Map.of(
                "preferred_username", "agente_sin_unidad",
                "name", "Agente Sin Unidad"
                // Sin claim 'unidad'
        ));

        AuthenticatedUser user = new AuthenticatedUserService(LAB_CLAIMS).getAuthenticatedUser();

        assertThat(user.username()).isEqualTo("agente_sin_unidad");
        assertThat(user.unitCode()).isNull();
        assertThat(user.roles()).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4 — Token sin roles
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token sin roles — roles es Set vacío, no lanza excepción")
    void tokenSinRoles_devuelve_roles_vacio_sin_excepcion() {
        autenticarEnContexto(Map.of(
                "preferred_username", "agente_sin_roles",
                "name", "Agente Sin Roles",
                "unidad", "PJ-LUG-01"
                // Sin resource_access ni roles
        ));

        AuthenticatedUser user = new AuthenticatedUserService(LAB_CLAIMS).getAuthenticatedUser();

        assertThat(user.roles()).isEmpty();
        assertThat(user.isSuperadmin()).isFalse();
        assertThat(user.hasRole("TRAMITADOR")).isFalse();
        assertThat(user.hasRole("ADMIN_UNIDAD")).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 5 — Token con rol no reconocido por la aplicación
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token con rol no reconocido — se incluye en roles, @PreAuthorize denegará")
    void tokenConRolNoReconocido_se_incluye_pero_no_otorga_permisos_corporativos() {
        autenticarEnContexto(Map.of(
                "preferred_username", "agente_externo",
                "name", "Agente Externo",
                "unidad", "PJ-LUG-01",
                "resource_access", Map.of(
                        "diligencias-backend", Map.of(
                                "roles", List.of("ROL_ORGANISMO_EXTERNO")
                        )
                )
        ));

        AuthenticatedUser user = new AuthenticatedUserService(LAB_CLAIMS).getAuthenticatedUser();

        // El rol se extrae pero no coincide con ninguno de los 4 roles corporativos
        assertThat(user.roles()).containsExactly("ROL_ORGANISMO_EXTERNO");
        assertThat(user.hasRole(Roles.CONSULTA)).isFalse();
        assertThat(user.hasRole(Roles.TRAMITADOR)).isFalse();
        assertThat(user.hasRole(Roles.ADMIN_UNIDAD)).isFalse();
        assertThat(user.hasRole(Roles.SUPERADMIN)).isFalse();
        // Un @PreAuthorize("hasAnyRole('CONSULTA','TRAMITADOR','ADMIN_UNIDAD','SUPERADMIN')")
        // devolvería 403 para este usuario
    }
}
