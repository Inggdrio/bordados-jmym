package com.jmym.dao;

import com.jmym.modelo.Pago;
import java.util.ArrayList;

/** Consultas y cambios de la tabla pago. */
public class PagoDAO extends BaseDAO {
    public ArrayList<Pago> listarPorOrden(int cod) {
        return consultar("SELECT cod_pago, cod_orden, valor, medio_pago, fecha_pago FROM pago WHERE cod_orden = ? ORDER BY cod_pago",
                rs -> { Pago p = new Pago(rs.getInt(2), rs.getDouble(3), rs.getString(4), rs.getTimestamp(5)); p.setCodPago(rs.getInt(1)); return p; }, cod);
    }

    /** Los triggers de la base de datos validan el saldo y actualizan el estado de pago. */
    public void insertar(Pago p, int codUsuario) {
        p.setCodPago(consultarUno("INSERT INTO pago (cod_orden, cod_usuario, valor, medio_pago, fecha_pago) VALUES (?, ?, ?, ?, ?) RETURNING cod_pago",
                rs -> rs.getInt(1), p.getCodOrden(), codUsuario, p.getValor(), p.getMedio(), p.getFecha()));
    }
}
