package com.jmym.vista;

import com.jmym.controlador.OrdenControlador;
import com.jmym.controlador.Sesion;
import com.jmym.modelo.*;
import com.jmym.util.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Ventana para registrar una orden nueva o modificar una orden Pendiente (solo Vendedor).
 * El tipo de bordado es una lista editable: se puede elegir uno o escribirlo a mano.
 */
public class DialogoOrden extends JDialog {
    private final OrdenControlador ctrl = new OrdenControlador();
    private final ArrayList<Bordado> bordados = new ArrayList<>();   // lista de bordados de la orden
    private final int codOrden;                                      // 0 = orden nueva
    private Cliente clienteExistente;

    private final JTextField txtCedula = Estilo.campo(), txtNombre = Estilo.campo(), txtTelefono = Estilo.campo();
    private final JTextField txtColor = Estilo.campo(), txtCantidad = Estilo.campo(), txtPrecio = Estilo.campo(), txtNotas = Estilo.campo();
    private final JComboBox<Object> cmbTipo = Estilo.combo((Object[]) Constantes.TIPOS_BORDADO);
    private final JButton btnBuscar = Estilo.botonClaro("Buscar");
    private final JLabel lblAyuda = Estilo.etiqueta("Escriba la cédula y presione Buscar.", Estilo.FUENTE, Estilo.GRIS), lblTotal = Estilo.etiqueta("Total: $ 0", Estilo.TITULO.deriveFont(18f), Estilo.TEXTO);
    private final Tabla tabla = new Tabla("Tipo", "Color", "Cant.", "Precio", "Subtotal", "Notas", "Acciones");

    public DialogoOrden(Component padre, int codOrden) {
        super(SwingUtilities.getWindowAncestor(padre), codOrden == 0 ? "Nueva orden" : "Modificar orden N° " + codOrden, ModalityType.APPLICATION_MODAL);
        this.codOrden = codOrden;
        cmbTipo.setEditable(true);
        getContentPane().setBackground(Estilo.FONDO);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(16, 18, 16, 18));
        setLayout(new BorderLayout(0, 12));

        // ---- Cliente
        JPanel cedula = new JPanel(new BorderLayout(8, 0));
        cedula.setOpaque(false);
        cedula.add(txtCedula); cedula.add(btnBuscar, BorderLayout.EAST);
        JPanel cliente = Estilo.tarjeta("Cliente"), campos = Estilo.conRelleno(Estilo.vertical());
        campos.add(Estilo.fila(Estilo.conEtiqueta("Cédula", cedula), Estilo.conEtiqueta("Nombre", txtNombre), Estilo.conEtiqueta("Teléfono", txtTelefono)));
        lblAyuda.setAlignmentX(LEFT_ALIGNMENT);
        campos.add(Box.createVerticalStrut(4)); campos.add(lblAyuda);
        cliente.add(campos);
        Estilo.click(btnBuscar, this::buscarCliente);

        // ---- Agregar bordado + tabla
        JButton agregar = Estilo.boton("+ Agregar");
        Estilo.click(agregar, this::agregarBordado);
        JPanel nuevo = Estilo.conRelleno(new JPanel(new BorderLayout(12, 0)));
        nuevo.setOpaque(false);
        nuevo.add(Estilo.fila(Estilo.conEtiqueta("Tipo (elija o escriba)", cmbTipo), Estilo.conEtiqueta("Color", txtColor), Estilo.conEtiqueta("Cantidad", txtCantidad),
                Estilo.conEtiqueta("Precio unitario", txtPrecio), Estilo.conEtiqueta("Notas (opcional)", txtNotas)));
        JPanel boton = new JPanel(new BorderLayout());
        boton.setOpaque(false);
        boton.add(agregar, BorderLayout.SOUTH);
        nuevo.add(boton, BorderLayout.EAST);
        JPanel detalle = Estilo.tarjeta("Bordados de la orden");
        detalle.add(nuevo, BorderLayout.SOUTH);
        detalle.add(tabla.scroll());
        tabla.alClic((fila, accion) -> Estilo.intentar(() -> { if (accion.equals("Editar")) editar(fila); else quitar(fila); }));

