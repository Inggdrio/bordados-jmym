package com.jmym.controlador;

import com.jmym.dao.AuditoriaDAO;
import com.jmym.dao.SedeDAO;
import com.jmym.modelo.Gerente;
import com.jmym.modelo.Sede;
import com.jmym.util.NegocioException;
import java.util.ArrayList;

/** Gestión de sedes (RF-002). Solo el Gerente puede crearlas o cambiarlas. */
public class SedeControlador {
    private final SedeDAO dao = new SedeDAO();
    private final AuditoriaDAO auditoria = new AuditoriaDAO();

    public ArrayList<Sede> listar(boolean soloActivas) { return dao.listar(soloActivas); }

    public void guardar(int codSede, String nombre, String direccion, String telefono) {
        Sesion.como(Gerente.class);
        Sede s = new Sede(nombre, direccion, telefono);
        s.setCodSede(codSede);
        if (dao.existeNombre(s.getNombre(), codSede)) throw new NegocioException("Ya existe una sede con ese nombre.");
        if (codSede == 0) dao.insertar(s); else dao.actualizar(s);
        auditoria.registrar(Sesion.getUsuario().getCodUsuario(), codSede == 0 ? "CREAR_SEDE" : "EDITAR_SEDE", "sede", null, null, "sede=" + s.getNombre());
    }

    /** Órdenes sin entregar de la sede (para avisar antes de inactivarla). */
    public int ordenesEnCurso(Sede s) { return dao.contarOrdenesEnCurso(s.getCodSede()); }

    public void cambiarEstado(Sede s, String nuevoEstado) {
        Sesion.como(Gerente.class);
        dao.cambiarEstado(s.getCodSede(), nuevoEstado);
        auditoria.registrar(Sesion.getUsuario().getCodUsuario(), "CAMBIO_ESTADO_SEDE", "sede", null, s.getNombre() + ": " + s.getEstado(), nuevoEstado);
    }
}
