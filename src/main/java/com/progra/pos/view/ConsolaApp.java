package com.progra.pos.view;


import com.progra.pos.controller.*;
import com.progra.pos.model.*;
import com.progra.pos.util.AccesoDatosException;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
public final class ConsolaApp {
    private final BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
    private String leer(String mensaje) throws IOException {
        System.out.print(mensaje + ": ");
        String linea = entrada.readLine(); if (linea == null) throw new EOFException(); return linea.strip();
    }
    private int entero(String mensaje) throws IOException { return Integer.parseInt(leer(mensaje)); }
    private boolean booleano(String mensaje) throws IOException {
        String s = leer(mensaje + " (s/n)");
        if (!s.equalsIgnoreCase("s") && !s.equalsIgnoreCase("n")) throw new IllegalArgumentException("Responda s o n.");
        return s.equalsIgnoreCase("s");
    }
    private String clave(String mensaje) throws IOException {
        Console consola = System.console();
        if (consola != null) {
            char[] chars = consola.readPassword("%s: ",mensaje);
            if (chars == null) throw new EOFException();
            try { return new String(chars); } finally { Arrays.fill(chars,'\0'); }
        }
        // NetBeans puede no ofrecer Console. No mostrar contraseñas en capturas del IDE.
        System.out.println("Aviso: esta terminal mostrará lo que escriba; use una terminal real para ocultar la clave.");
        System.out.print(mensaje + ": ");
        String valor = entrada.readLine();
        if (valor == null) throw new EOFException();
        return valor; // La contraseña conserva sus espacios; no se aplica strip().
    }
    public static void main(String[] args) {
        try { new ConsolaApp().ejecutar(args); }
        catch (EOFException e) { System.out.println("Entrada finalizada."); }
        catch (AccesoDatosException | IllegalArgumentException | SecurityException | IllegalStateException e) { System.err.println(e.getMessage()); System.exit(1); }
        catch (IOException e) { System.err.println("No se pudo leer la entrada."); System.exit(1); }
    }
    private void ejecutar(String[] args) throws IOException {
        AutenticacionController autenticacion = new AutenticacionController();
        if (args.length == 1 && args[0].equals("--crear-admin")) {
            int id = autenticacion.crearPrimerAdministrador(leer("Nombre"),leer("Username"),clave("Contraseña, mínimo 8 caracteres"));
            System.out.println("Administrador creado: " + id); return;
        }
        Sesion sesion = autenticacion.iniciarSesion(leer("Username"),clave("Contraseña"));
        CatalogoController catalogo = new CatalogoController(sesion);
        VentaController caja = new VentaController(sesion);
        boolean administrador = sesion.exigir(Permiso.CONSULTAR_PRODUCTOS).obtenerRol() == Rol.ADMINISTRADOR;
        while (true) {
            System.out.println("\nproductos | categorias | clientes | agregar | quitar | carrito | confirmar | historial | salir");
            if (administrador) System.out.println("categoria-crear/editar/borrar | producto-crear/editar/borrar | cliente-borrar | usuarios | usuario-crear/editar/borrar");
            System.out.println("cliente-crear | cliente-editar");
            String orden = leer("Operación");
            if (orden.equals("salir")) return;
            try {
                switch (orden) {
                    case "categorias" -> catalogo.listarCategorias().forEach(c -> System.out.printf("%d | %s | %s%n",c.getIdCategoria(),c.getNombre(),c.getDescripcion()));
                    case "categoria-crear" -> System.out.println("Id: " + catalogo.crearCategoria(new Categoria(0,leer("Nombre"),leer("Descripción"))));
                    case "categoria-editar" -> catalogo.actualizarCategoria(new Categoria(entero("Id categoría"),leer("Nombre"),leer("Descripción")));
                    case "categoria-borrar" -> catalogo.eliminarCategoria(entero("Id categoría"));
                    case "productos" -> catalogo.buscarProductos(leer("Buscar, vacío para todos")).forEach(p -> System.out.printf("%d | %s | %s | Q%s | stock %d | activo %s%s%n",p.getIdProducto(),p.getCodigo(),p.getNombre(),p.getPrecio(),p.getStock(),p.isActivo(),p.estaEnStockMinimo() ? " | ALERTA STOCK" : ""));
                    case "producto-crear" -> System.out.println("Id: " + catalogo.crearProducto(producto(0,catalogo)));
                    case "producto-editar" -> catalogo.actualizarProducto(producto(entero("Id producto"),catalogo));
                    case "producto-borrar" -> { catalogo.eliminarProducto(entero("Id producto")); System.out.println("Eliminado si no tenía ventas; desactivado si tenía ventas."); }
                    case "clientes" -> catalogo.listarClientes().forEach(c -> System.out.printf("%d | %s | %s | %s%n",c.getIdCliente(),c.getNombre(),c.getNit(),c.getTelefono()));
                    case "cliente-crear" -> System.out.println("Id: " + catalogo.crearCliente(cliente(0)));
                    case "cliente-editar" -> catalogo.actualizarCliente(cliente(entero("Id cliente")));
                    case "cliente-borrar" -> catalogo.eliminarCliente(entero("Id cliente"));
                    case "usuarios" -> catalogo.listarUsuarios().forEach(u -> System.out.printf("%d | %s | %s | %s | activo %s%n",u.getIdUsuario(),u.getNombre(),u.getUsername(),u.obtenerRol(),u.isActivo()));
                    case "usuario-crear" -> System.out.println("Id: " + catalogo.crearUsuario(leer("Nombre"),leer("Username"),clave("Contraseña"),Rol.valueOf(leer("ADMINISTRADOR o CAJERO"))));
                    case "usuario-editar" -> {
                        int id = entero("Id usuario"); String nombre = leer("Nombre"), username = leer("Username");
                        String password = clave("Nueva contraseña, vacío conserva la actual");
                        catalogo.actualizarUsuario(id,nombre,username,password.isEmpty() ? null : password,Rol.valueOf(leer("ADMINISTRADOR o CAJERO")),booleano("Activo"));
                    }
                    case "usuario-borrar" -> catalogo.eliminarUsuario(entero("Id usuario"));
                    case "agregar" -> caja.agregar(entero("Id producto"),entero("Cantidad"));
                    case "quitar" -> caja.quitar(entero("Id producto"));
                    case "carrito" -> {
                        VentaController.ResumenCarrito resumen = caja.consultarResumen();
                        resumen.lineas().forEach(l -> System.out.printf("%s | cantidad %d | unitario Q%s | subtotal Q%s%n",l.nombre(),l.cantidad(),l.precioUnitario(),l.subtotal()));
                        System.out.println("Total estimado Q" + resumen.total() + "; precio y stock se validan nuevamente al confirmar.");
                    }
                    case "confirmar" -> {
                        int clienteId = entero("Id cliente (consulte clientes para Consumidor Final)");
                        MetodoPago metodo = MetodoPago.valueOf(leer("EFECTIVO o TARJETA"));
                        if (booleano("Confirmar con precios vigentes")) mostrarVenta(caja.confirmar(clienteId,metodo));
                    }
                    case "historial" -> caja.historial(LocalDate.parse(leer("Desde AAAA-MM-DD")),LocalDate.parse(leer("Hasta AAAA-MM-DD"))).forEach(ConsolaApp::mostrarVenta);
                    default -> System.out.println("Operación desconocida.");
                }
            } catch (AccesoDatosException | IllegalArgumentException | SecurityException | IllegalStateException | java.time.DateTimeException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    private Cliente cliente(int id) throws IOException { return new Cliente(id,leer("Nombre"),leer("NIT"),leer("Teléfono")); }
    private Producto producto(int id, CatalogoController catalogo) throws IOException {
        String codigo = leer("Código"), nombre = leer("Nombre"); BigDecimal precio = new BigDecimal(leer("Precio con IVA, punto decimal"));
        int stock = entero("Stock"), minimo = entero("Stock mínimo"); boolean activo = booleano("Activo");
        Categoria categoria = catalogo.buscarCategoria(entero("Id categoría"));
        return booleano("Perecedero")
            ? new ProductoPerecedero(id,codigo,nombre,precio,stock,minimo,activo,categoria,LocalDate.parse(leer("Vencimiento AAAA-MM-DD")))
            : new ProductoNoPerecedero(id,codigo,nombre,precio,stock,minimo,activo,categoria);
    }
    private static void mostrarVenta(Venta v) {
        System.out.printf("Venta %d | %s | usuario %s | cliente %s | %s | Q%s%n",v.getIdVenta(),v.getFecha(),v.getUsuario().getUsername(),v.getCliente().getNombre(),v.getMetodoPago(),v.getTotal());
        v.obtenerDetalles().forEach(d -> System.out.printf("  %s x%d | unitario Q%s | subtotal Q%s%n",d.getProducto().getNombre(),d.getCantidad(),d.getPrecioUnitario(),d.getSubtotal()));
    }
}
