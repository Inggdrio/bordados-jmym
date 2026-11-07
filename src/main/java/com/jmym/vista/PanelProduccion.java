package com.jmym.vista;

import com.jmym.modelo.*;
import com.jmym.util.Formato;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Módulo "Producción" del Diseñador: ve las órdenes Pendientes y En Producción (sin precios) con el nombre y el teléfono
 * del cliente, las avanza de estado y, al elegir una fila, ve debajo el detalle de los bordados.
 */
public class PanelProduccion extends PanelListaOrdenes {
    private final Tabla detalle = new Tabla("Tipo", "Color", "Cant.", "Notas");

    public PanelProduccion(Usuario usuario) {
        super("Panel de producción", usuario, true, new String[]{"Pendiente", "En Producción"}, "N°", "Nombre", "Teléfono", "Sede", "Fecha", "Estado", "Acciones");
        JPanel tarjeta = Estilo.tarjeta("Detalle de la orden");
        tarjeta.setPreferredSize(new Dimension(0, 210));
        tarjeta.add(detalle.scroll());
        centro.add(tarjeta, BorderLayout.SOUTH);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) Estilo.intentar(() -> mostrarDetalle(ordenes.get(tabla.getSelectedRow())));
        });
    }

    private void mostrarDetalle(Orden o) {
        detalle.limpiar();
        for (Bordado b : ctrl.obtener(o.getCodOrden()).getBordados()) detalle.agregar(new Object[]{b.getTipo(), b.getColor(), b.getCantidad(), b.getNotas()});
    }

    @Override public void alMostrar() { super.alMostrar(); detalle.limpiar(); }

    @Override protected ArrayList<Orden> datos(String texto, String estado, String sede) { return ctrl.listarPanel(texto, estado, sede); }

    @Override protected Object[] fila(Orden o) {
        return new Object[]{o.getCodOrden(), o.getCliente().getNombre(), o.getCliente().getTelefono(), o.getNombreSede(), Formato.fecha(o.getFechaCreacion()), o.getEstado()};
    }

    @Override protected String[] acciones(Orden o) { return new String[]{o.getEstado().equals("Pendiente") ? "Iniciar" : "Marcar listo"}; }

    /** Pendiente -> En Producción -> Listo para Entrega (al terminar, la orden pasa al Historial). */
    @Override protected void accion(Orden o, String accion) {
        if (Estilo.confirmar(this, "¿Pasar la orden N° " + o.getCodOrden() + " de \"" + o.getEstado() + "\" a \"" + o.siguienteEstado() + "\"?")) { ctrl.avanzarEstado(o); alMostrar(); }
    }
}
