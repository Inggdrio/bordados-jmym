package com.jmym.vista;

import com.jmym.controlador.LoginControlador;
import com.jmym.util.NegocioException;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** Pantalla de inicio de sesión: correo y contraseña (RF-001). */
public class VentanaLogin extends JFrame {
    private final JTextField txtCorreo = Estilo.campoConPista("usuario@correo.com");
    private final JPasswordField txtClave = Estilo.campo(new JPasswordField());
    private final JLabel lblMensaje = Estilo.etiqueta(" ", Estilo.PEQUENA, Estilo.ERROR);

    public VentanaLogin() {
        super("Sistema de Gestión de Pedidos de Bordados J.M.Y.M.");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Estilo.FONDO);

        JButton ingresar = Estilo.boton("Iniciar sesión");
        ingresar.addActionListener(e -> ingresar());
        JLabel logo = Estilo.logo(254), titulo = Estilo.etiqueta("Iniciar sesión", Estilo.TITULO.deriveFont(22f), Estilo.TEXTO),
                subtitulo = Estilo.etiqueta("Sistema de Gestión de Pedidos de Bordados J.M.Y.M.", Estilo.FUENTE, Estilo.GRIS);
        JPanel tarjeta = Estilo.vertical();
        tarjeta.setOpaque(true);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new CompoundBorder(new LineBorder(Estilo.BORDE), new EmptyBorder(26, 32, 26, 32)));
        for (JComponent c : new JComponent[]{logo, titulo, subtitulo}) c.setAlignmentX(CENTER_ALIGNMENT);
        tarjeta.add(logo); tarjeta.add(Box.createVerticalStrut(10)); tarjeta.add(titulo); tarjeta.add(subtitulo); tarjeta.add(Box.createVerticalStrut(16));
        for (JComponent c : new JComponent[]{Estilo.conEtiqueta("Correo electrónico", txtCorreo), Estilo.conEtiqueta("Contraseña", txtClave), lblMensaje, ingresar}) {
            c.setAlignmentX(CENTER_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
            tarjeta.add(c);
            tarjeta.add(Box.createVerticalStrut(8));
        }
        setLayout(new GridBagLayout());
        add(tarjeta);
        getRootPane().setDefaultButton(ingresar);   // la tecla Enter presiona "Iniciar sesión"
        setSize(820, 640);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            var usuario = new LoginControlador().iniciarSesion(txtCorreo.getText(), new String(txtClave.getPassword()));
            dispose();
            new VentanaPrincipal(usuario).setVisible(true);
        } catch (NegocioException e) {
            lblMensaje.setText(e.getMessage());
            txtClave.setText("");
        } finally { setCursor(Cursor.getDefaultCursor()); }
    }
}
