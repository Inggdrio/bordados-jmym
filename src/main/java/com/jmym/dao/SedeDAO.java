package com.jmym.dao;

import com.jmym.modelo.Sede;
import java.util.ArrayList;

/** Consultas y cambios de la tabla sede. */
public class SedeDAO extends BaseDAO {
    public ArrayList<Sede> listar(boolean soloActivas) {
        return consultar("SELECT cod_sede, nombre_sede, direccion, telefono, estado FROM sede " + (soloActivas ? "WHERE estado = 'Activa' " : "") + "ORDER BY cod_sede",
                rs -> { Sede s = new Sede(rs.getString(2), rs.getString(3), rs.getString(4)); s.setCodSede(rs.getInt(1)); s.setEstado(rs.getString(5)); return s; });
    }

    public boolean existeNombre(String nombre, int excluirCod) {
        return consultarUno("SELECT COUNT(*) FROM sede WHERE LOWER(nombre_sede) = LOWER(?) AND cod_sede <> ?", rs -> rs.getInt(1), nombre, excluirCod) > 0;
    }

    public void insertar(Sede s) { ejecutar("INSERT INTO sede (nombre_sede, direccion, telefono) VALUES (?, ?, ?)", s.getNombre(), s.getDireccion(), s.getTelefono()); }

    public void actualizar(Sede s) {
        ejecutar("UPDATE sede SET nombre_sede = ?, direccion = ?, telefono = ? WHERE cod_sede = ?", s.getNombre(), s.getDireccion(), s.getTelefono(), s.getCodSede());
    }

    public void cambiarEstado(int cod, String estado) { ejecutar("UPDATE sede SET estado = ? WHERE cod_sede = ?", estado, cod); }

    /** Órdenes sin entregar que tiene la sede (para avisar antes de inactivarla). */
    public int contarOrdenesEnCurso(int cod) { return consultarUno("SELECT COUNT(*) FROM orden WHERE cod_sede = ? AND estado <> 'Entregada'", rs -> rs.getInt(1), cod); }
}
