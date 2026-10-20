package com.progra.pos.view.swing;


import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;
/** Editor modal: valida antes de cerrar; la escritura JDBC se hace después, con SwingWorker. */
final class Formulario extends JDialog {
    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JLabel error = new JLabel(" ");
    private int fila;
    private Object resultado;
    Formulario(Component padre, String titulo) {
        super(SwingUtilities.getWindowAncestor(padre),titulo,ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); setName("formulario");
        setLayout(new BorderLayout(10,10)); campos.setBorder(BorderFactory.createEmptyBorder(14,18,8,18)); add(campos,BorderLayout.CENTER);
        error.setForeground(new Color(160,30,30)); error.setName("errorFormulario");
    }
    void campo(String etiqueta, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints(); g.gridy = fila++; g.insets = new Insets(5,5,5,5); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; JLabel l = new JLabel(etiqueta); l.setLabelFor(componente); campos.add(l,g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL; campos.add(componente,g);
    }
    <T> T mostrar(Supplier<T> validar) {
        JButton guardar = Ui.boton("Guardar","guardarFormulario",() -> {
            try { resultado = validar.get(); dispose(); }
            catch (IllegalArgumentException | java.time.DateTimeException e) { error.setText(Ui.mensaje(e)); pack(); }
        });
        JPanel pie = new JPanel(new BorderLayout()); pie.add(error,BorderLayout.NORTH);
        pie.add(Ui.fila(guardar,Ui.boton("Cancelar","cancelarFormulario",this::dispose)),BorderLayout.SOUTH); add(pie,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(guardar); pack(); setMinimumSize(new Dimension(460,getHeight())); setLocationRelativeTo(getOwner()); setVisible(true);
        @SuppressWarnings("unchecked") T valor = (T) resultado; return valor;
    }
}
