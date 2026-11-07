-- =====================================================================
-- Sistema Integral de Gestión de Pedidos para Bordados J.M.Y.M.
-- Base de datos: PostgreSQL (Neon)  |  versión 2.0 convertida desde MySQL
--
-- CÓMO EJECUTARLO (DBeaver):
--   1. Conéctate a tu base de datos de Neon.
--   2. Abre este archivo (Archivo > Abrir archivo) y presiona
--      "Ejecutar script" (Alt + X).
-- El script se puede ejecutar varias veces: borra y recrea las tablas.
--
-- CAMBIOS RESPECTO A LA VERSIÓN DE MySQL
--   * ENUM  -> VARCHAR con CHECK (más fácil de usar desde Java).
--   * AUTO_INCREMENT -> SERIAL.   DATETIME -> TIMESTAMP.
--   * Procedimientos y triggers reescritos en PL/pgSQL (misma lógica y
--     los mismos mensajes de error).
--   * Se agregaron 3 usuarios de prueba (contraseña: Jmym2026).
--   * v2.1: bordado.tipo_bordado es texto libre; tipo_uniforme y descripcion son opcionales.
--   * v2.2: una orden con saldo pendiente no se puede entregar; cuentas de las 3 sedes y 30 clientes de prueba.
-- =====================================================================

-- SECCION: TABLAS -----------------------------------------------------
DROP VIEW  IF EXISTS v_panel_ordenes, v_bordado_produccion;
DROP TABLE IF EXISTS auditoria, pago, bordado, orden, cliente, usuario, sede, rol CASCADE;

CREATE TABLE rol (
    cod_rol    SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE sede (
    cod_sede    SERIAL PRIMARY KEY,
    nombre_sede VARCHAR(50)  NOT NULL UNIQUE,
    direccion   VARCHAR(100) NOT NULL,
    telefono    VARCHAR(10)  NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'Activa'
                CHECK (estado IN ('Activa','Inactiva'))
);

CREATE TABLE usuario (
    cod_usuario       SERIAL PRIMARY KEY,
    identificacion    VARCHAR(12)  NOT NULL UNIQUE,
    nombre            VARCHAR(100) NOT NULL,
    correo            VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash   VARCHAR(255) NOT NULL,
    cod_rol           INT NOT NULL REFERENCES rol (cod_rol),
    cod_sede          INT NOT NULL REFERENCES sede (cod_sede),
    estado            VARCHAR(10) NOT NULL DEFAULT 'Activo'
                      CHECK (estado IN ('Activo','Inactivo','Bloqueado')),
    intentos_fallidos INT NOT NULL DEFAULT 0 CHECK (intentos_fallidos >= 0),
    fecha_creacion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuario_identificacion CHECK (identificacion ~ '^[0-9]+$'),
    CONSTRAINT chk_usuario_correo CHECK (correo LIKE '%_@_%._%')
);

CREATE TABLE cliente (
    cod_cliente SERIAL PRIMARY KEY,
    cedula      VARCHAR(12)  NOT NULL UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    telefono    VARCHAR(10)  NOT NULL,
    CONSTRAINT chk_cliente_cedula   CHECK (cedula   ~ '^[0-9]+$'),
    CONSTRAINT chk_cliente_telefono CHECK (telefono ~ '^[0-9]+$')
);

CREATE TABLE orden (
    cod_orden      SERIAL PRIMARY KEY,
    cod_cliente    INT NOT NULL REFERENCES cliente (cod_cliente),
    cod_sede       INT NOT NULL REFERENCES sede (cod_sede),
    cod_usuario    INT NOT NULL REFERENCES usuario (cod_usuario),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega  TIMESTAMP NULL,
    estado         VARCHAR(20) NOT NULL DEFAULT 'Pendiente'
                   CHECK (estado IN ('Pendiente','En Producción','Listo para Entrega','Entregada')),
    estado_pago    VARCHAR(10) NOT NULL DEFAULT 'Pendiente'
                   CHECK (estado_pago IN ('Pendiente','Abonado','Pagado')),
    total          NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    version        INT NOT NULL DEFAULT 1
);
CREATE INDEX idx_orden_sede_estado ON orden (cod_sede, estado);
CREATE INDEX idx_orden_fecha ON orden (fecha_creacion);

CREATE TABLE bordado (
    cod_bordado     SERIAL PRIMARY KEY,
    cod_orden       INT NOT NULL REFERENCES orden (cod_orden) ON DELETE CASCADE,
    tipo_bordado    VARCHAR(50)  NOT NULL,   -- texto libre (se sugieren: Presilla, Tarjetero, ...)
    tipo_uniforme   VARCHAR(30)  NULL,       -- ya no se pide en la pantalla (opcional)
    color           VARCHAR(30)  NOT NULL,
    cantidad        INT NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario > 0),
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0,
    descripcion     VARCHAR(250) NULL,       -- ya no se pide en la pantalla (opcional)
    notas           VARCHAR(250) NULL
);

