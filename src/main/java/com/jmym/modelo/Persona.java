package com.jmym.modelo;

import com.jmym.util.Validador;

/**
 * CLASE ABSTRACTA: base de Cliente y Usuario (no se puede crear con "new Persona()").
 * ENCAPSULACIÓN: atributos privados; los set validan el dato antes de guardarlo.
 */
public abstract class Persona {
    private String identificacion, nombre;

    public Persona(String identificacion, String nombre) { setIdentificacion(identificacion); setNombre(nombre); }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }

    public void setIdentificacion(String v) {
        if (!Validador.soloDigitos(v, 12)) throw new IllegalArgumentException("La identificación debe tener solo números (máximo 12).");
        identificacion = v.trim();
    }

    public void setNombre(String v) {
        if (Validador.vacio(v) || v.trim().length() > 100) throw new IllegalArgumentException("El nombre es obligatorio (máximo 100 letras).");
        nombre = v.trim();
    }

    /** Cada clase hija escribe su propia descripción (POLIMORFISMO). */
    public abstract String descripcion();

    @Override public String toString() { return descripcion(); }
}
