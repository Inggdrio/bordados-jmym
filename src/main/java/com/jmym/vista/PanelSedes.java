package com.jmym.vista;

import com.jmym.controlador.SedeControlador;
import com.jmym.modelo.Sede;
import com.jmym.modelo.Usuario;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Módulo "Sedes" (Gerente): administrar las sedes J.M.Y.M., The Police y Lobo. */
public class PanelSedes extends PanelBase {
    private final SedeControlador ctrl = new SedeControlador();
    private final Tabla tabla = new Tabla("Nombre", "Dirección", "Teléfono", "Estado", "Acciones");
    private final JLabel lblTotal = Estilo.etiqueta("0", Estilo.TITULO.deriveFont(24f), Estilo.TEXTO);
    private ArrayList<Sede> sedes = new ArrayList<>();

    public PanelSedes(Usuario usuario) {
        super("Gestión de sedes", usuario);
        JPanel total = Estilo.tarjeta(null), texto = Estilo.vertical();
        texto.setBorder(new EmptyBorder(14, 16, 14, 40));
        texto.add(Estilo.etiqueta("Total de sedes", Estilo.FUENTE, Estilo.GRIS)); texto.add(lblTotal);
        total.add(texto);
        JButton nueva = Estilo.boton("+ Nueva sede");
        Estilo.click(nueva, () -> formulario(null));
        tabla.alClic((fila, accion) -> Estilo.intentar(() -> accion(sedes.get(fila), accion)));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(total, BorderLayout.WEST);
        JPanel boton = new JPanel(new BorderLayout());
        boton.setOpaque(false);
        boton.add(nueva, BorderLayout.SOUTH);
        barra.add(boton, BorderLayout.EAST);
        JPanel tarjeta = Estilo.tarjeta(null);
        tarjeta.add(tabla.scroll());
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(barra, BorderLayout.NORTH); contenido.add(tarjeta);
        add(contenido);
    }

    @Override public void alMostrar() {
        sedes = ctrl.listar(false);
        lblTotal.setText(String.valueOf(sedes.size()));
        tabla.limpiar();
        for (Sede s : sedes) tabla.agregar(new Object[]{s.getNombre(), s.getDireccion(), s.getTelefono(), s.getEstado()}, "Editar", s.getEstado().equals("Activa") ? "Inactivar" : "Activar");
    }

    private void accion(Sede s, String accion) {
        if (accion.equals("Editar")) { formulario(s); return; }
        String pregunta = "¿Cambiar el estado de la sede " + s.getNombre() + " a " + (accion.equals("Activar") ? "Activa" : "Inactiva") + "?";
        int enCurso = accion.equals("Inactivar") ? ctrl.ordenesEnCurso(s) : 0;
        if (enCurso > 0) pregunta = "La sede tiene " + enCurso + " orden(es) sin entregar.\nSeguirán visibles, pero la sede no aparecerá en los formularios.\n¿Desea inactivarla?";
        if (Estilo.confirmar(this, pregunta)) { ctrl.cambiarEstado(s, accion.equals("Activar") ? "Activa" : "Inactiva"); alMostrar(); }
    }

    /** Ventana para crear (s = null) o editar una sede. */
    private void formulario(Sede s) {
        Formulario f = new Formulario(this, s == null ? "Nueva sede" : "Editar sede");
        JTextField nombre = f.agregar("Nombre", Estilo.campo()), direccion = f.agregar("Dirección", Estilo.campo()), telefono = f.agregar("Teléfono", Estilo.campo());
        if (s != null) { nombre.setText(s.getNombre()); direccion.setText(s.getDireccion()); telefono.setText(s.getTelefono()); }
        f.mostrar("Guardar", () -> { ctrl.guardar(s == null ? 0 : s.getCodSede(), nombre.getText(), direccion.getText(), telefono.getText()); alMostrar(); });
    }
}
