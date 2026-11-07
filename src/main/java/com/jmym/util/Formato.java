package com.jmym.util;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Convierte números y fechas a texto (y al revés). */
public class Formato {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String dinero(double v) { return "$ " + NumberFormat.getIntegerInstance(Locale.of("es", "CO")).format(Math.round(v)); }
    public static String fecha(Timestamp t) { return t == null ? "—" : t.toLocalDateTime().format(FECHA); }
    public static String fechaHora(Timestamp t) { return t == null ? "—" : t.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")); }
    public static String hoy() { return LocalDate.now().format(FECHA); }

    /** "25/10/2026" -> LocalDate (null si el texto está vacío). */
    public static LocalDate aFecha(String t) {
        if (Validador.vacio(t)) return null;
        try { return LocalDate.parse(t.trim(), FECHA); }
        catch (DateTimeParseException e) { throw new NegocioException("Fecha inválida. Use el formato dd/mm/aaaa (ejemplo: 25/10/2026)."); }
    }
}
