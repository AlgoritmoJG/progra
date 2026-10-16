package com.progra.pos.model;


import com.progra.pos.util.Validacion;
public final class Categoria {
    private final int idCategoria;
    private final String nombre, descripcion;
    public Categoria(int id, String nombre, String descripcion) {
        idCategoria = Validacion.noNegativo(id, "Id");
        this.nombre = Validacion.texto(nombre, "Nombre", 100);
        if (descripcion != null && descripcion.length() > 255) throw new IllegalArgumentException("Descripción: máximo 255 caracteres.");
        this.descripcion = descripcion == null ? "" : descripcion.strip();
    }
    public int getIdCategoria() { return idCategoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
