package es.guardiacivil.diligencias.requerimiento.service;

import es.guardiacivil.diligencias.requerimiento.dto.CrearRequerimientoJudicialRequest;
import es.guardiacivil.diligencias.diligencia.dto.EstadoDiligenciaJudicialResponse;
import es.guardiacivil.diligencias.requerimiento.dto.RequerimientoJudicialResponse;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.requerimiento.entity.EstadoRequerimientoJudicial;
import es.guardiacivil.diligencias.requerimiento.entity.RequerimientoJudicial;
import es.guardiacivil.diligencias.diligencia.repository.DiligenciaRepository;
import es.guardiacivil.diligencias.requerimiento.repository.RequerimientoJudicialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RequerimientoJudicialService {

    private final RequerimientoJudicialRepository requerimientoRepository;
    private final DiligenciaRepository diligenciaRepository;

    @Transactional(readOnly = true)
    public EstadoDiligenciaJudicialResponse consultarEstado(String numeroDiligencia, String identificador) {
        RequerimientoJudicial req = requerimientoRepository.findBySistemaOrigenAndIdentificadorExterno("SISTEMA_JUDICIAL", identificador)
                .orElseThrow(() -> new IllegalArgumentException("Requerimiento no encontrado"));
        return new EstadoDiligenciaJudicialResponse(
                req.getReferenciaJudicial(),
                req.getDiligencia().getNumeroExpediente(),
                req.getEstado().name(),
                req.getFechaModificacion() != null ? req.getFechaModificacion().toInstant(java.time.ZoneOffset.UTC) : req.getFechaCreacion().toInstant(java.time.ZoneOffset.UTC),
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public boolean existeRequerimiento(String identificador) {
        return requerimientoRepository.findBySistemaOrigenAndIdentificadorExterno("SISTEMA_JUDICIAL", identificador).isPresent();
    }

    @Transactional
    public RequerimientoJudicialResponse crearRequerimiento(CrearRequerimientoJudicialRequest request, String numeroDiligencia) {
        Diligencia diligencia = diligenciaRepository.findByNumeroExpediente(numeroDiligencia)
                .orElseThrow(() -> new IllegalArgumentException("Diligencia no encontrada"));

        RequerimientoJudicial req = RequerimientoJudicial.builder()
                .diligencia(diligencia)
                .referenciaJudicial(request.referenciaJudicial())
                .identificadorExterno(request.identificadorExterno())
                .sistemaOrigen("SISTEMA_JUDICIAL")
                .tipo(request.tipo())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad())
                .estado(EstadoRequerimientoJudicial.RECIBIDO)
                .fechaLimite(request.fechaLimite())
                .fechaRecepcion(Instant.now())
                .build();

        RequerimientoJudicial saved = requerimientoRepository.save(req);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public RequerimientoJudicialResponse consultarRequerimiento(Long id) {
        RequerimientoJudicial req = requerimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Requerimiento no encontrado"));
        return toDto(req);
    }

    private RequerimientoJudicialResponse toDto(RequerimientoJudicial req) {
        return new RequerimientoJudicialResponse(
                req.getId(),
                req.getReferenciaJudicial(),
                req.getDiligencia().getNumeroExpediente(),
                req.getTipo().name(),
                req.getDescripcion(),
                req.getPrioridad().name(),
                req.getEstado().name(),
                req.getFechaLimite(),
                req.getFechaRecepcion(),
                req.getFechaAsignacion(),
                req.getInstructorTip(),
                req.getUnidadResponsable(),
                req.getResultado(),
                req.getObservaciones()
        );
    }
}
