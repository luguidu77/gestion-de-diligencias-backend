package es.guardiacivil.diligencias.tarea.controller;

import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;
import es.guardiacivil.diligencias.tarea.service.TaskIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ====================================================================
 *  REST Controller — Integración con App Externa de Tareas
 * ====================================================================
 *
 * Expone los endpoints de tareas al Frontend Angular.
 * Todos los endpoints requieren autenticación JWT (Bearer Token de Keycloak).
 *
 * Base URL: /api/v1/tareas
 *
 * Seguridad aplicada por método con @PreAuthorize para control ABAC/RBAC.
 */
@RestController
@RequestMapping("/api/v1/tareas")
public class TaskController {

    private final TaskIntegrationService taskIntegrationService;

    public TaskController(TaskIntegrationService taskIntegrationService) {
        this.taskIntegrationService = taskIntegrationService;
    }

    /**
     * GET /api/v1/tareas/agente/{tip}
     *
     * Devuelve las tareas pendientes de un agente identificado por su TIP.
     * El agente solo puede consultar sus propias tareas.
     *
     * Ejemplo: GET /api/v1/tareas/agente/S11223A
     */
    @GetMapping("/agente/{tip}")
    @PreAuthorize("hasRole('AGENTE') or hasRole('INSTRUCTOR') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<TareaResponse>> obtenerTareasPorAgente(@PathVariable String tip) {
        List<TareaResponse> tareas = taskIntegrationService.obtenerTareasPorAgente(tip);
        return ResponseEntity.ok(tareas);
    }

    /**
     * POST /api/v1/tareas
     *
     * Crea una nueva tarea en la app externa de tareas vinculada a un expediente.
     * Restringido a roles con permisos de gestión (INSTRUCTOR / SUPERVISOR / ADMIN).
     *
     * Body JSON:
     * {
     *   "numeroExpediente": "2026-001755-0001442",
     *   "tipAgente": "S11223A",
     *   "empleoAgente": "Sargento",
     *   "descripcion": "Realizar inspección ocular del vehículo.",
     *   "prioridad": "ALTA",
     *   "fechaLimite": "2026-08-01T09:00:00",
     *   "urlExpediente": "http://localhost:8080/api/v1/diligencias/2026-001755-0001442"
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<String> crearTarea(@RequestBody SolicitudTareaRequest request) {
        String tareaExternaId = taskIntegrationService.notificarAsignacion(request);
        return ResponseEntity.ok(tareaExternaId);
    }

    /**
     * PATCH /api/v1/tareas/{tareaId}/estado
     *
     * Actualiza el estado de una tarea existente en la app externa.
     * Requiere rol SUPERVISOR o ADMIN.
     *
     * Ejemplo: PATCH /api/v1/tareas/TASK-SIM-1001/estado?nuevoEstado=COMPLETADA
     */
    @PatchMapping("/{tareaId}/estado")
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable String tareaId,
            @RequestParam String nuevoEstado) {
        taskIntegrationService.sincronizarEstado(tareaId, nuevoEstado);
        return ResponseEntity.noContent().build();
    }
}
