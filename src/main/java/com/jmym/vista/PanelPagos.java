package com.jmym.vista;

import com.jmym.controlador.PagoControlador;
import com.jmym.modelo.*;
import com.jmym.util.Constantes;
import com.jmym.util.Formato;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/** Módulo "Pagos" (Vendedor): consulta el saldo de una orden y registra abonos o el pago total. */
public class PanelPagos extends PanelBase {
    private final PagoControlador ctrl = new PagoControlador();
    private Orden orden;
    private ArrayList<Pago> pagos = new ArrayList<>();
    private final JTextField txtOrden = Estilo.campoConPista("N° de orden"), txtValor = Estilo.campo(), txtFecha = Estilo.campo();
    private final JComboBox<Object> cmbMedio = Estilo.combo((Object[]) Constantes.MEDIOS_PAGO);
    private final JLabel lblOrden = Estilo.etiqueta(" ", Estilo.NEGRITA, Estilo.TEXTO);
    private final JLabel[] valores = new JLabel[3];   // valor total, total pagado, saldo pendiente
    private final Tabla tabla = new Tabla("Fecha", "Medio", "Valor");

    public PanelPagos(Usuario usuario) {
        super("Pagos y saldo", usuario);
        txtOrden.setPreferredSize(new Dimension(220, 34));
        JButton buscar = Estilo.boton("Buscar");
        Estilo.click(buscar, () -> cargar(txtOrden.getText()));
        txtOrden.addActionListener(e -> buscar.doClick());

        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 14, 0));
        tarjetas.setOpaque(false);
        String[] titulos = {"Valor total", "Total pagado", "Saldo pendiente"};
        for (int i = 0; i < 3; i++) {
            valores[i] = Estilo.etiqueta("$ 0", Estilo.TITULO.deriveFont(24f), Estilo.TEXTO);
            JPanel t = Estilo.tarjeta(null), texto = Estilo.vertical();
            texto.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));
            texto.add(Estilo.etiqueta(titulos[i], Estilo.FUENTE, Estilo.GRIS)); texto.add(valores[i]);
            t.add(texto);
            tarjetas.add(t);
        }

        JButton comprobante = Estilo.botonClaro("Comprobante");
        Estilo.click(comprobante, this::verComprobante);
        JPanel historial = Estilo.tarjeta("Historial de pagos");
        historial.add(tabla.scroll());
        JPanel pie = Estilo.conRelleno(new JPanel(new BorderLayout()));
        pie.setOpaque(false);
        pie.add(comprobante, BorderLayout.WEST);
        historial.add(pie, BorderLayout.SOUTH);

        JButton guardar = Estilo.boton("Guardar pago");
        Estilo.click(guardar, this::guardarPago);
        JPanel form = Estilo.conRelleno(Estilo.vertical());
        form.add(Estilo.fila(Estilo.conEtiqueta("Valor", txtValor), Estilo.conEtiqueta("Fecha (dd/mm/aaaa)", txtFecha)));
        form.add(Box.createVerticalStrut(8));
        form.add(Estilo.conEtiqueta("Medio de pago", cmbMedio));
        form.add(Box.createVerticalStrut(14));
        guardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(guardar);
        JPanel registrar = Estilo.tarjeta("Registrar pago");
        registrar.setPreferredSize(new Dimension(420, 0));
        registrar.add(form);

        JPanel abajo = new JPanel(new BorderLayout(14, 0));
        abajo.setOpaque(false);
        abajo.add(historial); abajo.add(registrar, BorderLayout.EAST);
        JPanel arriba = new JPanel(new BorderLayout(0, 14));
        arriba.setOpaque(false);
        arriba.add(Estilo.barra(FlowLayout.LEFT, txtOrden, buscar, lblOrden), BorderLayout.NORTH); arriba.add(tarjetas);
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(arriba, BorderLayout.NORTH); contenido.add(abajo);
        add(contenido);
    }

    @Override public void alMostrar() {
        txtFecha.setText(Formato.hoy());
        if (orden != null) cargar("" + orden.getCodOrden());   // vuelve a leer los datos
    }

    private void cargar(String numero) {
        orden = null;
        pagos = new ArrayList<>();
        try { orden = ctrl.buscarOrden(numero); pagos = ctrl.listarPagos(orden.getCodOrden()); }
        finally { mostrar(); }   // se muestra (aunque sea en ceros) y luego el error, si lo hubo, llega al aviso
    }

    private void mostrar() {
        tabla.limpiar();
        pagos.forEach(p -> tabla.agregar(new Object[]{Formato.fechaHora(p.getFecha()), p.getMedio(), Formato.dinero(p.getValor())}));
        valores[0].setText(Formato.dinero(orden == null ? 0 : orden.getTotal()));
        valores[1].setText(Formato.dinero(orden == null ? 0 : orden.getTotalPagado()));
        valores[2].setText(Formato.dinero(orden == null ? 0 : orden.getSaldo()));
        lblOrden.setText(orden == null ? " " : "Orden N° " + orden.getCodOrden() + " - " + orden.getCliente().getNombre() + " - Pago: " + orden.getEstadoPago());
    }

    private void guardarPago() {
        if (orden == null) { Estilo.error(this, "Primero busque una orden."); return; }
        Pago p = ctrl.registrar(orden, txtValor.getText(), txtFecha.getText(), (String) cmbMedio.getSelectedItem());
        txtValor.setText("");
        cargar("" + orden.getCodOrden());   // actualiza saldo e historial
        mostrarTexto(ctrl.comprobante(orden, p));
    }

    private void verComprobante() {
        if (orden == null || pagos.isEmpty()) { Estilo.error(this, "No hay pagos para mostrar."); return; }
        int fila = tabla.getSelectedRow();
        mostrarTexto(ctrl.comprobante(orden, pagos.get(fila >= 0 ? fila : pagos.size() - 1)));   // el seleccionado o el último
    }

    private void mostrarTexto(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, area, "Comprobante de pago", JOptionPane.PLAIN_MESSAGE);
    }
}
