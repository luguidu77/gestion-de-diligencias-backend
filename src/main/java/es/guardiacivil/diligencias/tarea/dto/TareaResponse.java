package es.guardiacivil.diligencias.tarea.dto;

import java.time.LocalDateTime;

/**
 * DTO de Respuesta — Representa una tarea obtenida desde la app externa.
 *
 * Se devuelve por {@code TaskIntegrationPort#consultarTareasPorAgente(String)}
 * y se puede exponer directamente desde un controlador REST al Frontend Angular.
 *
 * @param tareaExternaId    Identificador único de la tarea en la app externa
 * @param numeroExpediente  Número oficial de la diligencia vinculada (Ej: '2026-001755-0001442')
 * @param descripcion       Descripción de la tarea asignada al agente
 * @param estado            Estado actual: PENDIENTE, EN_PROCESO, COMPLETADA, CANCELADA
 * @param prioridad         Prioridad: BAJA, MEDIA, ALTA, URGENTE
 * @param fechaCreacion     Timestamp de creación de la tarea
 * @param fechaLimite       Fecha límite de realización (puede ser null)
 * @param urlTareaExterna   URL directa a la tarea en la app externa (deep link)
 */
public record TareaResponse(
        String tareaExternaId,
        String numeroExpediente,
        String descripcion,
        String estado,
        String prioridad,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaLimite,
        String urlTareaExterna
) {}
