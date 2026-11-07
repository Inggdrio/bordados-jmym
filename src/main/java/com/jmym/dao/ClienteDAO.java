package com.jmym.dao;

import com.jmym.modelo.Cliente;

/** Consultas y cambios de la tabla cliente. */
public class ClienteDAO extends BaseDAO {
    /** Busca un cliente por cédula (null si no existe). */
    public Cliente buscarPorCedula(String cedula) {
        return consultarUno("SELECT cod_cliente, cedula, nombre, telefono FROM cliente WHERE cedula = ?",
                rs -> { Cliente c = new Cliente(rs.getString(2), rs.getString(3), rs.getString(4)); c.setCodCliente(rs.getInt(1)); return c; }, cedula);
    }

    /** Guarda el cliente y le asigna el código que generó la base de datos. */
    public void insertar(Cliente c) {
        c.setCodCliente(consultarUno("INSERT INTO cliente (cedula, nombre, telefono) VALUES (?, ?, ?) RETURNING cod_cliente",
                rs -> rs.getInt(1), c.getCedula(), c.getNombre(), c.getTelefono()));
    }

    public void actualizar(Cliente c) { ejecutar("UPDATE cliente SET cedula = ?, nombre = ?, telefono = ? WHERE cod_cliente = ?", c.getCedula(), c.getNombre(), c.getTelefono(), c.getCodCliente()); }

    public boolean cedulaEnUso(String cedula, int excluirCod) {
        return consultarUno("SELECT COUNT(*) FROM cliente WHERE cedula = ? AND cod_cliente <> ?", rs -> rs.getInt(1), cedula, excluirCod) > 0;
    }
}
