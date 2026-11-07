package com.jmym.vista;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

/**
 * Tabla con el estilo de los mockups. Dos columnas especiales (por su nombre):
 *   - "Estado" / "Pago": se muestran como una píldora de color.
 *   - "Acciones": cada fila muestra enlaces (ej.: "Ver | Editar"); al hacer clic se avisa con alClic().
 * Para llenarla se usa agregar(fila, acciones...) y para vaciarla limpiar().
 */
public class Tabla extends JTable {
    private final DefaultTableModel modelo;
    private final ArrayList<String[]> acciones = new ArrayList<>();   // enlaces de cada fila
    private BiConsumer<Integer, String> alClic = (fila, accion) -> { };

    public Tabla(String... columnas) {
        super(new DefaultTableModel(columnas, 0) { @Override public boolean isCellEditable(int f, int c) { return false; } });
        modelo = (DefaultTableModel) getModel();
        setRowHeight(38); setShowGrid(false); setIntercellSpacing(new Dimension(0, 0)); setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < columnas.length; i++) getColumnModel().getColumn(i).setPreferredWidth(ancho(columnas[i]));   // las acciones y los estados necesitan más espacio
        getTableHeader().setDefaultRenderer((t, v, sel, foco, f, c) -> {
            JLabel l = Estilo.etiqueta(String.valueOf(v), Estilo.PEQUENA, Estilo.TEXTO);
            l.setOpaque(true); l.setBackground(Estilo.ENCABEZADO); l.setBorder(new EmptyBorder(11, 12, 11, 12));
            return l;
        });
        setDefaultRenderer(Object.class, (t, v, sel, foco, f, c) -> celda(v, sel, f, c));
        addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { clic(e); } });
    }

    private static int ancho(String columna) {
        return switch (columna) {
            case "N° orden" -> 92;
            case "N°" -> 70;
            case "Estado" -> 165;
            case "Pago", "Total", "Saldo" -> 115;
            case "Cliente", "Nombre" -> 150;
            case "Correo", "Dirección", "Notas" -> 170;
            default -> columna.startsWith("Acci") ? 200 : 105;
        };
    }

    public void limpiar() { modelo.setRowCount(0); acciones.clear(); }

    /** Agrega una fila (los valores de las columnas) y los enlaces de acción de esa fila. */
    public void agregar(Object[] fila, String... enlaces) {
        modelo.addRow(Arrays.copyOf(fila, modelo.getColumnCount()));   // si falta la columna Acciones se rellena con null
        acciones.add(enlaces);
    }

    public void alClic(BiConsumer<Integer, String> accion) { alClic = accion; }

    public JScrollPane scroll() {
        JScrollPane s = new JScrollPane(this);
        s.setBorder(null); s.setViewportBorder(null); s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    private boolean esAcciones(int columna) { return getColumnName(columna).startsWith("Acci"); }

    /** Dibuja una celda: texto normal, píldora de estado o enlaces de acción. */
    private Component celda(Object valor, boolean seleccionada, int fila, int columna) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 9));
        p.setBackground(seleccionada ? Estilo.SELECCION : Color.WHITE);
        p.setBorder(new MatteBorder(0, 0, 1, 0, Estilo.BORDE));
        String columnaNombre = getColumnName(columna), texto = valor == null ? "" : valor.toString();
        if (esAcciones(columna)) {
            String[] enlaces = fila < acciones.size() ? acciones.get(fila) : new String[0];
            for (int i = 0; i < enlaces.length; i++) {
                if (i > 0) p.add(Estilo.etiqueta("|", Estilo.FUENTE, Estilo.GRIS));
                p.add(Estilo.etiqueta(enlaces[i], Estilo.NEGRITA, Estilo.AZUL));
            }
        } else if (columnaNombre.equals("Estado") || columnaNombre.equals("Pago")) {
            p.add(new Pildora(texto));
        } else {
            p.add(Estilo.etiqueta(texto, Estilo.FUENTE, Estilo.TEXTO));
        }
        return p;
    }

    /** Averigua sobre qué enlace de la columna Acciones se hizo clic. */
    private void clic(MouseEvent e) {
        int f = rowAtPoint(e.getPoint()), c = columnAtPoint(e.getPoint());
        if (f < 0 || c < 0 || !esAcciones(c)) return;
        Rectangle r = getCellRect(f, c, false);
        JPanel celda = (JPanel) celda(null, false, f, c);
        celda.setSize(r.getSize());
        celda.doLayout();
        if (celda.getComponentAt(e.getX() - r.x, e.getY() - r.y) instanceof JLabel l && !l.getText().equals("|")) alClic.accept(f, l.getText());
    }

    /** Etiqueta redondeada de color para los estados. */
    private static class Pildora extends JLabel {
        Pildora(String texto) {
            super(texto);
            Color[] c = Estilo.coloresDe(texto);
            setBackground(c[0]); setForeground(c[1]); setFont(Estilo.PEQUENA); setOpaque(false);
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }

        @Override protected void paintComponent(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            super.paintComponent(g);
        }
    }
}
