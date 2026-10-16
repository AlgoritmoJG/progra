package com.progra.pos.controller;


import com.progra.pos.dao.VentaDao;
import com.progra.pos.model.*;
import com.progra.pos.util.Validacion;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
public final class VentaController {
    private final Sesion sesion;
    private final CatalogoController catalogo;
    private final ArrayList<LineaCarrito> carrito = new ArrayList<>();
    public VentaController(Sesion sesion) { this.sesion = Objects.requireNonNull(sesion); catalogo = new CatalogoController(sesion); }
    public record LineaCarrito(int idProducto, int cantidad) {}
    public void agregar(int idProducto, int cantidad) {
        sesion.exigir(Permiso.REGISTRAR_VENTAS); Validacion.positivo(cantidad,"Cantidad");
        Producto p = catalogo.buscarProducto(idProducto);
        if (!p.puedeVenderse(LocalDate.now())) throw new IllegalArgumentException("Producto vencido o inactivo.");
        int indice = -1; long acumulada = cantidad;
        for (int i = 0; i < carrito.size(); i++) if (carrito.get(i).idProducto() == idProducto) { indice = i; acumulada += carrito.get(i).cantidad(); break; }
        if (acumulada > p.getStock()) throw new IllegalArgumentException("Stock insuficiente.");
        LineaCarrito linea = new LineaCarrito(idProducto,(int)acumulada);
        if (indice < 0) carrito.add(linea); else carrito.set(indice,linea);
    }
    public void quitar(int idProducto) { sesion.exigir(Permiso.REGISTRAR_VENTAS); carrito.removeIf(l -> l.idProducto() == idProducto); }
    public List<LineaCarrito> verCarrito() { sesion.exigir(Permiso.REGISTRAR_VENTAS); return List.copyOf(carrito); }
    public record LineaResumen(int idProducto, String nombre, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
    public record ResumenCarrito(List<LineaResumen> lineas, BigDecimal total) {}
    public ResumenCarrito consultarResumen() {
        sesion.exigir(Permiso.REGISTRAR_VENTAS);
        LocalDate fecha = LocalDate.now(); BigDecimal total = new BigDecimal("0.00");
        ArrayList<LineaResumen> lineas = new ArrayList<>();
        for (LineaCarrito linea : carrito) {
            Producto p = catalogo.buscarProducto(linea.idProducto());
            if (!p.puedeVenderse(fecha) || !p.tieneStock(linea.cantidad())) throw new IllegalArgumentException("Revise el carrito: producto no disponible.");
            BigDecimal precio = p.calcularPrecioFinal(fecha);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(linea.cantidad()));
            total = total.add(subtotal); lineas.add(new LineaResumen(p.getIdProducto(),p.getNombre(),linea.cantidad(),precio,subtotal));
        }
        return new ResumenCarrito(List.copyOf(lineas),total);
    }
    public Venta confirmar(int clienteId, MetodoPago metodo) {
        sesion.exigir(Permiso.REGISTRAR_VENTAS);
        Map<Integer,Integer> cantidades = new TreeMap<>(); for (LineaCarrito l : carrito) cantidades.put(l.idProducto(),l.cantidad());
        Venta venta = new VentaDao().confirmar(sesion.getUsuarioId(),clienteId,metodo,cantidades);
        carrito.clear(); return venta;
    }
    public ArrayList<Venta> historial(LocalDate desde, LocalDate hasta) {
        sesion.exigir(Permiso.CONSULTAR_VENTAS_PROPIAS); return new VentaDao().historial(sesion.getUsuarioId(),desde,hasta);
    }
}
