package com.progra.pos.view.swing;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
final class Ui {
    static final Color FONDO = new Color(245,247,250), AZUL = new Color(28,65,102);
    private Ui() {}
    static JButton boton(String texto, String nombre, Runnable accion) {
        JButton b = new JButton(texto); b.setName(nombre); b.addActionListener(e -> accion.run()); return b;
    }
    static JTextField texto(String nombre, int columnas) {
        JTextField f = new JTextField(columnas); f.setName(nombre); return f;
    }
    static JPanel fila(Component... elementos) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,10,6));
        for (Component c : elementos) p.add(c); return p;
    }
    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto); l.setFont(l.getFont().deriveFont(Font.BOLD,22)); l.setForeground(AZUL); return l;
    }
    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas,0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
            @Override public Class<?> getColumnClass(int columna) {
                return getRowCount() == 0 || getValueAt(0,columna) == null ? Object.class : getValueAt(0,columna).getClass();
            }
        };
    }
    static JTable tabla(String nombre, DefaultTableModel modelo) {
        JTable t = new JTable(modelo); t.setName(nombre); t.setRowHeight(30); t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false); return t;
    }
    static String moneda(BigDecimal importe) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.US); formato.setMinimumFractionDigits(2); formato.setMaximumFractionDigits(2);
        return "Q " + formato.format(importe);
    }
    static String mensaje(Throwable e) {
        if (e instanceof java.time.DateTimeException) return "Fecha inválida. Use AAAA-MM-DD, por ejemplo 2026-10-24.";
        return e instanceof IllegalArgumentException || e instanceof SecurityException || e instanceof IllegalStateException || e instanceof com.progra.pos.util.AccesoDatosException
                ? e.getMessage() : "No se pudo completar la operación. Intente de nuevo.";
    }
    static void error(Component padre, Throwable e) {
        JOptionPane.showMessageDialog(padre,mensaje(e),"Revise la operación",JOptionPane.ERROR_MESSAGE);
    }
    static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre,mensaje,"Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    static void info(Component padre, String mensaje) { JOptionPane.showMessageDialog(padre,mensaje,"Punto de Venta",JOptionPane.INFORMATION_MESSAGE); }
    static String requerido(JTextField campo, String nombre) { return com.progra.pos.util.Validacion.texto(campo.getText(),nombre,100); }
    static int entero(JTextField campo, String nombre) {
        try { return Integer.parseInt(campo.getText().strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(nombre + " debe ser un entero válido."); }
    }
    static BigDecimal decimal(JTextField campo) {
        try { return new BigDecimal(campo.getText().strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Precio inválido; use punto decimal, por ejemplo 12.50."); }
    }
}
