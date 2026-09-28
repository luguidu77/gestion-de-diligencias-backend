package es.guardiacivil.diligencias.core.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuraciÃ³n para el mapeo de claims del JWT.
 *
 * <p>Centraliza los nombres de claims que el backend lee del token. Todos los
 * nombres son configurables mediante variables de entorno, lo que permite cambiar
 * de proveedor de identidad (Keycloak, SSO corporativo, ADFS...) sin modificar
 * ningÃºn clase Java: solo es necesario actualizar las propiedades del perfil.</p>
 *
 * <p>Ejemplo de configuraciÃ³n para laboratorio (Keycloak):</p>
 * <pre>
 *   CLAIM_USERNAME:   preferred_username
 *   CLAIM_UNIT:       unidad
 *   CLAIM_ROLES_PATH: resource_access.diligencias-backend.roles
 * </pre>
 *
 * <p>Ejemplo de configuraciÃ³n para SSO corporativo hipotÃ©tico:</p>
 * <pre>
 *   CLAIM_USERNAME:   samAccountName
 *   CLAIM_UNIT:       unidadDestino
 *   CLAIM_ROLES_PATH: roles
 * </pre>
 *
 * @param username    Claim que contiene el identificador Ãºnico del usuario (TIP/username).
 * @param displayName Claim con el nombre completo para mostrar.
 * @param rank        Claim con el rango o empleo del agente (informativo, no otorga permisos).
 * @param unit        Claim con el cÃ³digo de unidad policial del agente.
 * @param rolesPath   Ruta de claim con los roles, en notaciÃ³n punto. Soporta rutas anidadas
 *                    como {@code resource_access.diligencias-backend.roles} o simples como {@code roles}.
 */
@ConfigurationProperties(prefix = "app.security.claims")
@Validated
public record SecurityClaimsProperties(
        @NotBlank String username,
        String displayName,
        String rank,
        String unit,
        @NotBlank String rolesPath
) {}
