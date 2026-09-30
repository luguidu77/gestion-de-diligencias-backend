package es.guardiacivil.diligencias.batch;

import es.guardiacivil.diligencias.batch.entity.SincronizacionBatch;
import es.guardiacivil.diligencias.batch.repository.SincronizacionBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportacionBatchListener implements JobExecutionListener {

    private static final String PROCESO_NOMBRE = "IMPORTACION_TAREAS_EXTERNAS";
    private final SincronizacionBatchRepository sincronizacionBatchRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Iniciando Job de Importación Batch: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Instant ahora = Instant.now();
        boolean exito = jobExecution.getStatus() == BatchStatus.COMPLETED;

        SincronizacionBatch sincronizacion = sincronizacionBatchRepository
                .findByNombreProceso(PROCESO_NOMBRE)
                .orElseGet(() -> SincronizacionBatch.builder()
                        .nombreProceso(PROCESO_NOMBRE)
                        .fechaUltimaImportacion(ahora)
                        .registrosProcesados(0)
                        .estado("EN_PROCESO")
                        .fechaInicio(ahora)
                        .build());

        sincronizacion.setFechaFin(ahora);

        if (exito) {
            long totalReadCount = jobExecution.getStepExecutions().stream()
                    .mapToLong(stepExecution -> stepExecution.getWriteCount())
                    .sum();

            sincronizacion.setFechaUltimaImportacion(ahora);
            sincronizacion.setRegistrosProcesados((int) totalReadCount);
            sincronizacion.setEstado("EXITO");
            sincronizacion.setMensajeError(null);
            log.info("Job completado con ÉXITO. Se procesaron {} registros. Nueva marca de agua: {}", totalReadCount, ahora);
        } else {
            sincronizacion.setEstado("ERROR");
            sincronizacion.setMensajeError(jobExecution.getAllFailureExceptions().toString());
            log.error("Job finalizado con ERROR: {}", jobExecution.getAllFailureExceptions());
        }

        sincronizacionBatchRepository.save(sincronizacion);
    }
}
