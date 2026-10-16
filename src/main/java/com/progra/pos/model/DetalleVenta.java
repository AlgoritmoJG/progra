package com.progra.pos.model;


import java.math.BigDecimal;
import java.util.Objects;
import com.progra.pos.util.Validacion;
public final class DetalleVenta {
    private final int idDetalleVenta;
    private final Producto producto;
    private final int cantidad;
    private final BigDecimal precioUnitario, subtotal;
    // Acceso de paquete: la creación se coordina desde Venta.
    DetalleVenta(int id, Producto producto, int cantidad, BigDecimal precio) {
        idDetalleVenta = Validacion.noNegativo(id, "Id detalle");
        this.producto = Objects.requireNonNull(producto);
        this.cantidad = Validacion.positivo(cantidad, "Cantidad");
        precioUnitario = Validacion.dinero(precio);
        subtotal = calcularSubtotal();
        if (subtotal.compareTo(new BigDecimal("9999999999.99")) > 0) throw new IllegalArgumentException("Subtotal fuera de rango.");
    }
    public int getIdDetalleVenta() { return idDetalleVenta; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal calcularSubtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
