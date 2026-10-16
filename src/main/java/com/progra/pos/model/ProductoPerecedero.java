package com.progra.pos.model;


import java.math.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
public final class ProductoPerecedero extends Producto {
    private final LocalDate fechaVencimiento;
    public ProductoPerecedero(int id, String codigo, String nombre, BigDecimal precio, int stock, int minimo, boolean activo, Categoria categoria, LocalDate vence) {
        super(id, codigo, nombre, precio, stock, minimo, activo, categoria);
        fechaVencimiento = Objects.requireNonNull(vence, "Fecha de vencimiento obligatoria.");
    }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    @Override public boolean puedeVenderse(LocalDate fecha) { return isActivo() && !fechaVencimiento.isBefore(fecha); }
    @Override public BigDecimal calcularPrecioFinal(LocalDate fecha) {
        if (!puedeVenderse(fecha)) throw new IllegalArgumentException("Producto vencido o inactivo.");
        long dias = ChronoUnit.DAYS.between(fecha, fechaVencimiento);
        // El mínimo de un centavo evita regalar productos de precio muy bajo.
        return dias <= 3 ? getPrecio().multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP).max(new BigDecimal("0.01")) : getPrecio();
    }
}
