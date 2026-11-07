package com.jmym.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/** Consultas de los reportes y del panel de inicio del Gerente. Cada fila es un arreglo (Object[]). */
public class ReporteDAO extends BaseDAO {
    private static final String PAGOS = "LEFT JOIN (SELECT cod_orden, SUM(valor) AS pagado FROM pago GROUP BY cod_orden) pg ON pg.cod_orden = o.cod_orden ";

    /** Condiciones del filtro (sede y fechas); los valores se agregan a "p". */
    private String filtros(Integer sede, LocalDate desde, LocalDate hasta, ArrayList<Object> p) {
        String w = "";
        if (sede != null) { w += " AND o.cod_sede = ?"; p.add(sede); }
        if (desde != null) { w += " AND o.fecha_creacion >= ?"; p.add(Timestamp.valueOf(desde.atStartOfDay())); }
        if (hasta != null) { w += " AND o.fecha_creacion < ?"; p.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay())); }
        return w;
    }

    private ArrayList<Object[]> filas(String sql, int columnas, Object... p) {
        return consultar(sql, rs -> { Object[] f = new Object[columnas]; for (int i = 0; i < columnas; i++) f[i] = rs.getObject(i + 1); return f; }, p);
    }

    /** {total facturado, total cobrado} */
    public double[] resumen(Integer sede, LocalDate desde, LocalDate hasta) {
        ArrayList<Object> p = new ArrayList<>();
        Object[] f = filas("SELECT COALESCE(SUM(o.total), 0), COALESCE(SUM(pg.pagado), 0) FROM orden o " + PAGOS + "WHERE 1 = 1" + filtros(sede, desde, hasta, p), 2, p.toArray()).get(0);
        return new double[]{((Number) f[0]).doubleValue(), ((Number) f[1]).doubleValue()};
    }

    public ArrayList<Object[]> bordadosRealizados(Integer sede, LocalDate desde, LocalDate hasta) {
        ArrayList<Object> p = new ArrayList<>();
        return filas("SELECT b.tipo_bordado, SUM(b.cantidad), COUNT(DISTINCT o.cod_orden) FROM bordado b JOIN orden o ON o.cod_orden = b.cod_orden "
                + "WHERE o.estado IN ('Listo para Entrega', 'Entregada')" + filtros(sede, desde, hasta, p) + " GROUP BY b.tipo_bordado ORDER BY 2 DESC", 3, p.toArray());
    }

    public ArrayList<Object[]> ventasPorSede(Integer sede, LocalDate desde, LocalDate hasta) {
        ArrayList<Object> p = new ArrayList<>();
        return filas("SELECT s.nombre_sede, COUNT(o.cod_orden), COALESCE(SUM(o.total), 0), COALESCE(SUM(pg.pagado), 0) FROM orden o JOIN sede s ON s.cod_sede = o.cod_sede "
                + PAGOS + "WHERE 1 = 1" + filtros(sede, desde, hasta, p) + " GROUP BY s.nombre_sede ORDER BY s.nombre_sede", 4, p.toArray());
    }

    public ArrayList<Object[]> trazabilidad(Integer sede, LocalDate desde, LocalDate hasta) {
        ArrayList<Object> p = new ArrayList<>();
        return filas("SELECT o.cod_orden, c.nombre, s.nombre_sede, o.estado, o.fecha_creacion, o.fecha_entrega FROM orden o JOIN cliente c ON c.cod_cliente = o.cod_cliente "
                + "JOIN sede s ON s.cod_sede = o.cod_sede WHERE 1 = 1" + filtros(sede, desde, hasta, p) + " ORDER BY o.cod_orden DESC", 6, p.toArray());
    }

    /** {órdenes activas, listas para entrega, sedes activas, saldo pendiente} para el panel de inicio. */
    public double[] indicadores() {
        Object[] f = filas("SELECT COUNT(*) FILTER (WHERE estado <> 'Entregada'), COUNT(*) FILTER (WHERE estado = 'Listo para Entrega'), "
                + "(SELECT COUNT(*) FROM sede WHERE estado = 'Activa'), COALESCE(SUM(total), 0) - COALESCE((SELECT SUM(valor) FROM pago), 0) FROM orden", 4).get(0);
        double[] r = new double[4];
        for (int i = 0; i < 4; i++) r[i] = ((Number) f[i]).doubleValue();
        return r;
    }
}
