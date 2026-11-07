package com.jmym.modelo;

import com.jmym.util.Validador;

/** Una sede de la empresa (J.M.Y.M., The Police o Lobo). */
public class Sede {
    private int codSede;
    private String nombre, direccion, telefono, estado = "Activa";

    public Sede(String nombre, String direccion, String telefono) {
        if (Validador.vacio(nombre) || Validador.vacio(direccion)) throw new IllegalArgumentException("El nombre y la dirección de la sede son obligatorios.");
        if (!Validador.soloDigitos(telefono, 10)) throw new IllegalArgumentException("El teléfono debe tener solo números (máximo 10).");
        this.nombre = nombre.trim(); this.direccion = direccion.trim(); this.telefono = telefono.trim();
    }

    public int getCodSede() { return codSede; }
    public void setCodSede(int c) { codSede = c; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { estado = e; }

    @Override public String toString() { return nombre; }   // lo que se ve en las listas desplegables
    @Override public boolean equals(Object o) { return o instanceof Sede s && s.codSede == codSede; }   // dos objetos Sede con el mismo código son la misma sede
    @Override public int hashCode() { return codSede; }
}
