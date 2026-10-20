package com.progra.pos.view.swing;


import com.progra.pos.controller.VentaController;
import com.progra.pos.model.Venta;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
final class PanelHistorial extends JPanel {
    private final TareasUi tareas;
    private final VentaController ventas;
    private final JTextField desde = Ui.texto("desde",10), hasta = Ui.texto("hasta",10);
    private final DefaultTableModel modelo = Ui.modelo("Id","Fecha","Usuario","Cliente","Método","Total");
    private final JTable tabla = Ui.tabla("historialTabla",modelo);
    private List<Venta> filas = List.of();
    PanelHistorial(TareasUi tareas, VentaController ventas, boolean administrador) {
        super(new BorderLayout(10,10)); this.tareas = tareas; this.ventas = ventas; setName("historial"); setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        desde.setText(LocalDate.now().withDayOfMonth(1).toString()); hasta.setText(LocalDate.now().toString());
        JPanel cabecera = new JPanel(new BorderLayout()); cabecera.add(Ui.titulo(administrador ? "Historial de todas las ventas" : "Mis ventas"),BorderLayout.NORTH);
        cabecera.add(Ui.fila(new JLabel("Desde (AAAA-MM-DD)"),desde,new JLabel("Hasta"),hasta,Ui.boton("Consultar","consultarHistorial",this::cargar),Ui.boton("Ver detalle","verDetalle",this::detalle)),BorderLayout.SOUTH);
        add(cabecera,BorderLayout.NORTH); add(new JScrollPane(tabla),BorderLayout.CENTER);
        add(new JLabel("Las fechas incluyen ambos extremos. El detalle conserva el precio cobrado."),BorderLayout.SOUTH);
    }
    void cargar() {
        try {
            LocalDate inicio = LocalDate.parse(desde.getText().strip()), fin = LocalDate.parse(hasta.getText().strip());
            if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
            tareas.ejecutar("Consultando ventas…",() -> ventas.historial(inicio,fin),lista -> {
                filas = List.copyOf(lista); modelo.setRowCount(0);
                for (Venta v : filas) modelo.addRow(new Object[]{v.getIdVenta(),v.getFecha().toString(),v.getUsuario().getUsername(),v.getCliente().getNombre(),v.getMetodoPago(),Ui.moneda(v.getTotal())});
            });
        } catch (IllegalArgumentException | java.time.DateTimeException e) { Ui.error(this,e); }
    }
    private void detalle() {
        int seleccion = tabla.getSelectedRow();
        if (seleccion < 0) { Ui.info(this,"Seleccione una venta."); return; }
        new DetalleVentaDialog(this,filas.get(tabla.convertRowIndexToModel(seleccion))).setVisible(true);
    }
}