CREATE TABLE pago (
    cod_pago    SERIAL PRIMARY KEY,
    cod_orden   INT NOT NULL REFERENCES orden (cod_orden),
    cod_usuario INT NOT NULL REFERENCES usuario (cod_usuario),
    valor       NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    medio_pago  VARCHAR(15) NOT NULL CHECK (medio_pago IN ('Efectivo','Transferencia')),
    fecha_pago  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auditoria (
    cod_auditoria  BIGSERIAL PRIMARY KEY,
    cod_usuario    INT NULL REFERENCES usuario (cod_usuario),
    cod_sede       INT NULL REFERENCES sede (cod_sede),
    operacion      VARCHAR(50) NOT NULL,
    tabla_afectada VARCHAR(50) NULL,
    cod_orden      INT NULL,
    valor_anterior TEXT NULL,
    valor_nuevo    TEXT NULL,
    fecha_hora     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_auditoria_orden ON auditoria (cod_orden);

-- SECCION: FUNCIONES (antes "procedimientos") -------------------------

-- Registra una operación crítica en la auditoría (RNF-005).
CREATE OR REPLACE FUNCTION sp_registrar_auditoria(
    p_cod_usuario INT, p_operacion VARCHAR, p_tabla VARCHAR,
    p_cod_orden INT, p_valor_anterior TEXT, p_valor_nuevo TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO auditoria (cod_usuario, cod_sede, operacion, tabla_afectada,
                           cod_orden, valor_anterior, valor_nuevo)
    VALUES (p_cod_usuario,
            (SELECT cod_sede FROM usuario WHERE cod_usuario = p_cod_usuario),
            p_operacion, p_tabla, p_cod_orden, p_valor_anterior, p_valor_nuevo);
END;
$$ LANGUAGE plpgsql;

-- Suma un intento fallido de inicio de sesión y bloquea al llegar al máximo (RNF-001).
CREATE OR REPLACE FUNCTION sp_registrar_intento_fallido(p_correo VARCHAR, p_max_intentos INT)
RETURNS VOID AS $$
DECLARE
    v_cod INT; v_estado VARCHAR(10); v_intentos INT;
BEGIN
    SELECT cod_usuario, estado, intentos_fallidos INTO v_cod, v_estado, v_intentos
    FROM usuario WHERE correo = p_correo;
    IF v_cod IS NOT NULL AND v_estado = 'Activo' THEN
        v_intentos := v_intentos + 1;
        UPDATE usuario
           SET intentos_fallidos = v_intentos,
               estado = CASE WHEN v_intentos >= p_max_intentos THEN 'Bloqueado' ELSE estado END
         WHERE cod_usuario = v_cod;
        IF v_intentos >= p_max_intentos THEN
            PERFORM sp_registrar_auditoria(v_cod, 'BLOQUEO_POR_INTENTOS', 'usuario', NULL, 'Activo', 'Bloqueado');
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Cambia el estado de una orden respetando la secuencia y el rol (RF-005).
--   Diseñador: Pendiente -> En Producción -> Listo para Entrega
--   Vendedor : Listo para Entrega -> Entregada (solo sus propias órdenes y SOLO si está pagada por completo)
--   Gerente  : solo consulta
CREATE OR REPLACE FUNCTION sp_cambiar_estado_orden(
    p_cod_orden INT, p_nuevo_estado VARCHAR, p_cod_usuario INT)
RETURNS VOID AS $$
DECLARE
    v_estado_actual VARCHAR(20); v_cod_vendedor INT; v_rol VARCHAR(20); v_bordados INT := 0; v_pago VARCHAR(10);
BEGIN
    SELECT r.nombre_rol INTO v_rol
    FROM usuario u JOIN rol r ON u.cod_rol = r.cod_rol
    WHERE u.cod_usuario = p_cod_usuario AND u.estado = 'Activo';

    SELECT estado, cod_usuario INTO v_estado_actual, v_cod_vendedor
    FROM orden WHERE cod_orden = p_cod_orden;

    IF v_estado_actual IS NULL THEN RAISE EXCEPTION 'La orden no existe.'; END IF;
    IF v_rol IS NULL OR v_rol = 'Gerente' THEN
        RAISE EXCEPTION 'No tiene permisos para esta función.';
    END IF;

    IF p_nuevo_estado = 'Entregada' THEN
        IF v_rol <> 'Vendedor' THEN RAISE EXCEPTION 'No tiene permisos para esta función.'; END IF;
        IF v_estado_actual <> 'Listo para Entrega' OR v_cod_vendedor <> p_cod_usuario THEN
            RAISE EXCEPTION 'Solo puede entregar órdenes propias en estado Listo para Entrega.';
        END IF;
        -- La factura se cierra al pagar el total: con saldo pendiente NO se puede entregar.
        SELECT estado_pago INTO v_pago FROM orden WHERE cod_orden = p_cod_orden;
        IF v_pago <> 'Pagado' THEN
            RAISE EXCEPTION 'No se puede entregar la orden: el cliente aún tiene saldo pendiente. La factura se cierra al pagar el total.';
        END IF;
    ELSIF p_nuevo_estado IN ('En Producción', 'Listo para Entrega') THEN
        IF v_rol <> 'Diseñador' THEN RAISE EXCEPTION 'No tiene permisos para esta función.'; END IF;
        IF NOT ((v_estado_actual = 'Pendiente' AND p_nuevo_estado = 'En Producción')
             OR (v_estado_actual = 'En Producción' AND p_nuevo_estado = 'Listo para Entrega')) THEN
            RAISE EXCEPTION 'No es posible cambiar el estado sin cumplir la secuencia establecida.';
        END IF;
    ELSE
        RAISE EXCEPTION 'No es posible cambiar el estado sin cumplir la secuencia establecida.';
    END IF;

    IF p_nuevo_estado = 'En Producción' THEN
        SELECT COUNT(*) INTO v_bordados FROM bordado WHERE cod_orden = p_cod_orden;
        IF v_bordados = 0 THEN RAISE EXCEPTION 'La orden debe tener al menos un bordado.'; END IF;
    END IF;

    UPDATE orden
       SET estado = p_nuevo_estado,
           fecha_entrega = CASE WHEN p_nuevo_estado = 'Entregada' THEN NOW() ELSE fecha_entrega END,
           version = version + 1
     WHERE cod_orden = p_cod_orden AND estado = v_estado_actual;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La orden fue modificada por otro usuario. Actualice e intente de nuevo.';
    END IF;

    PERFORM sp_registrar_auditoria(p_cod_usuario, 'CAMBIO_ESTADO', 'orden',
                                   p_cod_orden, v_estado_actual, p_nuevo_estado);
END;
$$ LANGUAGE plpgsql;

-- Elimina una orden registrada por error (RF-003). Solo Gerente, solo Pendiente y sin pagos.
CREATE OR REPLACE FUNCTION sp_eliminar_orden(p_cod_orden INT, p_cod_usuario INT)
RETURNS VOID AS $$
DECLARE
    v_rol VARCHAR(20); v_estado VARCHAR(20); v_cliente INT; v_sede INT; v_total NUMERIC(12,2);
BEGIN
    SELECT r.nombre_rol INTO v_rol
    FROM usuario u JOIN rol r ON u.cod_rol = r.cod_rol
    WHERE u.cod_usuario = p_cod_usuario AND u.estado = 'Activo';

    IF v_rol IS NULL OR v_rol <> 'Gerente' THEN
        RAISE EXCEPTION 'No tiene permisos para esta función.';
    END IF;

    SELECT estado, cod_cliente, cod_sede, total INTO v_estado, v_cliente, v_sede, v_total
    FROM orden WHERE cod_orden = p_cod_orden;

    IF v_estado IS NULL THEN RAISE EXCEPTION 'La orden no existe.'; END IF;
    IF v_estado <> 'Pendiente' THEN
        RAISE EXCEPTION 'No es posible eliminar una orden en producción o entregada.';
    END IF;
    IF EXISTS (SELECT 1 FROM pago WHERE cod_orden = p_cod_orden) THEN
        RAISE EXCEPTION 'No es posible eliminar una orden con pagos registrados.';
    END IF;

    DELETE FROM orden WHERE cod_orden = p_cod_orden;

    PERFORM sp_registrar_auditoria(p_cod_usuario, 'ELIMINAR_ORDEN', 'orden', p_cod_orden,
        'cliente=' || v_cliente || '; sede=' || v_sede || '; total=' || v_total, NULL);
END;
$$ LANGUAGE plpgsql;

-- SECCION: TRIGGERS ---------------------------------------------------

-- Toda orden pertenece a una sede activa: la del vendedor que la registra (RF-003).
CREATE OR REPLACE FUNCTION fn_orden_valida_ins() RETURNS trigger AS $$
DECLARE v_rol VARCHAR(20); v_sede INT; v_estado_sede VARCHAR(10);
BEGIN
    SELECT r.nombre_rol, u.cod_sede INTO v_rol, v_sede
    FROM usuario u JOIN rol r ON u.cod_rol = r.cod_rol
    WHERE u.cod_usuario = NEW.cod_usuario;
    IF v_rol IS NULL OR v_rol <> 'Vendedor' THEN
        RAISE EXCEPTION 'No tiene permisos para esta función.';
    END IF;
    IF NEW.cod_sede <> v_sede THEN
        RAISE EXCEPTION 'La orden debe quedar en la sede del vendedor que la registra.';
    END IF;
    SELECT estado INTO v_estado_sede FROM sede WHERE cod_sede = NEW.cod_sede;
    IF v_estado_sede <> 'Activa' THEN RAISE EXCEPTION 'La sede seleccionada está inactiva.'; END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orden_valida_ins BEFORE INSERT ON orden
    FOR EACH ROW EXECUTE FUNCTION fn_orden_valida_ins();

-- Toda orden queda en la auditoría desde su creación (RNF-005).
CREATE OR REPLACE FUNCTION fn_orden_auditoria_ins() RETURNS trigger AS $$
BEGIN
    PERFORM sp_registrar_auditoria(NEW.cod_usuario, 'CREAR_ORDEN', 'orden', NEW.cod_orden,
        NULL, 'cliente=' || NEW.cod_cliente || '; sede=' || NEW.cod_sede);
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orden_auditoria_ins AFTER INSERT ON orden
    FOR EACH ROW EXECUTE FUNCTION fn_orden_auditoria_ins();

-- Una orden solo se elimina mientras está Pendiente (RF-003).
CREATE OR REPLACE FUNCTION fn_orden_solo_pendiente_del() RETURNS trigger AS $$
BEGIN
    IF OLD.estado <> 'Pendiente' THEN
        RAISE EXCEPTION 'No es posible eliminar una orden en producción o entregada.';
    END IF;
    RETURN OLD;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orden_solo_pendiente_del BEFORE DELETE ON orden
    FOR EACH ROW EXECUTE FUNCTION fn_orden_solo_pendiente_del();

-- Si cambia el total: no puede quedar por debajo de lo pagado y se recalcula el estado de pago.
CREATE OR REPLACE FUNCTION fn_orden_ajusta_pago() RETURNS trigger AS $$
DECLARE v_pagado NUMERIC(12,2);
BEGIN
    IF NEW.total <> OLD.total THEN
        SELECT COALESCE(SUM(valor), 0) INTO v_pagado FROM pago WHERE cod_orden = NEW.cod_orden;
        IF NEW.total < v_pagado THEN
            RAISE EXCEPTION 'El total de la orden no puede ser menor a lo ya pagado.';
        END IF;
        NEW.estado_pago := CASE WHEN v_pagado = 0 THEN 'Pendiente'
                                WHEN v_pagado < NEW.total THEN 'Abonado'
                                ELSE 'Pagado' END;
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orden_ajusta_pago BEFORE UPDATE ON orden
    FOR EACH ROW EXECUTE FUNCTION fn_orden_ajusta_pago();

-- Todo usuario nuevo debe tener una sede activa (RF-002).
CREATE OR REPLACE FUNCTION fn_usuario_sede_activa() RETURNS trigger AS $$
DECLARE v_estado VARCHAR(10);
BEGIN
    SELECT estado INTO v_estado FROM sede WHERE cod_sede = NEW.cod_sede;
    IF v_estado <> 'Activa' THEN RAISE EXCEPTION 'La sede seleccionada está inactiva.'; END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_usuario_sede_activa BEFORE INSERT ON usuario
    FOR EACH ROW EXECUTE FUNCTION fn_usuario_sede_activa();

-- Bordados: solo se agregan/modifican/quitan con la orden Pendiente (RF-004)
-- y el subtotal se calcula solo (cantidad x precio).
CREATE OR REPLACE FUNCTION fn_bordado_antes_ins_upd() RETURNS trigger AS $$
DECLARE v_estado VARCHAR(20);
BEGIN
    SELECT estado INTO v_estado FROM orden WHERE cod_orden = NEW.cod_orden;
    IF v_estado <> 'Pendiente' THEN
        RAISE EXCEPTION 'No es posible modificar una orden que ya está en producción.';
    END IF;
    NEW.subtotal := NEW.cantidad * NEW.precio_unitario;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_bordado_subtotal BEFORE INSERT OR UPDATE ON bordado
    FOR EACH ROW EXECUTE FUNCTION fn_bordado_antes_ins_upd();

CREATE OR REPLACE FUNCTION fn_bordado_bloqueo_del() RETURNS trigger AS $$
DECLARE v_estado VARCHAR(20);
BEGIN
    SELECT estado INTO v_estado FROM orden WHERE cod_orden = OLD.cod_orden;
    IF v_estado <> 'Pendiente' THEN
        RAISE EXCEPTION 'No es posible modificar una orden que ya está en producción.';
    END IF;
    RETURN OLD;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_bordado_bloqueo_del BEFORE DELETE ON bordado
    FOR EACH ROW EXECUTE FUNCTION fn_bordado_bloqueo_del();

-- Recalcula el total de la orden cada vez que cambia un bordado.
CREATE OR REPLACE FUNCTION fn_orden_total() RETURNS trigger AS $$
DECLARE v_orden INT;
BEGIN
    IF TG_OP = 'DELETE' THEN v_orden := OLD.cod_orden; ELSE v_orden := NEW.cod_orden; END IF;
    UPDATE orden
       SET total = (SELECT COALESCE(SUM(subtotal), 0) FROM bordado WHERE cod_orden = v_orden)
     WHERE cod_orden = v_orden;
    RETURN NULL;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_orden_total AFTER INSERT OR UPDATE OR DELETE ON bordado
    FOR EACH ROW EXECUTE FUNCTION fn_orden_total();

-- Valida el pago (RF-006): lo registra el Vendedor dueño de la orden y no supera el saldo.
CREATE OR REPLACE FUNCTION fn_pago_valida() RETURNS trigger AS $$
DECLARE v_total NUMERIC(12,2); v_pagado NUMERIC(12,2); v_vendedor INT; v_rol VARCHAR(20);
BEGIN
    SELECT r.nombre_rol INTO v_rol
    FROM usuario u JOIN rol r ON u.cod_rol = r.cod_rol
    WHERE u.cod_usuario = NEW.cod_usuario AND u.estado = 'Activo';

    SELECT total, cod_usuario INTO v_total, v_vendedor
    FROM orden WHERE cod_orden = NEW.cod_orden FOR UPDATE;

    IF v_rol IS NULL OR v_rol <> 'Vendedor' OR v_vendedor <> NEW.cod_usuario THEN
        RAISE EXCEPTION 'No tiene permisos para esta función.';
    END IF;
    SELECT COALESCE(SUM(valor), 0) INTO v_pagado FROM pago WHERE cod_orden = NEW.cod_orden;
    IF NEW.valor > (v_total - v_pagado) THEN
        RAISE EXCEPTION 'El valor del pago supera el saldo pendiente.';
    END IF;
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_pago_valida BEFORE INSERT ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_pago_valida();

-- Después de cada abono: actualiza el estado de pago y deja auditoría.
CREATE OR REPLACE FUNCTION fn_pago_despues_ins() RETURNS trigger AS $$
DECLARE v_total NUMERIC(12,2); v_pagado NUMERIC(12,2);
BEGIN
    SELECT total INTO v_total FROM orden WHERE cod_orden = NEW.cod_orden;
    SELECT COALESCE(SUM(valor), 0) INTO v_pagado FROM pago WHERE cod_orden = NEW.cod_orden;
    UPDATE orden
       SET estado_pago = CASE WHEN v_pagado = 0 THEN 'Pendiente'
                              WHEN v_pagado < v_total THEN 'Abonado'
                              ELSE 'Pagado' END
     WHERE cod_orden = NEW.cod_orden;
    PERFORM sp_registrar_auditoria(NEW.cod_usuario, 'REGISTRAR_PAGO', 'pago', NEW.cod_orden, NULL,
        'cod_pago=' || NEW.cod_pago || '; valor=' || NEW.valor || '; medio=' || NEW.medio_pago);
    RETURN NEW;
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_pago_despues_ins AFTER INSERT ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_pago_despues_ins();

-- Los pagos registrados no se modifican ni se eliminan (RF-006).
CREATE OR REPLACE FUNCTION fn_pago_solo_lectura() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Los pagos registrados no pueden modificarse ni eliminarse.';
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_pago_no_cambios BEFORE UPDATE OR DELETE ON pago
    FOR EACH ROW EXECUTE FUNCTION fn_pago_solo_lectura();

-- Los registros de auditoría son de solo lectura (RNF-005).
CREATE OR REPLACE FUNCTION fn_auditoria_solo_lectura() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Los registros de auditoría no pueden modificarse ni eliminarse.';
END; $$ LANGUAGE plpgsql;
CREATE TRIGGER trg_auditoria_no_cambios BEFORE UPDATE OR DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria_solo_lectura();

-- SECCION: VISTAS -----------------------------------------------------
-- Detalle de bordado para el Diseñador: sin precios, subtotales ni totales (RF-004).
CREATE VIEW v_bordado_produccion AS
SELECT b.cod_orden, b.cod_bordado, b.tipo_bordado, b.tipo_uniforme,
       b.color, b.cantidad, b.descripcion, b.notas
FROM bordado b;

-- Panel unificado de órdenes de las tres sedes, sin valores en pesos (RF-005).
CREATE VIEW v_panel_ordenes AS
SELECT o.cod_orden, c.cedula, c.nombre AS cliente, s.nombre_sede AS sede_origen,
       o.estado, o.fecha_creacion, o.fecha_entrega
FROM orden o
JOIN cliente c ON o.cod_cliente = c.cod_cliente
JOIN sede s    ON o.cod_sede    = s.cod_sede;

-- SECCION: DATOS INICIALES --------------------------------------------
INSERT INTO rol (nombre_rol) VALUES ('Gerente'), ('Vendedor'), ('Diseñador');

-- Dirección y teléfono provisionales: reemplazar por los datos reales.
INSERT INTO sede (nombre_sede, direccion, telefono) VALUES
    ('J.M.Y.M.',   'Por definir', '0000000000'),
    ('The Police', 'Por definir', '0000000000'),
    ('Lobo',       'Por definir', '0000000000');

-- Usuarios de PRUEBA (contraseña de los tres: Jmym2026). Cámbialos o elimínalos
-- cuando el sistema entre en uso real.
INSERT INTO usuario (identificacion, nombre, correo, contrasena_hash, cod_rol, cod_sede) VALUES
    ('1000000001', 'Gerente Prueba',   'gerente@jmym.com',   '39f17d2561e6b18bce70e7350c84af97:edcf04acb4bf4959f998492445e5111a2532300a182a3571000e4ef3734562c3', 1, 1),
    ('1000000002', 'Vendedor Prueba',  'vendedor@jmym.com',  '39f17d2561e6b18bce70e7350c84af97:edcf04acb4bf4959f998492445e5111a2532300a182a3571000e4ef3734562c3', 2, 1),
    ('1000000003', 'Diseñador Prueba', 'disenador@jmym.com', '39f17d2561e6b18bce70e7350c84af97:edcf04acb4bf4959f998492445e5111a2532300a182a3571000e4ef3734562c3', 3, 1);

-- SECCION: CUENTAS DE LAS TRES SEDES ----------------------------------
-- Cada sede tiene su Vendedor y su Diseñador. El Gerente (gerente@jmym.com) supervisa las tres.
-- J.M.Y.M.: vendedor@jmym.com y disenador@jmym.com (creados arriba).  Contraseña de todas: Jmym2026
INSERT INTO usuario (identificacion, nombre, correo, contrasena_hash, cod_rol, cod_sede)
SELECT v.ident, v.nombre, v.correo, '39f17d2561e6b18bce70e7350c84af97:edcf04acb4bf4959f998492445e5111a2532300a182a3571000e4ef3734562c3', r.cod_rol, s.cod_sede
FROM (VALUES
    ('1000000004', 'Vendedor The Police',  'vendedor.police@jmym.com',  'Vendedor',  'The Police'),
    ('1000000005', 'Diseñador The Police', 'disenador.police@jmym.com', 'Diseñador', 'The Police'),
    ('1000000006', 'Vendedor Lobo',        'vendedor.lobo@jmym.com',    'Vendedor',  'Lobo'),
    ('1000000007', 'Diseñador Lobo',       'disenador.lobo@jmym.com',   'Diseñador', 'Lobo')
) AS v (ident, nombre, correo, rol, sede)
JOIN rol  r ON r.nombre_rol  = v.rol
JOIN sede s ON s.nombre_sede = v.sede
ON CONFLICT DO NOTHING;

-- SECCION: 30 CLIENTES DE PRUEBA --------------------------------------
-- 30 clientes (cédulas 1090000001 a 1090000030), uno por orden, repartidos entre las 3 sedes:
--   1-9   Pendiente (algunos con abono)       10-14 En Producción
--   15-20 Listo para Entrega (15-17 pagadas = ya se pueden entregar; 18-20 con saldo = NO se pueden entregar)
--   21-30 Entregada (todas pagadas por completo)
-- Se crean con las mismas funciones y triggers del sistema, así que también quedan en la auditoría.
-- Para borrarlos: elimina las órdenes/clientes de prueba o vuelve a ejecutar el script completo.
DO $$
DECLARE
    nombres   TEXT[] := ARRAY['Carlos','María','Juan','Laura','Andrés','Camila','Felipe','Valentina','Santiago','Daniela',
                              'Sebastián','Paola','Julián','Natalia','Mateo','Sofía','Diego','Carolina','Esteban','Luisa',
                              'Nicolás','Juliana','Alejandro','Mónica','David','Ángela','Ricardo','Tatiana','Jorge','Diana'];
    apellidos TEXT[] := ARRAY['Gómez','Rodríguez','Martínez','López','García','Hernández','Torres','Ramírez','Díaz','Vargas',
                              'Castro','Moreno','Rojas','Ortiz','Silva','Cárdenas','Mejía','Pardo','Suárez','Ríos'];
    tipos     TEXT[] := ARRAY['Presilla','Tarjetero','Presilla Heráldica','Parche','Escudo','Insignia'];
    colores   TEXT[] := ARRAY['Azul','Rojo','Negro','Blanco','Dorado','Verde','Plateado'];
    sedes INT[]; vend INT[]; dise INT[];
    i INT; k INT; s INT; v_cliente INT; v_orden INT; v_total NUMERIC; v_primero NUMERIC; v_nombre TEXT;
BEGIN
    IF EXISTS (SELECT 1 FROM cliente WHERE cedula = '1090000001') THEN
        RAISE NOTICE 'Los 30 clientes de prueba ya existen: no se crean de nuevo.';
        RETURN;
    END IF;
    -- Posición 1 = J.M.Y.M., 2 = The Police, 3 = Lobo
    sedes := ARRAY[(SELECT cod_sede FROM sede WHERE nombre_sede = 'J.M.Y.M.'), (SELECT cod_sede FROM sede WHERE nombre_sede = 'The Police'), (SELECT cod_sede FROM sede WHERE nombre_sede = 'Lobo')];
    vend  := ARRAY[(SELECT cod_usuario FROM usuario WHERE correo = 'vendedor@jmym.com'), (SELECT cod_usuario FROM usuario WHERE correo = 'vendedor.police@jmym.com'), (SELECT cod_usuario FROM usuario WHERE correo = 'vendedor.lobo@jmym.com')];
    dise  := ARRAY[(SELECT cod_usuario FROM usuario WHERE correo = 'disenador@jmym.com'), (SELECT cod_usuario FROM usuario WHERE correo = 'disenador.police@jmym.com'), (SELECT cod_usuario FROM usuario WHERE correo = 'disenador.lobo@jmym.com')];

    FOR i IN 1..30 LOOP
        s := (i - 1) % 3 + 1;
        v_nombre := nombres[i] || ' ' || apellidos[(i * 7) % 20 + 1];
        INSERT INTO cliente (cedula, nombre, telefono)
        VALUES ((1090000000 + i)::TEXT, v_nombre, '31' || LPAD(((i * 1234567) % 100000000)::TEXT, 8, '0'))
        RETURNING cod_cliente INTO v_cliente;

        INSERT INTO orden (cod_cliente, cod_sede, cod_usuario) VALUES (v_cliente, sedes[s], vend[s]) RETURNING cod_orden INTO v_orden;
        FOR k IN 1..(1 + i % 3) LOOP
            INSERT INTO bordado (cod_orden, tipo_bordado, color, cantidad, precio_unitario, notas)
            VALUES (v_orden, tipos[(i + k) % 6 + 1], colores[(i * k) % 7 + 1], 5 + (i * k * 3) % 46, (3 + (i + k) % 8) * 1500,
                    CASE WHEN k = 1 THEN 'Bordar: ' || v_nombre END);
        END LOOP;
        SELECT total INTO v_total FROM orden WHERE cod_orden = v_orden;

        -- Producción: del 10 en adelante el diseñador la inicia; del 15 en adelante la deja lista
        IF i >= 10 THEN PERFORM sp_cambiar_estado_orden(v_orden, 'En Producción', dise[s]); END IF;
        IF i >= 15 THEN PERFORM sp_cambiar_estado_orden(v_orden, 'Listo para Entrega', dise[s]); END IF;

        -- Pagos: abono parcial (40 %), pago completo en dos abonos, o ninguno
        IF i IN (2, 5, 8, 10, 12, 18, 19) THEN
            INSERT INTO pago (cod_orden, cod_usuario, valor, medio_pago) VALUES (v_orden, vend[s], ROUND(v_total * 0.4 / 1000) * 1000, 'Efectivo');
        ELSIF i IN (15, 16, 17) OR i >= 21 THEN
            v_primero := ROUND(v_total * 0.5 / 1000) * 1000;
            INSERT INTO pago (cod_orden, cod_usuario, valor, medio_pago) VALUES (v_orden, vend[s], v_primero, 'Efectivo');
            INSERT INTO pago (cod_orden, cod_usuario, valor, medio_pago) VALUES (v_orden, vend[s], v_total - v_primero, 'Transferencia');
        END IF;

        IF i >= 21 THEN PERFORM sp_cambiar_estado_orden(v_orden, 'Entregada', vend[s]); END IF;

        -- Fechas de creación escalonadas (la más antigua hace 30 días)
        UPDATE orden SET fecha_creacion = NOW() - ((31 - i) || ' days')::INTERVAL WHERE cod_orden = v_orden;
    END LOOP;
END $$;
