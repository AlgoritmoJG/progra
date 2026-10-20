package com.progra.pos.view.swing;


import com.progra.pos.controller.CatalogoController;
import com.progra.pos.model.Categoria;
import javax.swing.*;
import java.util.List;
final class PanelCategorias extends PanelListado<Categoria> {
    private final CatalogoController catalogo;
    PanelCategorias(TareasUi tareas, CatalogoController catalogo) {
        super("Categorías","categorias",tareas,true,"Id","Nombre","Descripción"); this.catalogo = catalogo;
    }
    @Override List<Categoria> listar(String filtro) { return catalogo.listarCategorias().stream().filter(c -> c.getNombre().toLowerCase(java.util.Locale.ROOT).contains(filtro.toLowerCase(java.util.Locale.ROOT))).toList(); }
    @Override Object[] fila(Categoria c) { return new Object[]{c.getIdCategoria(),c.getNombre(),c.getDescripcion()}; }
    @Override void editar(Categoria actual) {
        Formulario f = new Formulario(this,actual == null ? "Nueva categoría" : "Editar categoría");
        JTextField nombre = Ui.texto("nombre",26), descripcion = Ui.texto("descripcion",26);
        if (actual != null) { nombre.setText(actual.getNombre()); descripcion.setText(actual.getDescripcion()); }
        f.campo("Nombre",nombre); f.campo("Descripción",descripcion);
        Categoria c = f.mostrar(() -> new Categoria(actual == null ? 0 : actual.getIdCategoria(),nombre.getText(),descripcion.getText()));
        if (c != null) tareas.ejecutar("Guardando categoría…",() -> { if (actual == null) catalogo.crearCategoria(c); else catalogo.actualizarCategoria(c); return true; },ok -> cargar());
    }
    @Override void eliminar(Categoria c) { catalogo.eliminarCategoria(c.getIdCategoria()); }
}
