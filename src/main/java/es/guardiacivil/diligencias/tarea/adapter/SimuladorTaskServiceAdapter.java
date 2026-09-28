package es.guardiacivil.diligencias.tarea.adapter;

import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;
import es.guardiacivil.diligencias.tarea.port.TaskIntegrationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ====================================================================
 *  ADAPTADOR DE LABORATORIO — Simulador de App Externa de Tareas
 * ====================================================================
 *
 * Implementa {@link TaskIntegrationPort} para el entorno de laboratorio FP
 * (@Profile "lab"). NO realiza ninguna llamada HTTP real.
 *
 * Simula el comportamiento de la app de tareas con respuestas ficticias
 * coherentes, permitiendo desarrollar y probar todo el flujo de asignación
 * de tareas sin depender de ninguna app externa instalada.
 *
 * Al pasar a producción, este adaptador es sustituido automáticamente por
 * el adaptador real (@Profile "prod") sin modificar ninguna otra clase.
 *
 * Activación: spring.profiles.active=lab  (application-lab.yml)
 */
@Service
@Profile({"lab", "test", "local"})
public class SimuladorTaskServiceAdapter implements TaskIntegrationPort {

    private static final Logger log = LoggerFactory.getLogger(SimuladorTaskServiceAdapter.class);

    // Contador incremental para generar IDs simulados únicos por cada tarea creada
    private final AtomicLong contadorSimulado = new AtomicLong(1000L);

    /**
     * Simula la creación de una tarea en la app externa.
     * En producción, aquí se haría una llamada HTTP REST a la API real (Jira, Wekan, etc.)
     */
    @Override
    public String crearTarea(SolicitudTareaRequest request) {
        String idSimulado = "TASK-SIM-" + contadorSimulado.getAndIncrement();

        log.info(
            "[SIMULADOR TAREAS] ✅ Tarea creada — ID: {} | Expediente: {} | Agente: {} ({}) | Prioridad: {}",
            idSimulado,
            request.numeroExpediente(),
            request.tipAgente(),
            request.empleoAgente(),
            request.prioridad()
        );

        return idSimulado;
    }

    /**
     * Simula la actualización de estado de una tarea existente.
     * En producción, aquí se haría un PATCH/PUT a la API real.
     */
    @Override
    public void actualizarEstadoTarea(String tareaExternaId, String nuevoEstado) {
        log.info(
            "[SIMULADOR TAREAS] 🔄 Estado actualizado — Tarea: {} → Nuevo estado: {}",
            tareaExternaId,
            nuevoEstado
        );
    }

    /**
     * Simula la consulta de tareas pendientes de un agente.
     * Devuelve una lista ficticia de 2 tareas de ejemplo para pruebas de frontend.
     */
    @Override
    public List<TareaResponse> consultarTareasPorAgente(String tip) {
        log.info("[SIMULADOR TAREAS] 🔍 Consultando tareas del agente TIP: {}", tip);

        return List.of(
            new TareaResponse(
                "TASK-SIM-1001",
                "2026-001755-0001442",
                "Realizar inspección ocular del vehículo. Localización: Polígono Industrial Norte.",
                "PENDIENTE",
                "ALTA",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusDays(1),
                "http://localhost:8080/api/v1/tareas/TASK-SIM-1001"
            ),
            new TareaResponse(
                "TASK-SIM-1002",
                "2026-001755-0001443",
                "Recopilar documentación bancaria para informe pericial.",
                "EN_PROCESO",
                "MEDIA",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(3),
                "http://localhost:8080/api/v1/tareas/TASK-SIM-1002"
            )
        );
    }
}
