package com.jmym.modelo;

/** Crea el tipo de usuario correcto según el rol. Devuelve un "Usuario" que en realidad es Gerente, Vendedor o Diseñador (POLIMORFISMO). */
public class UsuarioFactory {
    public static Usuario crear(String rol, String identificacion, String nombre, String correo) {
        return switch (rol) {
            case "Gerente" -> new Gerente(identificacion, nombre, correo);
            case "Vendedor" -> new Vendedor(identificacion, nombre, correo);
            case "Diseñador" -> new Disenador(identificacion, nombre, correo);
            default -> throw new IllegalArgumentException("Rol desconocido: " + rol);
        };
    }
}
