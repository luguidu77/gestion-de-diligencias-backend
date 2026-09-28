package es.guardiacivil.diligencias.diligencia.dto;

import es.guardiacivil.diligencias.diligencia.entity.GravedadDelito;
import lombok.Data;

@Data
public class DiligenciaCreateRequest {
    private String delitoPrincipal;
    private GravedadDelito gravedadDelito;
    private String resumen;
}
