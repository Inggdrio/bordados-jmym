package com.jmym.controlador;

import com.jmym.dao.OrdenDAO;
import com.jmym.dao.PagoDAO;
import com.jmym.modelo.*;
import com.jmym.util.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Pagos y saldo de las órdenes (RF-006). Solo el Vendedor dueño de la orden. */
public class PagoControlador {
    private final OrdenDAO ordenes = new OrdenDAO();
    private final PagoDAO dao = new PagoDAO();

    /** Busca una orden por su número; debe ser del vendedor que inició sesión. */
    public Orden buscarOrden(String numero) {
        Vendedor v = Sesion.como(Vendedor.class);
        int cod = Validador.aEntero(numero, "N° de orden");
        Orden o = ordenes.obtener(cod, true);
        if (o == null) throw new NegocioException("No se encontró la orden N° " + cod + ".");
        if (o.getCodVendedor() != v.getCodUsuario()) throw new NegocioException("No tiene permisos para esta función.");
        return o;
    }

    public ArrayList<Pago> listarPagos(int codOrden) { return dao.listarPorOrden(codOrden); }

    public Pago registrar(Orden orden, String valorTexto, String fechaTexto, String medio) {
        Vendedor v = Sesion.como(Vendedor.class);
        double valor = Validador.aDinero(valorTexto, "Valor");
        if (valor <= 0) throw new NegocioException("El valor del pago debe ser mayor a cero.");
        if (valor > orden.getSaldo()) throw new NegocioException("El valor del pago supera el saldo pendiente.");
        if (medio == null) throw new NegocioException("Seleccione el medio de pago.");
        LocalDate dia = Formato.aFecha(fechaTexto);
        if (dia == null) throw new NegocioException("Ingrese la fecha del pago.");
        if (dia.isAfter(LocalDate.now())) throw new NegocioException("La fecha del pago no puede ser futura.");
        Pago pago = new Pago(orden.getCodOrden(), valor, medio, Timestamp.valueOf(dia.equals(LocalDate.now()) ? LocalDateTime.now() : dia.atTime(12, 0)));
        dao.insertar(pago, v.getCodUsuario());
        return pago;
    }

    /** Texto del recibo que se le entrega al cliente (con los datos ya actualizados de la orden). */
    public String comprobante(Orden o, Pago p) {
        return "COMPROBANTE DE PAGO\nBordados J.M.Y.M. - Sede " + o.getNombreSede() + "\n------------------------------------\n"
                + "Orden N°:        " + o.getCodOrden() + "\nCliente:         " + o.getCliente().getNombre()
                + "\nFecha del pago:  " + Formato.fechaHora(p.getFecha()) + "\nMedio de pago:   " + p.getMedio()
                + "\nValor abonado:   " + Formato.dinero(p.getValor()) + "\nValor total:     " + Formato.dinero(o.getTotal())
                + "\nSaldo pendiente: " + Formato.dinero(o.getSaldo()) + "\n" + (o.getSaldo() <= 0 ? "\nFACTURA CERRADA: pagada por completo.\n" : "");
    }
}
