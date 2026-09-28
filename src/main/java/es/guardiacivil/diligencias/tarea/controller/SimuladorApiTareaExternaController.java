package es.guardiacivil.diligencias.tarea.controller;

import es.guardiacivil.diligencias.tarea.dto.TareaExternaDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Controlador que simula una API externa de tareas/atestados (Jira, Trello, App Externa).
 * Activo únicamente en el perfil de laboratorio ({@code @Profile("lab")}).
 */
@RestController
@RequestMapping("/api/simulador/tareas-externas")
@Profile("lab")
public class SimuladorApiTareaExternaController {

    @GetMapping
    public ResponseEntity<List<TareaExternaDTO>> listarTareasExternasPendientes(
            @RequestParam(name = "desde", required = false) String desdeStr) {

        Instant desde = (desdeStr != null && !desdeStr.isBlank()) 
                ? Instant.parse(desdeStr) 
                : Instant.now().minusSeconds(86400);

        List<TareaExternaDTO> tareasSimuladas = List.of(
            new TareaExternaDTO(
                "EXT-" + Instant.now().toEpochMilli() + "-01",
                "Diligencia de investigación por robo con fuerza en nalle comercial",
                "001755",
                "S11223A",
                "GRAVE",
                "Robo reportado en almacén central con rotura de cierre.",
                desde.plusSeconds(3600),
                "PENDIENTE_IMPORTACION"
            ),
            new TareaExternaDTO(
                "EXT-" + Instant.now().toEpochMilli() + "-02",
                "Acta de inspección ocular en siniestro vial en A-6 Km 422",
                "001755",
                "C44556B",
                "LEVE",
                "Colisión por alcance con daños materiales.",
                desde.plusSeconds(7200),
                "PENDIENTE_IMPORTACION"
            )
        );

        return ResponseEntity.ok(tareasSimuladas);
    }
}
