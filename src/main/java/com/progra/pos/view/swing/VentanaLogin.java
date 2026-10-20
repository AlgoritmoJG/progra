package com.progra.pos.view.swing;


import com.progra.pos.controller.*;
import com.progra.pos.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
public final class VentanaLogin extends JFrame {
    private final JTextField usuario = Ui.texto("loginUsuario",24);
    private final JPasswordField clave = new JPasswordField(24);
    private final JLabel estado = new JLabel("Ingrese con su cuenta del comercio");
    private final JProgressBar progreso = new JProgressBar();
    private final TareasUi tareas = new TareasUi(this,estado,progreso);
    private record Acceso(Sesion sesion, Usuario usuario) {}
    public VentanaLogin() {
        super("Punto de Venta — Iniciar sesión"); setName("login"); setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { if (!tareas.ocupado()) dispose(); }
            @Override public void windowOpened(WindowEvent e) {
                // Completar el primer dibujo después de que el sistema gráfico haya mostrado la ventana.
                SwingUtilities.invokeLater(() -> {
                    getRootPane().paintImmediately(0,0,getRootPane().getWidth(),getRootPane().getHeight());
                    Toolkit.getDefaultToolkit().sync();
                });
            }
        });
        clave.setName("loginPassword");
        JPanel marca = new JPanel(); marca.setLayout(new BoxLayout(marca,BoxLayout.Y_AXIS)); marca.setBackground(Ui.AZUL); marca.setBorder(BorderFactory.createEmptyBorder(48,30,40,30));
        JLabel logo = new JLabel("PUNTO DE VENTA"), detalle = new JLabel("Tu comercio, en orden.");
        logo.setFont(logo.getFont().deriveFont(Font.BOLD,23)); logo.setForeground(Color.WHITE); detalle.setForeground(new Color(206,221,236));
        marca.add(logo); marca.add(Box.createVerticalStrut(16)); marca.add(detalle); marca.add(Box.createVerticalStrut(60));
        JLabel modulos = new JLabel("<html>Ventas · Inventario · Clientes<br><br>Acceso según tu rol</html>"); modulos.setForeground(Color.WHITE); marca.add(modulos);
        JPanel campos = new JPanel(new GridBagLayout()); campos.setBorder(BorderFactory.createEmptyBorder(30,26,30,26));
        GridBagConstraints g = new GridBagConstraints(); g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(7,0,7,0);
        Component[] controles = {Ui.titulo("Bienvenido"),new JLabel("Usuario"),usuario,new JLabel("Contraseña"),clave,
            Ui.boton("Iniciar sesión","iniciarSesion",this::ingresar),Ui.boton("Crear administrador inicial","crearAdminInicial",this::crearAdministrador)};
        for (int i = 0; i < controles.length; i++) { g.gridy = i; campos.add(controles[i],g); }
        JPanel centro = new JPanel(new GridLayout(1,2)); centro.add(marca); centro.add(campos); add(centro,BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(10,0)); pie.setBorder(BorderFactory.createEmptyBorder(8,14,8,14)); pie.add(estado); progreso.setPreferredSize(new Dimension(100,14)); pie.add(progreso,BorderLayout.EAST); add(pie,BorderLayout.SOUTH);
        getRootPane().setDefaultButton((JButton)controles[5]); setSize(800,460); setMinimumSize(new Dimension(760,440)); setLocationRelativeTo(null);
    }
    private void ingresar() {
        String nombre = usuario.getText().strip(); char[] chars = clave.getPassword();
        if (nombre.isEmpty() || chars.length == 0) { Arrays.fill(chars,'\0'); Ui.info(this,"Ingrese usuario y contraseña."); return; }
        String password = new String(chars); Arrays.fill(chars,'\0'); clave.setText("");
        tareas.ejecutar("Comprobando credenciales…",() -> {
            Sesion sesion = new AutenticacionController().iniciarSesion(nombre,password);
            return new Acceso(sesion,sesion.exigir(Permiso.CONSULTAR_PRODUCTOS));
        },acceso -> { new VentanaPrincipal(acceso.sesion(),acceso.usuario()).setVisible(true); dispose(); });
    }
    private void crearAdministrador() {
        Formulario f = new Formulario(getRootPane(),"Primera instalación");
        JTextField nombre = Ui.texto("nombre",24), username = Ui.texto("username",24); JPasswordField password = new JPasswordField(24); password.setName("password");
        f.campo("Nombre",nombre); f.campo("Usuario",username); f.campo("Clave (mín. 8 caracteres)",password);
        record Alta(String nombre, String usuario, String clave) {}
        Alta alta = f.mostrar(() -> {
            String n = com.progra.pos.util.Validacion.texto(nombre.getText(),"Nombre",100), u = com.progra.pos.util.Validacion.texto(username.getText(),"Usuario",50);
            char[] chars = password.getPassword(); String p;
            try { p = new String(chars); } finally { Arrays.fill(chars,'\0'); }
            if (p.length() < 8 || p.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Contraseña: mínimo 8 caracteres y máximo 72 bytes UTF-8.");
            return new Alta(n,u,p);
        }); password.setText("");
        if (alta != null) tareas.ejecutar("Creando administrador inicial…",() -> new AutenticacionController().crearPrimerAdministrador(alta.nombre(),alta.usuario(),alta.clave()),id -> {
            usuario.setText(alta.usuario()); Ui.info(this,"Administrador creado. Ya puede iniciar sesión. Esta opción solo funciona si no hay usuarios.");
        });
    }
}
