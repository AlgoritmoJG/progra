package com.progra.pos.view.swing;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
/** Ejecuta JDBC fuera del hilo gráfico y evita dos operaciones simultáneas por ventana. */
final class TareasUi {
    private final JFrame ventana;
    private final JLabel estado;
    private final JProgressBar progreso;
    private boolean ocupado;
    TareasUi(JFrame ventana, JLabel estado, JProgressBar progreso) {
        this.ventana = ventana; this.estado = estado; this.progreso = progreso;
    }
    boolean ocupado() { return ocupado; }
    <T> void ejecutar(String mensaje, Callable<T> trabajo, Consumer<T> alTerminar) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Inicie tareas desde el hilo gráfico.");
        if (ocupado) return;
        ocupado = true; estado.setText(mensaje); progreso.setIndeterminate(true);
        Map<Component,Boolean> habilitados = new IdentityHashMap<>();
        bloquear(ventana.getContentPane(),habilitados);
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return trabajo.call(); }
            @Override protected void done() {
                ocupado = false; habilitados.forEach(Component::setEnabled); progreso.setIndeterminate(false); estado.setText("Listo");
                if (!ventana.isDisplayable()) return;
                try { alTerminar.accept(get()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); Ui.error(ventana,new IllegalStateException("Operación interrumpida.")); }
                catch (ExecutionException e) { estado.setText("Operación no completada"); Ui.error(ventana,e.getCause()); }
                catch (RuntimeException e) { Ui.error(ventana,e); }
            }
        }.execute();
    }
    private void bloquear(Component componente, Map<Component,Boolean> habilitados) {
        habilitados.put(componente,componente.isEnabled()); componente.setEnabled(false);
        if (componente instanceof Container contenedor) for (Component hijo : contenedor.getComponents()) bloquear(hijo,habilitados);
    }
}
