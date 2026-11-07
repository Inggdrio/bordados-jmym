package com.jmym.conexion;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Abre la conexión con Neon (PostgreSQL). Todos los DAO comparten esta conexión.
 * Lee "config.properties": primero el archivo junto al programa, y si no existe, el del proyecto.
 */
public class ConexionBD {
    private static Connection conexion;

    public static Connection obtener() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(3)) {
            DriverManager.setLoginTimeout(20);
            Properties p = new Properties();
            File f = new File("config.properties");
            try (InputStream in = f.exists() ? new FileInputStream(f) : ConexionBD.class.getResourceAsStream("/config.properties")) {
                p.load(new InputStreamReader(in, "UTF-8"));
            } catch (Exception e) {
                throw new SQLException("No se pudo leer config.properties", "08001", e);
            }
            conexion = DriverManager.getConnection(
                    System.getenv().getOrDefault("DB_URL", p.getProperty("db.url")),
                    System.getenv().getOrDefault("DB_USER", p.getProperty("db.usuario")),
                    System.getenv().getOrDefault("DB_PASSWORD", p.getProperty("db.clave")));
        }
        return conexion;
    }

    public static void cerrar() {
        try { if (conexion != null) conexion.close(); } catch (SQLException e) { /* ya estaba cerrada */ }
        conexion = null;
    }
}
