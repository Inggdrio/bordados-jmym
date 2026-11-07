package com.jmym.controlador;

import com.jmym.dao.UsuarioDAO;
import com.jmym.modelo.Usuario;
import com.jmym.util.*;

/** Inicio de sesión (RF-001). */
public class LoginControlador {
    private final UsuarioDAO dao = new UsuarioDAO();

    public Usuario iniciarSesion(String correo, String clave) {
        if (Validador.vacio(correo) || Validador.vacio(clave)) throw new NegocioException("Ingrese su correo y su contraseña.");
        String c = correo.trim().toLowerCase();
        Usuario u = dao.buscarPorCorreo(c);
        if (u == null) throw new NegocioException("Credenciales inválidas.");
        if (u.getEstado().equals("Bloqueado")) throw new NegocioException("Cuenta bloqueada por intentos fallidos. Contacte al Gerente.");
        if (!u.getEstado().equals("Activo")) throw new NegocioException("Credenciales inválidas o usuario bloqueado.");
        if (!SeguridadUtil.verificar(clave, u.getContrasenaHash())) {
            dao.registrarIntentoFallido(c);   // al 3.er intento se bloquea
            throw new NegocioException("Credenciales inválidas.");
        }
        dao.reiniciarIntentos(u.getCodUsuario());
        Sesion.iniciar(u);
        return u;
    }
}
