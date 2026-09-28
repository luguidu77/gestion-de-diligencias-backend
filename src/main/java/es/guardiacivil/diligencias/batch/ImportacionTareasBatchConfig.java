package es.guardiacivil.diligencias.batch;

import es.guardiacivil.diligencias.tarea.dto.TareaExternaDTO;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ImportacionTareasBatchConfig {

    private final TareaExternaItemReader reader;
    private final TareaExternaToDiligenciaProcessor processor;
    private final DiligenciaItemWriter writer;
    private final ImportacionBatchListener listener;

    @Bean
    public Step importacionTareasStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("importacionTareasStep", jobRepository)
                .<TareaExternaDTO, Diligencia>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importacionTareasJob(JobRepository jobRepository, Step importacionTareasStep) {
        return new JobBuilder("importacionTareasJob", jobRepository)
                .listener(listener)
                .start(importacionTareasStep)
                .build();
    }
}
