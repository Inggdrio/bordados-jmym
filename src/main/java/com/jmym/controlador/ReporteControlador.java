package com.jmym.controlador;

import com.jmym.dao.ReporteDAO;
import com.jmym.modelo.Gerente;
import com.jmym.modelo.ResultadoReporte;
import com.jmym.modelo.Sede;
import com.jmym.util.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/** Reportes e indicadores (RF-007). Solo el Gerente. */
public class ReporteControlador {
    private final ReporteDAO dao = new ReporteDAO();

    public ResultadoReporte generar(String tipo, Sede sede, String desdeTexto, String hastaTexto) {
        Sesion.como(Gerente.class);
        LocalDate desde = Formato.aFecha(desdeTexto), hasta = Formato.aFecha(hastaTexto);
        if (desde != null && hasta != null && hasta.isBefore(desde)) throw new NegocioException("Rango de fechas inválido.");
        Integer cod = sede == null ? null : sede.getCodSede();
        double[] dinero = dao.resumen(cod, desde, hasta);
        ArrayList<Object[]> filas = new ArrayList<>();
        String[] columnas;
        if (tipo.equals("Bordados realizados")) {
            columnas = new String[]{"Tipo de bordado", "Unidades", "Órdenes"};
            dao.bordadosRealizados(cod, desde, hasta).forEach(f -> filas.add(new Object[]{f[0], f[1], f[2]}));
        } else if (tipo.equals("Ventas por sede")) {
            columnas = new String[]{"Sede", "Órdenes", "Facturado", "Cobrado", "Saldo"};
            dao.ventasPorSede(cod, desde, hasta).forEach(f -> {
                double facturado = ((Number) f[2]).doubleValue(), cobrado = ((Number) f[3]).doubleValue();
                filas.add(new Object[]{f[0], f[1], Formato.dinero(facturado), Formato.dinero(cobrado), Formato.dinero(facturado - cobrado)});
            });
        } else {
            columnas = new String[]{"N° orden", "Cliente", "Sede", "Estado", "Creación", "Entrega"};
            dao.trazabilidad(cod, desde, hasta).forEach(f -> filas.add(new Object[]{f[0], f[1], f[2], f[3], Formato.fecha((Timestamp) f[4]), Formato.fecha((Timestamp) f[5])}));
        }
        if (filas.isEmpty()) throw new NegocioException("No hay información para los filtros seleccionados.");
        return new ResultadoReporte(columnas, filas, dinero[0], dinero[1]);
    }

    /** {órdenes activas, listas para entrega, sedes activas, saldo pendiente} del panel de inicio. */
    public double[] indicadores() { Sesion.como(Gerente.class); return dao.indicadores(); }
}
