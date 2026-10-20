package com.progra.pos.view.swing;


import com.progra.pos.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
final class DetalleVentaDialog extends JDialog {
    DetalleVentaDialog(Component padre, Venta venta) {
        super(SwingUtilities.getWindowAncestor(padre),"Venta #" + venta.getIdVenta(),ModalityType.APPLICATION_MODAL); setName("detalleVenta"); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel cabecera = new JPanel(new GridLayout(0,1,4,4)); cabecera.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));
        cabecera.add(Ui.titulo("Venta #" + venta.getIdVenta())); cabecera.add(new JLabel("Fecha: " + venta.getFecha() + " · " + venta.getMetodoPago()));
        cabecera.add(new JLabel("Registró: " + venta.getUsuario().getNombre() + " · Cliente: " + venta.getCliente().getNombre()));
        DefaultTableModel modelo = Ui.modelo("Código","Producto","Cantidad","Precio cobrado","Subtotal");
        for (DetalleVenta d : venta.obtenerDetalles()) modelo.addRow(new Object[]{d.getProducto().getCodigo(),d.getProducto().getNombre(),d.getCantidad(),Ui.moneda(d.getPrecioUnitario()),Ui.moneda(d.getSubtotal())});
        add(cabecera,BorderLayout.NORTH); add(new JScrollPane(Ui.tabla("detalleVentaTabla",modelo)),BorderLayout.CENTER);
        JLabel total = Ui.titulo("Total: " + Ui.moneda(venta.getTotal())); total.setName("totalVenta");
        JPanel pie = new JPanel(new BorderLayout()); pie.setBorder(BorderFactory.createEmptyBorder(12,16,12,16)); pie.add(total); pie.add(Ui.boton("Cerrar","cerrarDetalle",this::dispose),BorderLayout.EAST); add(pie,BorderLayout.SOUTH);
        setSize(780,440); setLocationRelativeTo(getOwner());
    }
}
