package com.jmym.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/** Contraseñas cifradas: se guarda "sal:hash" donde hash = SHA-256(sal + contraseña). */
public class SeguridadUtil {
    public static String generarHash(String clave) {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        String sal = hex(b);
        return sal + ":" + sha256(sal + clave);
    }

    public static boolean verificar(String clave, String guardado) {
        String[] p = guardado == null ? new String[0] : guardado.split(":");
        return p.length == 2 && sha256(p[0] + clave).equals(p[1]);
    }

    private static String sha256(String t) {
        try { return hex(MessageDigest.getInstance("SHA-256").digest(t.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte x : bytes) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
