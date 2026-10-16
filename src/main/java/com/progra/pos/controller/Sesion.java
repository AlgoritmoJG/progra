package com.progra.pos.controller;


import com.progra.pos.dao.UsuarioDao;
import com.progra.pos.model.*;
public final class Sesion {
    private final int usuarioId;
    // Solo AutenticacionController crea sesiones después de validar la contraseña.
    Sesion(int usuarioId) { this.usuarioId = usuarioId; }
    public Usuario exigir(Permiso permiso) {
        Usuario actual = new UsuarioDao().buscar(usuarioId);
        if (!actual.tienePermiso(permiso)) throw new SecurityException("Sesión inactiva o permiso insuficiente.");
        return actual;
    }
    int getUsuarioId() { return usuarioId; }
}
