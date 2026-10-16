# Fase 2 — Lógica, JDBC y MVC

## Recorrido para estudiar
1. `model/Producto.java` y sus hijas: invariantes y polimorfismo. No conocen JDBC ni componentes gráficos.
2. `model/Venta.java` y `DetalleVenta.java`: ArrayList, suma con BigDecimal y conservación de importes históricos.
3. `util/Conexion.java`, `Validacion.java`, `PasswordUtil.java`, `AccesoDatosException.java`: conexión por variables, reglas compartidas, BCrypt y traducción de errores.
4. `dao/CategoriaDao.java`: ejemplo pequeño de INSERT, SELECT, UPDATE y DELETE con PreparedStatement y try-with-resources.
5. `dao/ProductoDao.java`, `ClienteDao.java`, `UsuarioDao.java`: CRUD, discriminadores y borrados condicionados por historial.
6. `controller/AutenticacionController.java`, `Sesion.java`, `CatalogoController.java`: autenticación y permisos comprobados en cada operación. Sesion conserva solo el id y consulta estado/rol actual.
7. `controller/VentaController.java`, `dao/VentaDao.java`: carrito, confirmación atómica e historial con filtro por usuario.
8. `view/ConsolaApp.java`: entrada/salida y captura de errores; no contiene SQL ni llama DAO directamente. Fase 3 añadirá vistas Swing que reutilicen controladores.

Todas las rutas son relativas a `src/main/java/com/progra/pos/`. Las pruebas están en `src/test/java/com/progra/pos/`.

## Para la defensa: modelo y colecciones
**Explicación:** ArrayList mantiene el carrito y sus líneas; se busca por id y se reemplaza la línea al aumentar cantidad. Las copias no modificables impiden saltarse las reglas.

1. **¿Alta, baja y búsqueda en memoria?** VentaController.agregar añade o reemplaza por id; quitar usa removeIf; el bucle de agregar busca la línea existente.
2. **¿Por qué BigDecimal y no double?** BigDecimal representa decimales monetarios sin errores binarios; se construye desde String y se redondea explícitamente.
3. **¿El polimorfismo necesita instanceof?** El cálculo de precio no: se invoca sobre Producto. El DAO sí distingue la subclase al guardar sus datos específicos.

**Cambio en vivo: prohibir cantidades mayores a 100 por línea.** 1) Validar cantidad acumulada en Venta.agregarDetalle. 2) Repetir límite temprano en VentaController.agregar. 3) Mantener la verificación final del DAO por medio de Venta. 4) Probar 100, 101 y dos adiciones 60+50. 5) Documentar la regla.

## Para la defensa: JDBC y transacciones
**Explicación:** confirmar no es un conjunto de consultas independientes. Se abre una conexión, se desactiva autocommit, se bloquea el usuario y después los productos en orden de id, se valida y actualiza, se insertan venta y detalles y se hace commit. Si falla cualquier paso se ejecuta rollback.

1. **¿Por qué SELECT FOR UPDATE?** Retiene bloqueos hasta commit/rollback e impide que dos transacciones descuenten simultáneamente la misma existencia como si fuera la última disponible.
2. **¿Por qué ordenar productos?** Mantener un orden común reduce interbloqueos. No los elimina completamente; si ocurren se informa el fallo y el usuario puede volver a intentar.
3. **¿Cómo evita inyección PreparedStatement?** Separa la estructura SQL de los valores; el texto introducido se trata como dato, no como instrucciones.

**Cambio en vivo: venta solo con stock final >= 2.** 1) En VentaDao confirmar, antes del UPDATE, comprobar `(long) producto.getStock() - cantidad >= 2`. 2) Mantenerlo dentro del bloqueo. 3) Añadir prueba con stock 3/cantidad 1 (acepta) y stock 2/cantidad 1 (rechaza). 4) Comprobar que rollback no descuenta otros productos. Esta política es distinta de la alerta de stock mínimo.

## Para la defensa: MVC, permisos y excepciones
**Explicación:** la vista transforma entrada textual en datos y muestra mensajes. El controlador autoriza y coordina. El modelo valida su estado. El DAO trabaja con MySQL. Ocultar un botón será una ayuda visual, no la autorización real.

1. **¿Dónde se atrapa SQLException?** En DAO; se envuelve conservando causa. La vista captura AccesoDatosException y muestra un mensaje sin imprimir credenciales ni SQL.
2. **¿Qué ocurre si desactivan a un usuario conectado?** Sesion.exigir consulta otra vez su estado; además la confirmación de venta comprueba al usuario bloqueado dentro de su transacción.
3. **¿Swing debe ejecutar SQL desde un botón?** No. El evento llamará al controlador y la operación de BD deberá ejecutarse fuera del hilo de eventos con SwingWorker.

**Cambio en vivo: cajero sin acceso a historial.** 1) Quitar CONSULTAR_VENTAS_PROPIAS de Cajero.obtenerPermisos. 2) Mantener las comprobaciones existentes. 3) Ocultar la opción de consola/Swing para ese rol. 4) Probar llamada al controlador, que debe rechazar incluso sin botón. 5) Actualizar RF02/RF11 y pruebas.

## Alcances de seguridad académica
El control de permisos protege las rutas de la aplicación. Quien conoce la contraseña MySQL o ejecuta código arbitrario con acceso a DAO puede saltarse la capa de control: las credenciales de BD no deben compartirse con usuarios finales. La creación inicial de administrador es una operación local de instalación, solo con la tabla usuarios vacía; ejecutarla una sola vez, sin instalaciones concurrentes. El bloqueo evita autoeliminación del administrador, pero no implementa coordinación global entre varios administradores que se desactiven mutuamente. No es un producto listo para Internet.
