package com.jmym.vista;

import com.jmym.controlador.SedeControlador;
import com.jmym.controlador.UsuarioControlador;
import com.jmym.modelo.Sede;
import com.jmym.modelo.Usuario;
import com.jmym.util.Constantes;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.*;

/** Módulo "Usuarios" (Gerente): buscar, crear, editar, bloquear, inactivar y reactivar usuarios. */
public class PanelUsuarios extends PanelBase {
    private final UsuarioControlador ctrl = new UsuarioControlador();
    private final Tabla tabla = new Tabla("Cédula", "Nombre", "Correo", "Rol", "Sede", "Estado", "Acciones");
    private final JTextField txtBuscar = Estilo.campoConPista("Buscar...");
    private final JComboBox<Object> cmbRol = Estilo.combo("Todos los roles"), cmbEstado = Estilo.combo("Todos los estados", "Activo", "Inactivo", "Bloqueado");
    private final ArrayList<Usuario> visibles = new ArrayList<>();   // misma posición que las filas

    public PanelUsuarios(Usuario usuario) {
        super("Gestión de usuarios", usuario);
        for (String r : Constantes.ROLES) cmbRol.addItem(r);
        txtBuscar.setPreferredSize(new java.awt.Dimension(260, 34));
        JButton nuevo = Estilo.boton("+ Nuevo usuario");
        Estilo.click(nuevo, () -> formulario(null));
        txtBuscar.addActionListener(e -> Estilo.intentar(this::alMostrar));
        cmbRol.addActionListener(e -> Estilo.intentar(this::alMostrar));
        cmbEstado.addActionListener(e -> Estilo.intentar(this::alMostrar));
        tabla.alClic((fila, accion) -> Estilo.intentar(() -> accion(visibles.get(fila), accion)));

        JPanel barra = new JPanel(new java.awt.BorderLayout());
        barra.setOpaque(false);
        barra.add(Estilo.barra(FlowLayout.LEFT, txtBuscar, cmbRol, cmbEstado), java.awt.BorderLayout.WEST);
        barra.add(nuevo, java.awt.BorderLayout.EAST);
        JPanel tarjeta = Estilo.tarjeta(null);
        tarjeta.add(tabla.scroll());
        JPanel contenido = new JPanel(new java.awt.BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(barra, java.awt.BorderLayout.NORTH); contenido.add(tarjeta);
        add(contenido);
    }

    @Override public void alMostrar() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        visibles.clear();
        tabla.limpiar();
        for (Usuario u : ctrl.listar()) {
            boolean coincide = (texto.isEmpty() || (u.getNombre() + u.getCorreo() + u.getIdentificacion()).toLowerCase().contains(texto))
                    && (cmbRol.getSelectedIndex() == 0 || u.getRol().equals(cmbRol.getSelectedItem()))
                    && (cmbEstado.getSelectedIndex() == 0 || u.getEstado().equals(cmbEstado.getSelectedItem()));
            if (!coincide) continue;
            visibles.add(u);
            tabla.agregar(new Object[]{u.getIdentificacion(), u.getNombre(), u.getCorreo(), u.getRol(), u.getNombreSede(), u.getEstado()},
                    u.getEstado().equals("Activo") ? new String[]{"Editar", "Bloquear", "Inactivar"} : new String[]{"Editar", "Reactivar"});
        }
    }

    private void accion(Usuario u, String accion) {
        if (accion.equals("Editar")) { formulario(u); return; }
        String estado = accion.equals("Bloquear") ? "Bloqueado" : accion.equals("Inactivar") ? "Inactivo" : "Activo";
        if (Estilo.confirmar(this, "¿Cambiar el estado de " + u.getNombre() + " a " + estado + "?")) { ctrl.cambiarEstado(u, estado); alMostrar(); }
    }

    /** Ventana para crear (u = null) o editar un usuario. */
    private void formulario(Usuario u) {
        Formulario f = new Formulario(this, u == null ? "Nuevo usuario" : "Editar usuario");
        JTextField cedula = f.agregar("Cédula", Estilo.campo()), nombre = f.agregar("Nombre", Estilo.campo()), correo = f.agregar("Correo", Estilo.campo());
        JPasswordField clave = f.agregar(u == null ? "Contraseña" : "Contraseña (vacía = no cambiar)", Estilo.campo(new JPasswordField()));
        JComboBox<Object> rol = f.agregar("Rol", Estilo.combo((Object[]) Constantes.ROLES)), sede = f.agregar("Sede", Estilo.combo(new SedeControlador().listar(true).toArray()));
        if (u != null) {
            cedula.setText(u.getIdentificacion()); nombre.setText(u.getNombre()); correo.setText(u.getCorreo()); rol.setSelectedItem(u.getRol());
            for (int i = 0; i < sede.getItemCount(); i++) if (((Sede) sede.getItemAt(i)).getCodSede() == u.getCodSede()) sede.setSelectedIndex(i);
        }
        f.mostrar("Guardar", () -> {
            ctrl.guardar(u == null ? 0 : u.getCodUsuario(), cedula.getText(), nombre.getText(), correo.getText(), new String(clave.getPassword()), (String) rol.getSelectedItem(), (Sede) sede.getSelectedItem());
            alMostrar();
        });
    }
}
