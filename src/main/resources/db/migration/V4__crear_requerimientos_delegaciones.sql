CREATE TABLE historico_estados_diligencia (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    diligencia_id    BIGINT NOT NULL,
    estado_anterior  VARCHAR(30),
    estado_nuevo     VARCHAR(30) NOT NULL,
    motivo           TEXT,
    usuario_id       BIGINT NOT NULL,
    fecha_cambio     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_historico_estados PRIMARY KEY (id),
    CONSTRAINT fk_historico_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE requerimientos_judiciales (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY,
    diligencia_id          BIGINT NOT NULL,
    identificador_externo  VARCHAR(100) NOT NULL,
    referencia_judicial    VARCHAR(100) NOT NULL,
    juzgado_origen         VARCHAR(200) NOT NULL,
    numero_procedimiento   VARCHAR(100) NOT NULL,
    tipo                   VARCHAR(50) NOT NULL DEFAULT 'AMPLIACION_DILIGENCIAS',
    prioridad              VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    estado                 VARCHAR(30) NOT NULL DEFAULT 'RECIBIDO',
    asunto                 VARCHAR(250) NOT NULL,
    descripcion            TEXT,
    fecha_escrito          DATE NOT NULL,
    fecha_recepcion        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite           DATE,
    fecha_asignacion       TIMESTAMPTZ,
    fecha_respuesta        TIMESTAMPTZ,
    instructor_tip         VARCHAR(10),
    instructor_nombre      VARCHAR(150),
    unidad_responsable     VARCHAR(150),
    resultado              VARCHAR(2000),
    observaciones          VARCHAR(2000),
    version                BIGINT NOT NULL DEFAULT 0,
    fecha_creacion         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por             VARCHAR(100),
    modificado_por         VARCHAR(100),

    CONSTRAINT pk_requerimientos_judiciales PRIMARY KEY (id),
    CONSTRAINT uk_requerimientos_identificador UNIQUE (identificador_externo),
    CONSTRAINT fk_requerimientos_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT chk_tipo_req CHECK (tipo IN ('AMPLIACION_DILIGENCIAS', 'CITACION', 'INFORME', 'DOCUMENTACION', 'DECLARACION', 'MANDAMIENTO', 'OTRO')),
    CONSTRAINT chk_prioridad_req CHECK (prioridad IN ('BAJA', 'NORMAL', 'ALTA', 'URGENTE')),
    CONSTRAINT chk_estado_req CHECK (estado IN ('RECIBIDO', 'ASIGNADO', 'EN_TRAMITACION', 'PENDIENTE_RESPUESTA', 'RESPONDIDO', 'CERRADO', 'RECHAZADO'))
);

CREATE TABLE delegaciones (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_delegante_id  BIGINT NOT NULL,
    usuario_delegado_id   BIGINT NOT NULL,
    unidad_id             BIGINT,
    diligencia_id         BIGINT,
    tipo_delegacion       VARCHAR(50) NOT NULL DEFAULT 'PUNTUAL_EXPEDIENTE',
    motivo                VARCHAR(250) NOT NULL,
    creado_por_id         BIGINT NOT NULL,
    fecha_inicio          TIMESTAMPTZ NOT NULL,
    fecha_fin             TIMESTAMPTZ NOT NULL,
    activa                BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por            VARCHAR(100),
    modificado_por        VARCHAR(100),

    CONSTRAINT pk_delegaciones PRIMARY KEY (id),
    CONSTRAINT fk_delegaciones_delegante FOREIGN KEY (usuario_delegante_id) 
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_delegaciones_delegado FOREIGN KEY (usuario_delegado_id) 
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_delegaciones_unidad FOREIGN KEY (unidad_id) 
        REFERENCES unidades (id) ON DELETE CASCADE,
    CONSTRAINT fk_delegaciones_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_delegaciones_creado_por FOREIGN KEY (creado_por_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_delegaciones_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT chk_tipo_delegacion CHECK (tipo_delegacion IN ('MANDO_AREA', 'PUNTUAL_EXPEDIENTE', 'REASIGNACION_INSTRUCTOR'))
);

CREATE INDEX idx_historico_diligencia ON historico_estados_diligencia (diligencia_id);
CREATE INDEX idx_delegaciones_delegado ON delegaciones (usuario_delegado_id) WHERE activa = true;
