package com.progra.pos.view.swing;

import com.progra.pos.controller.*;
import com.progra.pos.model.*;
import com.progra.pos.util.Conexion;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de ventanas reales. Requieren MySQL pos_test y una pantalla (real o Xvfb). */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwingFlujoTest {
    private CatalogoController catalogo;
    private Sesion sesion;
    private int categoriaId, productoId;
    private Robot robot;
    @BeforeAll static void requisitos() {
        Assumptions.assumeTrue("true".equals(System.getenv("POS_RUN_SWING_TESTS")),"Pruebas Swing no habilitadas.");
        assertFalse(GraphicsEnvironment.isHeadless(),"Se necesita una pantalla real o virtual.");
        String url = System.getenv("POS_DB_URL");
        assertTrue(url != null && url.matches("jdbc:mysql://[^/]+/pos_test\\?.*"),"Solo se permite la BD pos_test.");
    }
    @BeforeEach void preparar() throws Exception {
        // Crea el esquema en una BD de pruebas vacía para poder ejecutar esta suite por separado.
        try (Connection c = Conexion.abrir()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()"); ResultSet rs = ps.executeQuery()) {
                rs.next(); if (rs.getInt(1) == 0) {
                    String sql = Files.readString(Path.of("sql/01_schema.sql")).replaceAll("(?m)--.*$","");
                    for (String orden : sql.split(";")) if (!orden.isBlank()) try (PreparedStatement crear = c.prepareStatement(orden)) { crear.execute(); }
                }
            }
            for (String tabla : List.of("detalle_ventas","ventas","productos","categorias","clientes","usuarios"))
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla)) { ps.executeUpdate(); }
        }
        AutenticacionController auth = new AutenticacionController();
        auth.crearPrimerAdministrador("Administradora Demo","admin_gui","AdminGui123");
        sesion = auth.iniciarSesion("admin_gui","AdminGui123"); catalogo = new CatalogoController(sesion);
        catalogo.crearUsuario("Cajera Demo","caja_gui","CajeraGui123",Rol.CAJERO);
        categoriaId = catalogo.crearCategoria(new Categoria(0,"Abarrotes","Comercio local"));
        catalogo.crearCliente(new Cliente(0,"Consumidor Final","CF",""));
        productoId = catalogo.crearProducto(new ProductoNoPerecedero(0,"ARROZ-01","Arroz",new BigDecimal("12.50"),8,2,true,catalogo.buscarCategoria(categoriaId)));
        catalogo.crearProducto(new ProductoPerecedero(0,"LECHE-01","Leche",new BigDecimal("10.00"),3,3,true,catalogo.buscarCategoria(categoriaId),java.time.LocalDate.now().plusDays(2)));
        robot = new Robot(); robot.setAutoDelay(80);
        edt(() -> { SwingApp.main(new String[0]); return null; });
        esperar(() -> existe("login") && buscar("iniciarSesion",JButton.class).isEnabled());
    }
    @AfterEach void cerrar() throws Exception {
        edt(() -> { for (Window w : Window.getWindows()) w.dispose(); return null; });
    }
    @Test @Order(1) void administradorCrudYVentaGrafica() throws Exception {
        captura("01-login"); entrar("admin_gui","AdminGui123");
        assertTrue(edt(() -> existe("navCategorias") && existe("navUsuarios")));
        captura("02-caja");
        pulsar("navCategorias"); esperarTabla("categoriasTabla",1);
        pulsar("nuevoRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("nombre","Limpieza"); texto("descripcion","Categoría creada desde Swing"); pulsar("guardarFormulario"); esperarTabla("categoriasTabla",2);
        seleccionar("categoriasTabla",1); pulsar("editarRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("nombre","Limpieza del hogar"); pulsar("guardarFormulario"); esperar(() -> tablaContiene("categoriasTabla","Limpieza del hogar"));
        captura("03-categorias");
        seleccionar("categoriasTabla",1); pulsar("eliminarRegistro"); aceptarDialogo(); esperarTabla("categoriasTabla",1);
        pulsar("navProductos"); esperarTabla("productosTabla",2); pulsar("nuevoRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("codigo","JABON-01"); texto("nombre","Jabón"); texto("precio","-1"); texto("stock","5"); texto("stockMinimo","1"); pulsar("guardarFormulario");
        esperar(() -> buscar("errorFormulario",JLabel.class).getText().contains("mayor a cero"));
        texto("precio","6.75"); pulsar("guardarFormulario"); esperarTabla("productosTabla",3);
        captura("04-productos");
        edt(() -> { buscar("productosTabla",JTable.class).getRowSorter().toggleSortOrder(2); return null; });
        seleccionar("productosTabla",1); pulsar("editarRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("precio","7.25"); pulsar("guardarFormulario"); esperar(() -> tablaContiene("productosTabla","Q 7.25"));
        seleccionar("productosTabla",1); pulsar("eliminarRegistro"); aceptarDialogo(); esperarTabla("productosTabla",2);
        pulsar("navCaja"); esperarTabla("cajaProductosTabla",2);
        seleccionar("cajaProductosTabla",0); texto("cantidad","2"); pulsar("agregarCarrito"); esperarTabla("carritoTabla",1);
        esperar(() -> buscar("totalCarrito",JLabel.class).getText().contains("25.00")); captura("05-carrito");
        pulsar("confirmarVenta"); aceptarDialogo(); esperar(() -> existe("detalleVenta"));
        assertTrue(edt(() -> buscar("totalVenta",JLabel.class).getText().contains("25.00"))); captura("06-venta-confirmada");
        pulsar("cerrarDetalle"); esperarTabla("carritoTabla",0);
        assertEquals(6,catalogo.buscarProducto(productoId).getStock());
        pulsar("navHistorial"); esperarTabla("historialTabla",1); captura("07-historial");
        seleccionar("historialTabla",0); pulsar("verDetalle"); esperar(() -> existe("detalleVenta")); pulsar("cerrarDetalle");
        pulsar("navUsuarios"); esperarTabla("usuariosTabla",2); pulsar("nuevoRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("nombre","Ayudante"); texto("username","apoyo_gui"); texto("password","123"); pulsar("guardarFormulario");
        esperar(() -> buscar("errorFormulario",JLabel.class).getText().contains("mínimo"));
        texto("password","ApoyoGui123"); edt(() -> { buscar("rol",JComboBox.class).setSelectedItem(Rol.CAJERO); return null; });
        pulsar("guardarFormulario"); esperarTabla("usuariosTabla",3);
        seleccionar("usuariosTabla",2); pulsar("editarRegistro"); esperar(() -> existe("guardarFormulario")); texto("nombre","Ayudante editado");
        edt(() -> { buscar("activo",JCheckBox.class).setSelected(false); return null; });
        pulsar("guardarFormulario"); esperar(() -> tablaContiene("usuariosTabla","Inactivo")); captura("08-usuarios");
        seleccionar("usuariosTabla",2); pulsar("eliminarRegistro"); aceptarDialogo(); esperarTabla("usuariosTabla",2);
        pulsar("navCaja"); esperarTabla("cajaProductosTabla",2); pulsar("gestionarClientes"); esperarTabla("clientesTabla",1);
        pulsar("nuevoRegistro"); esperar(() -> existe("guardarFormulario"));
        texto("nombre","Cliente Demo"); texto("nit","12345-6"); texto("telefono","55551234"); pulsar("guardarFormulario"); esperarTabla("clientesTabla",2); captura("09-clientes");
        pulsar("volverCaja"); esperarTabla("cajaProductosTabla",2);
        pulsar("cerrarSesion"); esperar(() -> existe("login"));
    }
    @Test @Order(2) void cajeroNoVeAdministracionYPuedeVender() throws Exception {
        entrar("caja_gui","CajeraGui123");
        assertFalse(edt(() -> existe("navUsuarios") || existe("navCategorias")));
        pulsar("navProductos"); esperarTabla("productosTabla",2);
        assertFalse(edt(() -> existe("nuevoRegistro") || existe("editarRegistro") || existe("eliminarRegistro")));
        captura("10-consulta-cajero");
        pulsar("navCaja"); esperarTabla("cajaProductosTabla",2); seleccionar("cajaProductosTabla",1);
        pulsar("agregarCarrito"); esperarTabla("carritoTabla",1);
        assertTrue(edt(() -> buscar("totalCarrito",JLabel.class).getText().contains("9.00")));
        pulsar("confirmarVenta"); aceptarDialogo(); esperar(() -> existe("detalleVenta")); pulsar("cerrarDetalle"); esperarTabla("carritoTabla",0);
        pulsar("navHistorial"); esperarTabla("historialTabla",1);
        assertTrue(edt(() -> tablaContiene("historialTabla","caja_gui")));
    }
    @Test @Order(3) void erroresLoginCantidadEHistorialNoCreanVentas() throws Exception {
        texto("loginUsuario","admin_gui"); texto("loginPassword","incorrecta"); pulsar("iniciarSesion"); esperarDialogo();
        assertFalse(edt(() -> existe("principal"))); cerrarMensaje();
        entrar("caja_gui","CajeraGui123"); seleccionar("cajaProductosTabla",0); texto("cantidad","999"); pulsar("agregarCarrito"); esperarDialogo(); cerrarMensaje();
        assertEquals(0,edt(() -> buscar("carritoTabla",JTable.class).getRowCount()));
        texto("cantidad","abc"); pulsar("agregarCarrito"); esperarDialogo(); cerrarMensaje();
        pulsar("navHistorial"); esperarTabla("historialTabla",0); texto("desde","fecha inválida"); pulsar("consultarHistorial"); esperarDialogo(); cerrarMensaje();
        assertTrue(new VentaController(sesion).historial(java.time.LocalDate.now(),java.time.LocalDate.now()).isEmpty());
    }
    @Test @Order(4) void swingWorkerMantieneLibreElHiloGrafico() throws Exception {
        CountDownLatch trabajo = new CountDownLatch(1), liberar = new CountDownLatch(1), terminado = new CountDownLatch(1);
        AtomicBoolean fueraEdt = new AtomicBoolean(), callbackEdt = new AtomicBoolean();
        edt(() -> {
            JFrame f = buscar("login",JFrame.class); TareasUi tareas = new TareasUi(f,new JLabel(),new JProgressBar());
            tareas.ejecutar("Prueba…",() -> { fueraEdt.set(!SwingUtilities.isEventDispatchThread()); trabajo.countDown(); liberar.await(10,TimeUnit.SECONDS); return true; },ok -> { callbackEdt.set(SwingUtilities.isEventDispatchThread()); terminado.countDown(); }); return null;
        });
        try {
            assertTrue(trabajo.await(10,TimeUnit.SECONDS));
            assertTrue(edt(() -> !buscar("iniciarSesion",JButton.class).isEnabled()));
            assertEquals("responde",edt(() -> "responde"));
        } finally { liberar.countDown(); }
        assertTrue(terminado.await(10,TimeUnit.SECONDS)); assertTrue(fueraEdt.get()); assertTrue(callbackEdt.get());
    }
    private void entrar(String usuario, String clave) throws Exception {
        texto("loginUsuario",usuario); texto("loginPassword",clave); pulsar("iniciarSesion"); esperarTabla("cajaProductosTabla",2);
    }
    private void texto(String nombre, String valor) throws Exception { edt(() -> { buscar(nombre,JTextField.class).setText(valor); return null; }); }
    private void seleccionar(String nombre, int fila) throws Exception { edt(() -> { buscar(nombre,JTable.class).setRowSelectionInterval(fila,fila); return null; }); }
    private void pulsar(String nombre) throws Exception {
        esperar(() -> existe(nombre) && buscar(nombre,JButton.class).isEnabled());
        Point p = edt(() -> { JButton b = buscar(nombre,JButton.class); Point xy = b.getLocationOnScreen(); xy.translate(b.getWidth()/2,b.getHeight()/2); return xy; });
        robot.mouseMove(p.x,p.y); robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    private void aceptarDialogo() throws Exception {
        esperarDialogo(); edt(() -> { JOptionPane p = panelMensaje(); p.setValue(JOptionPane.YES_OPTION); return null; });
    }
    private void cerrarMensaje() throws Exception { edt(() -> { panelMensaje().setValue(JOptionPane.OK_OPTION); return null; }); }
    private void esperarDialogo() throws Exception { esperar(() -> panelMensaje() != null); }
    private static JOptionPane panelMensaje() {
        for (Window w : Window.getWindows()) if (w.isShowing()) { JOptionPane p = encontrarTipo(w,JOptionPane.class); if (p != null) return p; } return null;
    }
    private void esperarTabla(String nombre, int filas) throws Exception {
        esperar(() -> existe(nombre) && buscar(nombre,JTable.class).isEnabled() && buscar(nombre,JTable.class).getRowCount() == filas);
    }
    private static boolean tablaContiene(String nombre, Object valor) {
        JTable t = buscar(nombre,JTable.class);
        for (int i = 0; i < t.getRowCount(); i++) for (int j = 0; j < t.getColumnCount(); j++) if (valor.equals(t.getValueAt(i,j))) return true;
        return false;
    }
    private void esperar(BooleanSupplier condicion) throws Exception {
        long limite = System.nanoTime()+TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < limite) { if (edt(condicion::getAsBoolean)) return; Thread.sleep(60); }
        captura("fallo"); fail("La interfaz no alcanzó el estado esperado.");
    }
    private static <T> T edt(Callable<T> accion) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) return accion.call();
        FutureTask<T> tarea = new FutureTask<>(accion); SwingUtilities.invokeLater(tarea); return tarea.get(10,TimeUnit.SECONDS);
    }
    private static boolean existe(String nombre) { return buscarOpcional(nombre,Component.class) != null; }
    private static <T extends Component> T buscar(String nombre, Class<T> tipo) {
        T c = buscarOpcional(nombre,tipo); if (c == null) throw new IllegalStateException("Componente no visible: " + nombre); return c;
    }
    private static <T extends Component> T buscarOpcional(String nombre, Class<T> tipo) {
        for (Window w : Window.getWindows()) if (w.isShowing()) { T c = encontrar(w,nombre,tipo); if (c != null) return c; } return null;
    }
    private static <T extends Component> T encontrar(Component c, String nombre, Class<T> tipo) {
        if (!c.isShowing()) return null;
        if (nombre.equals(c.getName()) && tipo.isInstance(c)) return tipo.cast(c);
        if (c instanceof Container padre) for (Component hijo : padre.getComponents()) { T valor = encontrar(hijo,nombre,tipo); if (valor != null) return valor; } return null;
    }
    private static <T extends Component> T encontrarTipo(Component c, Class<T> tipo) {
        if (tipo.isInstance(c)) return tipo.cast(c);
        if (c instanceof Container padre) for (Component hijo : padre.getComponents()) { T valor = encontrarTipo(hijo,tipo); if (valor != null) return valor; } return null;
    }
    private void captura(String nombre) throws Exception {
        robot.waitForIdle();
        robot.waitForIdle(); Thread.sleep(180); Toolkit.getDefaultToolkit().sync();
        Path directorio = Path.of("target/capturas"); Files.createDirectories(directorio);
        BufferedImage imagen = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        if (nombre.equals("01-login")) {
            Point origen = edt(() -> buscar("login",JFrame.class).getLocationOnScreen());
            assertEquals(Ui.AZUL.getRGB(),imagen.getRGB(origen.x+20,origen.y+140),"Login debe dibujar su fondo desde el primer repintado.");
        }
        ImageIO.write(imagen,"png",directorio.resolve(nombre+".png").toFile());
    }
}
