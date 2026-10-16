package com.progra.pos.dao;


import com.progra.pos.model.Categoria;
import com.progra.pos.util.*;
import java.sql.*;
import java.util.ArrayList;
public final class CategoriaDao {
    public int crear(Categoria categoria) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO categorias(nombre,descripcion) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre()); ps.setString(2, categoria.getDescripcion()); ps.executeUpdate(); return Jdbc.id(ps);
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public ArrayList<Categoria> listar() {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT id_categoria,nombre,descripcion FROM categorias ORDER BY id_categoria"); ResultSet rs = ps.executeQuery()) {
            ArrayList<Categoria> lista = new ArrayList<>(); while (rs.next()) lista.add(mapear(rs)); return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public Categoria buscar(int id) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT id_categoria,nombre,descripcion FROM categorias WHERE id_categoria=?")) {
            ps.setInt(1, Validacion.positivo(id,"Id")); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalArgumentException("Categoría inexistente."); return mapear(rs); }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    private Categoria mapear(ResultSet rs) throws SQLException { return new Categoria(rs.getInt("id_categoria"),rs.getString("nombre"),rs.getString("descripcion")); }
    public void actualizar(Categoria categoria) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE categorias SET nombre=?,descripcion=? WHERE id_categoria=?")) {
            ps.setString(1,categoria.getNombre()); ps.setString(2,categoria.getDescripcion()); ps.setInt(3,Validacion.positivo(categoria.getIdCategoria(),"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void eliminar(int id) {
        // La FK bloquea la eliminación aun cuando los productos estén desactivados.
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM categorias WHERE id_categoria=?")) {
            ps.setInt(1,Validacion.positivo(id,"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
