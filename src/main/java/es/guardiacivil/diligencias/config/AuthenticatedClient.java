package es.guardiacivil.diligencias.config;

import es.guardiacivil.diligencias.core.config.Roles;

import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;

import java.util.Set;

/**
 * Representación interna del cliente técnico autenticado mediante client_credentials OAuth2.
 *
 * <p>Análogo a {@link AuthenticatedUser} para usuarios humanos, pero para clientes técnicos
 * (aplicaciones que se autentican sin intervención humana).
 * Todos los servicios de integración deben recibir este objeto como parámetro
 * en vez de leer el JWT directamente.</p>
 *
 * @param clientId El identificador del cliente OAuth2 extraído del claim {@code azp} o {@code client_id}.
 * @param roles    Conjunto de roles técnicos asignados al cliente en Keycloak.
 */
public record AuthenticatedClient(
        String clientId,
        Set<String> roles
) {
    /**
     * Comprueba si el cliente tiene el rol indicado.
     *
     * @param role Nombre del rol sin prefijo {@code ROLE_}.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    @Override
    public String toString() {
        return "AuthenticatedClient{clientId='" + clientId + "', roles=" + roles + "}";
    }
}
