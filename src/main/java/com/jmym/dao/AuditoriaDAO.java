package com.jmym.dao;

import java.sql.Timestamp;
import java.util.ArrayList;

/** Guarda y consulta la auditoría (quién hizo qué y cuándo). */
public class AuditoriaDAO extends BaseDAO {
    /** codOrden puede ser null cuando la operación no es sobre una orden. */
    public void registrar(int codUsuario, String operacion, String tabla, Integer codOrden, String anterior, String nuevo) {
        ejecutar("SELECT sp_registrar_auditoria(?, ?, ?, ?, ?, ?)", codUsuario, operacion, tabla, codOrden, anterior, nuevo);
    }

    /** Historial de una orden: cada fila es un arreglo {fecha, usuario, operación, antes, después}. */
    public ArrayList<Object[]> porOrden(int codOrden) {
        return consultar("SELECT a.fecha_hora, COALESCE(u.nombre, '—'), a.operacion, COALESCE(a.valor_anterior, ''), COALESCE(a.valor_nuevo, '') "
                + "FROM auditoria a LEFT JOIN usuario u ON u.cod_usuario = a.cod_usuario WHERE a.cod_orden = ? ORDER BY a.cod_auditoria",
                rs -> new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)}, codOrden);
    }
}
