package es.guardiacivil.diligencias.diligencia.dto;

import es.guardiacivil.diligencias.diligencia.entity.TipoActuacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActuacionDTO {
    private Long id;
    private Long diligenciaId;
    private Integer numeroOrden;
    private String titulo;
    private TipoActuacion tipoActuacion;
    private String lugar;
    private String resumen;
    private Long usuarioCreadorId;
    private LocalDateTime fechaActuacion;
}
