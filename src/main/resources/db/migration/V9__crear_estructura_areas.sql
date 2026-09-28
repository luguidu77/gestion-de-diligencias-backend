-- V9__crear_estructura_areas.sql
-- Estructura organizativa de areas, pertenencia de usuarios y jefaturas.

CREATE TABLE areas (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    unidad_id           BIGINT NOT NULL,
    codigo              VARCHAR(30) NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    descripcion         VARCHAR(500),
    activa              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por          VARCHAR(100),
    modificado_por      VARCHAR(100),

    CONSTRAINT pk_areas PRIMARY KEY (id),
    CONSTRAINT uk_areas_unidad_codigo UNIQUE (unidad_id, codigo),
    CONSTRAINT fk_areas_unidad FOREIGN KEY (unidad_id)
        REFERENCES unidades (id) ON DELETE RESTRICT,
    CONSTRAINT chk_areas_codigo_no_vacio CHECK (btrim(codigo) <> ''),
    CONSTRAINT chk_areas_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE TABLE usuarios_areas (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    area_id             BIGINT NOT NULL,
    usuario_id          BIGINT NOT NULL,
    area_principal      BOOLEAN NOT NULL DEFAULT FALSE,
    activa              BOOLEAN NOT NULL DEFAULT TRUE,
    asignado_por_id     BIGINT NOT NULL,
    fecha_asignacion    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_baja          TIMESTAMPTZ,
    creado_por          VARCHAR(100),
    modificado_por      VARCHAR(100),

    CONSTRAINT pk_usuarios_areas PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_areas_area_usuario UNIQUE (area_id, usuario_id),
    CONSTRAINT fk_usuarios_areas_area FOREIGN KEY (area_id)
        REFERENCES areas (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_areas_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_usuarios_areas_asignador FOREIGN KEY (asignado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_usuarios_areas_estado CHECK (
        (activa = TRUE AND fecha_baja IS NULL)
        OR (activa = FALSE AND fecha_baja IS NOT NULL)
    ),
    CONSTRAINT chk_usuarios_areas_fechas CHECK (
        fecha_baja IS NULL OR fecha_baja >= fecha_asignacion
    )
);

CREATE TABLE jefaturas_area (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    area_id             BIGINT NOT NULL,
    usuario_id          BIGINT NOT NULL,
    designado_por_id    BIGINT NOT NULL,
    activa              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin           TIMESTAMPTZ,
    observaciones       VARCHAR(500),
    creado_por          VARCHAR(100),
    modificado_por      VARCHAR(100),

    CONSTRAINT pk_jefaturas_area PRIMARY KEY (id),
    CONSTRAINT fk_jefaturas_area_miembro FOREIGN KEY (area_id, usuario_id)
        REFERENCES usuarios_areas (area_id, usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_jefaturas_area_designador FOREIGN KEY (designado_por_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_jefaturas_area_estado CHECK (
        (activa = TRUE AND fecha_fin IS NULL)
        OR (activa = FALSE AND fecha_fin IS NOT NULL)
    ),
    CONSTRAINT chk_jefaturas_area_fechas CHECK (
        fecha_fin IS NULL OR fecha_fin >= fecha_inicio
    )
);

-- Una unidad no puede tener dos areas activas con el mismo nombre,
-- aunque se escriban con distinta combinacion de mayusculas y minusculas.
CREATE UNIQUE INDEX uk_areas_unidad_nombre_activa
    ON areas (unidad_id, lower(btrim(nombre)))
    WHERE activa = TRUE;

-- Un usuario solo puede tener un area principal activa.
CREATE UNIQUE INDEX uk_usuarios_areas_principal_activa
    ON usuarios_areas (usuario_id)
    WHERE activa = TRUE AND area_principal = TRUE;

-- Cada area solo puede tener una jefatura activa.
CREATE UNIQUE INDEX uk_jefaturas_area_activa
    ON jefaturas_area (area_id)
    WHERE activa = TRUE;

CREATE INDEX idx_areas_unidad_activas
    ON areas (unidad_id, nombre)
    WHERE activa = TRUE;

CREATE INDEX idx_usuarios_areas_usuario_activas
    ON usuarios_areas (usuario_id, area_id)
    WHERE activa = TRUE;

CREATE INDEX idx_jefaturas_area_usuario_activas
    ON jefaturas_area (usuario_id, area_id)
    WHERE activa = TRUE;
