package com.progra.pos.model;


import com.progra.pos.util.Validacion;
public final class Cliente {
    private final int idCliente;
    private final String nombre, nit, telefono;
    public Cliente(int id, String nombre, String nit, String telefono) {
        idCliente = Validacion.noNegativo(id, "Id");
        this.nombre = Validacion.texto(nombre, "Nombre", 100);
        this.nit = Validacion.texto(nit, "NIT", 20);
        if (telefono != null && telefono.length() > 25) throw new IllegalArgumentException("Teléfono: máximo 25 caracteres.");
        this.telefono = telefono == null ? "" : telefono.strip();
    }
    public int getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getTelefono() { return telefono; }
}
