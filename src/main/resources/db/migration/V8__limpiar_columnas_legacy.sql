-- V8__limpiar_columnas_legacy.sql
-- Limpiar columnas antiguas de la tabla usuarios tras el paso a Keycloak

DO $$ 
BEGIN
    -- 1. Eliminar la columna 'rol' (ya que los roles RBAC viajan en el JWT)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name='usuarios' AND column_name='rol'
    ) THEN
        ALTER TABLE usuarios DROP COLUMN rol;
    END IF;

    -- 2. Eliminar la columna 'id_unidad' (la correcta y mapeada en JPA es 'unidad_id')
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name='usuarios' AND column_name='id_unidad'
    ) THEN
        ALTER TABLE usuarios DROP COLUMN id_unidad;
    END IF;
END $$;
