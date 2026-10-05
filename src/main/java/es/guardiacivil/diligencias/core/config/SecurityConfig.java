package es.guardiacivil.diligencias.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuración de seguridad del backend REST.
 *
 * <p>Toda la configuración sensible se inyecta desde propiedades o variables de entorno.
 * No se codifica ninguna URL ni lógica específica de Keycloak u otro proveedor.</p>
 *
 * <p>El {@link JwtDecoder} se configura con:</p>
 * <ul>
 *   <li>{@code OIDC_JWK_SET_URI}: endpoint de claves públicas JWKS.</li>
 *   <li>{@code OIDC_ISSUER_URI}: emisor exacto requerido en el claim {@code iss}.</li>
 *   <li>{@code OIDC_AUDIENCE}: API requerida en el claim {@code aud}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * URI del endpoint JWKS para obtener las claves públicas del proveedor OIDC.
     * Configurable mediante la variable de entorno {@code OIDC_JWK_SET_URI}.
     */
    @Value("${app.security.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * URI exacto del emisor que se acepta como válido.
     * Configurable mediante {@code OIDC_ISSUER_URI}.
     * Es obligatorio para impedir que se acepten tokens de otro emisor.
     */
    @Value("${app.security.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * Audience esperado en el token JWT. Es obligatorio para asegurar que
     * el token está destinado a esta API.
     */
    @Value("${app.security.jwt.audience}")
    private String expectedAudience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html", "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthConverter)
                    .decoder(jwtDecoder)
                )
            );

        return http.build();
    }

    /**
     * Construye el decodificador JWT con:
     * <ol>
     *   <li>Validación de firma mediante las claves de {@code OIDC_JWK_SET_URI}.</li>
     *   <li>Validación temporal de {@code exp} y {@code nbf}.</li>
     *   <li>Validación exacta del emisor mediante {@code OIDC_ISSUER_URI}.</li>
     *   <li>Validación de audiencia mediante {@code OIDC_AUDIENCE}.</li>
     * </ol>
     *
     * <p>No contiene referencias a URLs específicas de ningún proveedor.</p>
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        Assert.hasText(issuerUri, "La propiedad app.security.jwt.issuer-uri es obligatoria");
        Assert.hasText(expectedAudience, "La propiedad app.security.jwt.audience es obligatoria");

        String requiredIssuer = issuerUri.trim();
        String requiredAudience = expectedAudience.trim();

        List<OAuth2TokenValidator<Jwt>> validators = List.of(
                JwtValidators.createDefaultWithIssuer(requiredIssuer),
                new AudienceValidator(requiredAudience)
        );

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        log.info(
                "Validación JWT estricta activa. Issuer: '{}'; audience: '{}'",
                requiredIssuer,
                requiredAudience
        );
        return decoder;
    }

    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String expectedAudience;

        public AudienceValidator(String expectedAudience) {
            this.expectedAudience = expectedAudience;
        }

        @Override
        public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getAudience() != null && jwt.getAudience().contains(expectedAudience)) {
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
            }
            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El token no está destinado a esta API (audience incorrecta o ausente)", null)
            );
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "X-Requested-With"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
