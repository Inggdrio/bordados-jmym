package com.jmym.modelo;

import java.sql.Timestamp;

/** Un abono o pago de una orden. */
public class Pago {
    private int codPago;
    private final int codOrden;
    private final double valor;
    private final String medio;
    private final Timestamp fecha;

    public Pago(int codOrden, double valor, String medio, Timestamp fecha) { this.codOrden = codOrden; this.valor = valor; this.medio = medio; this.fecha = fecha; }

    public int getCodPago() { return codPago; }
    public void setCodPago(int c) { codPago = c; }
    public int getCodOrden() { return codOrden; }
    public double getValor() { return valor; }
    public String getMedio() { return medio; }
    public Timestamp getFecha() { return fecha; }
}
