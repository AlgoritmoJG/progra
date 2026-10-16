package com.progra.pos.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import com.progra.pos.util.Validacion;
public final class Venta {
    private final int idVenta;
    private final LocalDateTime fecha;
    private final Usuario usuario;
    private final Cliente cliente;
    private final MetodoPago metodoPago;
    private BigDecimal total = new BigDecimal("0.00");
    private final ArrayList<DetalleVenta> detalles = new ArrayList<>();
    public Venta(int id, LocalDateTime fecha, Usuario usuario, Cliente cliente, MetodoPago metodoPago) {
        idVenta = Validacion.noNegativo(id, "Id venta"); this.fecha = Objects.requireNonNull(fecha);
        this.usuario = Objects.requireNonNull(usuario); this.cliente = Objects.requireNonNull(cliente);
        this.metodoPago = Objects.requireNonNull(metodoPago);
    }
    public void agregarDetalle(Producto producto, int cantidad) {
        editable(); Validacion.positivo(cantidad, "Cantidad");
        if (!producto.puedeVenderse(fecha.toLocalDate())) throw new IllegalArgumentException("Producto inactivo o vencido.");
        int indice = -1; long acumulada = cantidad;
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getProducto().getIdProducto() == producto.getIdProducto()) {
                indice = i; acumulada += detalles.get(i).getCantidad(); break;
            }
        }
        if (acumulada > producto.getStock()) throw new IllegalArgumentException("Stock insuficiente.");
        DetalleVenta nuevo = new DetalleVenta(0, producto, (int) acumulada, producto.calcularPrecioFinal(fecha.toLocalDate()));
        BigDecimal siguiente = total.add(nuevo.getSubtotal());
        if (indice >= 0) siguiente = siguiente.subtract(detalles.get(indice).getSubtotal());
        if (siguiente.compareTo(new BigDecimal("9999999999.99")) > 0) throw new IllegalArgumentException("Total fuera de rango.");
        if (indice >= 0) detalles.set(indice, nuevo); else detalles.add(nuevo);
        calcularTotal();
    }
    public void quitarDetalle(int idProducto) {
        editable(); detalles.removeIf(d -> d.getProducto().getIdProducto() == idProducto); calcularTotal();
    }
    private void editable() { if (idVenta != 0) throw new IllegalStateException("Una venta confirmada no se modifica."); }
    public BigDecimal calcularTotal() {
        total = detalles.stream().map(DetalleVenta::getSubtotal).reduce(new BigDecimal("0.00"), BigDecimal::add); return total;
    }
    // Reconstruye el historial conservando el precio cobrado, incluso si el producto ya venció.
    public static Venta reconstruir(int id, LocalDateTime fecha, Usuario usuario, Cliente cliente, MetodoPago metodo, List<DetalleGuardado> filas) {
        Validacion.positivo(id, "Id venta");
        if (filas.isEmpty()) throw new IllegalArgumentException("Venta sin detalles.");
        Venta v = new Venta(id, fecha, usuario, cliente, metodo);
        for (DetalleGuardado d : filas) v.detalles.add(new DetalleVenta(d.id(), d.producto(), d.cantidad(), d.precio()));
        v.calcularTotal(); return v;
    }
    public record DetalleGuardado(int id, Producto producto, int cantidad, BigDecimal precio) {}
    public int getIdVenta() { return idVenta; }
    public LocalDateTime getFecha() { return fecha; }
    public Usuario getUsuario() { return usuario; }
    public Cliente getCliente() { return cliente; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public BigDecimal getTotal() { return total; }
    public List<DetalleVenta> obtenerDetalles() { return List.copyOf(detalles); }
}
