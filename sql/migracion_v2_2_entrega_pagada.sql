-- =====================================================================
-- MIGRACIÓN v2.2  (solo si YA tienes la base creada con una versión anterior)
--   1. Regla nueva: una orden con saldo pendiente NO se puede entregar.
--   2. Cuentas de las sedes The Police y Lobo (Vendedor y Diseñador).
--   3. 30 clientes de prueba con órdenes Pendientes, En Producción, Listas para Entrega y Entregadas.
-- No borra datos. Se puede ejecutar más de una vez (no duplica nada).
-- Ejecútalo en DBeaver como script (Alt + X). Si ejecutas el script completo, NO lo necesitas.
-- =====================================================================

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
