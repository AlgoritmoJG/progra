package com.progra.pos.view.swing;


import javax.swing.*;
import java.awt.*;
public final class SwingApp {
    private SwingApp() {}
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Swing necesita una sesión gráfica. Abra el proyecto en NetBeans en su escritorio."); return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo estilo : UIManager.getInstalledLookAndFeels())
                    if ("Nimbus".equals(estilo.getName())) { UIManager.setLookAndFeel(estilo.getClassName()); break; }
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
                System.err.println("Se utilizará el estilo Swing predeterminado.");
            }
            UIManager.put("defaultFont",new Font(Font.SANS_SERIF,Font.PLAIN,13));
            new VentanaLogin().setVisible(true);
        });
    }
}
