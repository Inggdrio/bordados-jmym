package com.jmym.vista;

import com.jmym.modelo.*;
import com.jmym.util.Formato;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Detalle de una orden: datos del cliente y sus bordados.
 * conValores = false (Diseñador): sin precios. historial != null (Gerente): pestaña con la auditoría de la orden.
 */
public class DialogoDetalle {
    public static void mostrar(Component padre, Orden o, boolean conValores, ArrayList<Object[]> historial) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(padre), "Orden N° " + o.getCodOrden(), Dialog.ModalityType.APPLICATION_MODAL);
        d.getContentPane().setBackground(Color.WHITE);
        JPanel datos = new JPanel(new GridLayout(0, 2, 20, 4));
        datos.setOpaque(false);
        datos.setBorder(new EmptyBorder(16, 18, 10, 18));
        for (String t : new String[]{"Cliente: " + o.getCliente().descripcion(), "Teléfono: " + o.getCliente().getTelefono(), "Sede: " + o.getNombreSede(),
            "Estado: " + o.getEstado(), "Creada: " + Formato.fecha(o.getFechaCreacion()), conValores ? "Total: " + Formato.dinero(o.getTotal()) + "  |  Saldo: " + Formato.dinero(o.getSaldo()) : " "})
            datos.add(Estilo.etiqueta(t, Estilo.FUENTE, Estilo.TEXTO));

        Tabla bordados = conValores ? new Tabla("Tipo", "Color", "Cant.", "Precio", "Subtotal", "Notas") : new Tabla("Tipo", "Color", "Cant.", "Notas");
        for (Bordado b : o.getBordados())
            bordados.agregar(conValores ? new Object[]{b.getTipo(), b.getColor(), b.getCantidad(), Formato.dinero(b.getPrecioUnitario()), Formato.dinero(b.getSubtotal()), b.getNotas()}
                    : new Object[]{b.getTipo(), b.getColor(), b.getCantidad(), b.getNotas()});
        JComponent centro = bordados.scroll();
        if (historial != null) {
            Tabla h = new Tabla("Fecha", "Usuario", "Operación", "Antes", "Después");
            historial.forEach(f -> h.agregar(new Object[]{Formato.fechaHora((java.sql.Timestamp) f[0]), f[1], f[2], f[3], f[4]}));
            JTabbedPane pestanas = new JTabbedPane();
            pestanas.addTab("Bordados", centro);
            pestanas.addTab("Historial de la orden", h.scroll());
            centro = pestanas;
        }
        JButton cerrar = Estilo.boton("Cerrar");
        cerrar.addActionListener(e -> d.dispose());
        JPanel pie = Estilo.barra(FlowLayout.CENTER, cerrar);
        pie.setBorder(new EmptyBorder(10, 0, 12, 0));
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(new EmptyBorder(0, 18, 0, 18));
        contenedor.setOpaque(false);
        contenedor.add(centro);
        d.add(datos, BorderLayout.NORTH); d.add(contenedor); d.add(pie, BorderLayout.SOUTH);
        d.setSize(820, 440);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }
}
