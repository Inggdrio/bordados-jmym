package com.jmym.modelo;

/** El Vendedor registra órdenes, las modifica mientras están Pendientes, cobra y consulta su historial. */
public class Vendedor extends Usuario {
    public Vendedor(String identificacion, String nombre, String correo) { super(identificacion, nombre, correo); }
    @Override public String getRol() { return "Vendedor"; }
    @Override public String[] getModulos() { return new String[]{"Órdenes", "Pagos", "Historial"}; }
}
