CREATE TABLE diligencias (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY,
    numero_expediente          VARCHAR(25) NOT NULL,
    unidad_id                  BIGINT NOT NULL,
    agente_instructor_id       BIGINT NOT NULL,
    agente_secretario_id       BIGINT,
    estado                     VARCHAR(30) NOT NULL DEFAULT 'ABIERTA',
    delito_principal           VARCHAR(150) NOT NULL,
    gravedad_delito            VARCHAR(20) NOT NULL DEFAULT 'GRAVE',
    anios_retencion_legal      INTEGER NOT NULL DEFAULT 10,
    fecha_expiracion_retencion DATE NOT NULL,
    resumen                    TEXT,
    version                    BIGINT NOT NULL DEFAULT 0,
    fecha_creacion             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por                 VARCHAR(100),
    modificado_por             VARCHAR(100),

    CONSTRAINT pk_diligencias PRIMARY KEY (id),
    CONSTRAINT uk_diligencias_numero UNIQUE (numero_expediente),
    CONSTRAINT fk_diligencias_unidad FOREIGN KEY (unidad_id) 
        REFERENCES unidades (id) ON DELETE RESTRICT,
    CONSTRAINT fk_diligencias_instructor FOREIGN KEY (agente_instructor_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_diligencias_secretario FOREIGN KEY (agente_secretario_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_diligencias_numero_format 
        CHECK (numero_expediente ~ '^[0-9]{4}-[0-9]{6}-[0-9]{7}$'),
    CONSTRAINT chk_diligencias_retencion_pos 
        CHECK (anios_retencion_legal > 0),
    CONSTRAINT chk_estado_diligencia CHECK (estado IN ('ABIERTA', 'EN_TRAMITACION', 'PENDIENTE_REVISION', 'REMITIDA_JUZGADO', 'ARCHIVADA', 'CANCELADA'))
);

CREATE TABLE diligencia_participantes (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    diligencia_id      BIGINT NOT NULL,
    usuario_id         BIGINT NOT NULL,
    rol_participante   VARCHAR(30) NOT NULL,
    fecha_asignacion   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por_id    BIGINT NOT NULL,

    CONSTRAINT pk_diligencia_participantes PRIMARY KEY (id),
    CONSTRAINT uk_diligencia_usuario_rol UNIQUE (diligencia_id, usuario_id, rol_participante),
    CONSTRAINT fk_participantes_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_participantes_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_participantes_asignador FOREIGN KEY (asignado_por_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_rol_participante CHECK (rol_participante IN ('INSTRUCTOR', 'SECRETARIO', 'ACTUANTE', 'COLABORADOR'))
);

CREATE INDEX idx_diligencias_unidad ON diligencias (unidad_id);
CREATE INDEX idx_diligencias_instructor ON diligencias (agente_instructor_id);
CREATE INDEX idx_diligencias_secretario ON diligencias (agente_secretario_id);
CREATE INDEX idx_participantes_diligencia ON diligencia_participantes (diligencia_id, usuario_id);
CREATE INDEX idx_diligencias_unidad_fecha ON diligencias (unidad_id, fecha_creacion DESC);
