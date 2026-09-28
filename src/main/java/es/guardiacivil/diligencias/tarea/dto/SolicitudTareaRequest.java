package es.guardiacivil.diligencias.tarea.dto;

import java.time.LocalDateTime;

/**
 * DTO de Solicitud — Datos enviados al crear una nueva tarea en la app externa.
 *
 * Se construye en el servicio de aplicación (TaskIntegrationService) y se pasa
 * al adaptador concreto que implementa TaskIntegrationPort.
 *
 * @param numeroExpediente  Número oficial de la diligencia (Ej: '2026-001755-0001442')
 * @param tipAgente         TIP del agente responsable de la tarea (Ej: 'S11223A')
 * @param empleoAgente      Empleo / Rango del agente (Ej: 'Sargento') — para cabecera de tarea
 * @param descripcion       Descripción de la tarea a realizar (ej: 'Realizar inspección ocular')
 * @param prioridad         Nivel de prioridad: BAJA, MEDIA, ALTA, URGENTE
 * @param fechaLimite       Fecha límite de realización (puede ser null si no aplica)
 * @param urlExpediente     URL directa al expediente en GestionDiligencias (trazabilidad)
 */
public record SolicitudTareaRequest(
        String numeroExpediente,
        String tipAgente,
        String empleoAgente,
        String descripcion,
        String prioridad,
        LocalDateTime fechaLimite,
        String urlExpediente
) {}
