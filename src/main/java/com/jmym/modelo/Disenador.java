package com.jmym.modelo;

/** El Diseñador produce los bordados. No ve valores en pesos. */
public class Disenador extends Usuario {
    public Disenador(String identificacion, String nombre, String correo) { super(identificacion, nombre, correo); }
    @Override public String getRol() { return "Diseñador"; }
    @Override public String[] getModulos() { return new String[]{"Producción", "Historial"}; }
}
