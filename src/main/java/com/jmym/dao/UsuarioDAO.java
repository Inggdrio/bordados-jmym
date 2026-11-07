package com.jmym.dao;

import com.jmym.modelo.Usuario;
import com.jmym.modelo.UsuarioFactory;
import com.jmym.util.Constantes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Consultas y cambios de la tabla usuario. */
public class UsuarioDAO extends BaseDAO {
    private static final String SELECT = "SELECT u.cod_usuario, u.identificacion, u.nombre, u.correo, u.contrasena_hash, r.nombre_rol, "
            + "u.cod_sede, s.nombre_sede, u.estado FROM usuario u JOIN rol r ON r.cod_rol = u.cod_rol JOIN sede s ON s.cod_sede = u.cod_sede ";

    /** Convierte una fila en un Gerente, Vendedor o Diseñador. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = UsuarioFactory.crear(rs.getString(6), rs.getString(2), rs.getString(3), rs.getString(4));
        u.setCodUsuario(rs.getInt(1)); u.setContrasenaHash(rs.getString(5)); u.setCodSede(rs.getInt(7));
        u.setNombreSede(rs.getString(8)); u.setEstado(rs.getString(9));
        return u;
    }

    public Usuario buscarPorCorreo(String correo) { return consultarUno(SELECT + "WHERE u.correo = ?", this::mapear, correo); }
    public ArrayList<Usuario> listar() { return consultar(SELECT + "ORDER BY u.nombre", this::mapear); }
    public void registrarIntentoFallido(String correo) { ejecutar("SELECT sp_registrar_intento_fallido(?, ?)", correo, Constantes.MAX_INTENTOS); }
    public void reiniciarIntentos(int cod) { ejecutar("UPDATE usuario SET intentos_fallidos = 0 WHERE cod_usuario = ?", cod); }

    /** Cambia el estado (Activo, Inactivo o Bloqueado). Al reactivar se reinician los intentos. */
    public void cambiarEstado(int cod, String estado) { ejecutar("UPDATE usuario SET estado = ?, intentos_fallidos = 0 WHERE cod_usuario = ?", estado, cod); }

    public boolean existeDuplicado(String identificacion, String correo, int excluirCod) {
        return consultarUno("SELECT COUNT(*) FROM usuario WHERE (identificacion = ? OR correo = ?) AND cod_usuario <> ?",
                rs -> rs.getInt(1), identificacion, correo, excluirCod) > 0;
    }

    public void insertar(Usuario u) {
        ejecutar("INSERT INTO usuario (identificacion, nombre, correo, contrasena_hash, cod_rol, cod_sede) "
                + "VALUES (?, ?, ?, ?, (SELECT cod_rol FROM rol WHERE nombre_rol = ?), ?)",
                u.getIdentificacion(), u.getNombre(), u.getCorreo(), u.getContrasenaHash(), u.getRol(), u.getCodSede());
    }

    /** Actualiza el usuario. La contraseña solo cambia si cambiarClave es true. */
    public void actualizar(Usuario u, boolean cambiarClave) {
        String sql = "UPDATE usuario SET identificacion = ?, nombre = ?, correo = ?, cod_rol = (SELECT cod_rol FROM rol WHERE nombre_rol = ?), cod_sede = ?"
                + (cambiarClave ? ", contrasena_hash = ?" : "") + " WHERE cod_usuario = ?";
        if (cambiarClave) ejecutar(sql, u.getIdentificacion(), u.getNombre(), u.getCorreo(), u.getRol(), u.getCodSede(), u.getContrasenaHash(), u.getCodUsuario());
        else ejecutar(sql, u.getIdentificacion(), u.getNombre(), u.getCorreo(), u.getRol(), u.getCodSede(), u.getCodUsuario());
    }
}
