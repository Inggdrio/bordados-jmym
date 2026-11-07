package com.jmym.vista;

import com.jmym.controlador.ReporteControlador;
import com.jmym.controlador.SedeControlador;
import com.jmym.modelo.ResultadoReporte;
import com.jmym.modelo.Sede;
import com.jmym.modelo.Usuario;
import com.jmym.util.Constantes;
import com.jmym.util.Formato;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Módulo "Reportes" (Gerente): bordados realizados, ventas por sede y trazabilidad de órdenes. */
public class PanelReportes extends PanelBase {
    private final ReporteControlador ctrl = new ReporteControlador();
    private final JComboBox<Object> cmbTipo = Estilo.combo((Object[]) Constantes.TIPOS_REPORTE), cmbSede = Estilo.combo("Todas las sedes");
    private final JTextField txtDesde = Estilo.campoConPista("dd/mm/aaaa"), txtHasta = Estilo.campoConPista("dd/mm/aaaa");
    private final JLabel[] valores = new JLabel[3];   // facturado, cobrado, saldo
    private final JPanel resultado = Estilo.tarjeta("Resultado");
    private Tabla tabla = new Tabla(" ");

    public PanelReportes(Usuario usuario) {
        super("Reportes e indicadores", usuario);
        txtDesde.setColumns(9); txtHasta.setColumns(9);
        JButton generar = Estilo.boton("Generar");
        Estilo.click(generar, this::generar);
        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 14, 0));
        tarjetas.setOpaque(false);
        String[] titulos = {"Total facturado", "Total cobrado", "Saldo pendiente"};
        for (int i = 0; i < 3; i++) {
            valores[i] = Estilo.etiqueta("$ 0", Estilo.TITULO.deriveFont(24f), Estilo.TEXTO);
            JPanel t = Estilo.tarjeta(null), texto = Estilo.vertical();
            texto.setBorder(new EmptyBorder(14, 16, 14, 16));
            texto.add(Estilo.etiqueta(titulos[i], Estilo.FUENTE, Estilo.GRIS)); texto.add(valores[i]);
            t.add(texto);
            tarjetas.add(t);
        }
        resultado.add(tabla.scroll());
        JPanel arriba = new JPanel(new BorderLayout(0, 14));
        arriba.setOpaque(false);
        arriba.add(Estilo.barra(FlowLayout.LEFT, Estilo.conEtiqueta("Tipo de reporte", cmbTipo), Estilo.conEtiqueta("Sede", cmbSede), Estilo.conEtiqueta("Desde", txtDesde), Estilo.conEtiqueta("Hasta", txtHasta), generar), BorderLayout.NORTH);
        arriba.add(tarjetas);
        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setOpaque(false);
        contenido.add(arriba, BorderLayout.NORTH); contenido.add(resultado);
        add(contenido);
    }

    @Override public void alMostrar() {
        Object anterior = cmbSede.getSelectedItem();
        cmbSede.removeAllItems();
        cmbSede.addItem("Todas las sedes");
        new SedeControlador().listar(false).forEach(cmbSede::addItem);
        cmbSede.setSelectedItem(anterior);
    }

    private void generar() {
        ResultadoReporte r = ctrl.generar((String) cmbTipo.getSelectedItem(), cmbSede.getSelectedItem() instanceof Sede s ? s : null, txtDesde.getText(), txtHasta.getText());
        valores[0].setText(Formato.dinero(r.facturado()));
        valores[1].setText(Formato.dinero(r.cobrado()));
        valores[2].setText(Formato.dinero(r.saldo()));
        resultado.remove(resultado.getComponentCount() - 1);
        tabla = new Tabla(r.columnas());
        r.filas().forEach(f -> tabla.agregar(f));
        resultado.add(tabla.scroll());
        resultado.revalidate();
        resultado.repaint();
    }
}
