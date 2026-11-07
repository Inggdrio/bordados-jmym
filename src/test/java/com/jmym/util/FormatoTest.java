package com.jmym.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del formateo de dinero y fechas.
 * Responsable: módulo Reportes (Sebastián Lasso).
 */
@DisplayName("Formato - dinero y fechas")
class FormatoTest {

    @Test
    @DisplayName("dinero(): antepone '$ ' y conserva los dígitos del valor redondeado")
    void dinero() {
        String texto = Formato.dinero(15000);
        assertTrue(texto.startsWith("$ "), "Debe empezar con '$ '");
        // Robusto ante el separador de miles de cada JVM: comparamos solo los dígitos.
        assertEquals("15000", texto.replaceAll("[^0-9]", ""));
        assertEquals("0", Formato.dinero(0).replaceAll("[^0-9]", ""));
        assertEquals("1501", Formato.dinero(1500.6).replaceAll("[^0-9]", "")); // redondea
    }

    @Test
    @DisplayName("fecha(): formatea dd/MM/yyyy y muestra '—' cuando es nula")
    void fecha() {
        Timestamp t = Timestamp.valueOf("2026-10-25 12:00:00");
        assertEquals("25/10/2026", Formato.fecha(t));
        assertEquals("—", Formato.fecha(null));
        assertEquals("—", Formato.fechaHora(null));
    }

    @Test
    @DisplayName("aFecha(): convierte texto válido y null cuando está vacío")
    void aFecha() {
        assertEquals(LocalDate.of(2026, 10, 25), Formato.aFecha("25/10/2026"));
        assertNull(Formato.aFecha(""));
        assertNull(Formato.aFecha(null));
    }

    @Test
    @DisplayName("aFecha(): lanza NegocioException con una fecha mal escrita")
    void aFechaInvalida() {
        assertThrows(NegocioException.class, () -> Formato.aFecha("2026-10-25"));
        assertThrows(NegocioException.class, () -> Formato.aFecha("32/13/2026"));
    }
}
