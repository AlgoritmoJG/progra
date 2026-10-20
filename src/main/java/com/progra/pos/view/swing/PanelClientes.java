package com.progra.pos.view.swing;


import com.progra.pos.controller.CatalogoController;
import com.progra.pos.model.Cliente;
import javax.swing.*;
import java.util.List;
final class PanelClientes extends PanelListado<Cliente> {
    private final CatalogoController catalogo;
    private final boolean administrador;
    PanelClientes(TareasUi tareas, CatalogoController catalogo, boolean administrador) {
        super("Clientes de Caja","clientes",tareas,true,"Id","Nombre","NIT","Teléfono"); this.catalogo = catalogo; this.administrador = administrador;
        if (!administrador) ocultarBorrado(this);
    }
    private void ocultarBorrado(java.awt.Container contenedor) {
        for (java.awt.Component c : contenedor.getComponents()) {
            if ("eliminarRegistro".equals(c.getName())) c.setVisible(false);
            if (c instanceof java.awt.Container hijo) ocultarBorrado(hijo);
        }
    }
    @Override List<Cliente> listar(String filtro) {
        String texto = filtro.toLowerCase(java.util.Locale.ROOT);
        return catalogo.listarClientes().stream().filter(c -> (c.getNombre()+" "+c.getNit()).toLowerCase(java.util.Locale.ROOT).contains(texto)).toList();
    }
    @Override Object[] fila(Cliente c) { return new Object[]{c.getIdCliente(),c.getNombre(),c.getNit(),c.getTelefono()}; }
    @Override void editar(Cliente actual) {
        if (actual != null && actual.getNit().equalsIgnoreCase("CF")) { Ui.info(this,"Consumidor Final se conserva sin modificaciones."); return; }
        Formulario f = new Formulario(this,actual == null ? "Nuevo cliente" : "Editar cliente");
        JTextField nombre = Ui.texto("nombre",26), nit = Ui.texto("nit",20), telefono = Ui.texto("telefono",20);
        if (actual != null) { nombre.setText(actual.getNombre()); nit.setText(actual.getNit()); telefono.setText(actual.getTelefono()); }
        f.campo("Nombre",nombre); f.campo("NIT",nit); f.campo("Teléfono",telefono);
        Cliente c = f.mostrar(() -> new Cliente(actual == null ? 0 : actual.getIdCliente(),nombre.getText(),nit.getText(),telefono.getText()));
        if (c != null) tareas.ejecutar("Guardando cliente…",() -> { if (actual == null) catalogo.crearCliente(c); else catalogo.actualizarCliente(c); return true; },ok -> cargar());
    }
    @Override void eliminar(Cliente c) {
        if (!administrador) throw new SecurityException("Solo el administrador puede eliminar clientes."); catalogo.eliminarCliente(c.getIdCliente());
    }
}
