package com.jmym.vista;

import com.jmym.conexion.ConexionBD;
import com.jmym.controlador.Sesion;
import com.jmym.modelo.*;
import com.jmym.util.Constantes;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Ventana principal (como los mockups): menú azul con el logo y los módulos del rol, encabezado
 * "Panel de control" con el rol, y la zona central donde se muestra cada módulo.
 */
public class VentanaPrincipal extends JFrame {
    private final Usuario usuario;
    private final CardLayout cartas = new CardLayout();
    private final JPanel zona = new JPanel(cartas);
    private final HashMap<String, PanelBase> paneles = new HashMap<>();
    private final HashMap<String, JButton> botones = new HashMap<>();
    private final Timer inactividad;
    private final AWTEventListener actividad;

    public VentanaPrincipal(Usuario usuario) {
        super("Sistema de Gestión de Pedidos de Bordados J.M.Y.M.");
        this.usuario = usuario;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { ConexionBD.cerrar(); System.exit(0); } });

        JPanel derecha = new JPanel(new BorderLayout());
        zona.setBackground(Estilo.FONDO);
        derecha.add(encabezado(), BorderLayout.NORTH); derecha.add(zona);
        add(menuLateral(), BorderLayout.WEST); add(derecha);

        for (String modulo : usuario.getModulos()) {   // un panel por cada módulo que permite el rol
            paneles.put(modulo, crearPanel(modulo));
            zona.add(paneles.get(modulo), modulo);
        }
        irA(usuario.getModulos()[0]);

        // Cierre de sesión por inactividad (RNF-001)
        inactividad = new Timer(Constantes.MINUTOS_INACTIVIDAD * 60 * 1000, e -> { Estilo.info(this, "La sesión se cerró por inactividad."); cerrarSesion(); });
        inactividad.setRepeats(false);
        inactividad.start();
        actividad = e -> inactividad.restart();
        Toolkit.getDefaultToolkit().addAWTEventListener(actividad, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1050, 660));
        setLocationRelativeTo(null);
    }

    private JPanel menuLateral() {
        JPanel menu = Estilo.vertical();
        menu.setOpaque(true);
        menu.setBackground(Estilo.AZUL);
        menu.setBorder(new EmptyBorder(14, 10, 14, 10));
        menu.setPreferredSize(new Dimension(214, 0));
        JLabel logo = Estilo.logo(170), subtitulo = Estilo.etiqueta("Gestión de pedidos de bordados", Estilo.FUENTE.deriveFont(10.5f), new Color(0xD5E4FA));
        logo.setAlignmentX(CENTER_ALIGNMENT); subtitulo.setAlignmentX(CENTER_ALIGNMENT);
        menu.add(logo); menu.add(Box.createVerticalStrut(6)); menu.add(subtitulo); menu.add(Box.createVerticalStrut(20));
        for (String modulo : usuario.getModulos()) {
            JButton b = new JButton(modulo);
            b.setFont(Estilo.NEGRITA); b.setForeground(Color.WHITE); b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(11, 14, 11, 14)); b.setFocusPainted(false); b.setOpaque(true);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); b.setAlignmentX(CENTER_ALIGNMENT);
            b.addActionListener(e -> Estilo.intentar(() -> irA(modulo)));
            botones.put(modulo, b);
            menu.add(b); menu.add(Box.createVerticalStrut(4));
        }
        menu.add(Box.createVerticalGlue());
        JButton cerrar = Estilo.botonClaro("Cerrar sesión");
        cerrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); cerrar.setAlignmentX(CENTER_ALIGNMENT);
        cerrar.addActionListener(e -> { if (Estilo.confirmar(this, "¿Desea cerrar la sesión?")) cerrarSesion(); });
        menu.add(cerrar);
        return menu;
    }

    private JPanel encabezado() {
        JLabel avatar = new JLabel(usuario.getRol().substring(0, 2).toUpperCase(), SwingConstants.CENTER) {   // círculo azul con las iniciales del rol
            @Override protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Estilo.AZUL);
                g.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        avatar.setFont(Estilo.PEQUENA); avatar.setForeground(Color.WHITE); avatar.setPreferredSize(new Dimension(32, 32));
        JLabel rol = Estilo.etiqueta(usuario.getRol(), Estilo.NEGRITA, Estilo.TEXTO);
        rol.setToolTipText(usuario.getNombre() + " - Sede " + usuario.getNombreSede());
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Estilo.BORDE), new EmptyBorder(8, 26, 8, 26)));
        p.add(Estilo.etiqueta("Panel de control", Estilo.NEGRITA, Estilo.TEXTO), BorderLayout.WEST);
        JPanel usuarioPanel = Estilo.barra(FlowLayout.RIGHT, avatar, rol);
        p.add(usuarioPanel, BorderLayout.EAST);
        return p;
    }

    /** Crea la pantalla de cada módulo. Todas son hijas de PanelBase, por eso se devuelven como PanelBase (polimorfismo). */
    private PanelBase crearPanel(String modulo) {
        return switch (modulo) {
            case "Inicio" -> new PanelInicio(usuario);
            case "Sedes" -> new PanelSedes(usuario);
            case "Usuarios" -> new PanelUsuarios(usuario);
            case "Reportes" -> new PanelReportes(usuario);
            case "Pagos" -> new PanelPagos(usuario);
            case "Producción" -> new PanelProduccion(usuario);
            case "Historial" -> new PanelHistorial(usuario);
            case "Órdenes" -> usuario instanceof Vendedor ? new PanelOrdenesVendedor(usuario) : new PanelOrdenesGerente(usuario);
            default -> throw new IllegalArgumentException("Módulo desconocido: " + modulo);
        };
    }

    private void irA(String modulo) {
        cartas.show(zona, modulo);
        botones.forEach((nombre, b) -> b.setBackground(nombre.equals(modulo) ? Estilo.AZUL_OSCURO : Estilo.AZUL));
        paneles.get(modulo).alMostrar();
    }

    private void cerrarSesion() {
        inactividad.stop();
        Toolkit.getDefaultToolkit().removeAWTEventListener(actividad);
        Sesion.cerrar();
        dispose();
        new VentanaLogin().setVisible(true);
    }
}
