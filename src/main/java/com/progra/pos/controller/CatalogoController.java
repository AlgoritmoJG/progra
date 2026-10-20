package com.progra.pos.controller;


import com.progra.pos.dao.*;
import com.progra.pos.model.*;
import com.progra.pos.util.*;
import java.util.*;
public final class CatalogoController {
    private final Sesion sesion;
    private final CategoriaDao categorias = new CategoriaDao();
    private final ProductoDao productos = new ProductoDao();
    private final ClienteDao clientes = new ClienteDao();
    private final UsuarioDao usuarios = new UsuarioDao();
    public CatalogoController(Sesion sesion) { this.sesion = Objects.requireNonNull(sesion); }
    public ArrayList<Categoria> listarCategorias() { sesion.exigir(Permiso.CONSULTAR_PRODUCTOS); return categorias.listar(); }
    public Categoria buscarCategoria(int id) { sesion.exigir(Permiso.CONSULTAR_PRODUCTOS); return categorias.buscar(id); }
    public int crearCategoria(Categoria c) { sesion.exigir(Permiso.GESTIONAR_CATEGORIAS); return categorias.crear(c); }
    public void actualizarCategoria(Categoria c) { sesion.exigir(Permiso.GESTIONAR_CATEGORIAS); categorias.actualizar(c); }
    public void eliminarCategoria(int id) { sesion.exigir(Permiso.GESTIONAR_CATEGORIAS); categorias.eliminar(id); }
    public ArrayList<Producto> buscarProductos(String texto) { sesion.exigir(Permiso.CONSULTAR_PRODUCTOS); return productos.buscarPorTexto(texto); }
    public Producto buscarProducto(int id) { sesion.exigir(Permiso.CONSULTAR_PRODUCTOS); return productos.buscar(id); }
    public int crearProducto(Producto p) { sesion.exigir(Permiso.GESTIONAR_PRODUCTOS); return productos.crear(p); }
    public void actualizarProducto(Producto p) { sesion.exigir(Permiso.GESTIONAR_PRODUCTOS); productos.actualizar(p); }
    public void actualizarProductoSiVigente(Producto nuevo, Producto anterior) {
        sesion.exigir(Permiso.GESTIONAR_PRODUCTOS); productos.actualizarSiVigente(nuevo,anterior);
    }
    public void eliminarProducto(int id) { sesion.exigir(Permiso.GESTIONAR_PRODUCTOS); productos.eliminar(id); }
    // Caja permite registrar y corregir clientes; Consumidor Final está protegido en el DAO.
    public ArrayList<Cliente> listarClientes() { sesion.exigir(Permiso.REGISTRAR_VENTAS); return clientes.listar(); }
    public int crearCliente(Cliente c) { sesion.exigir(Permiso.REGISTRAR_VENTAS); return clientes.crear(c); }
    public void actualizarCliente(Cliente c) { sesion.exigir(Permiso.REGISTRAR_VENTAS); clientes.actualizar(c); }
    public void eliminarCliente(int id) { sesion.exigir(Permiso.GESTIONAR_USUARIOS); clientes.eliminar(id); }
    public ArrayList<Usuario> listarUsuarios() { sesion.exigir(Permiso.GESTIONAR_USUARIOS); return usuarios.listar(); }
    private Usuario usuario(int id, String nombre, String username, String clave, Rol rol, boolean activo) {
        Objects.requireNonNull(rol,"Rol obligatorio.");
        String hash = clave == null && id > 0 ? usuarios.buscar(id).getPasswordHash() : PasswordUtil.hash(clave);
        return rol == Rol.ADMINISTRADOR ? new Administrador(id,nombre,username,hash,activo) : new Cajero(id,nombre,username,hash,activo);
    }
    public int crearUsuario(String nombre, String username, String clave, Rol rol) {
        sesion.exigir(Permiso.GESTIONAR_USUARIOS); return usuarios.crear(usuario(0,nombre,username,clave,rol,true));
    }
    public void actualizarUsuario(int id, String nombre, String username, String clave, Rol rol, boolean activo) {
        sesion.exigir(Permiso.GESTIONAR_USUARIOS);
        if (id == sesion.getUsuarioId() && (rol != Rol.ADMINISTRADOR || !activo)) throw new IllegalArgumentException("No puede quitarse su propio acceso de administrador.");
        usuarios.actualizar(usuario(id,nombre,username,clave,rol,activo));
    }
    public void eliminarUsuario(int id) {
        sesion.exigir(Permiso.GESTIONAR_USUARIOS);
        if (id == sesion.getUsuarioId()) throw new IllegalArgumentException("No puede eliminar su propia cuenta.");
        usuarios.eliminar(id);
    }
}
