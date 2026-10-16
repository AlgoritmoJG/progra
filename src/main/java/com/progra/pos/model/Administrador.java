package com.progra.pos.model;


import java.util.Set;
public final class Administrador extends Usuario {
    public Administrador(int id, String nombre, String username, String hash, boolean activo) { super(id, nombre, username, hash, activo); }
    @Override public Rol obtenerRol() { return Rol.ADMINISTRADOR; }
    @Override public Set<Permiso> obtenerPermisos() { return Set.copyOf(java.util.EnumSet.allOf(Permiso.class)); }
}
