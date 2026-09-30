package es.guardiacivil.diligencias.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/fixtures/unidades-seguridad.sql")
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ----- Pruebas sin Autenticación (401) -----
    @Test
    @DisplayName("Sin JWT -> 401 Unauthorized")
    void sinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/diligencias"))
                .andExpect(status().isUnauthorized());
    }

    // ----- Pruebas de CONSULTA -----
    @Test
    @DisplayName("Rol CONSULTA -> GET permitido, POST denegado (403)")
    void rolConsulta_restriccionMetodos() throws Exception {
        mockMvc.perform(get("/api/diligencias")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10001B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10001B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isForbidden());

        // Para probar PUT y DELETE, creamos primero un expediente usando un TRAMITADOR
        String responseBody = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo Creado\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10002B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(responseBody, "$.id").toString();

        mockMvc.perform(put("/api/diligencias/" + id)
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo Modificado\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10001B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/diligencias/" + id)
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10001B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isForbidden());
    }

    // ----- Pruebas de TRAMITADOR -----
    @Test
    @DisplayName("Rol TRAMITADOR -> POST permitido, DELETE denegado (403)")
    void rolTramitador_restriccionMetodos() throws Exception {
        String responseBody = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10002B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(responseBody, "$.id").toString();

        mockMvc.perform(put("/api/diligencias/" + id)
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo Modificado\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10002B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/diligencias/" + id)
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10002B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isForbidden());
    }

    // ----- Pruebas de AISLAMIENTO MULTI-UNIDAD (ABAC) -----
    @Test
    @DisplayName("Un usuario de Lugo no puede consultar una diligencia de Sevilla")
    void aislamientoMultiunidad_accesoDenegado() throws Exception {
        // Sevilla crea una diligencia.
        String responseBody = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Robo en Sevilla\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10003B")
                                        .claim("unidad", "PJ-SEV-01")
                                        .claim("resource_access", Map.of(
                                                "diligencias-backend",
                                                Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(responseBody, "$.id").toString();

        // Lugo no puede consultar el detalle de la diligencia de Sevilla.
        mockMvc.perform(get("/api/diligencias/" + id)
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10004B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of(
                                                "diligencias-backend",
                                                Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isNotFound());

        // La lista paginada de Lugo tampoco muestra diligencias de Sevilla.
        mockMvc.perform(get("/api/diligencias/paginado?page=0&size=10")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10004B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of(
                                                "diligencias-backend",
                                                Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ----- Pruebas de SUPERADMIN -----
    @Test
    @DisplayName("Rol SUPERADMIN -> Salta el filtro de unidad (Bypass ABAC)")
    void superadmin_bypassUnidad() throws Exception {
        // Creamos un expediente de Lugo
        String responseBody = mockMvc.perform(post("/api/diligencias")
                .contentType("application/json")
                .content("{\"delitoPrincipal\":\"Robo en Lugo\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                .with(jwt()
                        .jwt(j -> j
                                .claim("preferred_username", "A10004B")
                                .claim("unidad", "PJ-LUG-01")
                                .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                        )
                        .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
                
        String id = JsonPath.read(responseBody, "$.id").toString();
        String expUrl = "/api/diligencias/" + id;
                
        // El superadmin (de Madrid) accede al expediente de Lugo -> 200 OK
        mockMvc.perform(get(expUrl)
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10005B")
                                        .claim("unidad", "PJ-MAD-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("SUPERADMIN"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))))
                .andExpect(status().isOk());

        // El superadmin comprueba que el listado global incluye este expediente
        mockMvc.perform(get("/api/diligencias")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10005B")
                                        .claim("unidad", "PJ-MAD-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("SUPERADMIN"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ----- Pruebas de Auditoría -----
    @Test
    @DisplayName("TRAMITADOR no puede ver logs de auditoría (403)")
    void tramitador_auditoriaProhibida() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10004B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN_UNIDAD sí puede ver logs de auditoría (200)")
    void adminUnidad_auditoriaPermitida() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10006B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("ADMIN_UNIDAD"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_UNIDAD"))))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("ADMIN_UNIDAD de Lugo -> no puede ver logs de Sevilla (403)")
    void adminUnidad_noPuedeVerAuditoriaOtraUnidad() throws Exception {
        mockMvc.perform(get("/api/audit-logs?unidad=PJ-SEV-01")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10006B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("ADMIN_UNIDAD"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_UNIDAD"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rol Desconocido -> Token con rol no mapeado devuelve 403")
    void rolDesconocido_devuelve403() throws Exception {
        mockMvc.perform(get("/api/diligencias")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10007B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("LIMPIEZA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_LIMPIEZA"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Token sin claim de unidad -> no debe consultar datos de unidades")
    void tokenSinUnidad_devuelve403() throws Exception {
        mockMvc.perform(get("/api/diligencias")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10008B")
                                        // omitimos el claim unidad intencionadamente
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rol SUPERADMIN -> Lista todas las unidades")
    void superadmin_listaTodasLasUnidades() throws Exception {
        mockMvc.perform(get("/api/diligencias")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10005B")
                                        .claim("unidad", "PJ-MAD-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("SUPERADMIN"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Rol CONSULTA -> GET permitido, PUT y DELETE denegados (403)")
    void consulta_noPuedeModificar() throws Exception {
        // Creamos expediente primero
        String responseBody = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Expediente para test CONSULTA\", \"gravedadDelito\":\"GRAVE\", \"resumen\":\"Prueba de autorizacion\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10002B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(responseBody, "$.id").toString();

        mockMvc.perform(put("/api/diligencias/" + id)
                        .contentType("application/json")
                        .content("{\"delitoPrincipal\":\"Intento de Modificación\"}")
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10009B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/diligencias/" + id)
                        .with(jwt()
                                .jwt(j -> j
                                        .claim("preferred_username", "A10009B")
                                        .claim("unidad", "PJ-LUG-01")
                                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("CONSULTA"))))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CONSULTA"))))
                .andExpect(status().isForbidden());
    }
}
