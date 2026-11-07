package com.jmym.vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Ventana emergente con campos (se usa para "Nuevo usuario", "Editar sede", etc.). */
public class Formulario extends JDialog {
    private final JPanel cuerpo = new JPanel(new GridLayout(0, 1, 0, 8));

    public Formulario(Component padre, String titulo) {
        super(SwingUtilities.getWindowAncestor(padre), titulo, ModalityType.APPLICATION_MODAL);
        cuerpo.setBorder(new EmptyBorder(18, 22, 6, 22));
        cuerpo.setBackground(Color.WHITE);
        getContentPane().setBackground(Color.WHITE);
        add(cuerpo);
    }

    /** Agrega un campo con su etiqueta y lo devuelve para poder leerlo después. */
    public <T extends JComponent> T agregar(String etiqueta, T campo) { cuerpo.add(Estilo.conEtiqueta(etiqueta, campo)); return campo; }

    /** Muestra la ventana. Si "guardar" lanza un error, se avisa y la ventana sigue abierta. */
    public void mostrar(String textoBoton, Runnable guardar) {
        JButton ok = Estilo.boton(textoBoton), cancelar = Estilo.botonClaro("Cancelar");
        Estilo.click(ok, () -> { guardar.run(); dispose(); });
        cancelar.addActionListener(e -> dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        pie.setBackground(Color.WHITE);
        pie.add(cancelar); pie.add(ok);
        add(pie, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(400, 0));
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
