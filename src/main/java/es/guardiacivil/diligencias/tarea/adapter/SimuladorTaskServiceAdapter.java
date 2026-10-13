package es.guardiacivil.diligencias.tarea.adapter;

import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;
import es.guardiacivil.diligencias.tarea.port.TaskIntegrationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulador local de tareas.
 * Conserva los datos en memoria hasta reiniciar la aplicación.
 */
@Service
@Profile({"lab", "test", "local"})
public class SimuladorTaskServiceAdapter implements TaskIntegrationPort {

    private record TareaAsignada(String tip, TareaResponse tarea) {}

    private final AtomicLong contador = new AtomicLong(1000L);
    private final ConcurrentMap<String, TareaAsignada> tareas =
            new ConcurrentHashMap<>();

    @Override
    public String crearTarea(SolicitudTareaRequest request) {
        String id = "TASK-SIM-" + contador.getAndIncrement();

        TareaResponse tarea = new TareaResponse(
                id,
                request.numeroExpediente(),
                request.descripcion(),
                "PENDIENTE",
                request.prioridad(),
                LocalDateTime.now(),
                request.fechaLimite(),
                "/api/v1/tareas/" + id
        );

        tareas.put(id, new TareaAsignada(request.tipAgente(), tarea));
        return id;
    }

    @Override
    public Optional<TareaResponse> consultarTareaPorId(String tareaExternaId) {
        return Optional.ofNullable(tareas.get(tareaExternaId))
                .map(TareaAsignada::tarea);
    }

    @Override
    public void actualizarEstadoTarea(String tareaExternaId, String nuevoEstado) {
        tareas.compute(tareaExternaId, (id, asignada) -> {
            if (asignada == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tarea no encontrada");
            }

            TareaResponse anterior = asignada.tarea();
            TareaResponse actualizada = new TareaResponse(
                    anterior.tareaExternaId(),
                    anterior.numeroExpediente(),
                    anterior.descripcion(),
                    nuevoEstado,
                    anterior.prioridad(),
                    anterior.fechaCreacion(),
                    anterior.fechaLimite(),
                    anterior.urlTareaExterna()
            );

            return new TareaAsignada(asignada.tip(), actualizada);
        });
    }

    @Override
    public List<TareaResponse> consultarTareasPorAgente(String tip) {
        return tareas.values().stream()
                .filter(asignada -> asignada.tip().equalsIgnoreCase(tip))
                .map(TareaAsignada::tarea)
                .sorted(Comparator.comparing(TareaResponse::tareaExternaId))
                .toList();
    }
}
