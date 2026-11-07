package com.jmym.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de los roles de usuario y la fábrica (herencia + polimorfismo).
 * Responsable: módulo Autenticación / control de acceso (German Darío Bolaños).
 */
@DisplayName("Usuario - roles, módulos y fábrica")
class UsuarioTest {

    @Test
    @DisplayName("Cada rol expone su nombre y sus módulos")
    void rolesYModulos() {
        assertEquals("Vendedor", new Vendedor("1", "V", "v@jmym.com").getRol());
        assertEquals("Gerente", new Gerente("1", "G", "g@jmym.com").getRol());
        assertEquals("Diseñador", new Disenador("1", "D", "d@jmym.com").getRol());

        assertArrayEquals(new String[]{"Órdenes", "Pagos", "Historial"},
                new Vendedor("1", "V", "v@jmym.com").getModulos());
        assertArrayEquals(new String[]{"Producción", "Historial"},
                new Disenador("1", "D", "d@jmym.com").getModulos());
    }

    @Test
    @DisplayName("puedeAcceder(): respeta los módulos permitidos por rol")
    void puedeAcceder() {
        Vendedor v = new Vendedor("1", "V", "v@jmym.com");
        assertTrue(v.puedeAcceder("Pagos"));
        assertFalse(v.puedeAcceder("Reportes"));           // el vendedor no ve reportes

        Gerente g = new Gerente("1", "G", "g@jmym.com");
        assertTrue(g.puedeAcceder("Reportes"));
    }

    @Test
    @DisplayName("El correo se valida y se guarda en minúsculas")
    void correo() {
        assertEquals("v@jmym.com", new Vendedor("1", "V", "V@JMYM.COM").getCorreo());
        assertThrows(IllegalArgumentException.class, () -> new Vendedor("1", "V", "sin-arroba"));
    }

    @Test
    @DisplayName("UsuarioFactory.crear(): devuelve la clase correcta según el rol")
    void fabrica() {
        assertInstanceOf(Vendedor.class, UsuarioFactory.crear("Vendedor", "1", "V", "v@jmym.com"));
        assertInstanceOf(Gerente.class, UsuarioFactory.crear("Gerente", "1", "G", "g@jmym.com"));
        assertInstanceOf(Disenador.class, UsuarioFactory.crear("Diseñador", "1", "D", "d@jmym.com"));
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioFactory.crear("Rol Inventado", "1", "X", "x@jmym.com"));
    }
}
