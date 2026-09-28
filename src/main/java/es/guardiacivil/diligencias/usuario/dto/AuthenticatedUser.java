package es.guardiacivil.diligencias.usuario.dto;

import es.guardiacivil.diligencias.core.config.Roles;

import java.util.Set;

/**
 * RepresentaciÃ³n interna y normalizada del usuario autenticado en la peticiÃ³n actual.
 *
 * <p>Este record es la abstracciÃ³n central que aÃ­sla al resto de la aplicaciÃ³n
 * de la estructura concreta del token JWT del proveedor de identidad.
 * Todos los servicios y controladores deben trabajar con {@code AuthenticatedUser},
 * nunca leer claims directamente del objeto {@link org.springframework.security.oauth2.jwt.Jwt}.</p>
 *
 * <p>La construcciÃ³n de este objeto es responsabilidad exclusiva de
 * {@link AuthenticatedUserService}, que aplica el mapeo configurado en
 * {@link SecurityClaimsProperties}.</p>
 *
 * @param username    Identificador Ãºnico del usuario (TIP corporativo, samAccountName, etc.).
 * @param displayName Nombre completo para mostrar en la interfaz y en los logs.
 * @param rank        Rango o empleo del agente. Dato informativo, no otorga permisos.
 * @param unitCode    CÃ³digo de la unidad policial a la que pertenece el agente.
 * @param roles       Conjunto de roles corporativos asignados al usuario en el proveedor de identidad.
 */
public record AuthenticatedUser(
        String username,
        String displayName,
        String rank,
        String unitCode,
        Set<String> roles
) {
    /**
     * Comprueba si el usuario tiene el rol indicado.
     *
     * @param role Nombre del rol sin prefijo {@code ROLE_}, p.ej. {@code "TRAMITADOR"}.
     * @return {@code true} si el usuario tiene ese rol.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Indica si el usuario es SUPERADMIN, lo que le permite operar sobre
     * expedientes de cualquier unidad sin restricciÃ³n de aislamiento.
     */
    public boolean isSuperadmin() {
        return hasRole(Roles.SUPERADMIN);
    }

    /**
     * Devuelve el username como representaciÃ³n de texto, Ãºtil para logs.
     */
    @Override
    public String toString() {
        return "AuthenticatedUser{username='" + username + "', unitCode='" + unitCode
               + "', roles=" + roles + "}";
    }
}
