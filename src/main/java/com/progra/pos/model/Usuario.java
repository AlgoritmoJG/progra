package com.progra.pos.model;


import java.util.Set;
import com.progra.pos.util.Validacion;
public abstract class Usuario {
    private final int idUsuario;
    private final String nombre, username, passwordHash;
    private final boolean activo;
    protected Usuario(int id, String nombre, String username, String hash, boolean activo) {
        this.idUsuario = Validacion.noNegativo(id, "Id");
        this.nombre = Validacion.texto(nombre, "Nombre", 100);
        this.username = Validacion.texto(username, "Username", 50);
        this.passwordHash = Validacion.texto(hash, "Hash", 100);
        this.activo = activo;
    }
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActivo() { return activo; }
    public abstract Rol obtenerRol();
    public abstract Set<Permiso> obtenerPermisos();
    public boolean tienePermiso(Permiso permiso) { return activo && obtenerPermisos().contains(permiso); }
}
