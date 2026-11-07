package com.jmym.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las validaciones de formularios.
 * Responsable: módulo Autenticación / utilidades (German Darío Bolaños).
 */
@DisplayName("Validador - validaciones de formularios")
class ValidadorTest {

    @Test
    @DisplayName("vacio(): detecta nulo, cadena vacía y solo espacios")
    void vacio() {
        assertTrue(Validador.vacio(null));
        assertTrue(Validador.vacio(""));
        assertTrue(Validador.vacio("   "));
        assertFalse(Validador.vacio("hola"));
    }

    @Test
    @DisplayName("soloDigitos(): acepta solo números dentro del máximo")
    void soloDigitos() {
        assertTrue(Validador.soloDigitos("12345", 12));
        assertFalse(Validador.soloDigitos("", 12));          // debe tener al menos un dígito
        assertFalse(Validador.soloDigitos("12a", 12));        // letras no
        assertFalse(Validador.soloDigitos("1234", 3));        // pasa del máximo
        assertFalse(Validador.soloDigitos(null, 12));
    }

    @Test
    @DisplayName("correoValido(): acepta correos con formato usuario@dominio.ext")
    void correoValido() {
        assertTrue(Validador.correoValido("vendedor@jmym.com"));
        assertTrue(Validador.correoValido("a.b@dominio.co"));
        assertFalse(Validador.correoValido("sinarroba.com"));
        assertFalse(Validador.correoValido("sin@punto"));
        assertFalse(Validador.correoValido("con espacio@jmym.com"));
        assertFalse(Validador.correoValido(null));
    }

    @Test
    @DisplayName("aDinero(): limpia símbolos y separadores y devuelve el número")
    void aDinero() {
        assertEquals(15000.0, Validador.aDinero("15.000", "Valor"));
        assertEquals(15000.0, Validador.aDinero("$ 15000", "Valor"));
        assertEquals(0.0, Validador.aDinero("0", "Valor"));
    }

    @Test
    @DisplayName("aDinero(): lanza NegocioException si está vacío o es demasiado largo")
    void aDineroInvalido() {
        assertThrows(NegocioException.class, () -> Validador.aDinero("", "Valor"));
        assertThrows(NegocioException.class, () -> Validador.aDinero("abc", "Valor"));
        assertThrows(NegocioException.class, () -> Validador.aDinero("12345678901", "Valor")); // 11 dígitos
    }

    @Test
    @DisplayName("aEntero(): convierte hasta 6 dígitos y recorta espacios")
    void aEntero() {
        assertEquals(42, Validador.aEntero("  42 ", "N°"));
        assertThrows(NegocioException.class, () -> Validador.aEntero("1234567", "N°")); // 7 dígitos
        assertThrows(NegocioException.class, () -> Validador.aEntero("12a", "N°"));
        assertThrows(NegocioException.class, () -> Validador.aEntero(null, "N°"));
    }
}
