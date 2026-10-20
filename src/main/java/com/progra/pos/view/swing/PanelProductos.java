package com.progra.pos.view.swing;


import com.progra.pos.controller.CatalogoController;
import com.progra.pos.model.*;
import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
final class PanelProductos extends PanelListado<Producto> {
    private final CatalogoController catalogo;
    PanelProductos(TareasUi tareas, CatalogoController catalogo, boolean administrador) {
        super(administrador ? "Productos e inventario" : "Consulta de productos","productos",tareas,administrador,"Id","Código","Nombre","Categoría","Precio base","Stock","Mínimo","Estado","Vencimiento","Alerta"); this.catalogo = catalogo;
    }
    @Override List<Producto> listar(String filtro) { return catalogo.buscarProductos(filtro); }
    @Override Object[] fila(Producto p) {
        return new Object[]{p.getIdProducto(),p.getCodigo(),p.getNombre(),p.getCategoria().getNombre(),Ui.moneda(p.getPrecio()),p.getStock(),p.getStockMinimo(),p.isActivo() ? "Activo" : "Inactivo",p instanceof ProductoPerecedero perecedero ? perecedero.getFechaVencimiento().toString() : "—",p.estaEnStockMinimo() ? "STOCK BAJO" : ""};
    }
    @Override void editar(Producto actual) {
        tareas.ejecutar("Cargando categorías…",catalogo::listarCategorias,categorias -> {
            if (categorias.isEmpty()) { Ui.info(this,"Cree al menos una categoría antes de registrar productos."); return; }
            Formulario f = new Formulario(this,actual == null ? "Nuevo producto" : "Editar producto");
            JTextField codigo = Ui.texto("codigo",24), nombre = Ui.texto("nombre",24), precio = Ui.texto("precio",12), stock = Ui.texto("stock",12), minimo = Ui.texto("stockMinimo",12), vence = Ui.texto("fechaVencimiento",12);
            JCheckBox activo = new JCheckBox("Disponible para venta",true), perecedero = new JCheckBox("Tiene fecha de vencimiento");
            activo.setName("activo"); perecedero.setName("perecedero");
            JComboBox<Categoria> categoria = new JComboBox<>(categorias.toArray(Categoria[]::new)); categoria.setName("categoria");
            categoria.setRenderer(new DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean foco) {
                    return super.getListCellRendererComponent(l,v instanceof Categoria c ? c.getNombre() : "",i,s,foco);
                }
            });
            stock.setText("0"); minimo.setText("0");
            if (actual != null) {
                codigo.setText(actual.getCodigo()); nombre.setText(actual.getNombre()); precio.setText(actual.getPrecio().toPlainString()); stock.setText(Integer.toString(actual.getStock())); minimo.setText(Integer.toString(actual.getStockMinimo())); activo.setSelected(actual.isActivo());
                for (Categoria c : categorias) if (c.getIdCategoria() == actual.getCategoria().getIdCategoria()) categoria.setSelectedItem(c);
                if (actual instanceof ProductoPerecedero p) { perecedero.setSelected(true); vence.setText(p.getFechaVencimiento().toString()); }
            }
            vence.setEnabled(perecedero.isSelected()); perecedero.addActionListener(e -> vence.setEnabled(perecedero.isSelected()));
            f.campo("Código único",codigo); f.campo("Nombre",nombre); f.campo("Precio con IVA (Q)",precio); f.campo("Stock",stock); f.campo("Stock mínimo",minimo); f.campo("Categoría",categoria); f.campo("Estado",activo); f.campo("Tipo",perecedero); f.campo("Vencimiento AAAA-MM-DD",vence);
            Producto p = f.mostrar(() -> {
                int id = actual == null ? 0 : actual.getIdProducto(); Categoria c = (Categoria) categoria.getSelectedItem();
                return perecedero.isSelected()
                    ? new ProductoPerecedero(id,codigo.getText(),nombre.getText(),Ui.decimal(precio),Ui.entero(stock,"Stock"),Ui.entero(minimo,"Mínimo"),activo.isSelected(),c,LocalDate.parse(vence.getText().strip()))
                    : new ProductoNoPerecedero(id,codigo.getText(),nombre.getText(),Ui.decimal(precio),Ui.entero(stock,"Stock"),Ui.entero(minimo,"Mínimo"),activo.isSelected(),c);
            });
            if (p != null) tareas.ejecutar("Guardando producto…",() -> { if (actual == null) catalogo.crearProducto(p); else catalogo.actualizarProductoSiVigente(p,actual); return true; },ok -> cargar());
        });
    }
    @Override void eliminar(Producto p) { catalogo.eliminarProducto(p.getIdProducto()); }
}
