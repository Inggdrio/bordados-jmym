package com.jmym.util;

import java.util.Arrays;

/** Listas fijas del negocio guardadas en ARREGLOS (arrays). */
public class Constantes {
    public static final String[] ROLES = {"Gerente", "Vendedor", "Diseñador"};
    public static final String[] ESTADOS_ORDEN = {"Pendiente", "En Producción", "Listo para Entrega", "Entregada"};
    public static final String[] TIPOS_BORDADO = {"Presilla", "Tarjetero", "Presilla Heráldica", "Parche"};
    public static final String[] MEDIOS_PAGO = {"Efectivo", "Transferencia"};
    public static final String[] TIPOS_REPORTE = {"Bordados realizados", "Ventas por sede", "Trazabilidad de órdenes"};
    public static final int MAX_INTENTOS = 3;          // intentos fallidos antes de bloquear la cuenta
    public static final int MINUTOS_INACTIVIDAD = 10;  // cierre de sesión automático

    /** Posición de un texto dentro de un arreglo (-1 si no está). */
    public static int posicion(String[] lista, String valor) { return Arrays.asList(lista).indexOf(valor); }
}
