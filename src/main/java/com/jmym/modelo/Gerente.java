package com.jmym.modelo;

/** El Gerente administra usuarios y sedes, supervisa las órdenes y ve los reportes. */
public class Gerente extends Usuario {
    public Gerente(String identificacion, String nombre, String correo) { super(identificacion, nombre, correo); }
    @Override public String getRol() { return "Gerente"; }
    @Override public String[] getModulos() { return new String[]{"Inicio", "Sedes", "Usuarios", "Órdenes", "Reportes"}; }
}
