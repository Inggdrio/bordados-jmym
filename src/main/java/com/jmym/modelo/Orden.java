package com.jmym.modelo;

import com.jmym.util.Constantes;
import java.sql.Timestamp;
import java.util.ArrayList;

/** Una orden de pedido: tiene un Cliente y una lista (ArrayList) de Bordados. */
public class Orden {
    private int codOrden, codSede, codVendedor;
    private Cliente cliente;
    private String nombreSede, estado = "Pendiente", estadoPago = "Pendiente";
    private Timestamp fechaCreacion, fechaEntrega, fechaFinalizada;
    private double total, totalPagado;
    private final ArrayList<Bordado> bordados = new ArrayList<>();   // ArrayList: lista que crece sola

    public Orden(Cliente cliente) { this.cliente = cliente; }

    public ArrayList<Bordado> getBordados() { return bordados; }
    public double calcularTotal() { return bordados.stream().mapToDouble(Bordado::getSubtotal).sum(); }
    public double getSaldo() { return Math.round((total - totalPagado) * 100.0) / 100.0; }
    public boolean puedeModificarse() { return estado.equals("Pendiente"); }

    /** Siguiente estado según el arreglo de estados (null si ya es el último). */
    public String siguienteEstado() {
        int p = Constantes.posicion(Constantes.ESTADOS_ORDEN, estado);
        return p < 0 || p >= Constantes.ESTADOS_ORDEN.length - 1 ? null : Constantes.ESTADOS_ORDEN[p + 1];
    }

    /** Fecha en que el pedido terminó: la entrega al cliente o, si aún no, cuando el diseñador lo dejó listo. */
    public Timestamp getFechaCierre() { return fechaEntrega != null ? fechaEntrega : fechaFinalizada; }

    public int getCodOrden() { return codOrden; }
    public void setCodOrden(int c) { codOrden = c; }
    public int getCodSede() { return codSede; }
    public void setCodSede(int c) { codSede = c; }
    public int getCodVendedor() { return codVendedor; }
    public void setCodVendedor(int c) { codVendedor = c; }
    public Cliente getCliente() { return cliente; }
    public String getNombreSede() { return nombreSede; }
    public void setNombreSede(String s) { nombreSede = s; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { estado = e; }
    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String e) { estadoPago = e; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp f) { fechaCreacion = f; }
    public Timestamp getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Timestamp f) { fechaEntrega = f; }
    public Timestamp getFechaFinalizada() { return fechaFinalizada; }
    public void setFechaFinalizada(Timestamp f) { fechaFinalizada = f; }
    public double getTotal() { return total; }
    public void setTotal(double t) { total = t; }
    public double getTotalPagado() { return totalPagado; }
    public void setTotalPagado(double t) { totalPagado = t; }
}
