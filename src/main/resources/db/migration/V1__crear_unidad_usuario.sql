CREATE TABLE unidades (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo           VARCHAR(20) NOT NULL,   
    codunidad        VARCHAR(10) NOT NULL,   
    nombre           VARCHAR(150) NOT NULL,
    provincia        VARCHAR(50) NOT NULL,
    unidad_padre_id  BIGINT,
    telefono         VARCHAR(20),
    extension        VARCHAR(10),
    groupwise        VARCHAR(100),
    email            VARCHAR(100),
    direccion        VARCHAR(200),
    localidad        VARCHAR(100),
    codpostal        VARCHAR(10),
    dir3             VARCHAR(10),
    activa           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por       VARCHAR(100),
    modificado_por   VARCHAR(100),

    CONSTRAINT pk_unidades PRIMARY KEY (id),
    CONSTRAINT uk_unidades_codigo UNIQUE (codigo),
    CONSTRAINT uk_unidades_codunidad UNIQUE (codunidad),
    CONSTRAINT fk_unidades_padre FOREIGN KEY (unidad_padre_id) 
        REFERENCES unidades (id) ON DELETE RESTRICT,
    CONSTRAINT chk_unidades_codunidad_format CHECK (codunidad ~ '^[0-9]{6}$'),
    CONSTRAINT chk_unidades_codigo_format CHECK (codigo ~ '^[A-Z0-9\-]{3,20}$')
);

CREATE TABLE usuarios (
    id                BIGINT GENERATED ALWAYS AS IDENTITY,
    keycloak_subject  UUID NOT NULL,
    tip               VARCHAR(10) NOT NULL,
    nombre            VARCHAR(150) NOT NULL,
    empleo            VARCHAR(50),            
    email             VARCHAR(100),           
    telefono          VARCHAR(20),            
    unidad_id         BIGINT NOT NULL,
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_keycloak_subject UNIQUE (keycloak_subject),
    CONSTRAINT uk_usuarios_tip UNIQUE (tip),
    CONSTRAINT fk_usuarios_unidad FOREIGN KEY (unidad_id) 
        REFERENCES unidades (id) ON DELETE RESTRICT,
    CONSTRAINT chk_usuarios_tip_format 
        CHECK (tip ~ '^[A-Z][0-9]{5}[A-Z]$')
);

CREATE INDEX idx_unidades_padre ON unidades (unidad_padre_id);
CREATE INDEX idx_usuarios_unidad ON usuarios (unidad_id);