        // ---- Pie: total y botones
        JButton guardar = Estilo.boton(codOrden == 0 ? "Guardar orden" : "Guardar cambios"), cancelar = Estilo.botonClaro("Cancelar");
        Estilo.click(guardar, this::guardar);
        cancelar.addActionListener(e -> dispose());
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(lblTotal, BorderLayout.WEST); pie.add(Estilo.barra(FlowLayout.RIGHT, cancelar, guardar), BorderLayout.EAST);

        add(cliente, BorderLayout.NORTH); add(detalle); add(pie, BorderLayout.SOUTH);
        if (codOrden > 0) cargar();
        setSize(940, 600);
        setLocationRelativeTo(padre);
    }

    /** Modo modificar: carga la orden; solo se puede cambiar el teléfono y los bordados. */
    private void cargar() {
        Orden o = ctrl.obtener(codOrden);
        txtCedula.setText(o.getCliente().getCedula()); txtNombre.setText(o.getCliente().getNombre()); txtTelefono.setText(o.getCliente().getTelefono());
        txtCedula.setEditable(false); txtNombre.setEditable(false); btnBuscar.setEnabled(false);
        lblAyuda.setText("Solo puede cambiar el teléfono del cliente y los bordados.");
        bordados.addAll(o.getBordados());
        refrescar();
    }

    private void buscarCliente() {
        clienteExistente = ctrl.buscarCliente(txtCedula.getText());
        txtNombre.setEditable(clienteExistente == null);
        txtNombre.setText(clienteExistente == null ? "" : clienteExistente.getNombre());
        txtTelefono.setText(clienteExistente == null ? "" : clienteExistente.getTelefono());
        lblAyuda.setText(clienteExistente == null ? "Cliente nuevo: escriba su nombre y teléfono." : "Cliente encontrado. Confirme o corrija el teléfono.");
    }

    private void agregarBordado() {
        bordados.add(new Bordado(String.valueOf(cmbTipo.getSelectedItem()), txtColor.getText(), Validador.aEntero(txtCantidad.getText(), "Cantidad"),
                Validador.aDinero(txtPrecio.getText(), "Precio unitario"), txtNotas.getText()));
        txtColor.setText(""); txtCantidad.setText(""); txtPrecio.setText(""); txtNotas.setText("");
        refrescar();
    }

    /** "Editar" pasa el bordado al formulario y lo saca de la lista (se vuelve a agregar al terminar). */
    private void editar(int fila) {
        Bordado b = bordados.remove(fila);
        cmbTipo.setSelectedItem(b.getTipo());
        txtColor.setText(b.getColor()); txtCantidad.setText("" + b.getCantidad()); txtPrecio.setText("" + (long) b.getPrecioUnitario()); txtNotas.setText(b.getNotas());
        refrescar();
    }

    private void quitar(int fila) { bordados.remove(fila); refrescar(); }

    private void refrescar() {
        tabla.limpiar();
        double total = 0;
        for (Bordado b : bordados) {
            tabla.agregar(new Object[]{b.getTipo(), b.getColor(), b.getCantidad(), Formato.dinero(b.getPrecioUnitario()), Formato.dinero(b.getSubtotal()), b.getNotas()}, "Editar", "Quitar");
            total += b.getSubtotal();
        }
        lblTotal.setText("Total: " + Formato.dinero(total));
    }

    private void guardar() {
        if (codOrden == 0) {
            Estilo.info(this, "Orden N° " + ctrl.crearOrden(clienteExistente, txtCedula.getText(), txtNombre.getText(), txtTelefono.getText(), bordados) + " registrada correctamente.");
        } else {
            ctrl.modificarOrden(codOrden, txtTelefono.getText(), bordados);
            Estilo.info(this, "Orden N° " + codOrden + " modificada correctamente.");
        }
        dispose();
    }
}
