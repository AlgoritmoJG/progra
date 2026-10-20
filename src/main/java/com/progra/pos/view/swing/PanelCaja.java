package com.progra.pos.view.swing;


import com.progra.pos.controller.*;
import com.progra.pos.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
final class PanelCaja extends JPanel {
    private final TareasUi tareas;
    private final CatalogoController catalogo;
    private final VentaController ventas;
    private final JTextField busqueda = Ui.texto("buscarCaja",20), cantidad = Ui.texto("cantidad",5);
    private final DefaultTableModel productosModelo = Ui.modelo("Id","Código","Producto","Precio base","Stock","Estado");
    private final DefaultTableModel carritoModelo = Ui.modelo("Id","Producto","Cantidad","Unitario","Subtotal");
    private final JTable productosTabla = Ui.tabla("cajaProductosTabla",productosModelo), carritoTabla = Ui.tabla("carritoTabla",carritoModelo);
    private final JComboBox<Cliente> cliente = new JComboBox<>();
    private final JComboBox<MetodoPago> metodo = new JComboBox<>(MetodoPago.values());
    private final JLabel total = Ui.titulo("Total estimado: Q 0.00"), aviso = new JLabel(" ");
    private final JButton confirmar;
    private List<Producto> productosVisibles = List.of();
    private List<VentaController.LineaCarrito> lineas = List.of();
    private boolean hayCarrito;
    private record Datos(List<Producto> productos, List<Cliente> clientes, List<VentaController.LineaCarrito> carrito, VentaController.ResumenCarrito resumen, String aviso) {}
    PanelCaja(TareasUi tareas, CatalogoController catalogo, VentaController ventas, Runnable gestionarClientes) {
        super(new BorderLayout(10,10)); this.tareas = tareas; this.catalogo = catalogo; this.ventas = ventas; setName("caja"); setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        cantidad.setText("1"); cliente.setName("clienteCaja"); cliente.setPreferredSize(new Dimension(240,30)); metodo.setName("metodoPago"); total.setName("totalCarrito"); aviso.setName("avisoCarrito"); aviso.setForeground(new Color(160,30,30));
        cliente.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean foco) {
                return super.getListCellRendererComponent(l,v instanceof Cliente c ? c.getNombre()+" · "+c.getNit() : "",i,s,foco);
            }
        });
        JPanel cabecera = new JPanel(new BorderLayout()); cabecera.add(Ui.titulo("Caja · Nueva venta"),BorderLayout.NORTH);
        cabecera.add(Ui.fila(new JLabel("Producto o código"),busqueda,Ui.boton("Buscar / actualizar","buscarProductosCaja",this::cargar)),BorderLayout.SOUTH); busqueda.addActionListener(e -> cargar()); add(cabecera,BorderLayout.NORTH);
        JPanel productos = new JPanel(new BorderLayout(4,4)); productos.add(new JScrollPane(productosTabla));
        productos.add(Ui.fila(new JLabel("Cantidad"),cantidad,Ui.boton("Agregar al carrito","agregarCarrito",this::agregar)),BorderLayout.SOUTH);
        JPanel carrito = new JPanel(new BorderLayout(4,4)); carrito.add(new JLabel("Carrito de la venta"),BorderLayout.NORTH); carrito.add(new JScrollPane(carritoTabla));
        carrito.add(Ui.fila(Ui.boton("Quitar seleccionado","quitarCarrito",this::quitar),Ui.boton("Vaciar carrito","vaciarCarrito",this::vaciar)),BorderLayout.SOUTH);
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT,productos,carrito); division.setResizeWeight(0.52); division.setDividerLocation(230); add(division,BorderLayout.CENTER);
        JPanel pie = new JPanel(); pie.setLayout(new BoxLayout(pie,BoxLayout.Y_AXIS));
        pie.add(Ui.fila(new JLabel("Cliente"),cliente,Ui.boton("Gestionar clientes","gestionarClientes",gestionarClientes),new JLabel("Pago"),metodo));
        pie.add(Ui.fila(aviso)); confirmar = Ui.boton("Confirmar venta","confirmarVenta",this::confirmar); confirmar.setEnabled(false);
        pie.add(Ui.fila(total,confirmar)); pie.add(Ui.fila(new JLabel("IVA incluido. Se validan precio vigente y existencias al confirmar."))); add(pie,BorderLayout.SOUTH);
    }
    boolean tieneCarrito() { return hayCarrito; }
    private Datos datos() {
        List<Producto> productos = catalogo.buscarProductos(""); List<Cliente> clientes = catalogo.listarClientes();
        List<VentaController.LineaCarrito> carrito = ventas.verCarrito();
        try { return new Datos(productos,clientes,carrito,ventas.consultarResumen(),""); }
        catch (IllegalArgumentException e) { return new Datos(productos,clientes,carrito,null,e.getMessage()); }
    }
    void cargar() { ejecutar("Actualizando Caja…",() -> {}); }
    private void ejecutar(String texto, Runnable operacion) {
        String filtro = busqueda.getText().strip().toLowerCase(Locale.ROOT);
        Cliente seleccionado = (Cliente)cliente.getSelectedItem(); int idCliente = seleccionado == null ? 0 : seleccionado.getIdCliente();
        tareas.ejecutar(texto,() -> { operacion.run(); return datos(); },datos -> mostrar(datos,filtro,idCliente));
    }
    private void mostrar(Datos datos, String filtro, int idCliente) {
        productosVisibles = datos.productos().stream().filter(p -> (p.getCodigo()+" "+p.getNombre()).toLowerCase(Locale.ROOT).contains(filtro)).toList();
        productosModelo.setRowCount(0);
        for (Producto p : productosVisibles) productosModelo.addRow(new Object[]{p.getIdProducto(),p.getCodigo(),p.getNombre(),Ui.moneda(p.getPrecio()),p.getStock(),!p.isActivo() ? "Inactivo" : p instanceof ProductoPerecedero perecedero && perecedero.getFechaVencimiento().isBefore(java.time.LocalDate.now()) ? "Vencido" : p.estaEnStockMinimo() ? "Stock bajo" : "Disponible"});
        lineas = List.copyOf(datos.carrito()); hayCarrito = !lineas.isEmpty(); carritoModelo.setRowCount(0);
        Map<Integer,Producto> porId = new HashMap<>(); for (Producto p : datos.productos()) porId.put(p.getIdProducto(),p);
        Map<Integer,VentaController.LineaResumen> importes = new HashMap<>();
        if (datos.resumen() != null) for (var linea : datos.resumen().lineas()) importes.put(linea.idProducto(),linea);
        for (var linea : lineas) {
            Producto p = porId.get(linea.idProducto()); var importe = importes.get(linea.idProducto());
            carritoModelo.addRow(new Object[]{linea.idProducto(),p == null ? "Producto no disponible" : p.getNombre(),linea.cantidad(),importe == null ? "—" : Ui.moneda(importe.precioUnitario()),importe == null ? "—" : Ui.moneda(importe.subtotal())});
        }
        cliente.removeAllItems(); for (Cliente c : datos.clientes()) cliente.addItem(c);
        for (Cliente c : datos.clientes()) if ((idCliente == 0 && c.getNit().equalsIgnoreCase("CF")) || c.getIdCliente() == idCliente) cliente.setSelectedItem(c);
        total.setText(datos.resumen() == null ? "Revise los productos del carrito" : "Total estimado: " + Ui.moneda(datos.resumen().total()));
        aviso.setText(datos.aviso().isEmpty() ? " " : datos.aviso()); confirmar.setEnabled(hayCarrito && datos.resumen() != null && cliente.getItemCount() > 0);
    }
    private void agregar() {
        int seleccion = productosTabla.getSelectedRow();
        if (seleccion < 0) { Ui.info(this,"Seleccione un producto."); return; }
        try {
            int unidades = com.progra.pos.util.Validacion.positivo(Ui.entero(cantidad,"Cantidad"),"Cantidad");
            int id = productosVisibles.get(productosTabla.convertRowIndexToModel(seleccion)).getIdProducto();
            ejecutar("Agregando producto…",() -> ventas.agregar(id,unidades));
        } catch (IllegalArgumentException e) { Ui.error(this,e); }
    }
    private void quitar() {
        int seleccion = carritoTabla.getSelectedRow(); if (seleccion < 0) { Ui.info(this,"Seleccione una línea del carrito."); return; }
        int id = lineas.get(carritoTabla.convertRowIndexToModel(seleccion)).idProducto(); ejecutar("Quitando producto…",() -> ventas.quitar(id));
    }
    private void vaciar() {
        if (hayCarrito && Ui.confirmar(this,"¿Desea vaciar el carrito?"))
            ejecutar("Vaciando carrito…",() -> { for (var l : ventas.verCarrito()) ventas.quitar(l.idProducto()); });
    }
    private void confirmar() {
        Cliente c = (Cliente)cliente.getSelectedItem(); MetodoPago pago = (MetodoPago)metodo.getSelectedItem();
        if (c == null || !hayCarrito) { Ui.info(this,"Agregue productos y seleccione un cliente."); return; }
        if (!Ui.confirmar(this,total.getText()+"\nCliente: "+c.getNombre()+" · "+pago+"\nSe aplicarán los precios vigentes al confirmar. ¿Continuar?")) return;
        tareas.ejecutar("Confirmando venta…",() -> ventas.confirmar(c.getIdCliente(),pago),venta -> {
            // La venta ya quedó confirmada: no repetirla si después falla la actualización visual.
            hayCarrito = false; lineas = List.of(); carritoModelo.setRowCount(0); confirmar.setEnabled(false); total.setText("Venta confirmada: " + Ui.moneda(venta.getTotal()));
            new DetalleVentaDialog(this,venta).setVisible(true); cargar();
        });
    }
}
