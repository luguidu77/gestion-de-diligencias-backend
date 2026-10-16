package es.guardiacivil.diligencias.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/fixtures/unidades-seguridad.sql")
class TaskAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinJwtNoPuedeConsultarTareas() throws Exception {
        mockMvc.perform(get("/api/v1/tareas/agente/A10001B"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaPuedeLeerPeroNoCrearNiModificar() throws Exception {
        String numero = crearDiligencia("A10001B", "PJ-LUG-01");
        String tareaId = crearTarea(numero, "A10001B", "PJ-LUG-01");

        mockMvc.perform(get("/api/v1/tareas/agente/A10001B")
                        .with(usuario("A10001B", "PJ-LUG-01", "CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tareaExternaId").value(tareaId));

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType("application/json")
                        .content(solicitud(numero, "A10001B"))
                        .with(usuario("A10001B", "PJ-LUG-01", "CONSULTA")))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/tareas/" + tareaId + "/estado")
                        .param("nuevoEstado", "COMPLETADA")
                        .with(usuario("A10001B", "PJ-LUG-01", "CONSULTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tramitadorPuedeModificarTareaDeSuUnidad() throws Exception {
        String numero = crearDiligencia("A10002B", "PJ-LUG-01");
        String tareaId = crearTarea(numero, "A10002B", "PJ-LUG-01");

        mockMvc.perform(patch("/api/v1/tareas/" + tareaId + "/estado")
                        .param("nuevoEstado", "COMPLETADA")
                        .with(usuario("A10002B", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tareas/agente/A10002B")
                        .with(usuario("A10002B", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("COMPLETADA"));
    }

    @Test
    void noPuedeModificarTareaAjenaAunqueEnvieUnExpedientePropio() throws Exception {
        String sevilla = crearDiligencia("A10003B", "PJ-SEV-01");
        String tareaId = crearTarea(sevilla, "A10003B", "PJ-SEV-01");
        String lugo = crearDiligencia("A10004B", "PJ-LUG-01");

        mockMvc.perform(patch("/api/v1/tareas/" + tareaId + "/estado")
                        .param("numeroExpediente", lugo)
                        .param("nuevoEstado", "COMPLETADA")
                        .with(usuario("A10004B", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/tareas/agente/A10003B")
                        .with(usuario("A10003B", "PJ-SEV-01", "TRAMITADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void adminUnidadSoloPuedeConsultarAgentesDeSuUnidad() throws Exception {
        crearDiligencia("A10005B", "PJ-LUG-01");
        crearDiligencia("A10006B", "PJ-SEV-01");

        mockMvc.perform(get("/api/v1/tareas/agente/A10005B")
                        .with(usuario("A10007B", "PJ-LUG-01", "ADMIN_UNIDAD")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tareas/agente/A10006B")
                        .with(usuario("A10007B", "PJ-LUG-01", "ADMIN_UNIDAD")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/tareas/agente/A10005B")
                        .with(usuario("A10007B", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void superadminPuedeModificarTareaDeOtraUnidad() throws Exception {
        String numero = crearDiligencia("A10008B", "PJ-SEV-01");
        String tareaId = crearTarea(numero, "A10008B", "PJ-SEV-01");

        mockMvc.perform(patch("/api/v1/tareas/" + tareaId + "/estado")
                        .param("nuevoEstado", "COMPLETADA")
                        .with(usuario("A10009B", "PJ-LUG-01", "SUPERADMIN")))
                .andExpect(status().isNoContent());
    }

    private String crearDiligencia(String tip, String unidad) throws Exception {
        String respuesta = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "delitoPrincipal":"Prueba de autorización",
                                  "gravedadDelito":"GRAVE",
                                  "resumen":"Prueba de aislamiento por unidad"
                                }
                                """)
                        .with(usuario(tip, unidad, "TRAMITADOR")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(respuesta, "$.id")).longValue();
        String sigo = String.format("2026-000001-%08d", id);

        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", sigo)
                        .with(usuario(tip, unidad, "TRAMITADOR")))
                .andExpect(status().isOk());

        return sigo;
    }

    private String crearTarea(String numero, String tip, String unidad) throws Exception {
        return mockMvc.perform(post("/api/v1/tareas")
                        .contentType("application/json")
                        .content(solicitud(numero, tip))
                        .with(usuario(tip, unidad, "TRAMITADOR")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String solicitud(String numero, String tip) {
        return """
                {
                  "numeroExpediente":"%s",
                  "tipAgente":"%s",
                  "empleoAgente":"Guardia Civil",
                  "descripcion":"Tarea de prueba",
                  "prioridad":"MEDIA"
                }
                """.formatted(numero, tip);
    }

    private JwtRequestPostProcessor usuario(String tip, String unidad, String rol) {
        return jwt()
                .jwt(token -> token
                        .claim("preferred_username", tip)
                        .claim("unidad", unidad)
                        .claim("resource_access",
                                Map.of("diligencias-backend",
                                        Map.of("roles", List.of(rol)))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
