package es.guardiacivil.diligencias.tarea.port;

import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;

import java.util.List;

/**
 * Puerto de Salida (Output Port) — Patrón Arquitectura Hexagonal.
 *
 * Define el contrato genérico de integración con una aplicación externa de tareas
 * (Jira, Wekan, Redmine, app propia GC, etc.).
 *
 * El núcleo de negocio (servicios y controladores Spring) trabaja únicamente
 * contra esta interfaz, sin conocer jamás la implementación concreta.
 *
 * Adaptadores disponibles:
 *  - @Profile("lab")  → SimuladorTaskServiceAdapter  (Laboratorio FP)
 *  - @Profile("prod") → [Pendiente según app elegida] (Producción)
 */
public interface TaskIntegrationPort {

    /**
     * Crea una tarea en la aplicación externa cuando se asigna una diligencia
     * o se genera una actuación que requiere intervención de un agente.
     *
     * @param request Datos de la tarea a crear (expediente, agente, descripción, prioridad)
     * @return Identificador externo de la tarea creada (para trazabilidad y seguimiento)
     */
    String crearTarea(SolicitudTareaRequest request);

    /**
     * Actualiza el estado de una tarea ya existente en la aplicación externa.
     * Se invoca, por ejemplo, cuando la diligencia cambia de estado o el trámite
     * es completado / cancelado.
     *
     * @param tareaExternaId Identificador de la tarea en la app externa
     * @param nuevoEstado    Estado a establecer (PENDIENTE, EN_PROCESO, COMPLETADA, CANCELADA)
     */
    void actualizarEstadoTarea(String tareaExternaId, String nuevoEstado);

    /**
     * Obtiene el listado de tareas pendientes asignadas a un agente concreto,
     * identificado por su TIP oficial (Ej: 'S11223A').
     *
     * Permite al agente consultar sus tareas desde la propia aplicación GestionDiligencias
     * sin necesidad de abrir la app de tareas externa.
     *
     * @param tip TIP oficial del agente (formato: 1 Letra + 5 Dígitos + 1 Letra)
     * @return Lista de tareas asignadas al agente (puede estar vacía, nunca null)
     */
    List<TareaResponse> consultarTareasPorAgente(String tip);
}
