# Defensa oral — 3 minutos de demo y 7 de preguntas/modificación

## Preparación
Antes de presentar: MySQL iniciado, conexión configurada, proyecto compilado, cuentas administrador y cajero creadas, una categoría y dos productos de prueba. Un perecedero debe vencer en 2 días y otro no perecedero tener stock 3. Comprobar la fecha del equipo. No mostrar claves en capturas ni improvisar importación del esquema durante la demo.

## Demo de tres minutos
| Tiempo | Acción | Concepto que explicas |
|---|---|---|
| 0:00–0:25 | Abrir Swing e iniciar sesión como administrador. | Autenticación BCrypt y permisos. |
| 0:25–0:55 | Mostrar Productos, buscar uno, editar precio y consultar otra vez. | CRUD, validación, PreparedStatement y persistencia. |
| 0:55–1:25 | En Caja agregar dos productos, uno próximo a vencer. | ArrayList, polimorfismo, descuento del 10 % y BigDecimal. |
| 1:25–1:55 | Seleccionar CF, confirmar EFECTIVO y mostrar detalle. | Venta y detalles en una transacción con stock. |
| 1:55–2:25 | Consultar Historial y ver que stock disminuyó. | SELECT, asociaciones y precio histórico. |
| 2:25–3:00 | Cerrar sesión y entrar como cajero. Mostrar consulta sin botones CRUD y sus ventas. | Herencia, roles y autorización real en controlador. |

## Siete minutos de preguntas y modificación
Distribución de ensayo: 2 minutos para explicar MVC/POO, 2 para BD/transacción, 3 para realizar y probar un cambio pequeño. Es una pauta de práctica; la catedrática decide las preguntas reales.

### Bloque 1 — POO y MVC
**Explicación:** Producto abstrae atributos comunes; sus hijas deciden el precio. La vista llama controladores y no conoce SQL.
1. **¿Dónde está herencia?** Administrador/Cajero extienden Usuario; los dos tipos de producto extienden Producto.
2. **¿Dónde está composición?** Venta administra sus detalles y no permite agregar ni quitar después de confirmarse.
3. **¿Qué capa cambias para otro descuento?** El modelo ProductoPerecedero y sus pruebas; la vista muestra el resultado.

**Modificación: descuento del 15 % hasta cinco días antes.** 1) En ProductoPerecedero cambiar límite 3→5 y multiplicador 0.90→0.85. 2) Actualizar ModeloTest para días 0, 5, 6 y -1. 3) Ejecutar pruebas. 4) Probar un producto de Q100 que vence en cinco días: Caja debe mostrar Q85. 5) Actualizar reglas/UML.

### Bloque 2 — BD e integridad
**Explicación:** la venta se guarda con una conexión y un único commit; ante fallo se revierte todo. Las FK conservan referencias.
1. **¿Qué impide vender dos veces la última unidad?** FOR UPDATE y la verificación final de stock; la segunda transacción observa existencias ya actualizadas.
2. **¿Por qué guardar precio_unitario?** El catálogo cambia; una venta pasada debe conservar cuánto se cobró.
3. **¿Por qué no se borra una categoría ocupada?** Los productos la referencian y la FK usa RESTRICT. Se deben reasignar primero.

**Modificación: cambiar la alerta para avisar antes de llegar al mínimo.** 1) En Producto.estaEnStockMinimo usar `(long) stock <= (long) stockMinimo + 2`. 2) Probar mínimo5: stock7 alerta, stock8 no. 3) Abrir Productos y Caja. 4) Explicar que es una alerta y no prohíbe ventas.

### Bloque 3 — Swing y errores
**Explicación:** componentes en el hilo gráfico; consultas y escrituras en segundo plano. El estado ocupado impide doble clic y mantiene consistencia del carrito.
1. **¿Dónde se ejecuta una consulta?** TareasUi.doInBackground, llamando al controlador y al DAO.
2. **¿Qué hace done?** Recupera resultado, restaura controles y actualiza la vista; muestra errores sin imprimir credenciales.
3. **¿Por qué convertir el índice de una fila?** Al ordenar JTable, la fila visible puede corresponder a otra posición del modelo. convertRowIndexToModel evita editar un registro distinto.

**Modificación: comenzar Historial mostrando solo hoy.** 1) Abrir PanelHistorial. 2) Cambiar la fecha inicial de `LocalDate.now().withDayOfMonth(1)` a `LocalDate.now()`. 3) Ejecutar. 4) Confirmar que Desde/Hasta sean hoy y que ventas anteriores aparezcan al ampliar el intervalo.

## Cierre de estudio
Debes poder seguir sin memorizar: botón → controlador → modelo/DAO → MySQL → resultado → tabla. Localiza cada paso en el código y explica por qué la validación de la vista no reemplaza las reglas del modelo ni las restricciones de BD.
