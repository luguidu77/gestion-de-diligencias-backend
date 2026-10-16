package es.guardiacivil.diligencias.controller;

import es.guardiacivil.diligencias.requerimiento.controller.IntegracionJudicialController;
import es.guardiacivil.diligencias.requerimiento.controller.RequerimientoJudicialController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JudicialIntegrationDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void controladorExternoDesactivadoPorDefecto() {
        assertThat(environment.getProperty(
                "app.integraciones.judicial.enabled", Boolean.class, false))
                .isFalse();

        assertThat(context.getBeansOfType(IntegracionJudicialController.class))
                .isEmpty();

        assertThat(context.getBeansOfType(RequerimientoJudicialController.class))
                .hasSize(1);
    }

    @Test
    void noSeRegistranRutasExternasPeroSeConservaLaInterna() {
        RequestMappingHandlerMapping mapping = context.getBean(
                "requestMappingHandlerMapping",
                RequestMappingHandlerMapping.class);

        var rutas = mapping.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternValues().stream())
                .toList();

        assertThat(rutas)
                .noneMatch(ruta -> ruta.startsWith("/api/integraciones/judicial"));

        assertThat(rutas)
                .contains("/api/diligencias/{numeroDiligencia}/requerimientos");
    }

    @Test
    void openApiNoPublicaEndpointsExternos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/integraciones/judicial/requerimientos']")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/integraciones/judicial/diligencias/{referenciaJudicial}/estado']")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/integraciones/judicial/requerimientos/{id}']")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/diligencias/{numeroDiligencia}/requerimientos']")
                        .exists());
    }

    @Test
    void swaggerNoOfreceElGrupoJudicial() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[*].name",
                        not(hasItem("integracion-judicial"))))
                .andExpect(jsonPath("$.urls[*].name",
                        hasItem("api-interna")));
    }
}
