# Diccionario de clases del dominio
Ruta de las clases: `src/main/java/com/progra/pos/model/`.

| Clase | Responsabilidad y atributos principales | Métodos clave |
|---|---|---|
| Usuario (abstracta) | Identidad y autenticación: idUsuario:int, nombre/username/passwordHash:String, activo:boolean. | obtenerRol, obtenerPermisos, tienePermiso. |
| Administrador | Especialización de Usuario con todos los permisos. | Sobrescribe obtenerRol y obtenerPermisos. |
| Cajero | Especialización de Usuario limitada a consulta, Caja e historial propio. | Sobrescribe obtenerRol y obtenerPermisos. |
| Categoria | Agrupación: idCategoria:int, nombre/descripcion:String. | Constructor validado, getters. |
| Producto (abstracta) | Catálogo: idProducto:int; codigo/nombre:String; precio:BigDecimal; stock/stockMinimo:int; activo:boolean; categoria:Categoria. | calcularPrecioFinal, puedeVenderse, tieneStock, estaEnStockMinimo. |
| ProductoPerecedero | Agrega fechaVencimiento:LocalDate; valida vigencia y descuento. | Sobrescribe calcularPrecioFinal y puedeVenderse. |
| ProductoNoPerecedero | Mantiene precio base y exige estar activo. | Sobrescribe calcularPrecioFinal y puedeVenderse. |
| Cliente | Comprador: idCliente:int, nombre/nit/telefono:String. | Constructor validado, getters. |
| Venta | Encabezado: idVenta:int, fecha:LocalDateTime, usuario:Usuario, cliente:Cliente, metodoPago:MetodoPago, total:BigDecimal, detalles:ArrayList<DetalleVenta>. | agregarDetalle, quitarDetalle, calcularTotal, obtenerDetalles, reconstruir. |
| DetalleVenta | Línea: idDetalleVenta:int, producto:Producto, cantidad:int, precioUnitario/subtotal:BigDecimal. | calcularSubtotal; constructor de paquete coordinado por Venta. |
| Rol (enum) | ADMINISTRADOR, CAJERO. | valueOf, name heredados de Enum. |
| Permiso (enum) | Capacidades explícitas de cada rol. | Valores usados por Sesion.exigir. |
| MetodoPago (enum) | EFECTIVO, TARJETA. | valueOf, name. |

`Venta.DetalleGuardado` es un record auxiliar de reconstrucción JDBC; no es otra tabla. Transporta id:int, producto:Producto, cantidad:int y precio:BigDecimal para restaurar el precio histórico. Venta confirmada no permite agregar ni quitar detalles. Las listas devueltas son copias no modificables.

## Relaciones del UML
- Herencia: Administrador/Cajero → Usuario y Perecedero/NoPerecedero → Producto (`extends`).
- Agregación: Categoria 1 ◇— 0..* Producto; cada producto tiene exactamente una categoría y puede reasignarse.
- Composición: Venta 1 ◆— 0..* DetalleVenta durante preparación; al persistir exige 1..*. Cada detalle tiene una única venta propietaria.
- Asociación: Usuario 1 — 0..* Venta y Cliente 1 — 0..* Venta.
- Asociación navegable: DetalleVenta 0..* → 1 Producto.

## Para la defensa
**Explicación:** encapsular no exige crear setters para todo. Los objetos del catálogo son inmutables; editar significa construir una versión validada y enviarla al DAO. Venta sí cambia durante la preparación y controla sus detalles.

1. **¿Por qué Producto es abstracta?** No hay un producto genérico vendible sin decidir su comportamiento respecto del vencimiento.
2. **¿Dónde se usa ArrayList?** En detalles de Venta, líneas de VentaController y listas devueltas por los DAO.
3. **¿Por qué obtenerDetalles no devuelve la lista interna?** Para impedir que un llamador agregue líneas sin validar cantidad, precio y stock.

**Cambio en vivo: descuento de 15 % hasta cinco días antes.** 1) Abrir ProductoPerecedero.java. 2) Cambiar `dias <= 3` por `dias <= 5`. 3) Cambiar `0.90` por `0.85`. 4) Actualizar ModeloTest con días 0, 5, 6 y -1; Q100 debe resultar Q85, Q85, Q100 y rechazo. 5) Actualizar RF09 y la nota UML.
