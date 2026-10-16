package com.progra.pos;
import com.progra.pos.model.*;
import com.progra.pos.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ModeloTest {
    private final LocalDate hoy = LocalDate.of(2026,10,15);
    private final Categoria categoria = new Categoria(1,"Alimentos","");
    private Producto perecedero(int dias) { return new ProductoPerecedero(1,"LECHE","Leche",new BigDecimal("100.00"),10,2,true,categoria,hoy.plusDays(dias)); }
    private Venta venta() { return new Venta(0,hoy.atTime(10,0),new Cajero(1,"Ana","ana","hash",true),new Cliente(1,"Consumidor Final","CF",""),MetodoPago.EFECTIVO); }
    @Test void descuentoEnLimitesCeroYTres() {
        assertEquals(new BigDecimal("90.00"),perecedero(0).calcularPrecioFinal(hoy));
        assertEquals(new BigDecimal("90.00"),perecedero(3).calcularPrecioFinal(hoy));
        assertEquals(new BigDecimal("100.00"),perecedero(4).calcularPrecioFinal(hoy));
    }
    @Test void vencidoNoPuedeVenderse() {
        assertFalse(perecedero(-1).puedeVenderse(hoy));
        assertThrows(IllegalArgumentException.class,() -> perecedero(-1).calcularPrecioFinal(hoy));
    }
    @Test void polimorfismoNoPerecedero() {
        Producto p = new ProductoNoPerecedero(2,"JABON","Jabón",new BigDecimal("12.50"),5,5,true,categoria);
        assertEquals(new BigDecimal("12.50"),p.calcularPrecioFinal(hoy)); assertTrue(p.estaEnStockMinimo());
    }
    @Test void entradasInvalidasNoConstruyenProducto() {
        assertThrows(IllegalArgumentException.class,() -> new ProductoNoPerecedero(0,"X","X",BigDecimal.ZERO,1,0,true,categoria));
        assertThrows(IllegalArgumentException.class,() -> new ProductoNoPerecedero(0,"X","X",BigDecimal.ONE,-1,0,true,categoria));
        assertThrows(IllegalArgumentException.class,() -> new ProductoNoPerecedero(0,"X","X",new BigDecimal("1.001"),1,0,true,categoria));
    }
    @Test void carritoAgrupaBuscaYEliminaSinExponerLista() {
        Venta v = venta(); v.agregarDetalle(perecedero(3),2); v.agregarDetalle(perecedero(3),3);
        assertEquals(1,v.obtenerDetalles().size()); assertEquals(5,v.obtenerDetalles().getFirst().getCantidad());
        assertEquals(new BigDecimal("450.00"),v.getTotal());
        assertThrows(UnsupportedOperationException.class,() -> v.obtenerDetalles().clear());
        v.quitarDetalle(1); assertEquals(new BigDecimal("0.00"),v.getTotal());
    }
    @Test void cantidadAcumuladaNoExcedeStock() {
        Venta v = venta(); v.agregarDetalle(perecedero(1),6);
        assertThrows(IllegalArgumentException.class,() -> v.agregarDetalle(perecedero(1),5));
        assertEquals(6,v.obtenerDetalles().getFirst().getCantidad());
        assertThrows(IllegalArgumentException.class,() -> v.agregarDetalle(perecedero(1),0));
    }
    @Test void historialNoRecalculaDescuentoNiAdmiteCambios() {
        Venta v = Venta.reconstruir(1,hoy.atStartOfDay(),venta().getUsuario(),venta().getCliente(),MetodoPago.TARJETA,java.util.List.of(new Venta.DetalleGuardado(1,perecedero(-1),2,new BigDecimal("80.00"))));
        assertEquals(new BigDecimal("160.00"),v.getTotal());
        assertThrows(IllegalStateException.class,() -> v.quitarDetalle(1));
    }
    @Test void bcryptUsaSaltYVerifica() {
        String a = PasswordUtil.hash("MiClaveSegura123"), b = PasswordUtil.hash("MiClaveSegura123");
        assertNotEquals(a,b); assertTrue(PasswordUtil.verificar("MiClaveSegura123",a)); assertFalse(PasswordUtil.verificar("otra",a));
        assertThrows(IllegalArgumentException.class,() -> PasswordUtil.hash("123"));
        assertThrows(IllegalArgumentException.class,() -> PasswordUtil.hash("á".repeat(37)));
    }
}
