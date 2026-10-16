package es.guardiacivil.diligencias.diligencia.entity;

public enum EstadoDiligencia {
    /** Expediente creado sin número SIGO. No puede recibir remisiones ni requerimientos. */
    BORRADOR,
    ABIERTA,
    EN_TRAMITACION,
    PENDIENTE_REVISION,
    REMITIDA_JUZGADO,
    ARCHIVADA,
    CANCELADA
}
