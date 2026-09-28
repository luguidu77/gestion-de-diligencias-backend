-- Búsqueda de documentos por diligencia.
CREATE INDEX idx_documento_diligencia
    ON documentos_metadata (diligencia_id);

-- Búsqueda de documentos por actuación.
CREATE INDEX idx_documento_actuacion
    ON documentos_metadata (actuacion_id)
    WHERE actuacion_id IS NOT NULL;

-- Búsqueda de remisiones por documento.
CREATE INDEX idx_remision_documento
    ON remisiones_documento (documento_id);

-- Worker de remisiones pendientes.
CREATE INDEX idx_remision_estado_reintento
    ON remisiones_documento (
        estado,
        proximo_reintento,
        fecha_creacion
    )
    WHERE estado IN ('PENDIENTE', 'ERROR_REINTENTABLE');

-- Requerimientos de una diligencia por fecha.
CREATE INDEX idx_requerimiento_diligencia_recepcion
    ON requerimientos_judiciales (
        diligencia_id,
        fecha_recepcion DESC
    );
