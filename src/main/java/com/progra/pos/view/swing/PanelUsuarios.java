package com.progra.pos.view.swing;


import com.progra.pos.controller.CatalogoController;
import com.progra.pos.model.*;
import javax.swing.*;
import java.util.*;
final class PanelUsuarios extends PanelListado<Usuario> {
    private final CatalogoController catalogo;
    PanelUsuarios(TareasUi tareas, CatalogoController catalogo) {
        super("Usuarios y permisos","usuarios",tareas,true,"Id","Nombre","Usuario","Rol","Estado"); this.catalogo = catalogo;
    }
    @Override List<Usuario> listar(String filtro) {
        String texto = filtro.toLowerCase(Locale.ROOT);
        return catalogo.listarUsuarios().stream().filter(u -> (u.getNombre()+" "+u.getUsername()).toLowerCase(Locale.ROOT).contains(texto)).toList();
    }
    @Override Object[] fila(Usuario u) { return new Object[]{u.getIdUsuario(),u.getNombre(),u.getUsername(),u.obtenerRol(),u.isActivo() ? "Activo" : "Inactivo"}; }
    private record DatosUsuario(String nombre, String username, String clave, Rol rol, boolean activo) {}
    @Override void editar(Usuario actual) {
        Formulario f = new Formulario(this,actual == null ? "Nuevo usuario" : "Editar usuario");
        JTextField nombre = Ui.texto("nombre",26), username = Ui.texto("username",20); JPasswordField clave = new JPasswordField(20); clave.setName("password");
        JComboBox<Rol> rol = new JComboBox<>(Rol.values()); rol.setName("rol"); JCheckBox activo = new JCheckBox("Puede iniciar sesión",true); activo.setName("activo");
        if (actual != null) { nombre.setText(actual.getNombre()); username.setText(actual.getUsername()); rol.setSelectedItem(actual.obtenerRol()); activo.setSelected(actual.isActivo()); }
        f.campo("Nombre",nombre); f.campo("Usuario",username); f.campo(actual == null ? "Contraseña (mín. 8)" : "Nueva clave (vacío conserva)",clave); f.campo("Rol",rol);
        if (actual != null) f.campo("Estado",activo);
        DatosUsuario datos = f.mostrar(() -> {
            String n = com.progra.pos.util.Validacion.texto(nombre.getText(),"Nombre",100), u = com.progra.pos.util.Validacion.texto(username.getText(),"Usuario",50);
            char[] chars = clave.getPassword(); String password;
            try { password = new String(chars); } finally { Arrays.fill(chars,'\0'); }
            if ((actual == null || !password.isEmpty()) && (password.length() < 8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72))
                throw new IllegalArgumentException("Contraseña: mínimo 8 caracteres y máximo 72 bytes UTF-8.");
            return new DatosUsuario(n,u,password.isEmpty() ? null : password,(Rol)rol.getSelectedItem(),activo.isSelected());
        });
        clave.setText("");
        if (datos != null) tareas.ejecutar("Guardando usuario…",() -> {
            if (actual == null) catalogo.crearUsuario(datos.nombre(),datos.username(),datos.clave(),datos.rol());
            else catalogo.actualizarUsuario(actual.getIdUsuario(),datos.nombre(),datos.username(),datos.clave(),datos.rol(),datos.activo()); return true;
        },ok -> cargar());
    }
    @Override void eliminar(Usuario u) { catalogo.eliminarUsuario(u.getIdUsuario()); }
}
