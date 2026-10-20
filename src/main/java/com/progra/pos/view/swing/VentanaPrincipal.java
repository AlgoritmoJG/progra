package com.progra.pos.view.swing;


import com.progra.pos.controller.*;
import com.progra.pos.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public final class VentanaPrincipal extends JFrame {
    private final JPanel contenido = new JPanel(new BorderLayout());
    private final JLabel estado = new JLabel("Listo");
    private final JProgressBar progreso = new JProgressBar();
    private final TareasUi tareas = new TareasUi(this,estado,progreso);
    private final CatalogoController catalogo;
    private final boolean administrador;
    private final PanelCaja caja;
    public VentanaPrincipal(Sesion sesion, Usuario usuario) {
        super("Punto de Venta — " + usuario.getNombre()); setName("principal"); setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        catalogo = new CatalogoController(sesion); administrador = usuario.obtenerRol() == Rol.ADMINISTRADOR;
        caja = new PanelCaja(tareas,catalogo,new VentaController(sesion),this::clientes);
        JPanel lateral = new JPanel(); lateral.setLayout(new BoxLayout(lateral,BoxLayout.Y_AXIS)); lateral.setBackground(Ui.AZUL); lateral.setBorder(BorderFactory.createEmptyBorder(20,12,20,12)); lateral.setPreferredSize(new Dimension(200,600));
        JLabel marca = new JLabel("PUNTO DE VENTA"); marca.setForeground(Color.WHITE); marca.setFont(marca.getFont().deriveFont(Font.BOLD,17)); lateral.add(marca); lateral.add(Box.createVerticalStrut(22));
        boton(lateral,"Caja","navCaja",() -> mostrar(caja,caja::cargar));
        PanelProductos productos = new PanelProductos(tareas,catalogo,administrador);
        boton(lateral,"Productos","navProductos",() -> mostrar(productos,productos::cargar));
        if (administrador) {
            PanelCategorias categorias = new PanelCategorias(tareas,catalogo);
            boton(lateral,"Categorías","navCategorias",() -> mostrar(categorias,categorias::cargar));
        }
        PanelHistorial historial = new PanelHistorial(tareas,new VentaController(sesion),administrador);
        boton(lateral,"Historial de ventas","navHistorial",() -> mostrar(historial,historial::cargar));
        if (administrador) {
            PanelUsuarios usuarios = new PanelUsuarios(tareas,catalogo);
            boton(lateral,"Usuarios","navUsuarios",() -> mostrar(usuarios,usuarios::cargar));
        }
        lateral.add(Box.createVerticalGlue()); boton(lateral,"Cerrar sesión","cerrarSesion",this::cerrarSesion);
        JPanel cabecera = new JPanel(new BorderLayout()); cabecera.setBorder(BorderFactory.createEmptyBorder(14,20,14,20)); cabecera.add(new JLabel(usuario.getNombre() + "  ·  " + usuario.obtenerRol()),BorderLayout.WEST);
        cabecera.add(new JLabel("Comercios locales"),BorderLayout.EAST);
        JPanel centro = new JPanel(new BorderLayout()); centro.add(cabecera,BorderLayout.NORTH); centro.add(contenido,BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout()); pie.setBorder(BorderFactory.createEmptyBorder(6,16,6,16)); pie.add(estado); progreso.setPreferredSize(new Dimension(120,14)); pie.add(progreso,BorderLayout.EAST);
        add(lateral,BorderLayout.WEST); add(centro,BorderLayout.CENTER); add(pie,BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { if (!tareas.ocupado() && descartar()) dispose(); } });
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(1050,700)); setSize(Math.min(1240,pantalla.width-40),Math.min(780,pantalla.height-60)); setLocationRelativeTo(null);
        // Se carga después de que JFrame sea visible y todos los componentes estén registrados.
        SwingUtilities.invokeLater(() -> mostrar(caja,caja::cargar));
    }
    private void boton(JPanel panel, String texto, String nombre, Runnable accion) {
        JButton b = Ui.boton(texto,nombre,accion); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(180,40)); panel.add(b); panel.add(Box.createVerticalStrut(9));
    }
    private void mostrar(JPanel panel, Runnable cargar) {
        if (tareas.ocupado()) return;
        contenido.removeAll(); contenido.add(panel); contenido.revalidate(); contenido.repaint(); cargar.run();
    }
    private void clientes() {
        PanelClientes clientes = new PanelClientes(tareas,catalogo,administrador);
        JButton volver = Ui.boton("Volver a Caja","volverCaja",() -> mostrar(caja,caja::cargar));
        clientes.add(Ui.fila(volver),BorderLayout.SOUTH); mostrar(clientes,clientes::cargar);
    }
    private boolean descartar() { return !caja.tieneCarrito() || Ui.confirmar(this,"Hay productos en el carrito. ¿Desea salir y descartarlos?"); }
    private void cerrarSesion() { if (!tareas.ocupado() && descartar()) { new VentanaLogin().setVisible(true); dispose(); } }
}
