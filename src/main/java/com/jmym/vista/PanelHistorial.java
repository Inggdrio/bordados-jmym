package com.jmym.vista;

import com.jmym.modelo.*;
import com.jmym.util.Formato;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JTextField;

/** Módulo "Historial": el Diseñador ve los pedidos que ya finalizó (sin precios) y el Vendedor sus órdenes entregadas (con valores). */
public class PanelHistorial extends PanelListaOrdenes {
    private final boolean conValores;
    private final JTextField txtDesde = Estilo.campoConPista("Desde"), txtHasta = Estilo.campoConPista("Hasta");

    public PanelHistorial(Usuario usuario) {
        super("Historial de órdenes", usuario, usuario instanceof Disenador, usuario instanceof Disenador ? new String[]{"Listo para Entrega", "Entregada"} : new String[]{"Entregada"},
                usuario instanceof Disenador ? new String[]{"N° orden", "Cliente", "Sede", "Creación", "Finalizada", "Estado", "Acciones"}
                        : new String[]{"N° orden", "Cliente", "Creación", "Entrega", "Estado", "Total", "Pago", "Acciones"});
        conValores = !(usuario instanceof Disenador);
        for (JTextField f : new JTextField[]{txtDesde, txtHasta}) { f.setPreferredSize(new java.awt.Dimension(100, 34)); f.setToolTipText("Fecha en formato dd/mm/aaaa"); }
        JButton buscar = Estilo.boton("Buscar");
        buscar.addActionListener(e -> recargar());
        filtros.add(txtDesde); filtros.add(txtHasta); filtros.add(buscar);
        if (usuario instanceof Disenador) botones.remove(actualizar);   // el historial del Diseñador no lleva el botón Actualizar
    }

    @Override protected ArrayList<Orden> datos(String texto, String estado, String sede) { return ctrl.listarHistorial(texto, estado, sede, Formato.aFecha(txtDesde.getText()), Formato.aFecha(txtHasta.getText())); }

    @Override protected Object[] fila(Orden o) {
        return conValores ? new Object[]{o.getCodOrden(), o.getCliente().getNombre(), Formato.fecha(o.getFechaCreacion()), Formato.fecha(o.getFechaEntrega()), o.getEstado(), Formato.dinero(o.getTotal()), o.getEstadoPago()}
                : new Object[]{o.getCodOrden(), o.getCliente().getNombre(), o.getNombreSede(), Formato.fecha(o.getFechaCreacion()), Formato.fecha(o.getFechaCierre()), o.getEstado()};
    }

    @Override protected String[] acciones(Orden o) { return new String[]{"Ver"}; }
}
