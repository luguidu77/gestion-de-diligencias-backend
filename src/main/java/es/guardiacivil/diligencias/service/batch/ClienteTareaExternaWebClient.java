package es.guardiacivil.diligencias.service.batch;

import es.guardiacivil.diligencias.tarea.dto.TareaExternaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Cliente HTTP basado en Spring WebClient para consumir la API externa de tareas.
 */
@Service
@Slf4j
public class ClienteTareaExternaWebClient {

    private final WebClient webClient;

    public ClienteTareaExternaWebClient(
            WebClient.Builder webClientBuilder,
            @Value("${diligencias.batch.api-externa.url:http://localhost:8080/api/simulador/tareas-externas}") String apiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(apiBaseUrl).build();
    }

    /**
     * Consume tareas de la API externa creadas o modificadas desde la fecha de última importación.
     */
    public List<TareaExternaDTO> consumirTareasDesde(Instant fechaUltimaImportacion) {
        log.info("Llamando a API externa de tareas mediante WebClient desde: {}", fechaUltimaImportacion);
        try {
            List<TareaExternaDTO> tareas = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("desde", fechaUltimaImportacion != null ? fechaUltimaImportacion.toString() : "")
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TareaExternaDTO>>() {})
                    .block();

            return tareas != null ? tareas : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Error al consumir tareas externas vía WebClient: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
