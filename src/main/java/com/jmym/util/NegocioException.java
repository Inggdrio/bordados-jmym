package com.jmym.util;

/** Error que se le muestra al usuario (ej.: "Credenciales inválidas."). Es "unchecked": no hace falta declararla con throws. */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) { super(mensaje); }
}
