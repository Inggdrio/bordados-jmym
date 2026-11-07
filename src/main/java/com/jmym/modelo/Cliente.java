package com.jmym.modelo;

import com.jmym.util.Validador;

/** Cliente que pide los bordados. HEREDA de Persona (su cédula es la identificación). */
public class Cliente extends Persona {
    private int codCliente;   // 0 = todavía no está guardado en la base de datos
    private String telefono;

    public Cliente(String cedula, String nombre, String telefono) { super(cedula, nombre); setTelefono(telefono); }

    public int getCodCliente() { return codCliente; }
    public void setCodCliente(int c) { codCliente = c; }
    public String getCedula() { return getIdentificacion(); }
    public String getTelefono() { return telefono; }

    public void setTelefono(String t) {
        if (!Validador.soloDigitos(t, 10)) throw new IllegalArgumentException("El teléfono debe tener solo números (máximo 10).");
        telefono = t.trim();
    }

    @Override public String descripcion() { return getNombre() + " (C.C. " + getCedula() + ")"; }
}
