package com.jmym;

import com.jmym.vista.Estilo;
import com.jmym.vista.VentanaLogin;
import javax.swing.SwingUtilities;

/** Punto de entrada del programa: aquí empieza todo (el método main). */
public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicar();
            new VentanaLogin().setVisible(true);
        });
    }
}
