package com.progra.pos.dao;


import com.progra.pos.model.*;
import com.progra.pos.util.*;
import java.sql.*;
import java.util.ArrayList;
public final class ProductoDao {
    private static final String SELECT = "SELECT p.*,c.nombre AS categoria_nombre,c.descripcion AS categoria_descripcion FROM productos p JOIN categorias c ON c.id_categoria=p.categoria_id ";
    public int crear(Producto p) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO productos(codigo,nombre,precio,stock,stock_minimo,activo,categoria_id,tipo,fecha_vencimiento) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            campos(ps,p); ps.executeUpdate(); return Jdbc.id(ps);
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void actualizar(Producto p) {
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE productos SET codigo=?,nombre=?,precio=?,stock=?,stock_minimo=?,activo=?,categoria_id=?,tipo=?,fecha_vencimiento=? WHERE id_producto=?")) {
            campos(ps,p); ps.setInt(10,Validacion.positivo(p.getIdProducto(),"Id")); Jdbc.uno(ps.executeUpdate());
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    private void campos(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1,p.getCodigo()); ps.setString(2,p.getNombre()); ps.setBigDecimal(3,p.getPrecio()); ps.setInt(4,p.getStock()); ps.setInt(5,p.getStockMinimo()); ps.setBoolean(6,p.isActivo()); ps.setInt(7,Validacion.positivo(p.getCategoria().getIdCategoria(),"Categoría"));
        if (p instanceof ProductoPerecedero perecedero) { ps.setString(8,"PERECEDERO"); ps.setDate(9,Date.valueOf(perecedero.getFechaVencimiento())); }
        else { ps.setString(8,"NO_PERECEDERO"); ps.setNull(9,Types.DATE); }
    }
    static Producto mapear(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(rs.getInt("categoria_id"),rs.getString("categoria_nombre"),rs.getString("categoria_descripcion"));
        if (rs.getString("tipo").equals("PERECEDERO")) return new ProductoPerecedero(rs.getInt("id_producto"),rs.getString("codigo"),rs.getString("nombre"),rs.getBigDecimal("precio"),rs.getInt("stock"),rs.getInt("stock_minimo"),rs.getBoolean("activo"),cat,rs.getDate("fecha_vencimiento").toLocalDate());
        return new ProductoNoPerecedero(rs.getInt("id_producto"),rs.getString("codigo"),rs.getString("nombre"),rs.getBigDecimal("precio"),rs.getInt("stock"),rs.getInt("stock_minimo"),rs.getBoolean("activo"),cat);
    }
    public Producto buscar(int id) {
        try (Connection c = Conexion.abrir()) { return buscar(c,Validacion.positivo(id,"Id"),false); }
        catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    static Producto buscar(Connection c, int id, boolean bloquear) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.id_producto=?" + (bloquear ? " FOR UPDATE" : ""))) {
            ps.setInt(1,id); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalArgumentException("Producto inexistente."); return mapear(rs); }
        }
    }
    public ArrayList<Producto> buscarPorTexto(String texto) {
        if (texto == null || texto.length() > 100) throw new IllegalArgumentException("Búsqueda inválida.");
        try (Connection c = Conexion.abrir(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE LOCATE(?,p.nombre)>0 OR LOCATE(?,p.codigo)>0 ORDER BY p.id_producto")) {
            ps.setString(1,texto.strip()); ps.setString(2,texto.strip());
            try (ResultSet rs = ps.executeQuery()) { ArrayList<Producto> lista = new ArrayList<>(); while (rs.next()) lista.add(mapear(rs)); return lista; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public void eliminar(int id) {
        // Bloqueo común con la confirmación de ventas: evita carreras entre borrar y vender.
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                buscar(c,Validacion.positivo(id,"Id"),true);
                boolean vendido;
                try (PreparedStatement ps = c.prepareStatement("SELECT id_detalle_venta FROM detalle_ventas WHERE producto_id=? LIMIT 1")) {
                    ps.setInt(1,id); try (ResultSet rs = ps.executeQuery()) { vendido = rs.next(); }
                }
                String sql = vendido ? "UPDATE productos SET activo=FALSE WHERE id_producto=?" : "DELETE FROM productos WHERE id_producto=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) { ps.setInt(1,id); Jdbc.uno(ps.executeUpdate()); }
                c.commit();
            } catch (SQLException | RuntimeException e) { try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
