package com.jmym.dao;

import com.jmym.conexion.ConexionBD;
import com.jmym.util.NegocioException;
import java.sql.*;
import java.util.ArrayList;

/**
 * CLASE ABSTRACTA de la que heredan todos los DAO (DAO = clase que habla con la base de datos).
 * Aquí está el código JDBC que se repetía: consultar, ejecutar y manejar transacciones.
 * Cualquier error de SQL se convierte en un NegocioException con un mensaje claro.
 */
public abstract class BaseDAO {

    /** Convierte una fila (ResultSet) en un objeto. Se usa con lambdas:  rs -> new Algo(rs.getString(1)) */
    protected interface Fila<T> { T de(ResultSet rs) throws SQLException; }

    /** SELECT que devuelve una lista. Los "?" del SQL se reemplazan por los parámetros en orden. */
    protected <T> ArrayList<T> consultar(String sql, Fila<T> fila, Object... parametros) {
        try (PreparedStatement ps = preparar(sql, parametros); ResultSet rs = ps.executeQuery()) {
            ArrayList<T> lista = new ArrayList<>();
            while (rs.next()) lista.add(fila.de(rs));
            return lista;
        } catch (SQLException e) { throw error(e); }
    }

    /** Igual que consultar pero devuelve solo la primera fila (o null si no hay). */
    protected <T> T consultarUno(String sql, Fila<T> fila, Object... parametros) {
        ArrayList<T> lista = consultar(sql, fila, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** INSERT, UPDATE, DELETE o llamada a una función de la base de datos. */
    protected void ejecutar(String sql, Object... parametros) {
        try (PreparedStatement ps = preparar(sql, parametros)) { ps.execute(); }
        catch (SQLException e) { throw error(e); }
    }

    /** Ejecuta varias operaciones juntas: o se guardan todas, o ninguna. */
    protected void transaccion(Runnable tarea) {
        try {
            Connection c = ConexionBD.obtener();
            try { c.setAutoCommit(false); tarea.run(); c.commit(); }
            catch (RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        } catch (SQLException e) { throw error(e); }
    }

    private PreparedStatement preparar(String sql, Object[] parametros) throws SQLException {
        PreparedStatement ps = ConexionBD.obtener().prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
        return ps;
    }

    /** Convierte el error técnico de PostgreSQL en un mensaje que entienda el usuario. */
    private static NegocioException error(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08")) return new NegocioException("No se pudo conectar con la base de datos. Revise su internet y el archivo config.properties.");
        if (estado.equals("23505")) return new NegocioException("Ya existe un registro con esos datos.");
        String m = String.valueOf(e.getMessage()).split("\n")[0];
        return new NegocioException(m.startsWith("ERROR: ") ? m.substring(7) : m);
    }
}
