-- =====================================================================
-- MIGRACIÓN v2.1  (solo si YA tienes la base de datos creada en Neon)
-- Cambios en la tabla bordado:
--   * tipo_bordado: ahora es texto libre (se quita la lista fija).
--   * tipo_uniforme y descripcion: pasan a ser opcionales (ya no se piden).
-- Los datos existentes NO se pierden. Ejecutar como script en DBeaver (Alt + X).
-- Si creas la base desde cero con bordados_jmym_postgresql.sql NO necesitas este archivo.
-- =====================================================================
DROP VIEW IF EXISTS v_bordado_produccion;

ALTER TABLE bordado DROP CONSTRAINT IF EXISTS bordado_tipo_bordado_check;
ALTER TABLE bordado ALTER COLUMN tipo_bordado  TYPE VARCHAR(50);
ALTER TABLE bordado ALTER COLUMN tipo_uniforme DROP NOT NULL;
ALTER TABLE bordado ALTER COLUMN descripcion   DROP NOT NULL;

CREATE VIEW v_bordado_produccion AS
SELECT b.cod_orden, b.cod_bordado, b.tipo_bordado, b.tipo_uniforme,
       b.color, b.cantidad, b.descripcion, b.notas
FROM bordado b;
