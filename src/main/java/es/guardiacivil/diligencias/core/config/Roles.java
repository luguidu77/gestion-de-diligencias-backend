package es.guardiacivil.diligencias.core.config;

/**
 * Constantes con los nombres de los roles corporativos del sistema.
 *
 * <p>La fuente de autoridad de los roles es <b>Keycloak</b>. Estos valores deben
 * coincidir exactamente con los nombres de roles definidos en el realm "diligencias"
 * de Keycloak. Spring Security los prefijará internamente con {@code ROLE_} al
 * convertirlos desde el claim {@code realm_access.roles} del JWT.</p>
 *
 * <p>Esta clase se usa en las expresiones SpEL de {@code @PreAuthorize}:</p>
 * <pre>
 *   {@code @PreAuthorize("hasAnyRole('" + Roles.TRAMITADOR + "', '" + Roles.ADMIN_UNIDAD + "')")}
 * </pre>
 *
 * <p>Política de acceso:</p>
 * <ul>
 *   <li><b>CONSULTA:</b>   Solo lectura, únicamente su unidad.</li>
 *   <li><b>TRAMITADOR:</b> Lectura, creación y modificación en su unidad.</li>
 *   <li><b>ADMIN_UNIDAD:</b> CRUD completo en su unidad + acceso a auditoría de su unidad.</li>
 *   <li><b>SUPERADMIN:</b>  CRUD completo en todas las unidades + auditoría global.</li>
 * </ul>
 */
public final class Roles {

    /** Solo lectura, únicamente expedientes y gestiones de su unidad. */
    public static final String CONSULTA = "CONSULTA";

    /** Lectura, creación y modificación en su unidad. Sin eliminación. */
    public static final String TRAMITADOR = "TRAMITADOR";

    /** CRUD completo en su unidad. Acceso a logs de auditoría de su unidad. */
    public static final String ADMIN_UNIDAD = "ADMIN_UNIDAD";

    /** CRUD completo en todas las unidades. Acceso a auditoría global. */
    public static final String SUPERADMIN = "SUPERADMIN";

    // SpEL expressions reutilizables para @PreAuthorize
    /** Todos los roles: cualquier usuario autenticado con rol corporativo. */
    public static final String TODOS = "hasAnyRole('CONSULTA','TRAMITADOR','ADMIN_UNIDAD','SUPERADMIN')";

    /** Roles que pueden crear y modificar (excluye CONSULTA). */
    public static final String PUEDE_ESCRIBIR = "hasAnyRole('TRAMITADOR','ADMIN_UNIDAD','SUPERADMIN')";

    /** Roles que pueden eliminar. */
    public static final String PUEDE_ELIMINAR = "hasAnyRole('ADMIN_UNIDAD','SUPERADMIN')";

    /** Roles que pueden ver logs de auditoría. */
    public static final String PUEDE_AUDITAR = "hasAnyRole('ADMIN_UNIDAD','SUPERADMIN')";

    // ─────────────────────────────────────────────────────────────────────────
    // Roles técnicos para la integración con la aplicación judicial externa.
    // Se asignan a clientes OAuth2 (client_credentials), no a usuarios humanos.
    // ─────────────────────────────────────────────────────────────────────────

    /** Permite consultar el estado de las diligencias desde la aplicación judicial. */
    public static final String CONSULTAR_ESTADO_JUDICIAL = "CONSULTAR_ESTADO_JUDICIAL";

    /** Permite enviar (crear) requerimientos judiciales. */
    public static final String ENVIAR_REQUERIMIENTOS_JUDICIALES = "ENVIAR_REQUERIMIENTOS_JUDICIALES";

    /** Permite consultar las respuestas a requerimientos judiciales. */
    public static final String CONSULTAR_RESPUESTAS_JUDICIALES = "CONSULTAR_RESPUESTAS_JUDICIALES";

    /** SpEL: cliente judicial con permiso de consulta de estado. */
    public static final String PUEDE_CONSULTAR_JUDICIAL =
            "hasAuthority('ROLE_CONSULTAR_ESTADO_JUDICIAL')";
    /** SpEL: cliente judicial con permiso de enviar requerimientos. */
    public static final String PUEDE_ENVIAR_JUDICIAL =
            "hasAuthority('ROLE_ENVIAR_REQUERIMIENTOS_JUDICIALES')";
    /** SpEL: cliente judicial con permiso de consultar respuestas. */
    public static final String PUEDE_CONSULTAR_RESPUESTAS_JUDICIALES =
            "hasAuthority('ROLE_CONSULTAR_RESPUESTAS_JUDICIALES')";

    private Roles() {
        // Clase de constantes, no instanciable
    }
}
