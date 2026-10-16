package com.progra.pos.model;


import java.util.Set;
public final class Cajero extends Usuario {
    public Cajero(int id, String nombre, String username, String hash, boolean activo) { super(id, nombre, username, hash, activo); }
    @Override public Rol obtenerRol() { return Rol.CAJERO; }
    @Override public Set<Permiso> obtenerPermisos() { return Set.of(Permiso.CONSULTAR_PRODUCTOS, Permiso.REGISTRAR_VENTAS, Permiso.CONSULTAR_VENTAS_PROPIAS); }
}
