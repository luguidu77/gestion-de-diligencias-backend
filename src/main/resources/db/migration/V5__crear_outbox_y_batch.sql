CREATE TABLE sincronizaciones_batch (
    id                        BIGINT GENERATED ALWAYS AS IDENTITY,
    nombre_proceso            VARCHAR(100) NOT NULL,
    fecha_ultima_importacion  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registros_procesados      INTEGER NOT NULL DEFAULT 0,
    estado                    VARCHAR(50) NOT NULL DEFAULT 'EXITO',
    mensaje_error             TEXT,
    fecha_inicio              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin                 TIMESTAMPTZ,

    CONSTRAINT pk_sincronizaciones_batch PRIMARY KEY (id),
    CONSTRAINT uk_sincronizaciones_proceso UNIQUE (nombre_proceso)
);

CREATE TABLE outbox_eventos (
    id                BIGINT GENERATED ALWAYS AS IDENTITY,
    tipo_evento       VARCHAR(100) NOT NULL,
    payload_json      JSONB NOT NULL,
    estado            VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    intentos          INTEGER NOT NULL DEFAULT 0,
    proximo_reintento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_outbox_eventos PRIMARY KEY (id),
    CONSTRAINT chk_estado_outbox CHECK (estado IN ('PENDIENTE', 'ENVIADO', 'PROCESADO', 'ERROR'))
);

CREATE TABLE logs_auditoria (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    fecha_hora       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_tip      VARCHAR(10) NOT NULL,
    keycloak_subject UUID,
    usuario_nombre   VARCHAR(150),
    unidad_codigo    VARCHAR(20),
    rol_ejecutado    VARCHAR(50),
    accion           VARCHAR(100) NOT NULL,
    entidad          VARCHAR(100),
    entidad_id       VARCHAR(100),
    resultado        VARCHAR(30) NOT NULL,
    error_detalle    TEXT,
    ip_origen        VARCHAR(45),
    correlation_id   VARCHAR(64),
    detalles         JSONB,

    CONSTRAINT pk_logs_auditoria PRIMARY KEY (id),
    CONSTRAINT chk_resultado_auditoria CHECK (resultado IN ('OK', 'FORBIDDEN', 'ERROR'))
);

CREATE INDEX idx_outbox_pendientes ON outbox_eventos (proximo_reintento) WHERE estado = 'PENDIENTE';
CREATE INDEX idx_auditoria_unidad_fecha ON logs_auditoria (unidad_codigo, fecha_hora DESC);
