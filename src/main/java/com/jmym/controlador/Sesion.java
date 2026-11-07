package com.jmym.controlador;

import com.jmym.modelo.Usuario;

/** Guarda quién tiene la sesión abierta. */
public class Sesion {
    private static Usuario actual;
    public static void iniciar(Usuario u) { actual = u; }
    public static Usuario getUsuario() { return actual; }
    public static void cerrar() { actual = null; }

    /** Devuelve el usuario actual si es del rol pedido (Gerente.class, Vendedor.class...); si no, avisa que no tiene permisos. */
    public static <T extends Usuario> T como(Class<T> rol) {
        if (!rol.isInstance(actual)) throw new com.jmym.util.NegocioException("No tiene permisos para esta función.");
        return rol.cast(actual);
    }
}
