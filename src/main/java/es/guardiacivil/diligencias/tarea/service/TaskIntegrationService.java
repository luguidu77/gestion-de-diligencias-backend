package es.guardiacivil.diligencias.tarea.service;

import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;
import es.guardiacivil.diligencias.tarea.port.TaskIntegrationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ====================================================================
 *  Servicio de Orquestación — Integración con App Externa de Tareas
 * ====================================================================
 *
 * Capa de aplicación que coordina la lógica de negocio relacionada con
 * tareas. Delega en el puerto de salida {@link TaskIntegrationPort} para
 * la comunicación real con la app externa (Jira, Wekan, app GC, etc.).
 *
 * Esta clase NO conoce ni le importa qué app de tareas hay detrás.
 * Solo trabaja con la interfaz genérica TaskIntegrationPort.
 * El adaptador concreto (@Profile lab/prod) se inyecta automáticamente
 * por Spring Boot (Inversión de Control).
 *
 * Uso típico desde otros servicios:
 * <pre>
 *   // En DiligenciaService, al asignar un agente a una diligencia:
 *   taskIntegrationService.notificarAsignacion(diligencia, agente);
 * </pre>
 */
@Service
public class TaskIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(TaskIntegrationService.class);

    // Spring inyecta aquí el adaptador activo según el perfil (lab o prod)
    private final TaskIntegrationPort taskPort;

    public TaskIntegrationService(TaskIntegrationPort taskPort) {
        this.taskPort = taskPort;
    }

    /**
     * Notifica la asignación de un expediente a un agente creando una tarea
     * en la app externa. Se invoca desde DiligenciaService tras asignar agente.
     *
     * @param request Solicitud de tarea con datos del expediente y agente
     * @return ID externo de la tarea creada (se puede persistir en outbox_eventos)
     */
    @Transactional
    public String notificarAsignacion(SolicitudTareaRequest request) {
        log.info(
            "Notificando asignación al agente {} para expediente {}",
            request.tipAgente(),
            request.numeroExpediente()
        );

        String tareaExternaId = taskPort.crearTarea(request);

        log.info(
            "Asignación notificada correctamente — Tarea externa creada con ID: {}",
            tareaExternaId
        );

        // TODO (Fase 5): Persistir tareaExternaId en outbox_eventos para garantía de entrega
        // outboxRepository.save(new OutboxEvento(...));

        return tareaExternaId;
    }

    /**
     * Sincroniza el estado de una tarea existente en la app externa.
     * Se invoca cuando la diligencia cambia de estado (ARCHIVADA, CANCELADA, etc.)
     *
     * @param tareaExternaId ID de la tarea en la app externa
     * @param nuevoEstado    Nuevo estado a propagar (COMPLETADA, CANCELADA, etc.)
     */
    public void sincronizarEstado(String tareaExternaId, String nuevoEstado) {
        log.info("Sincronizando estado '{}' para tarea externa {}", nuevoEstado, tareaExternaId);
        taskPort.actualizarEstadoTarea(tareaExternaId, nuevoEstado);
    }

    /**
     * Consulta las tareas pendientes de un agente por su TIP.
     * Expuesto al Frontend Angular a través de un endpoint REST en TaskController.
     *
     * @param tip TIP oficial del agente (Ej: 'S11223A')
     * @return Lista de tareas del agente (vacía si no tiene ninguna)
     */
    public List<TareaResponse> obtenerTareasPorAgente(String tip) {
        log.info("Consultando tareas pendientes para agente TIP: {}", tip);
        return taskPort.consultarTareasPorAgente(tip);
    }
}
