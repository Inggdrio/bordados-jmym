package com.jmym.controlador;

import com.jmym.dao.*;
import com.jmym.modelo.*;
import com.jmym.util.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/** Órdenes, bordados, cambios de estado y consultas (RF-003, RF-004 y RF-005). */
public class OrdenControlador {
    private static final String[] ACTIVOS = {"Pendiente", "En Producción", "Listo para Entrega"};
    private static final String[] PRODUCCION = {"Pendiente", "En Producción"};
    private static final String[] FINALIZADOS = {"Listo para Entrega", "Entregada"};
    private static final String[] ENTREGADA = {"Entregada"};

    private final OrdenDAO dao = new OrdenDAO();
    private final ClienteDAO clientes = new ClienteDAO();

    /** El Diseñador no puede ver valores en pesos. */
    private boolean conValores() { return !(Sesion.getUsuario() instanceof Disenador); }
    private String[] estados(String estado, String[] porDefecto) { return estado == null ? porDefecto : new String[]{estado}; }

    // ---------------------------------------------------------------- Clientes
    public Cliente buscarCliente(String cedula) {
        if (!Validador.soloDigitos(cedula, 12)) throw new NegocioException("La cédula debe tener solo números (máximo 12).");
        return clientes.buscarPorCedula(cedula.trim());
    }

    /** El Gerente corrige los datos de un cliente (RF-003). */
    public void corregirCliente(Cliente c, String cedula, String nombre, String telefono) {
        Sesion.como(Gerente.class);
        Cliente nuevo = new Cliente(cedula, nombre, telefono);
        if (clientes.cedulaEnUso(nuevo.getCedula(), c.getCodCliente())) throw new NegocioException("Ya existe otro cliente con esa cédula.");
        nuevo.setCodCliente(c.getCodCliente());
        clientes.actualizar(nuevo);
    }

    // ---------------------------------------------------------------- Crear y modificar (Vendedor)
    /** Si el cliente ya existía se manda en "existente"; si es nuevo, "existente" es null y se usan la cédula y el nombre escritos. */
    public int crearOrden(Cliente existente, String cedula, String nombre, String telefono, ArrayList<Bordado> bordados) {
        Vendedor v = Sesion.como(Vendedor.class);
        if (bordados.isEmpty()) throw new NegocioException("La orden debe tener al menos un bordado.");
        Cliente cliente = existente;
        if (cliente != null) cliente.setTelefono(telefono);
        else if (clientes.buscarPorCedula(cedula.trim()) != null) throw new NegocioException("Ya existe un cliente con esa cédula. Presione Buscar.");
        else cliente = new Cliente(cedula, nombre, telefono);
        Orden orden = new Orden(cliente);
        orden.setCodSede(v.getCodSede());
        orden.getBordados().addAll(bordados);
        if (dao.existeDuplicada(orden)) throw new NegocioException("Ya existe una orden con los mismos datos.");
        return dao.crear(orden, v.getCodUsuario());
    }

    /** Cambia los bordados (y el teléfono) de una orden Pendiente del mismo vendedor. */
    public void modificarOrden(int codOrden, String telefono, ArrayList<Bordado> bordados) {
        Vendedor v = Sesion.como(Vendedor.class);
        if (bordados.isEmpty()) throw new NegocioException("La orden debe tener al menos un bordado.");
        Orden o = dao.obtener(codOrden, true);
        if (o == null) throw new NegocioException("La orden no existe.");
        if (o.getCodVendedor() != v.getCodUsuario()) throw new NegocioException("No tiene permisos para esta función.");
        if (!o.puedeModificarse()) throw new NegocioException("No es posible modificar una orden que ya está en producción.");
        o.getCliente().setTelefono(telefono);
        o.getBordados().clear();
        o.getBordados().addAll(bordados);
        dao.reemplazarBordados(o);
    }

    // ---------------------------------------------------------------- Consultas
    public Orden obtener(int codOrden) { return dao.obtener(codOrden, conValores()); }

    /** Órdenes en curso del vendedor (estado = null -> todos los activos). */
    public ArrayList<Orden> listarDelVendedor(String texto, String estado) {
        return dao.listar(Sesion.como(Vendedor.class).getCodUsuario(), texto, null, estados(estado, ACTIVOS), true);
    }

    /** Panel de las tres sedes: el Diseñador solo ve las órdenes en producción; el Gerente ve todas. */
    public ArrayList<Orden> listarPanel(String texto, String estado, String sede) {
        if (Sesion.getUsuario() instanceof Vendedor) throw new NegocioException("No tiene permisos para esta función.");
        boolean disenador = Sesion.getUsuario() instanceof Disenador;
        return dao.listar(0, texto, sede, estados(estado, disenador ? PRODUCCION : Constantes.ESTADOS_ORDEN), conValores());
    }

    /** Historial: el Diseñador ve lo que finalizó; el Vendedor ve sus órdenes entregadas. */
    public ArrayList<Orden> listarHistorial(String texto, String estado, String sede, LocalDate desde, LocalDate hasta) {
        Usuario u = Sesion.getUsuario();
        if (u instanceof Gerente) throw new NegocioException("No tiene permisos para esta función.");
        if (desde != null && hasta != null && hasta.isBefore(desde)) throw new NegocioException("Rango de fechas inválido.");
        boolean vendedor = u instanceof Vendedor;
        ArrayList<Orden> filtradas = new ArrayList<>();
        for (Orden o : dao.listar(vendedor ? u.getCodUsuario() : 0, texto, sede, vendedor ? ENTREGADA : estados(estado, FINALIZADOS), conValores())) {
            Timestamp f = o.getFechaCierre();
            LocalDate dia = f == null ? null : f.toLocalDateTime().toLocalDate();
            if (dia != null && ((desde != null && dia.isBefore(desde)) || (hasta != null && dia.isAfter(hasta)))) continue;
            filtradas.add(o);
        }
        return filtradas;
    }

    public ArrayList<Object[]> historialDeLaOrden(int codOrden) { Sesion.como(Gerente.class); return new AuditoriaDAO().porOrden(codOrden); }

    // ---------------------------------------------------------------- Cambios de estado
    /** El Diseñador pasa la orden al estado siguiente (Pendiente -> En Producción -> Listo para Entrega). */
    public void avanzarEstado(Orden o) {
        Disenador d = Sesion.como(Disenador.class);
        String siguiente = o.siguienteEstado();
        if (siguiente == null || siguiente.equals("Entregada")) throw new NegocioException("No es posible cambiar el estado sin cumplir la secuencia establecida.");
        dao.cambiarEstado(o.getCodOrden(), siguiente, d.getCodUsuario());
    }

    /** El Vendedor entrega una orden suya que está Lista para Entrega y pagada por completo (la factura debe estar cerrada). */
    public void entregar(Orden o) {
        Vendedor v = Sesion.como(Vendedor.class);
        Orden actual = dao.obtener(o.getCodOrden(), true);   // se vuelve a leer para tener el saldo al día
        if (actual != null && actual.getSaldo() > 0) throw new NegocioException("No se puede entregar: el cliente debe " + Formato.dinero(actual.getSaldo()) + ". La factura se cierra al pagar el total.");
        dao.cambiarEstado(o.getCodOrden(), "Entregada", v.getCodUsuario());
    }

    /** El Gerente elimina una orden Pendiente, sin pagos, registrada por error. */
    public void eliminar(Orden o) { dao.eliminar(o.getCodOrden(), Sesion.como(Gerente.class).getCodUsuario()); }
}
