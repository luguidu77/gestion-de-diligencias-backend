package es.guardiacivil.diligencias.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ConfiguraciÃ³n centralizada de OpenAPI 3 y Swagger UI para el backend de GestiÃ³n de Diligencias.
 *
 * <p>Define los metadatos de la API, el esquema de seguridad {@code bearerAuth} para tokens JWT,
 * los servidores del entorno de laboratorio/desarrollo y la agrupaciÃ³n de endpoints en
 * dos APIs independientes: Interna Policial e IntegraciÃ³n Judicial.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Gesti\u00f3n de Diligencias Policiales",
                version = "v1",
                description = """
                        API RESTful corporativa para la gestiÃ³n de diligencias policiales, expedientes,
                        documentos adjuntos (gestor documental Alfresco), auditorÃ­a legal inmutable e
                        integraciÃ³n bidireccional con aplicaciones judiciales externas.
                        """,
                contact = @Contact(
                        name = "Oficina TÃ©cnica de Soporte ASIR",
                        email = "soporte-diligencias@guardiacivil.es"
                ),
                license = @License(
                        name = "Uso Interno Corporativo / Reservado",
                        url = "https://www.guardiacivil.es"
                )
        ),
        servers = {
                @Server(
                        url = "${OPENAPI_SERVER_URL:http://10.10.10.1:8081}",
                        description = "Entorno de Laboratorio (Acceso controlado por WireGuard / VPN)"
                ),
                @Server(
                        url = "http://localhost:8081",
                        description = "Entorno Local de Desarrollo (TÃºnel SSH / Directo)"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Inserte el token JWT de acceso (Access Token) emitido por Keycloak o el Proveedor OIDC."
)
public class OpenApiConfig {

    /**
     * AgrupaciÃ³n para la API Interna Policial.
     * Incluye expedientes, documentos, auditorÃ­a, gestiones y perfil de usuario.
     * Excluye los endpoints de integraciÃ³n judicial tÃ©cnica.
     */
    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("api-interna")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/integraciones/judicial/**")
                .build();
    }

    /**
     * AgrupaciÃ³n para la API de IntegraciÃ³n Judicial Externa.
     * Incluye consulta de estado de diligencias y recepciÃ³n de requerimientos judiciales (client_credentials).
     */
    @Bean
    public GroupedOpenApi judicialApi() {
        return GroupedOpenApi.builder()
                .group("integracion-judicial")
                .pathsToMatch("/api/integraciones/judicial/**")
                .build();
    }
}

