package com.progra.pos;
import com.progra.pos.model.*;
import com.progra.pos.dao.*;
import com.progra.pos.controller.*;
import com.progra.pos.util.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
class MySqlTest {
    private CatalogoController admin, caja;
    private Sesion sesionAdmin, sesionCaja;
    private int categoriaId, clienteId, adminId, cajeroId;
    private final ProductoDao productos = new ProductoDao();
    @BeforeAll static void prepararEsquema() throws Exception {
        Assumptions.assumeTrue("true".equals(System.getenv("POS_RUN_DB_TESTS")),"MySQL no habilitado; ver docs/fase2/ejecucion.md");
        String url = System.getenv("POS_DB_URL");
        if (url == null || !url.matches("jdbc:mysql://[^/]+/pos_test\\?.*")) throw new IllegalStateException("Las pruebas requieren exclusivamente la BD pos_test.");
        try (Connection c = Conexion.abrir()) {
            // Esquema nuevo: no se borran tablas preexistentes ni se acepta una BD de producción.
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()"); ResultSet rs = ps.executeQuery()) {
                rs.next(); if (rs.getInt(1) == 0) {
                    String sql = Files.readString(Path.of("sql/01_schema.sql")).replaceAll("(?m)--.*$","");
                    for (String orden : sql.split(";")) if (!orden.isBlank()) try (PreparedStatement crear = c.prepareStatement(orden)) { crear.execute(); }
                }
            }
        }
    }
    @BeforeEach void datos() throws Exception {
        try (Connection c = Conexion.abrir()) {
            for (String tabla : List.of("detalle_ventas","ventas","productos","categorias","clientes","usuarios")) {
                // Nombres internos constantes; ninguna entrada de usuario se concatena.
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla)) { ps.executeUpdate(); }
            }
        }
        AutenticacionController auth = new AutenticacionController();
        adminId = auth.crearPrimerAdministrador("Admin","admin","ClaveAdmin123");
        sesionAdmin = auth.iniciarSesion("admin","ClaveAdmin123"); admin = new CatalogoController(sesionAdmin);
        cajeroId = admin.crearUsuario("Cajero","cajero","ClaveCajero123",Rol.CAJERO);
        sesionCaja = auth.iniciarSesion("cajero","ClaveCajero123"); caja = new CatalogoController(sesionCaja);
        categoriaId = admin.crearCategoria(new Categoria(0,"Básicos","Pruebas"));
        clienteId = admin.crearCliente(new Cliente(0,"Consumidor Final","CF",""));
    }
    private Producto producto(int id, String codigo, int stock, BigDecimal precio) { return new ProductoNoPerecedero(id,codigo,"Producto " + codigo,precio,stock,1,true,admin.buscarCategoria(categoriaId)); }
    private int crear(String codigo, int stock) { return admin.crearProducto(producto(0,codigo,stock,new BigDecimal("10.00"))); }
    private Venta vender(int id, int cantidad) { VentaController v = new VentaController(sesionCaja); v.agregar(id,cantidad); return v.confirmar(clienteId,MetodoPago.EFECTIVO); }
    @Test void loginPermisosEInyeccion() {
        AutenticacionController auth = new AutenticacionController();
        assertThrows(SecurityException.class,() -> auth.iniciarSesion("admin","mala"));
        assertThrows(SecurityException.class,() -> auth.iniciarSesion("' OR '1'='1","ClaveAdmin123"));
        assertThrows(SecurityException.class,() -> caja.crearCategoria(new Categoria(0,"Prohibida","")));
        admin.actualizarUsuario(cajeroId,"Cajero","cajero",null,Rol.CAJERO,false);
        assertThrows(SecurityException.class,() -> caja.buscarProductos(""));
        assertThrows(SecurityException.class,() -> auth.iniciarSesion("cajero","ClaveCajero123"));
    }
    @Test void crudCategoriasYRestriccion() {
        int extra = admin.crearCategoria(new Categoria(0,"Temporal",""));
        admin.actualizarCategoria(new Categoria(extra,"Nueva","Descripción"));
        assertEquals("Nueva",admin.buscarCategoria(extra).getNombre());
        admin.eliminarCategoria(extra); assertThrows(IllegalArgumentException.class,() -> admin.buscarCategoria(extra));
        crear("A",3); assertThrows(AccesoDatosException.class,() -> admin.eliminarCategoria(categoriaId));
    }
    @Test void crudProductosBusquedaYUnicidad() {
        int id = crear("A",3);
        admin.actualizarProducto(producto(id,"B",7,new BigDecimal("12.50")));
        assertEquals(7,admin.buscarProducto(id).getStock()); assertEquals(1,admin.buscarProductos("B").size());
        assertTrue(admin.buscarProductos("' OR 1=1 --").isEmpty());
        assertThrows(AccesoDatosException.class,() -> crear("B",1));
        admin.eliminarProducto(id); assertThrows(IllegalArgumentException.class,() -> admin.buscarProducto(id));
    }
    @Test void crudClientesYProteccionConsumidorFinal() {
        int id = caja.crearCliente(new Cliente(0,"Luis","123-4","12345678"));
        caja.actualizarCliente(new Cliente(id,"Luis Pérez","123-4","87654321"));
        assertEquals("Luis Pérez",new ClienteDao().buscar(id).getNombre());
        admin.eliminarCliente(id); assertThrows(IllegalArgumentException.class,() -> new ClienteDao().buscar(id));
        assertThrows(IllegalArgumentException.class,() -> admin.eliminarCliente(clienteId));
        assertThrows(IllegalArgumentException.class,() -> admin.actualizarCliente(new Cliente(clienteId,"Otro","CF","")));
    }
    @Test void crudUsuariosYCambioClave() {
        int id = admin.crearUsuario("Temporal","temporal","Temporal123",Rol.CAJERO);
        admin.actualizarUsuario(id,"Nuevo","nuevo","ClaveNueva123",Rol.ADMINISTRADOR,true);
        Sesion s = new AutenticacionController().iniciarSesion("nuevo","ClaveNueva123");
        assertEquals(Rol.ADMINISTRADOR,s.exigir(Permiso.GESTIONAR_USUARIOS).obtenerRol());
        admin.eliminarUsuario(id); assertNull(new UsuarioDao().porUsername("nuevo"));
        assertThrows(IllegalArgumentException.class,() -> admin.eliminarUsuario(adminId));
        assertThrows(IllegalArgumentException.class,() -> new AutenticacionController().crearPrimerAdministrador("Intruso","intruso","Intruso123"));
    }
    @Test void ventaDescuentoStockHistorialYBajaLogica() {
        Producto p = new ProductoPerecedero(0,"LECHE","Leche",new BigDecimal("100.00"),5,1,true,admin.buscarCategoria(categoriaId),LocalDate.now().plusDays(3));
        int id = admin.crearProducto(p); Venta venta = vender(id,2);
        assertEquals(new BigDecimal("180.00"),venta.getTotal()); assertEquals(3,admin.buscarProducto(id).getStock());
        admin.actualizarProducto(new ProductoPerecedero(id,"LECHE","Leche",new BigDecimal("200.00"),3,1,true,admin.buscarCategoria(categoriaId),LocalDate.now().minusDays(1)));
        ArrayList<Venta> historial = new VentaController(sesionCaja).historial(LocalDate.now(),LocalDate.now());
        assertEquals(new BigDecimal("180.00"),historial.getFirst().getTotal());
        assertEquals(new BigDecimal("90.00"),historial.getFirst().obtenerDetalles().getFirst().getPrecioUnitario());
        admin.eliminarProducto(id); assertFalse(admin.buscarProducto(id).isActivo());
        assertThrows(IllegalArgumentException.class,() -> vender(id,1));
        assertThrows(AccesoDatosException.class,() -> admin.eliminarCategoria(categoriaId));
    }
    @Test void rollbackReviertePrimerDescuentoSiSegundoProductoFalla() {
        int primero = crear("A",5), segundo = crear("B",1);
        assertThrows(IllegalArgumentException.class,() -> new VentaDao().confirmar(cajeroId,clienteId,MetodoPago.EFECTIVO,Map.of(primero,2,segundo,2)));
        assertEquals(5,productos.buscar(primero).getStock()); assertEquals(1,productos.buscar(segundo).getStock());
        assertTrue(new VentaController(sesionAdmin).historial(LocalDate.now(),LocalDate.now()).isEmpty());
    }
    @Test void ventaVaciaVencidaOCantidadInvalidaNoSeGuarda() {
        assertThrows(IllegalArgumentException.class,() -> new VentaController(sesionCaja).confirmar(clienteId,MetodoPago.EFECTIVO));
        int id = crear("A",3);
        assertThrows(IllegalArgumentException.class,() -> new VentaDao().confirmar(cajeroId,clienteId,MetodoPago.TARJETA,Map.of(id,0)));
        int vencido = admin.crearProducto(new ProductoPerecedero(0,"V","Vencido",BigDecimal.ONE,2,1,true,admin.buscarCategoria(categoriaId),LocalDate.now().minusDays(1)));
        assertThrows(IllegalArgumentException.class,() -> vender(vencido,1));
        assertEquals(2,productos.buscar(vencido).getStock());
    }
    @Test void cajeroSoloVeSusVentas() {
        int id = crear("A",5); vender(id,1);
        VentaController ventaAdmin = new VentaController(sesionAdmin); ventaAdmin.agregar(id,1); ventaAdmin.confirmar(clienteId,MetodoPago.TARJETA);
        assertEquals(1,new VentaController(sesionCaja).historial(LocalDate.now(),LocalDate.now()).size());
        assertEquals(2,ventaAdmin.historial(LocalDate.now(),LocalDate.now()).size());
    }
    @Test void dosCajasCompitenPorUltimaUnidad() throws Exception {
        int id = crear("ULTIMA",1);
        int otro = admin.crearUsuario("Otra caja","otra","OtraClave123",Rol.CAJERO);
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int usuario : List.of(cajeroId,otro)) resultados.add(ejecutor.submit(() -> {
                inicio.await();
                try { new VentaDao().confirmar(usuario,clienteId,MetodoPago.EFECTIVO,Map.of(id,1)); return true; }
                catch (IllegalArgumentException e) { return false; }
            }));
            inicio.countDown(); int exitos = 0; for (Future<Boolean> f : resultados) if (f.get(20,TimeUnit.SECONDS)) exitos++;
            assertEquals(1,exitos); assertEquals(0,productos.buscar(id).getStock());
            assertEquals(1,new VentaController(sesionAdmin).historial(LocalDate.now(),LocalDate.now()).size());
        }
    }
    @Test void resumenCarritoAgrupaYConservaLineasSiConfirmacionFalla() {
        int id = crear("CARRO",5); VentaController v = new VentaController(sesionCaja);
        v.agregar(id,1); v.agregar(id,2);
        assertEquals(1,v.verCarrito().size());
        assertEquals(new BigDecimal("30.00"),v.consultarResumen().total());
        assertEquals(3,v.consultarResumen().lineas().getFirst().cantidad());
        assertThrows(UnsupportedOperationException.class,() -> v.consultarResumen().lineas().clear());
        admin.actualizarProducto(producto(id,"CARRO",2,new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class,() -> v.confirmar(clienteId,MetodoPago.EFECTIVO));
        assertEquals(3,v.verCarrito().getFirst().cantidad());
        v.quitar(id); assertTrue(v.verCarrito().isEmpty());
        v.agregar(id,1); v.confirmar(clienteId,MetodoPago.TARJETA); assertTrue(v.verCarrito().isEmpty());
    }
    @Test void checkMySqlBloqueaStockNegativo() throws Exception {
        int id = crear("A",1);
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE productos SET stock=-1 WHERE id_producto=?")) {
            ps.setInt(1,id); assertThrows(SQLException.class,ps::executeUpdate);
        }
        assertEquals(1,productos.buscar(id).getStock());
    }
}
