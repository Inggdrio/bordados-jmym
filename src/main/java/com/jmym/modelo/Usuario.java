package com.jmym.modelo;

import com.jmym.util.Validador;
import java.util.Arrays;

/**
 * CLASE ABSTRACTA de los usuarios del sistema. Sus hijas (Gerente, Vendedor y Diseñador)
 * definen su rol y los módulos que pueden abrir (HERENCIA + POLIMORFISMO).
 */
public abstract class Usuario extends Persona {
    private int codUsuario, codSede;
    private String correo, contrasenaHash, nombreSede, estado = "Activo";

    public Usuario(String identificacion, String nombre, String correo) { super(identificacion, nombre); setCorreo(correo); }

    public abstract String getRol();
    /** Cada rol devuelve su lista de módulos en un ARREGLO. */
    public abstract String[] getModulos();

    public boolean puedeAcceder(String modulo) { return Arrays.asList(getModulos()).contains(modulo); }

    public int getCodUsuario() { return codUsuario; }
    public void setCodUsuario(int c) { codUsuario = c; }
    public int getCodSede() { return codSede; }
    public void setCodSede(int c) { codSede = c; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String h) { contrasenaHash = h; }
    public String getNombreSede() { return nombreSede; }
    public void setNombreSede(String s) { nombreSede = s; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { estado = e; }

    public void setCorreo(String c) {
        if (!Validador.correoValido(c)) throw new IllegalArgumentException("El correo no tiene un formato válido.");
        correo = c.trim().toLowerCase();
    }

    @Override public String descripcion() { return getNombre() + " (" + getRol() + ")"; }
}
