# Validación de Fase 3

## Resultado
25 pruebas distintas aprobadas, sin fallos, errores ni omisiones en la ejecución completa: 8 de ModeloTest, 13 de MySqlTest y 4 de SwingFlujoTest. Después de ajustar mensajes y presentación, se repitieron las 4 gráficas con éxito.

Java Temurin 25.0.4.1 (release 21), Maven 3.9.11 y MySQL 8.4.11. Las ventanas se ejecutaron con Swing/Nimbus en una pantalla virtual Xvfb de 1366×900; las pruebas utilizaron java.awt.Robot y consultas reales a MySQL. La BD desechable fue exclusivamente pos_test; no se borró pos_dev.

## Comprobaciones gráficas
- Login con credenciales válidas e inválidas, cambio de ventana y cierre de sesión.
- Categorías: crear, consultar, editar y eliminar desde botones/formularios.
- Productos: validación de precio negativo, crear, editar y borrar; tabla ordenada por nombre para verificar que se modifica el registro seleccionado correctamente.
- Usuarios: contraseña corta rechazada, crear cajero, editar nombre/estado conservando contraseña, eliminar; no se presentan hashes.
- Clientes: alta desde Caja, tabla y retorno a Caja.
- Caja: cantidad excesiva y texto inválido rechazados, suma con dos unidades, descuento del perecedero, confirmación y detalle, vaciado del carrito y stock persistente actualizado.
- Historial: consulta y detalle; cajero ve sus ventas y no tiene navegación administrativa ni botones CRUD de productos. Fecha inválida muestra un mensaje.
- SwingWorker: trabajo fuera del EDT, callback en EDT y procesamiento de eventos mientras la tarea está bloqueada; botones deshabilitados hasta terminar.
- JDBC: la nueva prueba de edición optimista demuestra que un formulario desactualizado no puede restablecer stock descontado por una venta.

## Capturas reales
Solo contienen datos de prueba; las claves no aparecen. Proceden de las ventanas que ejercitó la suite, con repintado esperado antes de capturar.

- [Login](capturas/login.png)
- [Productos](capturas/productos.png)
- [Caja con carrito](capturas/caja.png)
- [Venta confirmada](capturas/venta.png)
- [Historial](capturas/historial.png)
- [Consulta como cajero](capturas/cajero.png)

El arranque mediante scripts/swing.sh utiliza exec:exec@escritorio, comprobado en una JVM independiente. Se ajustó el primer repintado del Login en windowOpened tras detectar un dibujo inicial incompleto en la pantalla virtual. La prueba comprueba también que el fondo esté pintado, sin forzar repaint desde el test.

## Reproducir
Con MySQL local preparado:
```bash
scripts/mysql-local.sh
scripts/test-mysql.sh
# Con sesión X11 o pantalla virtual disponible:
scripts/test-swing.sh
```
Si Xvfb está instalado en Linux, `xvfb-run -a scripts/test-swing.sh` evita usar el escritorio personal. Los tests gráficos controlan el ratón: no usarlos durante una presentación.

En esta nube se proporcionó Xvfb mediante un contenedor de pruebas dedicado y DISPLAY=127.0.0.1:5. En el equipo del estudiante no se necesita Xvfb para usar la aplicación; basta el escritorio habitual y NetBeans.

Activación: POS_RUN_DB_TESTS=true para MySQL; POS_RUN_SWING_TESTS=true para gráficos. scripts/test-swing.sh configura ambas y exige DISPLAY. Sin esas condiciones, Maven omite las suites correspondientes; no confundir una omisión con una prueba aprobada. Ambas suites borran datos de pos_test antes de sus casos. No usar esa BD para información que se quiera conservar.

Reportes nuevos: target/surefire-reports/. Capturas generadas: target/capturas/. El JAR es target/punto-venta-1.0.0.jar y requiere sus dependencias; usar Maven o NetBeans para ejecutar.

## Pendientes de presentación
NetBeans 31 no está instalado aquí: abrir y verificar el proyecto en ese IDE con el JDK del estudiante. No se probó específicamente JDK 25.0.3. Confirmar versión del servidor MySQL mediante SELECT VERSION(). El video y el ensayo oral deben realizarlos el estudiante; se entrega el guion. No se incluyen impresión fiscal, pasarela bancaria ni un archivo Figma.
