package com.jmym.vista;

import com.jmym.controlador.OrdenControlador;
import com.jmym.controlador.ReporteControlador;
import com.jmym.modelo.Orden;
import com.jmym.modelo.Usuario;
import com.jmym.util.Formato;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Módulo "Inicio" (Gerente): cuatro indicadores y las últimas órdenes. */
public class PanelInicio extends PanelBase {
    private final String[] titulos = {"Órdenes activas", "Listas para entrega", "Sedes activas", "Saldo pendiente"};
    private final JLabel[] valores = new JLabel[titulos.length];   // arreglo con el valor de cada indicador
    private final Tabla tabla = new Tabla("N° orden", "Cliente", "Sede", "Fecha", "Estado");

    public PanelInicio(Usuario usuario) {
        super("Panel principal", usuario);
        JPanel tarjetas = new JPanel(new GridLayout(1, titulos.length, 14, 0));
        tarjetas.setOpaque(false);
        for (int i = 0; i < titulos.length; i++) {
            valores[i] = Estilo.etiqueta("0", Estilo.TITULO.deriveFont(24f), Estilo.TEXTO);
            JPanel t = Estilo.tarjeta(null), texto = Estilo.vertical();
            texto.setBorder(new EmptyBorder(14, 16, 14, 16));
            texto.add(Estilo.etiqueta(titulos[i], Estilo.FUENTE, Estilo.GRIS)); texto.add(valores[i]);
            t.add(texto);
            tarjetas.add(t);
        }
        JPanel ultimas = Estilo.tarjeta("Últimas órdenes");
        ultimas.add(tabla.scroll());
        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setOpaque(false);
        contenido.add(tarjetas, BorderLayout.NORTH); contenido.add(ultimas);
        add(contenido);
    }

    @Override public void alMostrar() {
        double[] d = new ReporteControlador().indicadores();
        for (int i = 0; i < 3; i++) valores[i].setText(String.valueOf((long) d[i]));
        valores[3].setText(Formato.dinero(d[3]));
        tabla.limpiar();
        ArrayList<Orden> todas = new OrdenControlador().listarPanel("", null, null);
        for (Orden o : todas.subList(0, Math.min(5, todas.size()))) tabla.agregar(new Object[]{o.getCodOrden(), o.getCliente().getNombre(), o.getNombreSede(), Formato.fecha(o.getFechaCreacion()), o.getEstado()});
    }
}
