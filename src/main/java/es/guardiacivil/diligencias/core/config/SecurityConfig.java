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
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ConfiguraciÃ³n de seguridad del backend REST.
 *
 * <p>Toda la configuraciÃ³n sensible se inyecta desde propiedades/variables de entorno.
 * No se codifica ninguna URL ni lÃ³gica especÃ­fica de Keycloak u otro proveedor.</p>
 *
 * <p>El {@link JwtDecoder} se configura con:</p>
 * <ul>
 *   <li>{@code OIDC_JWK_SET_URI}: URI del endpoint de claves pÃºblicas JWKS del proveedor.</li>
 *   <li>{@code OIDC_ISSUER_SUFFIX}: sufijo que debe coincidir con el claim {@code iss} del token.
 *       Permite validar tokens emitidos desde distintas URLs del mismo realm
 *       (p.ej. vÃ­a tÃºnel SSH {@code localhost:8080} o directamente {@code keycloak-pod-0:8080})
 *       sin requerir coincidencia exacta de URL.</li>
 * </ul>
 *
 * <p>En producciÃ³n, si el proveedor siempre emite tokens con el mismo {@code iss}, se puede
 * establecer {@code OIDC_ISSUER_SUFFIX} al path completo del realm para mayor rigor.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * URI del endpoint JWKS para obtener las claves pÃºblicas del proveedor OIDC.
     * Configurable mediante la variable de entorno {@code OIDC_JWK_SET_URI}.
     */
    @Value("${app.security.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * URI exacto del emisor que se acepta como vÃ¡lido.
     * Configurable mediante {@code OIDC_ISSUER_URI}.
     * Si es nulo o vacÃ­o, solo se valida la firma y la caducidad del token.
     */
    @Value("${app.security.jwt.issuer-uri:#{null}}")
    private String issuerUri;

    /**
     * Audience esperado en el token JWT.
     * Si no se configura, no se valida este claim.
     */
    @Value("${app.security.jwt.audience:#{null}}")
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
     * Omite la cadena de filtros de seguridad para las rutas de documentaciÃ³n OpenAPI y Swagger UI.
     * Garantiza que la lectura de la especificaciÃ³n y la consola web no requieran JWT ni emitan 401.
     */
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v3/api-docs"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v3/api-docs/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v3/api-docs.yaml"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui.html"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui/index.html"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/webjars/**")
        );
    }

    /**
     * Construye el decodificador JWT con:
     * <ol>
     *   <li>ValidaciÃ³n de firma mediante clave pÃºblica obtenida de {@code OIDC_JWK_SET_URI}.</li>
     *   <li>ValidaciÃ³n de caducidad ({@code exp} y {@code nbf}).</li>
     *   <li>ValidaciÃ³n de sufijo del emisor si {@code OIDC_ISSUER_SUFFIX} estÃ¡ configurado.</li>
     * </ol>
     *
     * <p>No contiene ninguna referencia a Keycloak ni a URLs especÃ­ficas de ningÃºn proveedor.</p>
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();

        if (issuerUri != null && !issuerUri.isBlank()) {
            validators.add(org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithIssuer(issuerUri.trim()));
            log.info("ValidaciÃ³n estricta de emisor JWT activa. Issuer requerido: '{}'", issuerUri.trim());
        } else {
            validators.add(new JwtTimestampValidator());
            log.warn("app.security.jwt.issuer-uri no configurado. Solo se valida firma y caducidad del token.");
        }

        if (expectedAudience != null && !expectedAudience.isBlank()) {
            validators.add(new AudienceValidator(expectedAudience.trim()));
            log.info("ValidaciÃ³n de audiencia activa. Audience requerido: '{}'", expectedAudience.trim());
        }

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
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
                    new OAuth2Error("invalid_token", "El token no estÃ¡ destinado a esta API (audience incorrecta o ausente)", null)
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

