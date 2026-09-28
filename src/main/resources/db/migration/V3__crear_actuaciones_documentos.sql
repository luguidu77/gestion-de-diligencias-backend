CREATE TABLE actuaciones (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    diligencia_id      BIGINT NOT NULL,
    numero_orden       INTEGER NOT NULL,
    titulo             VARCHAR(200) NOT NULL,
    tipo_actuacion     VARCHAR(100) NOT NULL,
    lugar              VARCHAR(200),
    resumen            TEXT NOT NULL,
    usuario_creador_id BIGINT NOT NULL,
    fecha_actuacion    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_actuaciones PRIMARY KEY (id),
    CONSTRAINT uk_actuaciones_orden UNIQUE (diligencia_id, numero_orden),
    CONSTRAINT fk_actuaciones_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_actuaciones_usuario FOREIGN KEY (usuario_creador_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_actuaciones_orden_pos CHECK (numero_orden > 0)
);

CREATE TABLE actuacion_intervinientes (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    actuacion_id       BIGINT NOT NULL,
    usuario_id         BIGINT NOT NULL,
    rol_en_actuacion   VARCHAR(50) NOT NULL DEFAULT 'INTERVINIENTE',
    fecha_registro     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_actuacion_intervinientes PRIMARY KEY (id),
    CONSTRAINT uk_actuacion_usuario UNIQUE (actuacion_id, usuario_id),
    CONSTRAINT fk_intervinientes_actuacion FOREIGN KEY (actuacion_id) 
        REFERENCES actuaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_intervinientes_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE documentos_metadata (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY,
    diligencia_id          BIGINT NOT NULL,
    actuacion_id           BIGINT,
    nombre_original        VARCHAR(255) NOT NULL,
    titulo_documento       VARCHAR(200),
    alfresco_node_id       VARCHAR(64),
    alfresco_folder_path   VARCHAR(300),
    mime_type              VARCHAR(100) NOT NULL,
    tamano_bytes           BIGINT NOT NULL,
    hash_sha256            VARCHAR(64) NOT NULL,
    hash_md5               VARCHAR(32),
    destino                VARCHAR(50) NOT NULL DEFAULT 'INTERNO_ALFRESCO',
    estado_documento       VARCHAR(50) NOT NULL DEFAULT 'BORRADOR',
    estado_almacenamiento  VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    subido_por_id          BIGINT NOT NULL,
    version                BIGINT NOT NULL DEFAULT 0,
    fecha_creacion         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por             VARCHAR(100),
    modificado_por         VARCHAR(100),

    CONSTRAINT pk_documentos_metadata PRIMARY KEY (id),
    CONSTRAINT uk_documentos_alfresco_node UNIQUE (alfresco_node_id),
    CONSTRAINT fk_documentos_diligencia FOREIGN KEY (diligencia_id) 
        REFERENCES diligencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_documentos_actuacion FOREIGN KEY (actuacion_id) 
        REFERENCES actuaciones (id) ON DELETE SET NULL,
    CONSTRAINT fk_documentos_subido_por FOREIGN KEY (subido_por_id) 
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_documentos_tamano_pos CHECK (tamano_bytes > 0),
    CONSTRAINT chk_documentos_sha256_length CHECK (length(hash_sha256) = 64),
    CONSTRAINT chk_estado_almacenamiento CHECK (estado_almacenamiento IN ('PENDIENTE', 'CONFIRMADO', 'ERROR_INTEGRIDAD', 'ERROR_CONEXION'))
);

CREATE INDEX idx_actuaciones_diligencia ON actuaciones (diligencia_id);
CREATE INDEX idx_intervinientes_actuacion ON actuacion_intervinientes (actuacion_id, usuario_id);
CREATE INDEX idx_documentos_diligencia ON documentos_metadata (diligencia_id);
CREATE INDEX idx_documentos_pendientes_subida ON documentos_metadata (id) WHERE estado_almacenamiento = 'PENDIENTE';
