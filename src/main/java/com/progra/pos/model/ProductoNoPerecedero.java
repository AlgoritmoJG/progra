package com.progra.pos.model;


import java.math.BigDecimal;
import java.time.LocalDate;
public final class ProductoNoPerecedero extends Producto {
    public ProductoNoPerecedero(int id, String codigo, String nombre, BigDecimal precio, int stock, int minimo, boolean activo, Categoria categoria) {
        super(id, codigo, nombre, precio, stock, minimo, activo, categoria);
    }
    @Override public boolean puedeVenderse(LocalDate fecha) { java.util.Objects.requireNonNull(fecha); return isActivo(); }
    @Override public BigDecimal calcularPrecioFinal(LocalDate fecha) {
        if (!puedeVenderse(fecha)) throw new IllegalArgumentException("Producto inactivo.");
        return getPrecio();
    }
}
