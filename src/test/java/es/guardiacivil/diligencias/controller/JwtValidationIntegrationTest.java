package es.guardiacivil.diligencias.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.guardiacivil.diligencias.core.config.SecurityClaimsProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.jwt.issuer-uri=http://keycloak-pod-0:8080/realms/diligencias",
        "app.security.jwt.audience=diligencias-backend"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtValidationIntegrationTest {

    private static RSAPrivateKey privateKey;
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey wrongPrivateKey;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUpKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        KeyPair wrongKeyPair = keyPairGenerator.generateKeyPair();
        wrongPrivateKey = (RSAPrivateKey) wrongKeyPair.getPrivate();
    }

    @TestConfiguration
    static class TestSecurityConfig {
        
        @org.springframework.beans.factory.annotation.Value("${app.security.jwt.audience}")
        private String expectedAudience;

        @Bean
        @Primary
        public JwtDecoder testJwtDecoder() {
            // Nivel 1: Construir decodificador directamente con la clave pública para aislar la validación criptográfica
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(JwtValidators.createDefaultWithIssuer("http://keycloak-pod-0:8080/realms/diligencias"));

            OAuth2TokenValidator<Jwt> audienceValidator = token -> {
                if (token.getAudience() != null && token.getAudience().contains(expectedAudience)) {
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Audience incorrecta o ausente", null)
                );
            };
            validators.add(audienceValidator);

            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
            return decoder;
        }
    }

    private String generateJwt(RSAPrivateKey signingKey, String issuer, List<String> audience, int expirationMinutes) throws JOSEException {
        Instant now = Instant.now();
        
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject("agente1")
                .claim("preferred_username", "agente1")
                .claim("unidad", "PJ-LUG-01")
                .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)));
                
        if (audience != null) {
            builder.audience(audience);
        }

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                builder.build()
        );

        signedJWT.sign(new RSASSASigner(signingKey));
        return signedJWT.serialize();
    }

    @Test
    @DisplayName("1. Firma válida, issuer y audience correctos -> 200")
    void tokenFirmaValidaEsAceptado() throws Exception {
        String token = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("diligencias-backend"), 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Múltiples audiencias incluyendo la correcta -> 200")
    void tokenMultiplesAudienciasEsAceptado() throws Exception {
        String token = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("account", "diligencias-backend", "broker"), 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. Firma de otra clave -> 401")
    void tokenFirmaInvalida() throws Exception {
        String token = generateJwt(
                wrongPrivateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("diligencias-backend"), 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Token manipulado -> 401")
    void tokenManipulado() throws Exception {
        String token = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("diligencias-backend"), 
                5
        );
        // Manipulamos el payload (la segunda parte del JWT)
        String[] parts = token.split("\\.");
        String manipulado = parts[0] + "." + parts[1] + "manipulado." + parts[2];

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + manipulado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Token caducado -> 401")
    void tokenCaducado() throws Exception {
        String token = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("diligencias-backend"), 
                -5 // Expiró hace 5 minutos
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("6. Issuer incorrecto -> 401")
    void tokenIssuerIncorrecto() throws Exception {
        String token = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/FALSO", 
                List.of("diligencias-backend"), 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Audience incorrecta / ausente -> 401")
    void tokenAudienceIncorrecta() throws Exception {
        // Audience incorrecta
        String tokenAudIncorrecta = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                List.of("frontend-app"), 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + tokenAudIncorrecta))
                .andExpect(status().isUnauthorized());

        // Audience ausente
        String tokenSinAud = generateJwt(
                privateKey, 
                "http://keycloak-pod-0:8080/realms/diligencias", 
                null, 
                5
        );

        mockMvc.perform(get("/api/diligencias")
                .header("Authorization", "Bearer " + tokenSinAud))
                .andExpect(status().isUnauthorized());
    }
}
