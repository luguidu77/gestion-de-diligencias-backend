-- Expedientes en borrador con asignación manual del número SIGO (#8)
--
-- Los expedientes existentes conservan sus datos intactos: solo se relajan
-- constraints (DROP NOT NULL, ampliar CHECK). No se modifica ningún dato.

-- 1. Hacer nullable el número de expediente SIGO
--    (los borradores no tienen número; múltiples NULLs no colisionan en UNIQUE)
ALTER TABLE diligencias
    ALTER COLUMN numero_expediente DROP NOT NULL;

-- 2. Corregir y adaptar la regex del número SIGO al formato real: año(4)-unidad(6)-expediente(8)
--    Ej.: 2026-000177-00023550
ALTER TABLE diligencias
    DROP CONSTRAINT chk_diligencias_numero_format;

ALTER TABLE diligencias
    ADD CONSTRAINT chk_diligencias_numero_format
    CHECK (numero_expediente IS NULL
        OR numero_expediente ~ '^[0-9]{4}-[0-9]{6}-[0-9]{8}$');

-- 3. Ampliar el check de estado para incluir BORRADOR
ALTER TABLE diligencias
    DROP CONSTRAINT chk_estado_diligencia;

ALTER TABLE diligencias
    ADD CONSTRAINT chk_estado_diligencia
    CHECK (estado IN (
        'BORRADOR',
        'ABIERTA',
        'EN_TRAMITACION',
        'PENDIENTE_REVISION',
        'REMITIDA_JUZGADO',
        'ARCHIVADA',
        'CANCELADA'
    ));

-- 4. Cambiar el estado por defecto a BORRADOR
ALTER TABLE diligencias
    ALTER COLUMN estado SET DEFAULT 'BORRADOR';

-- 5. Hacer nullable delito_principal y gravedad_delito
--    (pueden no conocerse al crear el borrador)
ALTER TABLE diligencias
    ALTER COLUMN delito_principal DROP NOT NULL;

ALTER TABLE diligencias
    ALTER COLUMN gravedad_delito DROP NOT NULL;

-- 6. Hacer nullable anios_retencion_legal y fecha_expiracion_retencion
--    (se calculan cuando el expediente pasa a ABIERTA)
ALTER TABLE diligencias
    ALTER COLUMN anios_retencion_legal DROP NOT NULL;

ALTER TABLE diligencias
    ALTER COLUMN fecha_expiracion_retencion DROP NOT NULL;

