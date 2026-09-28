ALTER TABLE unidades
    ALTER COLUMN codunidad TYPE TEXT;

ALTER TABLE unidades
    DROP CONSTRAINT chk_unidades_codunidad_format;

ALTER TABLE unidades
    ADD CONSTRAINT chk_unidades_codunidad_format
    CHECK (codunidad ~ '^[0-9]+$');

ALTER TABLE diligencias
    ALTER COLUMN numero_expediente TYPE TEXT;

ALTER TABLE diligencias
    DROP CONSTRAINT chk_diligencias_numero_format;

ALTER TABLE diligencias
    ADD CONSTRAINT chk_diligencias_numero_format
    CHECK (numero_expediente ~ '^[0-9]{4}-[0-9]+-[0-9]{7}$');