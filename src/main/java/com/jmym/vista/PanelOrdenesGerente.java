package com.jmym.vista;

import com.jmym.modelo.Cliente;
import com.jmym.modelo.Orden;
import com.jmym.modelo.Usuario;
import com.jmym.util.Constantes;
import com.jmym.util.Formato;
import java.util.ArrayList;
import javax.swing.JTextField;

/** Módulo "Órdenes" del Gerente: supervisa todas las órdenes, corrige clientes y elimina órdenes Pendientes. */
public class PanelOrdenesGerente extends PanelListaOrdenes {
    public PanelOrdenesGerente(Usuario usuario) {
        super("Órdenes de pedido", usuario, true, Constantes.ESTADOS_ORDEN, "N° orden", "Cliente", "Sede", "Fecha", "Estado", "Total", "Pago", "Acciones");
    }

    @Override protected ArrayList<Orden> datos(String texto, String estado, String sede) { return ctrl.listarPanel(texto, estado, sede); }

    @Override protected Object[] fila(Orden o) {
        return new Object[]{o.getCodOrden(), o.getCliente().getNombre(), o.getNombreSede(), Formato.fecha(o.getFechaCreacion()), o.getEstado(), Formato.dinero(o.getTotal()), o.getEstadoPago()};
    }

    @Override protected String[] acciones(Orden o) { return o.puedeModificarse() ? new String[]{"Ver", "Corregir", "Eliminar"} : new String[]{"Ver", "Corregir"}; }

    @Override protected void accion(Orden o, String accion) {
        if (accion.equals("Eliminar")) {
            if (Estilo.confirmar(this, "¿Eliminar la orden N° " + o.getCodOrden() + "?\nEsta acción no se puede deshacer.")) { ctrl.eliminar(o); alMostrar(); }
            return;
        }
        Cliente c = o.getCliente();
        Formulario f = new Formulario(this, "Corregir datos del cliente");
        JTextField cedula = f.agregar("Cédula", Estilo.campo()), nombre = f.agregar("Nombre", Estilo.campo()), telefono = f.agregar("Teléfono", Estilo.campo());
        cedula.setText(c.getCedula()); nombre.setText(c.getNombre()); telefono.setText(c.getTelefono());
        f.mostrar("Guardar", () -> { ctrl.corregirCliente(c, cedula.getText(), nombre.getText(), telefono.getText()); alMostrar(); });
    }
}
