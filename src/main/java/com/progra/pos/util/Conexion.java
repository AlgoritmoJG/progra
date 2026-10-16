package com.progra.pos.util;


import java.sql.*;
public final class Conexion {
    private Conexion() {}
    public static Connection abrir() throws SQLException {
        String url = System.getenv("POS_DB_URL");
        String usuario = System.getenv("POS_DB_USER");
        String clave = System.getenv("POS_DB_PASSWORD");
        if (url == null || usuario == null || clave == null)
            throw new SQLException("Configure POS_DB_URL, POS_DB_USER y POS_DB_PASSWORD.");
        return DriverManager.getConnection(url, usuario, clave);
    }
}
