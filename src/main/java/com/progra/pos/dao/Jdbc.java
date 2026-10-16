package com.progra.pos.dao;


import java.sql.*;
final class Jdbc {
    private Jdbc() {}
    static int id(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) throw new SQLException("No se recibió la llave generada.");
            return rs.getInt(1);
        }
    }
    static void uno(int filas) {
        if (filas != 1) throw new IllegalArgumentException("El registro no existe o no se puede modificar.");
    }
}
