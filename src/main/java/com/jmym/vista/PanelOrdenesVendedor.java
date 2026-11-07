package com.jmym.vista;

import com.jmym.modelo.Orden;
import com.jmym.modelo.Usuario;
import com.jmym.util.Formato;
import java.util.ArrayList;
import javax.swing.JButton;

/** Módulo "Órdenes" del Vendedor: sus órdenes en curso. Ver, Editar (solo Pendientes) y Entregar (solo Listas y pagadas por completo). */
public class PanelOrdenesVendedor extends PanelListaOrdenes {
    public PanelOrdenesVendedor(Usuario usuario) {
        super("Órdenes de pedido", usuario, false, new String[]{"Pendiente", "En Producción", "Listo para Entrega"},
                "N° orden", "Cliente", "Fecha", "Estado", "Pago", "Total", "Saldo", "Acciones");
        JButton nueva = Estilo.boton("+ Nueva orden");
        Estilo.click(nueva, () -> { new DialogoOrden(this, 0).setVisible(true); alMostrar(); });
        botones.add(nueva);
    }

    @Override protected ArrayList<Orden> datos(String texto, String estado, String sede) { return ctrl.listarDelVendedor(texto, estado); }

    @Override protected Object[] fila(Orden o) { return new Object[]{o.getCodOrden(), o.getCliente().getNombre(), Formato.fecha(o.getFechaCreacion()), o.getEstado(), o.getEstadoPago(), Formato.dinero(o.getTotal()), Formato.dinero(o.getSaldo())}; }

    /** Entregar solo aparece si la orden está lista Y pagada por completo (factura cerrada). */
    @Override protected String[] acciones(Orden o) {
        if (o.puedeModificarse()) return new String[]{"Ver", "Editar"};
        return o.getEstado().equals("Listo para Entrega") && o.getEstadoPago().equals("Pagado") ? new String[]{"Ver", "Entregar"} : new String[]{"Ver"};
    }

    @Override protected void accion(Orden o, String accion) {
        if (accion.equals("Editar")) { new DialogoOrden(this, o.getCodOrden()).setVisible(true); alMostrar(); }
        else if (Estilo.confirmar(this, "¿Marcar la orden N° " + o.getCodOrden() + " como entregada al cliente?")) { ctrl.entregar(o); alMostrar(); }
    }
}
