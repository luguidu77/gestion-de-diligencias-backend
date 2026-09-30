package es.guardiacivil.diligencias.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportacionTareasScheduler {

    private final JobOperator jobOperator;
    private final Job importacionTareasJob;

    /**
     * Sincronización periódica programada.
     * Por defecto cada 15 minutos en laboratorio o según propiedad dilgencias.batch.cron.
     */
    @Scheduled(cron = "${diligencias.batch.cron:0 */15 * * * ?}")
    public void ejecutarSincronizacionBatch() {
        log.info("Lanzando sincronización periódica programada con Spring Batch...");
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobOperator.start(importacionTareasJob, params);
        } catch (Exception ex) {
            log.error("Error al ejecutar el Job de sincronización programada: {}", ex.getMessage(), ex);
        }
    }
}
