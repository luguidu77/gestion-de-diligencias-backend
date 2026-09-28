package es.guardiacivil.diligencias.batch;

import es.guardiacivil.diligencias.tarea.dto.TareaExternaDTO;
import es.guardiacivil.diligencias.batch.entity.SincronizacionBatch;
import es.guardiacivil.diligencias.batch.repository.SincronizacionBatchRepository;
import es.guardiacivil.diligencias.service.batch.ClienteTareaExternaWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TareaExternaItemReader implements ItemReader<TareaExternaDTO> {

    private final ClienteTareaExternaWebClient clienteWebClient;
    private final SincronizacionBatchRepository sincronizacionBatchRepository;

    private Iterator<TareaExternaDTO> iterator;
    private boolean inicializado = false;

    @Override
    public TareaExternaDTO read() {
        if (!inicializado) {
            Instant fechaUltimaImportacion = sincronizacionBatchRepository
                    .findByNombreProceso("IMPORTACION_TAREAS_EXTERNAS")
                    .map(SincronizacionBatch::getFechaUltimaImportacion)
                    .orElseGet(() -> Instant.now().minusSeconds(86400));

            List<TareaExternaDTO> tareas = clienteWebClient.consumirTareasDesde(fechaUltimaImportacion);
            log.info("ItemReader: Se leyeron {} tareas externas de la API.", tareas.size());
            this.iterator = tareas.iterator();
            this.inicializado = true;
        }

        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }

        // Reiniciar para la siguiente ejecución del Job
        this.inicializado = false;
        return null;
    }
}
