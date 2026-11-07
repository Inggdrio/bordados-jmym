package com.jmym.vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;

/**
 * Estilo de todas las pantallas (colores, letras y componentes ya diseñados) tomado de los mockups:
 * menú azul, fondo gris claro, tarjetas blancas y botones cuadrados. Los métodos static crean
 * botones, campos, tarjetas, etc. con ese estilo.
 */
public class Estilo {
    public static final Color AZUL = new Color(0x0D4A9B), AZUL_OSCURO = new Color(0x0A3A7A), TEXTO = new Color(0x0F2A5C), GRIS = new Color(0x5B6F8F),
            FONDO = new Color(0xEEF2F9), BORDE = new Color(0xD5DEEC), ENCABEZADO = new Color(0xE9EFF9), SELECCION = new Color(0xDCE7F7), ERROR = new Color(0x9B1C1C);
    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 13), NEGRITA = FUENTE.deriveFont(Font.BOLD),
            PEQUENA = FUENTE.deriveFont(Font.BOLD, 11f), TITULO = FUENTE.deriveFont(Font.BOLD, 26f);

    /** Se llama una sola vez al iniciar el programa. */
    public static void aplicar() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) { /* se usa el estilo por defecto */ }
        for (String k : new String[]{"Label.font", "TextField.font", "PasswordField.font", "ComboBox.font", "OptionPane.messageFont", "TextArea.font", "TabbedPane.font"}) UIManager.put(k, FUENTE);
        UIManager.put("OptionPane.buttonFont", NEGRITA);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Button.disabledText", GRIS);
    }

    // ------------------------------------------------------------ Componentes
    public static JButton boton(String texto) { return boton(texto, AZUL, Color.WHITE, new EmptyBorder(9, 18, 9, 18)); }

    /** Botón secundario: fondo blanco, letras y borde azules. */
    public static JButton botonClaro(String texto) { return boton(texto, Color.WHITE, AZUL, new CompoundBorder(new LineBorder(AZUL), new EmptyBorder(8, 17, 8, 17))); }

    private static JButton boton(String texto, Color fondo, Color letra, Border borde) {
        JButton b = new JButton(texto);
        b.setFont(NEGRITA); b.setBackground(fondo); b.setForeground(letra); b.setBorder(borde);
        b.setFocusPainted(false); b.setOpaque(true); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static <T extends JTextComponent> T campo(T c) {
        c.setFont(FUENTE); c.setForeground(TEXTO);
        c.setBorder(new CompoundBorder(new LineBorder(BORDE), new EmptyBorder(7, 10, 7, 10)));
        return c;
    }

    public static JTextField campo() { return campo(new JTextField()); }

    /** Campo de texto que muestra una pista en gris cuando está vacío (ej.: "Buscar orden..."). */
    public static JTextField campoConPista(String pista) {
        return campo(new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g.setColor(new Color(0x8A9BB8)); g.setFont(FUENTE);
                    g.drawString(pista, getInsets().left, (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
                }
            }
        });
    }

    public static JComboBox<Object> combo(Object... elementos) {
        JComboBox<Object> c = new JComboBox<>(elementos);
        c.setFont(FUENTE); c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 34));
        return c;
    }

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente); l.setForeground(color);
        return l;
    }

    /** Etiqueta arriba y componente abajo (así se ven los campos en los mockups). */
    public static JPanel conEtiqueta(String texto, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(etiqueta(texto, PEQUENA, TEXTO), BorderLayout.NORTH);
        p.add(c);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));   // no se estira hacia abajo
        return p;
    }

    /** Varios componentes en una fila, del mismo ancho. */
    public static JPanel fila(JComponent... componentes) {
        JPanel p = new JPanel(new GridLayout(1, 0, 12, 0));
        p.setOpaque(false);
        for (JComponent c : componentes) p.add(c);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    /** Panel con la barra de componentes alineados a la izquierda o derecha. */
    public static JPanel barra(int alineacion, JComponent... componentes) {
        JPanel p = new JPanel(new FlowLayout(alineacion, 10, 0));
        p.setOpaque(false);
        for (JComponent c : componentes) p.add(c);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Tarjeta blanca con borde suave y un título opcional (null = sin título). Su contenido se agrega al centro. */
    public static JPanel tarjeta(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(BORDE));
        if (titulo != null) { JLabel t = etiqueta(titulo, NEGRITA.deriveFont(15f), TEXTO); t.setBorder(new EmptyBorder(14, 16, 10, 16)); p.add(t, BorderLayout.NORTH); }
        return p;
    }

    /** Agrega espacio alrededor de un componente (para formularios dentro de una tarjeta). */
    public static <T extends JComponent> T conRelleno(T c) { c.setBorder(new EmptyBorder(0, 16, 16, 16)); return c; }

    public static JPanel vertical() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    /** Logo de la empresa (archivo logo.png en resources) escalado al ancho pedido. */
    public static JLabel logo(int ancho) {
        java.net.URL url = Estilo.class.getResource("/logo.png");
        if (url == null) return new JLabel("LOGO", SwingConstants.CENTER);
        ImageIcon i = new ImageIcon(url);
        return new JLabel(new ImageIcon(i.getImage().getScaledInstance(ancho, i.getIconHeight() * ancho / i.getIconWidth(), Image.SCALE_SMOOTH)));
    }

    /** {fondo, letra} de cada estado (las "píldoras" de color de las tablas). */
    public static Color[] coloresDe(String estado) {
        return switch (estado) {
            case "Pendiente" -> new Color[]{new Color(0xFFE9BF), new Color(0x8A5A00)};
            case "En Producción", "Abonado" -> new Color[]{new Color(0xD6E6FB), AZUL};
            case "Listo para Entrega", "Activo", "Activa", "Pagado" -> new Color[]{new Color(0xD3F0DC), new Color(0x1F6B3A)};
            case "Entregada" -> new Color[]{new Color(0xE3E7EE), new Color(0x3F4B5E)};
            default -> new Color[]{new Color(0xF9D6D6), ERROR};   // Inactivo, Inactiva, Bloqueado
        };
    }

    // ------------------------------------------------------------ Mensajes
    /** Ejecuta una acción y, si falla por una regla del negocio o un dato inválido, muestra el aviso en una ventana. */
    public static void intentar(Runnable accion) {
        try { accion.run(); }
        catch (com.jmym.util.NegocioException | IllegalArgumentException e) { error(null, e.getMessage()); }
    }

    /** Conecta un botón con una acción protegida por intentar(). */
    public static void click(AbstractButton b, Runnable accion) { b.addActionListener(e -> intentar(accion)); }

    public static void error(Component padre, String mensaje) { JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE); }
    public static void info(Component padre, String mensaje) { JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE); }

    public static boolean confirmar(Component padre, String pregunta) {
        Object[] opciones = {"Sí", "No"};
        return JOptionPane.showOptionDialog(padre, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]) == 0;
    }
}
