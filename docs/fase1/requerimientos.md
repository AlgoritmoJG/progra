# Fase 1 — Sistema de Punto de Venta para Comercios Locales

## Problema y objetivo
Un comercio que registra ventas e inventario manualmente puede vender existencias agotadas, perder el historial de precios y carecer de responsables identificables. El sistema centraliza productos, categorías, clientes, usuarios y ventas; descuenta inventario de forma atómica y permite consultar transacciones por fecha y responsable.

## Alcance acordado
Aplicación de escritorio Java/Swing, MySQL y JDBC, organizada en MVC. Fase 2: lógica y vista de consola verificable; Fase 3: Swing. Sin proveedores, compras, devoluciones, facturación fiscal ni reportes avanzados. El precio incluye IVA: no se calcula ni desglosa el impuesto. Se registra el método de pago; no se procesa una tarjeta ni se almacenan sus datos.

Actores: administrador (todos los módulos) y cajero (consulta de productos, Caja, clientes desde Caja e historial propio). El administrador ve todas las ventas. La aplicación no usa una conexión MySQL distinta por persona; autentica usuarios de negocio con BCrypt.

## Requerimientos funcionales y reglas verificables
| ID | Requerimiento / regla | Criterio de aceptación |
|---|---|---|
| RF01 | Autenticar usuarios activos | Clave incorrecta, usuario inexistente o inactivo rechaza el acceso con mensaje genérico. |
| RF02 | Aplicar permisos en controladores | Un cajero no puede gestionar usuarios, categorías ni productos, aunque intente invocar la operación directamente. |
| RF03 | CRUD de categorías | Nombre obligatorio y único; borrar una categoría con productos falla, incluso con productos inactivos. |
| RF04 | CRUD y búsqueda de productos | Código único, nombre obligatorio, precio positivo con máximo 2 decimales, stock y mínimo enteros no negativos, categoría existente. |
| RF05 | Desactivar productos vendidos | Sin ventas se permite DELETE; con ventas se conserva la fila y se marca activo=false. |
| RF06 | Formar carrito con ArrayList | Agregar, agrupar el mismo producto, consultar y quitar; cantidades enteras positivas y sin exceder existencias. |
| RF07 | Confirmar venta atómicamente | Usuario activo, cliente existente y al menos un detalle; encabezado, detalles y stock se confirman juntos o se revierten juntos. |
| RF08 | Evitar sobreventa concurrente | Dos cajas que compiten por la última unidad producen una sola venta exitosa. |
| RF09 | Perecederos | Una fecha de vencimiento por producto. No vender después de esa fecha; de 0 a 3 días inclusive se descuenta 10 %, con HALF_UP a 2 decimales. |
| RF10 | Conservar precio histórico | Cada detalle guarda el precio aplicado y subtotal; cambiar el catálogo no modifica importes históricos. |
| RF11 | Consultar historial inclusivo por fechas | Administrador ve todas las ventas; cajero solo las propias; se muestran sus detalles. |
| RF12 | Alertar stock bajo | Mostrar alerta cuando stock <= stockMinimo. |
| RF13 | Gestionar clientes | Ambos roles pueden registrar y editar clientes desde Caja; solo administrador elimina clientes sin ventas. Consumidor Final (NIT CF) no se modifica ni elimina. |
| RF14 | Gestionar usuarios | Administrador crea, consulta, edita y elimina; cuentas con ventas se desactivan. No puede borrar su cuenta ni quitarse su propio acceso administrativo. |

## Requerimientos técnicos
- Java camelCase y SQL snake_case; paquetes `model`, `view`, `controller`, `dao`, `util` bajo `com.progra.pos`.
- Modelos encapsulados con atributos privados; constructores validan invariantes; modificación del catálogo mediante nuevos objetos y UPDATE explícito.
- Herencia y polimorfismo en usuarios y productos. ArrayList en carrito, detalles y resultados DAO.
- Todo SQL de la aplicación usa PreparedStatement; entradas del usuario mediante parámetros. Fragmentos SQL condicionales solo provienen de constantes internas.
- JDBC con try-with-resources. SQLException se traduce a AccesoDatosException; la vista muestra un mensaje sin exponer consultas ni credenciales. Rollback conserva la excepción original.
- Contraseñas BCrypt, factor 12 y salt por hash. Mínimo 8 caracteres y máximo 72 bytes UTF-8; no se truncan silenciosamente.
- BigDecimal ↔ DECIMAL(12,2); LocalDate ↔ DATE; LocalDateTime ↔ DATETIME. Reloj local del comercio: no mezclar zonas horarias entre cajas.
- No se guardan contraseñas de MySQL en Git. Variables POS_DB_URL, POS_DB_USER, POS_DB_PASSWORD.

## Decisiones y límites
`Venta.usuario` referencia Usuario, no exclusivamente Cajero: el administrador también vende. `Venta` en construcción puede estar vacía; el DAO no guarda ventas vacías. No hay edición ni eliminación de ventas confirmadas. La composición es conceptual y no exige borrado SQL en cascada: aquí se protege la auditoría con RESTRICT.

El carrito muestra un total estimado; al confirmar acepta precios vigentes, calculados nuevamente dentro de la transacción. Si cambia el precio mientras se prepara el carrito, el importe definitivo puede cambiar. El historial conserva precio y cantidad, pero muestra los nombres actuales de producto y cliente: no es un comprobante fiscal inmutable.

Se admite una sola fecha de vencimiento por código. Lotes con fechas diferentes requieren una ampliación fuera del alcance. El NIT se valida como texto obligatorio y único, sin validación tributaria oficial. No hay recuperación de contraseña, MFA ni límite de intentos: alcance académico local.

## Para la defensa
**Explicación:** una regla debe indicar qué puede ocurrir y cómo demostrar que se cumple. Las validaciones del modelo previenen entradas inválidas; las restricciones SQL protegen los datos; la transacción protege el conjunto de cambios.

1. **¿Por qué validar en Java y SQL?** Java ofrece mensajes tempranos; SQL impide guardar datos inválidos incluso si otro cliente accede a la BD.
2. **¿Por qué Usuario en Venta?** Ambos roles pueden registrar ventas, y el historial necesita identificar al responsable real.
3. **¿El stock del carrito garantiza la venta?** No. Otra caja puede comprar antes; se verifica nuevamente con bloqueo dentro de la transacción.

**Cambio en vivo: mostrar alerta tres unidades antes del mínimo.** 1) Abrir Producto.java. 2) Cambiar la comparación en estaEnStockMinimo a `(long) stock <= (long) stockMinimo + 3`. 3) Actualizar RF12. 4) Probar mínimo=5 con stock 8 y 9; solo 8 debe alertar. Convertir a long evita desbordamiento al sumar.
