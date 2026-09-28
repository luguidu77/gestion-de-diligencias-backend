CREATE TABLE remisiones_documento (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY,
    documento_id           BIGINT NOT NULL,
    sistema_destino        VARCHAR(100) NOT NULL,
    destinatario           VARCHAR(200) NOT NULL,
    estado                 VARCHAR(50) NOT NULL DEFAULT 'NO_SOLICITADA',
    tipo_remision          VARCHAR(50) NOT NULL,
    destino                VARCHAR(200) NOT NULL,
    solicitada_por         VARCHAR(50) NOT NULL,
    ejecutada_por          VARCHAR(50),
    referencia_externa     VARCHAR(100),
    numero_intentos        INTEGER NOT NULL DEFAULT 0,
    ultimo_error           TEXT,
    fecha_solicitud        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio            TIMESTAMPTZ,
    proximo_reintento      TIMESTAMPTZ,
    observaciones          VARCHAR(1000),
    mensaje_error          VARCHAR(1000),
    clave_idempotencia     VARCHAR(200) NOT NULL,
    version                BIGINT NOT NULL DEFAULT 0,
    creado_por             VARCHAR(100),
    modificado_por         VARCHAR(100),

    CONSTRAINT pk_remisiones PRIMARY KEY (id),
    CONSTRAINT fk_remisiones_documento FOREIGN KEY (documento_id) 
        REFERENCES documentos_metadata (id) ON DELETE CASCADE,
    CONSTRAINT uk_remision_referencia_externa UNIQUE (sistema_destino, referencia_externa),
    CONSTRAINT uk_remision_idempotencia UNIQUE (clave_idempotencia)
);

ALTER TABLE requerimientos_judiciales
    ADD COLUMN sistema_origen VARCHAR(100) NOT NULL DEFAULT 'SISTEMA_JUDICIAL',
    DROP CONSTRAINT uk_requerimientos_identificador,
    ADD CONSTRAINT uk_requerimiento_origen_externo UNIQUE (sistema_origen, identificador_externo);

ALTER TABLE documentos_metadata
    DROP CONSTRAINT chk_estado_almacenamiento,
    ADD CONSTRAINT chk_estado_almacenamiento CHECK (estado_almacenamiento IN ('PENDIENTE', 'ALMACENADO', 'CONFIRMADO', 'ERROR', 'ERROR_INTEGRIDAD', 'ERROR_CONEXION'));
