package es.guardiacivil.diligencias.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.integraciones.judicial.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/fixtures/unidades-seguridad.sql")
class ResourceAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private record Expediente(long id, String numero) {}

    @Test
    void tramitadorPuedeEscribirEnSuUnidad() throws Exception {
        Expediente expediente = crearDiligencia("B10001A", "PJ-LUG-01");
        long documento = subirDocumento(expediente, "B10001A", "PJ-LUG-01");

        comprobarRemision(documento, "B10001A", "PJ-LUG-01", "TRAMITADOR", 201);
        comprobarRequerimiento(expediente, "B10001A", "PJ-LUG-01", "TRAMITADOR", 201);
    }

    @Test
    void consultaNoPuedeEscribirEnNingunoDeLosTresModulos() throws Exception {
        Expediente expediente = crearDiligencia("B10002A", "PJ-LUG-01");
        long documento = subirDocumento(expediente, "B10002A", "PJ-LUG-01");

        mockMvc.perform(archivo(expediente.id())
                        .with(usuario("B10002A", "PJ-LUG-01", "CONSULTA")))
                .andExpect(status().isForbidden());

        comprobarRemision(documento, "B10002A", "PJ-LUG-01", "CONSULTA", 403);
        comprobarRequerimiento(expediente, "B10002A", "PJ-LUG-01", "CONSULTA", 403);
    }

    @Test
    void tramitadorNoPuedeEscribirEnOtraUnidad() throws Exception {
        Expediente expediente = crearDiligencia("B10003A", "PJ-SEV-01");
        long documento = subirDocumento(expediente, "B10003A", "PJ-SEV-01");

        mockMvc.perform(archivo(expediente.id())
                        .with(usuario("B10004A", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isNotFound());

        comprobarRemision(documento, "B10004A", "PJ-LUG-01", "TRAMITADOR", 404);
        comprobarRequerimiento(expediente, "B10004A", "PJ-LUG-01", "TRAMITADOR", 404);
    }

    @Test
    void superadminPuedeEscribirEnOtraUnidad() throws Exception {
        Expediente expediente = crearDiligencia("B10005A", "PJ-SEV-01");

        String respuesta = mockMvc.perform(archivo(expediente.id())
                        .with(usuario("B10006A", "PJ-LUG-01", "SUPERADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long documento = ((Number) JsonPath.read(respuesta, "$.id")).longValue();

        comprobarRemision(documento, "B10006A", "PJ-LUG-01", "SUPERADMIN", 201);
        comprobarRequerimiento(expediente, "B10006A", "PJ-LUG-01", "SUPERADMIN", 201);
    }

    @Test
    void rolesTecnicosNoPuedenUsarLosEndpointsHumanos() throws Exception {
        Expediente expediente = crearDiligencia("B10007A", "PJ-LUG-01");

        mockMvc.perform(archivo(expediente.id())
                        .with(usuario("cliente-judicial", "PJ-LUG-01",
                                "ENVIAR_REQUERIMIENTOS_JUDICIALES")))
                .andExpect(status().isForbidden());

        comprobarRequerimiento(expediente, "cliente-judicial", "PJ-LUG-01",
                "ENVIAR_REQUERIMIENTOS_JUDICIALES", 403);

        mockMvc.perform(get("/api/integraciones/judicial/diligencias/NIG-TEST/estado")
                        .with(usuario("B10007A", "PJ-LUG-01", "TRAMITADOR")))
                .andExpect(status().isForbidden());
    }

    private Expediente crearDiligencia(String tip, String unidad) throws Exception {
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

        return new Expediente(id, sigo);
    }

    private long subirDocumento(Expediente expediente, String tip, String unidad)
            throws Exception {
        String respuesta = mockMvc.perform(archivo(expediente.id())
                        .with(usuario(tip, unidad, "TRAMITADOR")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private MockMultipartHttpServletRequestBuilder archivo(long diligenciaId) {
        return multipart("/api/diligencias/" + diligenciaId + "/documentos")
                .file(new MockMultipartFile(
                        "archivo", "prueba.txt", "text/plain",
                        "contenido".getBytes(StandardCharsets.UTF_8)))
                .file(new MockMultipartFile(
                        "datos", "", "application/json",
                        """
                        {
                          "tituloDocumento":"Documento de prueba",
                          "destino":"INTERNO_ALFRESCO",
                          "estadoDocumento":"BORRADOR"
                        }
                        """.getBytes(StandardCharsets.UTF_8)));
    }

    private void comprobarRemision(long documento, String tip, String unidad,
                                   String rol, int estado) throws Exception {
        mockMvc.perform(post("/api/documentos/" + documento + "/remisiones")
                        .contentType("application/json")
                        .content("""
                                {"destino":"JUZGADO","tipoRemision":"SIMULADA"}
                                """)
                        .with(usuario(tip, unidad, rol)))
                .andExpect(status().is(estado));
    }

    private void comprobarRequerimiento(Expediente expediente, String tip,
                                        String unidad, String rol, int estado)
            throws Exception {
        mockMvc.perform(post("/api/diligencias/" + expediente.numero()
                                + "/requerimientos")
                        .contentType("application/json")
                        .content("""
                                {
                                  "referenciaJudicial":"NIG-TEST",
                                  "juzgadoOrigen":"Juzgado de prueba",
                                  "numeroProcedimiento":"AUTOS-TEST-2026",
                                  "asunto":"Solicitud de informe",
                                  "fechaEscrito":"2026-10-13",
                                  "referenciaPolicial":"%s",
                                  "tipo":"INFORME",
                                  "descripcion":"Requerimiento de prueba",
                                  "prioridad":"NORMAL",
                                  "identificadorExterno":"%s"
                                }
                                """.formatted(expediente.numero(), UUID.randomUUID()))
                        .with(usuario(tip, unidad, rol)))
                .andExpect(status().is(estado));
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
