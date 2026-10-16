package es.guardiacivil.diligencias.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.integraciones.judicial.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/fixtures/unidades-seguridad.sql")
class BorradorDiligenciaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TIP = "B10001A";
    private static final String UNIDAD = "PJ-LUG-01";
    private static final String SIGO_VALIDO_1 = "2026-000001-00000001";
    private static final String SIGO_VALIDO_2 = "2026-000001-00000002";

    private JwtRequestPostProcessor tramitador() {
        return jwt()
                .jwt(token -> token
                        .claim("preferred_username", TIP)
                        .claim("unidad", UNIDAD)
                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("TRAMITADOR")))))
                .authorities(new SimpleGrantedAuthority("ROLE_TRAMITADOR"));
    }

    @Test
    @DisplayName("Crear expediente nace en BORRADOR sin número SIGO ni retención")
    void crearExpediente_naceEnBorradorSinSigo() throws Exception {
        mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "delitoPrincipal": "Robo con fuerza",
                                  "gravedadDelito": "GRAVE",
                                  "resumen": "Expediente borrador inicial"
                                }
                                """)
                        .with(tramitador()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.numeroExpediente").doesNotExist())
                .andExpect(jsonPath("$.aniosRetencionLegal").doesNotExist())
                .andExpect(jsonPath("$.fechaExpiracionRetencion").doesNotExist());
    }

    @Test
    @DisplayName("Asignar número SIGO con formato inválido devuelve 400 Bad Request")
    void asignarNumeroSigo_formatoInvalido_devuelve400() throws Exception {
        long id = crearBorrador("Robo", "GRAVE");

        // Faltan dígitos o formato erróneo
        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", "2026-123-456")
                        .with(tramitador()))
                .andExpect(status().isBadRequest());

        // Caracteres alfabéticos
        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", "2026-ABCDEF-00000001")
                        .with(tramitador()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Asignar número SIGO válido con datos completos pasa automáticamente a ABIERTA y calcula retención")
    void asignarNumeroSigo_datosCompletos_pasaAAbierta() throws Exception {
        long id = crearBorrador("Robo con violencia", "MUY_GRAVE");

        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_1)
                        .with(tramitador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.numeroExpediente").value(SIGO_VALIDO_1))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.aniosRetencionLegal").value(10))
                .andExpect(jsonPath("$.fechaExpiracionRetencion").isNotEmpty());
    }

    @Test
    @DisplayName("Asignar número SIGO sin datos obligatorios mantiene BORRADOR; al actualizar datos pasa a ABIERTA")
    void asignarNumeroSigo_sinDatosObligatorios_mantieneBorradorHastaCompletar() throws Exception {
        // Creamos sin delito ni gravedad
        String respuesta = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "resumen": "Borrador preliminar sin delito asignado"
                                }
                                """)
                        .with(tramitador()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(respuesta, "$.id")).longValue();

        // Asignamos el SIGO: se guarda, pero sigue en BORRADOR
        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_1)
                        .with(tramitador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.numeroExpediente").value(SIGO_VALIDO_1));

        // Ahora completamos el delito y gravedad vía PUT -> debe pasar automáticamente a ABIERTA
        mockMvc.perform(put("/api/diligencias/" + id)
                        .contentType("application/json")
                        .content("""
                                {
                                  "delitoPrincipal": "Estafa informática",
                                  "gravedadDelito": "GRAVE",
                                  "resumen": "Borrador ya tipificado"
                                }
                                """)
                        .with(tramitador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.numeroExpediente").value(SIGO_VALIDO_1))
                .andExpect(jsonPath("$.aniosRetencionLegal").value(10));
    }

    @Test
    @DisplayName("Asignar número SIGO duplicado devuelve 409 Conflict")
    void asignarNumeroSigo_duplicado_devuelve409() throws Exception {
        long id1 = crearBorrador("Delito 1", "LEVE");
        long id2 = crearBorrador("Delito 2", "GRAVE");

        mockMvc.perform(patch("/api/diligencias/" + id1 + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_1)
                        .with(tramitador()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/diligencias/" + id2 + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_1)
                        .with(tramitador()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Asignar número SIGO a un expediente ya ABIERTO devuelve 422")
    void asignarNumeroSigo_expedienteYaAbierto_devuelve422() throws Exception {
        long id = crearBorrador("Homicidio", "MUY_GRAVE");

        // Pasa a ABIERTA
        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_1)
                        .with(tramitador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));

        // Intentar reasignar SIGO cuando ya no es BORRADOR
        mockMvc.perform(patch("/api/diligencias/" + id + "/numero-sigo")
                        .param("numeroSigo", SIGO_VALIDO_2)
                        .with(tramitador()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Transición directa BORRADOR -> ABIERTA sin pasar por numero-sigo devuelve 422")
    void transicionDirecta_borradorAAbierta_devuelve422() throws Exception {
        long id = crearBorrador("Delito", "GRAVE");

        mockMvc.perform(patch("/api/diligencias/" + id + "/estado")
                        .param("estado", "ABIERTA")
                        .with(tramitador()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Transición ilegal BORRADOR -> EN_TRAMITACION devuelve 422")
    void transicionIlegal_borradorAEnTramitacion_devuelve422() throws Exception {
        long id = crearBorrador("Delito", "GRAVE");

        mockMvc.perform(patch("/api/diligencias/" + id + "/estado")
                        .param("estado", "EN_TRAMITACION")
                        .with(tramitador()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Cancelar borrador BORRADOR -> CANCELADA está permitido")
    void cancelarBorrador_permitido() throws Exception {
        long id = crearBorrador("Delito cancelado", "LEVE");

        mockMvc.perform(patch("/api/diligencias/" + id + "/estado")
                        .param("estado", "CANCELADA")
                        .with(tramitador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("Eliminar físicamente un expediente en BORRADOR está bloqueado (422)")
    void eliminarBorradorFisico_bloqueado422() throws Exception {
        // Con usuario ADMIN_UNIDAD que tiene PUEDE_ELIMINAR
        JwtRequestPostProcessor adminUnidad = jwt()
                .jwt(token -> token
                        .claim("preferred_username", "B10002A")
                        .claim("unidad", UNIDAD)
                        .claim("resource_access", Map.of("diligencias-backend", Map.of("roles", List.of("ADMIN_UNIDAD")))))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_UNIDAD"));

        long id = crearBorrador("Delito no borrable", "LEVE");

        mockMvc.perform(delete("/api/diligencias/" + id)
                        .with(adminUnidad))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Remisión de documento bloqueada si el expediente está en BORRADOR (422)")
    void remisionDocumento_bloqueadaEnBorrador_devuelve422() throws Exception {
        long id = crearBorrador("Delito preliminar", "GRAVE");

        // Subir documento al borrador sí está permitido
        String respDoc = mockMvc.perform(multipart("/api/diligencias/" + id + "/documentos")
                        .file(new MockMultipartFile("archivo", "test.txt", "text/plain", "data".getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("datos", "", "application/json", "{\"tituloDocumento\":\"Doc test\",\"destino\":\"INTERNO_ALFRESCO\",\"estadoDocumento\":\"BORRADOR\"}".getBytes(StandardCharsets.UTF_8)))
                        .with(tramitador()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long documentoId = ((Number) JsonPath.read(respDoc, "$.id")).longValue();

        // Intentar remitir el documento
        mockMvc.perform(post("/api/documentos/" + documentoId + "/remisiones")
                        .contentType("application/json")
                        .content("{\"destino\":\"JUZGADO\",\"tipoRemision\":\"SIMULADA\"}")
                        .with(tramitador()))
                .andExpect(status().isUnprocessableEntity());
    }

    private long crearBorrador(String delito, String gravedad) throws Exception {
        String respuesta = mockMvc.perform(post("/api/diligencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "delitoPrincipal": "%s",
                                  "gravedadDelito": "%s",
                                  "resumen": "Resumen borrador"
                                }
                                """.formatted(delito, gravedad))
                        .with(tramitador()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }
}
