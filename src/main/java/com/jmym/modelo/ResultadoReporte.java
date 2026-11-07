package com.jmym.modelo;

import java.util.ArrayList;

/** Resultado de un reporte: encabezados + filas (cada fila es un arreglo) + resumen de dinero. (Un "record" es una clase corta de solo datos.) */
public record ResultadoReporte(String[] columnas, ArrayList<Object[]> filas, double facturado, double cobrado) {
    public double saldo() { return facturado - cobrado; }
}
