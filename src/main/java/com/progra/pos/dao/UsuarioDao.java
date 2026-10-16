package com.progra.pos.dao;


import com.progra.pos.model.*;
import com.progra.pos.util.*;
import java.sql.*;
import java.util.ArrayList;
public final class UsuarioDao {
    private static Usuario mapear(ResultSet rs) throws SQLException {
        return rs.getString("rol").equals("ADMINISTRADOR")
            ? new Administrador(rs.getInt("id_usuario"),rs.getString("nombre"),rs.getString("username"),rs.getString("password_hash"),rs.getBoolean("activo"))
            : new Cajero(rs.getInt("id_usuario"),rs.getString("nombre"),rs.getString("username"),rs.getString("password_hash"),rs.getBoolean("activo"));
    }
    public Usuario porUsername(String username) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios WHERE username=?")) {
            ps.setString(1,Validacion.texto(username,"Username",50)); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public Usuario buscar(int id) {
        try (Connection c = Conexion.abrir()) { return buscar(c,id,false); } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    static Usuario buscar(Connection c, int id, boolean bloquear) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios WHERE id_usuario=?" + (bloquear ? " FOR UPDATE" : ""))) {
            ps.setInt(1,Validacion.positivo(id,"Id")); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalArgumentException("Usuario inexistente."); return mapear(rs); }
        }
    }
    public ArrayList<Usuario> listar() {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios ORDER BY id_usuario"); ResultSet rs = ps.executeQuery()) {
            ArrayList<Usuario> lista = new ArrayList<>(); while (rs.next()) lista.add(mapear(rs)); return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public int crear(Usuario u) { return insertar(u,false); }
    public int crearPrimerAdministrador(Usuario u) {
        if (u.obtenerRol() != Rol.ADMINISTRADOR || !u.isActivo()) throw new IllegalArgumentException("Se necesita un administrador activo.");
        return insertar(u,true);
    }
    private int insertar(Usuario u, boolean soloPrimero) {
        // INSERT condicional: el arranque no permite agregar administradores una vez que existen usuarios.
        String sql = soloPrimero ? "INSERT INTO usuarios(nombre,username,password_hash,activo,rol) SELECT ?,?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM usuarios AS existentes)" : "INSERT INTO usuarios(nombre,username,password_hash,activo,rol) VALUES (?,?,?,?,?)";
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            campos(ps,u); Jdbc.uno(ps.executeUpdate()); return Jdbc.id(ps);
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    private void campos(PreparedStatement ps, Usuario u) throws SQLException { ps.setString(1,u.getNombre()); ps.setString(2,u.getUsername()); ps.setString(3,u.getPasswordHash()); ps.setBoolean(4,u.isActivo()); ps.setString(5,u.obtenerRol().name()); }
    public void actualizar(Usuario u) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET nombre=?,username=?,password_hash=?,activo=?,rol=? WHERE id_usuario=?")) {
            campos(ps,u); ps.setInt(6,Validacion.positivo(u.getIdUsuario(),"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void eliminar(int id) {
        // Usuarios con ventas se desactivan para conservar la auditoría.
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                buscar(c,id,true); boolean vendido;
                try (PreparedStatement ps = c.prepareStatement("SELECT id_venta FROM ventas WHERE usuario_id=? LIMIT 1")) { ps.setInt(1,id); try (ResultSet rs = ps.executeQuery()) { vendido = rs.next(); } }
                try (PreparedStatement ps = c.prepareStatement(vendido ? "UPDATE usuarios SET activo=FALSE WHERE id_usuario=?" : "DELETE FROM usuarios WHERE id_usuario=?")) { ps.setInt(1,id); Jdbc.uno(ps.executeUpdate()); }
                c.commit();
            } catch (SQLException | RuntimeException e) { try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
