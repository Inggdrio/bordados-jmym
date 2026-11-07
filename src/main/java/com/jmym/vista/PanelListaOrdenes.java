package com.jmym.vista;

import com.jmym.controlador.OrdenControlador;
import com.jmym.controlador.SedeControlador;
import com.jmym.modelo.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * CLASE ABSTRACTA de las pantallas que muestran una lista de órdenes (buscador + filtros + tabla con enlaces de acción).
 * Cada hija solo dice QUÉ datos mostrar (datos, fila, acciones) y QUÉ hacer con cada enlace (accion): POLIMORFISMO.
 */
public abstract class PanelListaOrdenes extends PanelBase {
    protected final OrdenControlador ctrl = new OrdenControlador();
    protected final Tabla tabla;
    protected ArrayList<Orden> ordenes = new ArrayList<>();   // misma posición que las filas de la tabla
    protected final JTextField txtBuscar = Estilo.campoConPista("Buscar orden o cliente...");
    protected final JComboBox<Object> cmbEstado = Estilo.combo("Todos los estados"), cmbSede = Estilo.combo("Todas las sedes");
    protected final JPanel filtros = Estilo.barra(FlowLayout.LEFT), botones = Estilo.barra(FlowLayout.RIGHT);
    protected final JPanel centro = new JPanel(new BorderLayout(14, 14));   // la tabla va al centro; las hijas pueden agregar algo abajo o a la derecha
    protected final JButton actualizar = Estilo.botonClaro("Actualizar");   // vuelve a leer los datos de la base de datos
    private final boolean conSede;
    private boolean cargando;   // evita recargar en cadena mientras se llenan las listas

    protected PanelListaOrdenes(String titulo, Usuario usuario, boolean conSede, String[] estados, String... columnas) {
        super(titulo, usuario);
        this.conSede = conSede;
        tabla = new Tabla(columnas);
        for (String e : estados) cmbEstado.addItem(e);
        txtBuscar.setPreferredSize(new Dimension(200, 34));
        filtros.add(txtBuscar); filtros.add(cmbEstado);
        if (conSede) filtros.add(cmbSede);
        actualizar.addActionListener(e -> recargar());
        botones.add(actualizar);
        txtBuscar.addActionListener(e -> recargar());
        cmbEstado.addActionListener(e -> recargar());
        cmbSede.addActionListener(e -> recargar());
        tabla.alClic((fila, accion) -> Estilo.intentar(() -> { if (accion.equals("Ver")) ver(ordenes.get(fila)); else accion(ordenes.get(fila), accion); }));

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(filtros, BorderLayout.WEST); barra.add(botones, BorderLayout.EAST);
        JPanel tarjeta = Estilo.tarjeta(null);
        tarjeta.add(tabla.scroll());
        centro.setOpaque(false);
        centro.add(tarjeta);
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(barra, BorderLayout.NORTH); contenido.add(centro);
        add(contenido);
    }

    protected abstract ArrayList<Orden> datos(String texto, String estado, String sede);   // qué órdenes se muestran
    protected abstract Object[] fila(Orden o);                                              // valores de las columnas
    protected abstract String[] acciones(Orden o);                                          // enlaces de la fila
    protected void accion(Orden o, String accion) { }                                       // qué hace cada enlace (menos "Ver")

    protected void recargar() { Estilo.intentar(this::alMostrar); }

    @Override public void alMostrar() {
        if (cargando) return;
        cargando = true;
        try {
            if (conSede) {
                Object anterior = cmbSede.getSelectedItem();
                cmbSede.removeAllItems();
                cmbSede.addItem("Todas las sedes");
                for (Sede s : new SedeControlador().listar(false)) cmbSede.addItem(s.getNombre());
                cmbSede.setSelectedItem(anterior);
            }
            ordenes = datos(txtBuscar.getText(), cmbEstado.getSelectedIndex() <= 0 ? null : (String) cmbEstado.getSelectedItem(),
                    conSede && cmbSede.getSelectedIndex() > 0 ? (String) cmbSede.getSelectedItem() : null);
            tabla.limpiar();
            for (Orden o : ordenes) tabla.agregar(fila(o), acciones(o));
        } finally { cargando = false; }
    }

    /** Detalle de la orden: el Diseñador no ve precios y el Gerente ve además el historial. */
    protected void ver(Orden o) {
        DialogoDetalle.mostrar(this, ctrl.obtener(o.getCodOrden()), !(usuario instanceof Disenador), usuario instanceof Gerente ? ctrl.historialDeLaOrden(o.getCodOrden()) : null);
    }
}
