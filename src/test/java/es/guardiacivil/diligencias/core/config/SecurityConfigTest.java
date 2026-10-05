package es.guardiacivil.diligencias.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private static final String JWK_SET_URI = "http://localhost:59999/jwks";
    private static final String ISSUER_URI = "http://localhost:8080/realms/diligencias";
    private static final String AUDIENCE = "diligencias-backend";

    @Test
    void debeCrearJwtDecoderConConfiguracionCompleta() {
        SecurityConfig config = createSecurityConfig(ISSUER_URI, AUDIENCE);

        assertThat(config.jwtDecoder()).isNotNull();
    }

    @Test
    void debeRechazarIssuerVacio() {
        SecurityConfig config = createSecurityConfig(" ", AUDIENCE);

        assertThatIllegalArgumentException()
                .isThrownBy(config::jwtDecoder)
                .withMessage("La propiedad app.security.jwt.issuer-uri es obligatoria");
    }

    @Test
    void debeRechazarAudienceVacia() {
        SecurityConfig config = createSecurityConfig(ISSUER_URI, " ");

        assertThatIllegalArgumentException()
                .isThrownBy(config::jwtDecoder)
                .withMessage("La propiedad app.security.jwt.audience es obligatoria");
    }

    private SecurityConfig createSecurityConfig(String issuerUri, String audience) {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthConverter.class));

        ReflectionTestUtils.setField(config, "jwkSetUri", JWK_SET_URI);
        ReflectionTestUtils.setField(config, "issuerUri", issuerUri);
        ReflectionTestUtils.setField(config, "expectedAudience", audience);

        return config;
    }
}
