package com.jmym.vista;

import com.jmym.modelo.Usuario;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * CLASE ABSTRACTA de todas las pantallas (módulos): todas heredan título, fondo y el usuario.
 * Cada hija DEBE escribir alMostrar(): lo que se hace cada vez que se abre la pantalla.
 */
public abstract class PanelBase extends JPanel {
    protected final Usuario usuario;

    public PanelBase(String titulo, Usuario usuario) {
        super(new BorderLayout(0, 16));
        this.usuario = usuario;
        setBackground(Estilo.FONDO);
        setBorder(new EmptyBorder(24, 26, 24, 26));
        add(Estilo.etiqueta(titulo, Estilo.TITULO, Estilo.TEXTO), BorderLayout.NORTH);
    }

    /** Se ejecuta cada vez que el usuario abre esta pantalla (aquí se recargan los datos). */
    public abstract void alMostrar();
}
