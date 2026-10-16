package com.progra.pos.model;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import com.progra.pos.util.Validacion;
public abstract class Producto {
    private final int idProducto;
    private final String codigo, nombre;
    private final BigDecimal precio;
    private final int stock, stockMinimo;
    private final boolean activo;
    private final Categoria categoria;
    protected Producto(int id, String codigo, String nombre, BigDecimal precio, int stock, int minimo, boolean activo, Categoria categoria) {
        idProducto = Validacion.noNegativo(id, "Id");
        this.codigo = Validacion.texto(codigo, "Código", 50);
        this.nombre = Validacion.texto(nombre, "Nombre", 100);
        this.precio = Validacion.dinero(precio);
        this.stock = Validacion.noNegativo(stock, "Stock");
        stockMinimo = Validacion.noNegativo(minimo, "Stock mínimo");
        this.activo = activo; this.categoria = Objects.requireNonNull(categoria, "Categoría obligatoria.");
    }
    public int getIdProducto() { return idProducto; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }
    public Categoria getCategoria() { return categoria; }
    public boolean tieneStock(int cantidad) { return cantidad > 0 && cantidad <= stock; }
    public boolean estaEnStockMinimo() { return stock <= stockMinimo; }
    public abstract BigDecimal calcularPrecioFinal(LocalDate fechaVenta);
    public abstract boolean puedeVenderse(LocalDate fechaVenta);
}
