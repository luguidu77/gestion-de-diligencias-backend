CREATE TABLE tareas (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY,
    nota               TEXT NOT NULL,
    estado             VARCHAR(255),
    id_usuario         BIGINT NOT NULL,
    fecha_creacion     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por         VARCHAR(100),
    modificado_por     VARCHAR(100),

    CONSTRAINT pk_tareas PRIMARY KEY (id),
    CONSTRAINT fk_tareas_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios (id) ON DELETE CASCADE
);
