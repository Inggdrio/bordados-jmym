package com.jmym.util;

/** Validaciones sencillas de los formularios. */
public class Validador {
    public static boolean vacio(String t) { return t == null || t.trim().isEmpty(); }
    public static boolean soloDigitos(String t, int max) { return t != null && t.matches("[0-9]{1," + max + "}"); }
    public static boolean correoValido(String c) { return c != null && c.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"); }

    /** "15.000" o "$ 15000" -> 15000 (los pesos no usan decimales). */
    public static double aDinero(String t, String campo) {
        String n = t == null ? "" : t.replaceAll("[^0-9]", "");
        if (n.isEmpty() || n.length() > 10) throw new NegocioException("El campo " + campo + " debe ser un número válido.");
        return Double.parseDouble(n);
    }

    public static int aEntero(String t, String campo) {
        if (t == null || !t.trim().matches("[0-9]{1,6}")) throw new NegocioException("El campo " + campo + " debe ser un número entero.");
        return Integer.parseInt(t.trim());
    }
}
