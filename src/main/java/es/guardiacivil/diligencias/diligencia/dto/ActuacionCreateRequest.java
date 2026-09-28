package es.guardiacivil.diligencias.diligencia.dto;

import es.guardiacivil.diligencias.diligencia.entity.TipoActuacion;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActuacionCreateRequest {
    private String titulo;
    private TipoActuacion tipoActuacion;
    private String lugar;
    private String resumen;
    private LocalDateTime fechaActuacion;
}
