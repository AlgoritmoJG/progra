package com.progra.pos.dao;


import com.progra.pos.model.*;
import com.progra.pos.util.*;
import java.sql.*;
import java.time.*;
import java.util.*;
public final class VentaDao {
    public Venta confirmar(int usuarioId, int clienteId, MetodoPago metodo, Map<Integer,Integer> cantidades) {
        Objects.requireNonNull(metodo,"Método obligatorio.");
        if (cantidades == null || cantidades.isEmpty()) throw new IllegalArgumentException("La venta requiere al menos un detalle.");
        // Todos los carritos bloquean productos en el mismo orden para reducir interbloqueos.
        TreeMap<Integer,Integer> ordenados = new TreeMap<>(cantidades);
        ordenados.forEach((id,cantidad) -> { Validacion.positivo(id,"Producto"); Validacion.positivo(cantidad,"Cantidad"); });
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                Usuario usuario = UsuarioDao.buscar(c,usuarioId,true);
                if (!usuario.tienePermiso(Permiso.REGISTRAR_VENTAS)) throw new SecurityException("Usuario inactivo o sin permiso para vender.");
                Cliente cliente = ClienteDao.buscar(c,clienteId);
                // Fecha única generada al confirmar; las fechas históricas no se aceptan como entrada.
                Venta venta = new Venta(0,LocalDateTime.now().withNano(0),usuario,cliente,metodo);
                for (var item : ordenados.entrySet()) {
                    Producto producto = ProductoDao.buscar(c,item.getKey(),true);
                    venta.agregarDetalle(producto,item.getValue());
                    try (PreparedStatement ps = c.prepareStatement("UPDATE productos SET stock=stock-? WHERE id_producto=? AND activo=TRUE AND stock>=?")) {
                        ps.setInt(1,item.getValue()); ps.setInt(2,item.getKey()); ps.setInt(3,item.getValue()); Jdbc.uno(ps.executeUpdate());
                    }
                }
                int ventaId;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO ventas(fecha,usuario_id,cliente_id,metodo_pago,total) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1,Timestamp.valueOf(venta.getFecha())); ps.setInt(2,usuarioId); ps.setInt(3,clienteId); ps.setString(4,metodo.name()); ps.setBigDecimal(5,venta.getTotal()); ps.executeUpdate(); ventaId = Jdbc.id(ps);
                }
                ArrayList<Venta.DetalleGuardado> filas = new ArrayList<>();
                for (DetalleVenta detalle : venta.obtenerDetalles()) {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO detalle_ventas(venta_id,producto_id,cantidad,precio_unitario,subtotal) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1,ventaId); ps.setInt(2,detalle.getProducto().getIdProducto()); ps.setInt(3,detalle.getCantidad()); ps.setBigDecimal(4,detalle.getPrecioUnitario()); ps.setBigDecimal(5,detalle.getSubtotal()); ps.executeUpdate();
                        filas.add(new Venta.DetalleGuardado(Jdbc.id(ps),detalle.getProducto(),detalle.getCantidad(),detalle.getPrecioUnitario()));
                    }
                }
                Venta confirmada = Venta.reconstruir(ventaId,venta.getFecha(),usuario,cliente,metodo,filas);
                c.commit(); return confirmada;
            } catch (SQLException | RuntimeException e) { try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
    public ArrayList<Venta> historial(int usuarioId, LocalDate desde, LocalDate hasta) {
        Objects.requireNonNull(desde); Objects.requireNonNull(hasta);
        if (hasta.isBefore(desde) || hasta.equals(LocalDate.MAX)) throw new IllegalArgumentException("Rango de fechas inválido.");
        try (Connection c = Conexion.abrir()) {
            // Una lectura consistente evita mezclar encabezados y detalles de momentos diferentes.
            c.setReadOnly(true); c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); c.setAutoCommit(false);
            try {
                Usuario consulta = UsuarioDao.buscar(c,usuarioId,false);
                if (!consulta.tienePermiso(Permiso.CONSULTAR_VENTAS_PROPIAS)) throw new SecurityException("Sin permiso para consultar ventas.");
                boolean todas = consulta.tienePermiso(Permiso.CONSULTAR_TODAS_LAS_VENTAS);
                String sql = "SELECT * FROM ventas WHERE fecha>=? AND fecha<?" + (todas ? "" : " AND usuario_id=?") + " ORDER BY fecha,id_venta";
                ArrayList<Venta> lista = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setTimestamp(1,Timestamp.valueOf(desde.atStartOfDay())); ps.setTimestamp(2,Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
                    if (!todas) ps.setInt(3,usuarioId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id_venta"); ArrayList<Venta.DetalleGuardado> filas = new ArrayList<>();
                            try (PreparedStatement pd = c.prepareStatement("SELECT * FROM detalle_ventas WHERE venta_id=? ORDER BY id_detalle_venta")) {
                                pd.setInt(1,id); try (ResultSet rd = pd.executeQuery()) { while (rd.next()) filas.add(new Venta.DetalleGuardado(rd.getInt("id_detalle_venta"),ProductoDao.buscar(c,rd.getInt("producto_id"),false),rd.getInt("cantidad"),rd.getBigDecimal("precio_unitario"))); }
                            }
                            Venta venta = Venta.reconstruir(id,rs.getTimestamp("fecha").toLocalDateTime(),UsuarioDao.buscar(c,rs.getInt("usuario_id"),false),ClienteDao.buscar(c,rs.getInt("cliente_id")),MetodoPago.valueOf(rs.getString("metodo_pago")),filas);
                            if (venta.getTotal().compareTo(rs.getBigDecimal("total")) != 0) throw new IllegalStateException("El total almacenado no coincide con sus detalles.");
                            lista.add(venta);
                        }
                    }
                }
                c.commit(); return lista;
            } catch (SQLException | RuntimeException e) { try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); } throw e; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
