package com.progra.pos.dao;


import com.progra.pos.model.Cliente;
import com.progra.pos.util.*;
import java.sql.*;
import java.util.ArrayList;
public final class ClienteDao {
    public int crear(Cliente cliente) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO clientes(nombre,nit,telefono) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            campos(ps,cliente); ps.executeUpdate(); return Jdbc.id(ps);
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    private void campos(PreparedStatement ps, Cliente cl) throws SQLException { ps.setString(1,cl.getNombre()); ps.setString(2,cl.getNit()); ps.setString(3,cl.getTelefono()); }
    static Cliente buscar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM clientes WHERE id_cliente=?")) {
            ps.setInt(1,Validacion.positivo(id,"Id")); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalArgumentException("Cliente inexistente."); return mapear(rs); }
        }
    }
    private static Cliente mapear(ResultSet rs) throws SQLException { return new Cliente(rs.getInt("id_cliente"),rs.getString("nombre"),rs.getString("nit"),rs.getString("telefono")); }
    public Cliente buscar(int id) { try (Connection c = Conexion.abrir()) { return buscar(c,id); } catch (SQLException e) { throw new AccesoDatosException(e); } }
    public ArrayList<Cliente> listar() {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT * FROM clientes ORDER BY id_cliente"); ResultSet rs = ps.executeQuery()) {
            ArrayList<Cliente> lista = new ArrayList<>(); while (rs.next()) lista.add(mapear(rs)); return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void actualizar(Cliente cl) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE clientes SET nombre=?,nit=?,telefono=? WHERE id_cliente=? AND nit<>'CF'")) {
            campos(ps,cl); ps.setInt(4,Validacion.positivo(cl.getIdCliente(),"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void eliminar(int id) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM clientes WHERE id_cliente=? AND nit<>'CF'")) {
            ps.setInt(1,Validacion.positivo(id,"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
