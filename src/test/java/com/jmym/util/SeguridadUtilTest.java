package com.jmym.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del cifrado de contraseñas (sal + SHA-256).
 * Responsable: módulo Autenticación / seguridad (German Darío Bolaños).
 */
@DisplayName("SeguridadUtil - cifrado de contraseñas")
class SeguridadUtilTest {

    @Test
    @DisplayName("generarHash(): devuelve el formato sal(32):hash(64)")
    void formatoDelHash() {
        String guardado = SeguridadUtil.generarHash("Jmym2026");
        String[] partes = guardado.split(":");
        assertEquals(2, partes.length, "Debe ser sal:hash");
        assertEquals(32, partes[0].length(), "La sal son 16 bytes = 32 hex");
        assertEquals(64, partes[1].length(), "SHA-256 son 32 bytes = 64 hex");
    }

    @Test
    @DisplayName("verificar(): la contraseña correcta coincide y la incorrecta no")
    void verificar() {
        String guardado = SeguridadUtil.generarHash("Jmym2026");
        assertTrue(SeguridadUtil.verificar("Jmym2026", guardado));
        assertFalse(SeguridadUtil.verificar("otra", guardado));
    }

    @Test
    @DisplayName("verificar(): valores nulos o mal formados no coinciden")
    void verificarValoresRaros() {
        assertFalse(SeguridadUtil.verificar("x", null));
        assertFalse(SeguridadUtil.verificar("x", "sinDosPuntos"));
        assertFalse(SeguridadUtil.verificar("x", "a:b:c"));
    }

    @Test
    @DisplayName("generarHash(): dos hashes de la misma clave son distintos (sal aleatoria)")
    void salAleatoria() {
        assertNotEquals(SeguridadUtil.generarHash("Jmym2026"),
                        SeguridadUtil.generarHash("Jmym2026"));
    }
}
