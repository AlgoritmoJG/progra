package com.progra.pos.view.swing;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
/** Estructura compartida de los CRUD. Los índices de una tabla ordenada se convierten al modelo. */
abstract class PanelListado<T> extends JPanel {
    final TareasUi tareas;
    final DefaultTableModel modelo;
    final JTable tabla;
    final JTextField filtro = Ui.texto("filtro",20);
    private List<T> elementos = List.of();
    PanelListado(String titulo, String nombre, TareasUi tareas, boolean editable, String... columnas) {
        super(new BorderLayout(10,10)); this.tareas = tareas; setName(nombre); setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        modelo = Ui.modelo(columnas); tabla = Ui.tabla(nombre + "Tabla",modelo);
        JPanel cabecera = new JPanel(new BorderLayout()); cabecera.add(Ui.titulo(titulo),BorderLayout.NORTH);
        JPanel acciones = Ui.fila(new JLabel("Buscar:"),filtro,Ui.boton("Actualizar","actualizarListado",this::cargar));
        filtro.addActionListener(e -> cargar());
        if (editable) {
            acciones.add(Ui.boton("Nuevo","nuevoRegistro",() -> editar(null)));
            acciones.add(Ui.boton("Editar","editarRegistro",() -> conSeleccion(this::editar)));
            acciones.add(Ui.boton("Eliminar / desactivar","eliminarRegistro",() -> conSeleccion(item -> {
                if (Ui.confirmar(this,"¿Desea eliminar este registro? Si tiene historial, se desactivará cuando corresponda."))
                    tareas.ejecutar("Eliminando…",() -> { eliminar(item); return true; },ok -> cargar());
            })));
        }
        cabecera.add(acciones,BorderLayout.SOUTH); add(cabecera,BorderLayout.NORTH); add(new JScrollPane(tabla),BorderLayout.CENTER);
    }
    final void cargar() {
        String texto = filtro.getText().strip();
        tareas.ejecutar("Consultando registros…",() -> listar(texto),lista -> {
            elementos = List.copyOf(lista); modelo.setRowCount(0); for (T item : elementos) modelo.addRow(fila(item));
        });
    }
    private void conSeleccion(java.util.function.Consumer<T> accion) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Ui.info(this,"Seleccione un registro de la tabla."); return; }
        accion.accept(elementos.get(tabla.convertRowIndexToModel(fila)));
    }
    abstract List<T> listar(String filtro);
    abstract Object[] fila(T item);
    abstract void editar(T actual);
    abstract void eliminar(T item);
}
