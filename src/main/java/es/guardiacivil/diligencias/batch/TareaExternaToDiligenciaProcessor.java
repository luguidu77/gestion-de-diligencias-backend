package es.guardiacivil.diligencias.batch;

import es.guardiacivil.diligencias.tarea.dto.TareaExternaDTO;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.diligencia.entity.EstadoDiligencia;
import es.guardiacivil.diligencias.diligencia.entity.GravedadDelito;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class TareaExternaToDiligenciaProcessor implements ItemProcessor<TareaExternaDTO, Diligencia> {

    @Override
    public Diligencia process(TareaExternaDTO item) throws Exception {
        Diligencia diligencia = new Diligencia();
        diligencia.setNumeroExpediente("EXT-" + item.idTareaExterna());
        diligencia.setDelitoPrincipal(item.titulo());
        diligencia.setResumen(item.resumen());
        diligencia.setEstado(EstadoDiligencia.ABIERTA);
        // We set dummy values to bypass constraints for now
        diligencia.setGravedadDelito(GravedadDelito.GRAVE);
        diligencia.setAniosRetencionLegal(10);
        diligencia.setFechaExpiracionRetencion(java.time.LocalDate.now().plusYears(10));
        return diligencia;
    }
}
