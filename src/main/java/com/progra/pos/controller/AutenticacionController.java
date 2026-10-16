package com.progra.pos.controller;


import com.progra.pos.dao.UsuarioDao;
import com.progra.pos.model.*;
import com.progra.pos.util.PasswordUtil;
public final class AutenticacionController {
    // Hash de relleno: una cuenta inexistente también ejecuta una comprobación BCrypt.
    private static final String HASH_RELLENO = PasswordUtil.hash("CuentaInexistente-Temporal");
    public Sesion iniciarSesion(String username, String clave) {
        Usuario u = new UsuarioDao().porUsername(username);
        boolean correcta = PasswordUtil.verificar(clave,u == null ? HASH_RELLENO : u.getPasswordHash());
        if (u == null || !u.isActivo() || !correcta) throw new SecurityException("Credenciales inválidas o usuario inactivo.");
        return new Sesion(u.getIdUsuario());
    }
    public int crearPrimerAdministrador(String nombre, String username, String clave) {
        return new UsuarioDao().crearPrimerAdministrador(new Administrador(0,nombre,username,PasswordUtil.hash(clave),true));
    }
}
