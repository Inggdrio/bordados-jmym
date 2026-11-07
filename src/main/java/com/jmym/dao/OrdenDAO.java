package com.jmym.dao;

import com.jmym.modelo.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/** Consultas y cambios de las tablas orden y bordado. */
public class OrdenDAO extends BaseDAO {
    private final ClienteDAO clientes = new ClienteDAO();

    /** conValores = false oculta los valores en pesos (el Diseñador no los ve). */
    private String select(boolean conValores) {
        return "SELECT o.cod_orden, c.cod_cliente, c.cedula, c.nombre, c.telefono, s.nombre_sede, o.cod_sede, o.cod_usuario, o.estado, o.estado_pago, "
                + "o.fecha_creacion, o.fecha_entrega, " + (conValores ? "o.total" : "0")
                + ", " + (conValores ? "COALESCE((SELECT SUM(p.valor) FROM pago p WHERE p.cod_orden = o.cod_orden), 0)" : "0")
                + ", (SELECT MAX(a.fecha_hora) FROM auditoria a WHERE a.cod_orden = o.cod_orden AND a.operacion = 'CAMBIO_ESTADO' AND a.valor_nuevo = 'Listo para Entrega') "
                + "FROM orden o JOIN cliente c ON c.cod_cliente = o.cod_cliente JOIN sede s ON s.cod_sede = o.cod_sede ";
    }

    private Orden mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(rs.getString(3), rs.getString(4), rs.getString(5));
        c.setCodCliente(rs.getInt(2));
        Orden o = new Orden(c);
        o.setCodOrden(rs.getInt(1)); o.setNombreSede(rs.getString(6)); o.setCodSede(rs.getInt(7)); o.setCodVendedor(rs.getInt(8));
        o.setEstado(rs.getString(9)); o.setEstadoPago(rs.getString(10)); o.setFechaCreacion(rs.getTimestamp(11)); o.setFechaEntrega(rs.getTimestamp(12));
        o.setTotal(rs.getDouble(13)); o.setTotalPagado(rs.getDouble(14)); o.setFechaFinalizada(rs.getTimestamp(15));
        return o;
    }

    /**
     * Lista órdenes con filtros opcionales.
     * codVendedor: solo las de ese vendedor (0 = todas) | texto: N° de orden, nombre o cédula |
     * sede: nombre de la sede (null = todas) | estados: ARREGLO con los estados que se quieren ver.
     */
    public ArrayList<Orden> listar(int codVendedor, String texto, String sede, String[] estados, boolean conValores) {
        StringBuilder sql = new StringBuilder(select(conValores) + "WHERE o.estado = ANY(string_to_array(?, ',')) ");
        ArrayList<Object> p = new ArrayList<>();
        p.add(String.join(",", estados));
        if (codVendedor > 0) { sql.append("AND o.cod_usuario = ? "); p.add(codVendedor); }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (CAST(o.cod_orden AS TEXT) = ? OR LOWER(c.nombre) LIKE ? OR c.cedula LIKE ?) ");
            p.add(texto.trim()); p.add("%" + texto.trim().toLowerCase() + "%"); p.add("%" + texto.trim() + "%");
        }
        if (sede != null) { sql.append("AND s.nombre_sede = ? "); p.add(sede); }
        return consultar(sql + "ORDER BY o.cod_orden DESC", this::mapear, p.toArray());
    }

    /** Una orden con todos sus bordados (null si no existe). */
    public Orden obtener(int cod, boolean conValores) {
        Orden o = consultarUno(select(conValores) + "WHERE o.cod_orden = ?", this::mapear, cod);
        if (o != null) o.getBordados().addAll(listarBordados(cod, conValores));
        return o;
    }

    /** Sin valores se usa la vista v_bordado_produccion, que NO tiene precios. */
    public ArrayList<Bordado> listarBordados(int cod, boolean conValores) {
        return consultar("SELECT cod_bordado, tipo_bordado, color, cantidad, " + (conValores ? "precio_unitario" : "1") + ", notas FROM "
                + (conValores ? "bordado" : "v_bordado_produccion") + " WHERE cod_orden = ? ORDER BY cod_bordado",
                rs -> { Bordado b = new Bordado(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getDouble(5), rs.getString(6)); b.setCodBordado(rs.getInt(1)); return b; }, cod);
    }

    // tipo_uniforme y descripcion ya no se piden: quedan vacías (NULL) en la tabla.
    private void insertarBordado(int cod, Bordado b) {
        ejecutar("INSERT INTO bordado (cod_orden, tipo_bordado, color, cantidad, precio_unitario, notas) VALUES (?, ?, ?, ?, ?, ?)",
                cod, b.getTipo(), b.getColor(), b.getCantidad(), b.getPrecioUnitario(), b.getNotas().isEmpty() ? null : b.getNotas());
    }

    /** Guarda cliente (si es nuevo), orden y bordados en UNA transacción. */
    public int crear(Orden o, int codVendedor) {
        int[] cod = new int[1];
        transaccion(() -> {
            Cliente c = o.getCliente();
            if (c.getCodCliente() == 0) clientes.insertar(c); else clientes.actualizar(c);
            cod[0] = consultarUno("INSERT INTO orden (cod_cliente, cod_sede, cod_usuario) VALUES (?, ?, ?) RETURNING cod_orden", rs -> rs.getInt(1), c.getCodCliente(), o.getCodSede(), codVendedor);
            o.getBordados().forEach(b -> insertarBordado(cod[0], b));
        });
        return cod[0];
    }

    /** Reemplaza los bordados de una orden Pendiente: primero agrega los nuevos y luego borra los viejos (el total nunca baja a cero a la mitad). */
    public void reemplazarBordados(Orden o) {
        transaccion(() -> {
            ArrayList<Bordado> viejos = listarBordados(o.getCodOrden(), true);
            o.getBordados().forEach(b -> insertarBordado(o.getCodOrden(), b));
            viejos.forEach(b -> ejecutar("DELETE FROM bordado WHERE cod_bordado = ?", b.getCodBordado()));
            clientes.actualizar(o.getCliente());
        });
    }

    public void cambiarEstado(int cod, String nuevoEstado, int codUsuario) { ejecutar("SELECT sp_cambiar_estado_orden(?, ?, ?)", cod, nuevoEstado, codUsuario); }
    public void eliminar(int cod, int codUsuario) { ejecutar("SELECT sp_eliminar_orden(?, ?)", cod, codUsuario); }

    /** ¿El mismo cliente ya tiene una orden Pendiente en esa sede con exactamente los mismos bordados? */
    public boolean existeDuplicada(Orden nueva) {
        if (nueva.getCliente().getCodCliente() == 0) return false;
        ArrayList<String> firmas = firmas(nueva.getBordados());
        for (int cod : consultar("SELECT cod_orden FROM orden WHERE cod_cliente = ? AND cod_sede = ? AND estado = 'Pendiente'", rs -> rs.getInt(1),
                nueva.getCliente().getCodCliente(), nueva.getCodSede())) {
            if (firmas(listarBordados(cod, true)).equals(firmas)) return true;
        }
        return false;
    }

    private ArrayList<String> firmas(ArrayList<Bordado> bordados) {
        ArrayList<String> lista = new ArrayList<>();
        bordados.forEach(b -> lista.add(b.firma()));
        Collections.sort(lista);
        return lista;
    }
}
