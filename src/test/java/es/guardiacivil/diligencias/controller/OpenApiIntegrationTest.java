package es.guardiacivil.diligencias.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Swagger UI disponible públicamente sin autenticación en entorno de test/lab")
    void swaggerUiDisponibleEnLaboratorio() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }

    @Test
    @DisplayName("2. Especificación OpenAPI JSON disponible en /v3/api-docs")
    void apiDocsJsonDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("API de Gestión de Diligencias Policiales"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    @DisplayName("3. Especificación OpenAPI YAML disponible en /v3/api-docs.yaml")
    void apiDocsYamlDisponible() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi:")))
                .andExpect(content().string(containsString("Diligencias Policiales")));
    }

    @Test
    @DisplayName("4. Grupo API Interna expone endpoints funcionales y excluye integración judicial")
    void apiDocsGrupoInterno() throws Exception {
        mockMvc.perform(get("/v3/api-docs/api-interna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/diligencias']").exists())
                .andExpect(jsonPath("$.paths['/api/diligencias/{diligenciaId}/documentos']").exists())
                .andExpect(jsonPath("$.paths['/api/integraciones/judicial/requerimientos']").doesNotExist());
    }

    @Test
    @DisplayName("5. Grupo Integración Judicial expone solo endpoints de la API judicial")
    void apiDocsGrupoJudicial() throws Exception {
        mockMvc.perform(get("/v3/api-docs/integracion-judicial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/integraciones/judicial/requerimientos']").exists())
                .andExpect(jsonPath("$.paths['/api/integraciones/judicial/diligencias/{referenciaJudicial}/estado']").exists())
                .andExpect(jsonPath("$.paths['/api/diligencias']").doesNotExist());
    }

    @Test
    @DisplayName("6. Seguridad perimetral: abrir Swagger NO relaja la seguridad de los endpoints funcionales (401 sin JWT)")
    void endpointFuncionalSigueExigiendoJwt() throws Exception {
        mockMvc.perform(get("/api/diligencias"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/integraciones/judicial/diligencias/NIG-123/estado"))
                .andExpect(status().isUnauthorized());
    }
}
