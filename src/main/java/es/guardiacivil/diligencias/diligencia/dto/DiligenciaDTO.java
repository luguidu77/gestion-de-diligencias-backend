package es.guardiacivil.diligencias.diligencia.dto;

import es.guardiacivil.diligencias.diligencia.entity.EstadoDiligencia;
import es.guardiacivil.diligencias.diligencia.entity.GravedadDelito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiligenciaDTO {
    private Long id;
    private String numeroExpediente;
    private Long unidadId;
    private Long agenteInstructorId;
    private Long agenteSecretarioId;
    private EstadoDiligencia estado;
    private String delitoPrincipal;
    private GravedadDelito gravedadDelito;
    private Integer aniosRetencionLegal;
    private LocalDate fechaExpiracionRetencion;
    private String resumen;
}
