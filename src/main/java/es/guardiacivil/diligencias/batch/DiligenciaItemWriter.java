package es.guardiacivil.diligencias.batch;

import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.diligencia.repository.DiligenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiligenciaItemWriter implements ItemWriter<Diligencia> {

    private final DiligenciaRepository diligenciaRepository;

    @Override
    public void write(Chunk<? extends Diligencia> chunk) {
        log.info("ItemWriter: Guardando lote de {} diligencias en PostgreSQL...", chunk.size());
        diligenciaRepository.saveAll(chunk.getItems());
    }
}
