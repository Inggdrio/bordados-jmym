package com.jmym.controlador;

import com.jmym.dao.AuditoriaDAO;
import com.jmym.dao.UsuarioDAO;
import com.jmym.modelo.*;
import com.jmym.util.*;
import java.util.ArrayList;

/** Gestión de usuarios y roles (RF-001). Solo el Gerente. */
public class UsuarioControlador {
    private final UsuarioDAO dao = new UsuarioDAO();
    private final AuditoriaDAO auditoria = new AuditoriaDAO();

    public ArrayList<Usuario> listar() { Sesion.como(Gerente.class); return dao.listar(); }

    /** codUsuario = 0 crea un usuario nuevo; si es mayor a 0 edita el existente. */
    public void guardar(int codUsuario, String identificacion, String nombre, String correo, String clave, String rol, Sede sede) {
        Usuario gerente = Sesion.como(Gerente.class);
        if (Validador.vacio(identificacion) || Validador.vacio(nombre) || Validador.vacio(correo) || rol == null || sede == null) throw new NegocioException("Complete todos los campos obligatorios.");
        if (codUsuario == 0 && Validador.vacio(clave)) throw new NegocioException("La contraseña es obligatoria para un usuario nuevo.");
        if (!Validador.vacio(clave) && clave.length() < 6) throw new NegocioException("La contraseña debe tener al menos 6 caracteres.");
        Usuario u = UsuarioFactory.crear(rol, identificacion.trim(), nombre, correo);
        u.setCodUsuario(codUsuario);
        u.setCodSede(sede.getCodSede());
        if (dao.existeDuplicado(u.getIdentificacion(), u.getCorreo(), codUsuario)) throw new NegocioException("La cédula o el correo ya están registrados.");
        boolean cambiarClave = !Validador.vacio(clave);
        if (cambiarClave) u.setContrasenaHash(SeguridadUtil.generarHash(clave));
        if (codUsuario == 0) dao.insertar(u); else dao.actualizar(u, cambiarClave);
        auditoria.registrar(gerente.getCodUsuario(), codUsuario == 0 ? "CREAR_USUARIO" : "EDITAR_USUARIO", "usuario", null, null, "correo=" + u.getCorreo() + "; rol=" + rol);
    }

    /** Bloquear, Inactivar o Reactivar ("Activo"). */
    public void cambiarEstado(Usuario u, String nuevoEstado) {
        Usuario gerente = Sesion.como(Gerente.class);
        if (u.getCodUsuario() == gerente.getCodUsuario()) throw new NegocioException("No puede cambiar el estado de su propia cuenta.");
        dao.cambiarEstado(u.getCodUsuario(), nuevoEstado);
        auditoria.registrar(gerente.getCodUsuario(), "CAMBIO_ESTADO_USUARIO", "usuario", null, u.getCorreo() + ": " + u.getEstado(), nuevoEstado);
    }
}
