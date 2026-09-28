package es.guardiacivil.diligencias.tarea.dto;

import java.time.Instant;

public record TareaExternaDTO(
    String idTareaExterna,
    String titulo,
    String codigoUnidad,
    String tipAgente,
    String gravedad,
    String resumen,
    Instant fechaCreacion,
    String estado
) {}
