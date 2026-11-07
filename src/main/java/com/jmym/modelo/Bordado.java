package com.jmym.modelo;

import com.jmym.util.Validador;

/** Un bordado de una orden. El tipo se puede escribir a mano (ej.: "Escudo"). */
public class Bordado {
    private int codBordado, cantidad;
    private double precioUnitario;
    private String tipo, color, notas;

    public Bordado(String tipo, String color, int cantidad, double precioUnitario, String notas) {
        if (Validador.vacio(tipo) || tipo.trim().length() > 50) throw new IllegalArgumentException("Escriba el tipo de bordado (máximo 50 letras).");
        if (Validador.vacio(color) || color.trim().length() > 30) throw new IllegalArgumentException("Escriba el color (máximo 30 letras).");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        if (precioUnitario <= 0) throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        if (notas != null && notas.trim().length() > 250) throw new IllegalArgumentException("Las notas no pueden pasar de 250 letras.");
        this.tipo = tipo.trim(); this.color = color.trim(); this.cantidad = cantidad; this.precioUnitario = precioUnitario;
        this.notas = notas == null ? "" : notas.trim();
    }

    public double getSubtotal() { return cantidad * precioUnitario; }   // se calcula solo
    public int getCodBordado() { return codBordado; }
    public void setCodBordado(int c) { codBordado = c; }
    public String getTipo() { return tipo; }
    public String getColor() { return color; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public String getNotas() { return notas; }

    /** Texto que identifica el bordado (sirve para detectar órdenes repetidas). */
    public String firma() { return (tipo + "|" + color + "|" + cantidad + "|" + notas).toLowerCase(); }
}
