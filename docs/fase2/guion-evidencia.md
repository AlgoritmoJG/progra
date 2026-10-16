# Evidencia de CRUD — video de 1–2 minutos

Preparación fuera de grabación: MySQL iniciado, proyecto compilado, administrador y cajero creados, consola autenticada. No mostrar contraseñas ni archivos `.local`. Para que quepa en dos minutos ensaya los comandos. Usa códigos únicos para no chocar con datos anteriores; anota los ids devueltos.

| Tiempo | Acción de consola | Evidencia esperada |
|---|---|---|
| 0:00–0:15 | `categoria-crear`: nombre Demo, descripción Defensa; `categorias`. | INSERT y SELECT: aparece el id de categoría. |
| 0:15–0:40 | `producto-crear`: código DEMO-01, nombre Cuaderno, precio 10.00, stock 5, mínimo 1, activo s, categoría anterior, perecedero n. Luego `productos`, buscar DEMO-01. | Producto persistido y búsqueda por código. |
| 0:40–1:00 | `producto-editar`: id creado, mismo código, nombre Cuaderno grande, precio 12.50, stock 5, mínimo 1, activo s, misma categoría, perecedero n. Buscar de nuevo. | UPDATE visible: nombre y precio nuevos. |
| 1:00–1:15 | `producto-borrar` con ese id; buscar de nuevo. | DELETE físico: producto sin ventas deja de aparecer. |
| 1:15–1:30 | `categoria-borrar` con el id de Demo; `categorias`. | Eliminación de categoría ahora sin productos. |
| 1:30–1:50 | Intentar `producto-crear` con precio -1; explicar rechazo; abrir CategoriaDao y señalar un parámetro ?. | Validación antes de BD y PreparedStatement. |
| 1:50–2:00 | Explicar capas: vista → controlador → DAO → MySQL. | Comprensión técnica. |

Capturas alternativas: lista inicial, producto creado, producto actualizado, búsqueda tras borrado y mensaje de validación. Añadir captura de SELECT en Workbench si la catedrática requiere ver persistencia independiente.

## Demostración adicional de ventas (fuera del video de CRUD)
Crear producto con stock 2; `agregar` cantidad 1; `carrito`; `clientes` para obtener CF; `confirmar` con EFECTIVO; `historial` con fecha de hoy. Verificar stock 1. Intentar cantidad 2 y mostrar rechazo. `producto-borrar` desactiva sin perder el historial. Mostrar prueba `rollbackReviertePrimerDescuentoSiSegundoProductoFalla` para explicar atomicidad.

## Para la defensa
**Explicación:** una demostración debe probar cambios persistentes y reglas, no solo que la aplicación abre.
1. **¿Cómo demuestras UPDATE?** Cambiar un valor y consultarlo nuevamente desde MySQL, no solo modificar una variable.
2. **¿Por qué este producto sí se elimina?** Porque no participa en ventas; uno vendido se desactiva.
3. **¿Qué pasa si escribes letras en stock?** La vista captura la conversión numérica inválida y no llama al DAO con ese dato.

**Cambio en vivo: buscar por nombre.** 1) Ejecutar productos e introducir parte del nombre. 2) Señalar LOCATE con parámetros en ProductoDao.buscarPorTexto. 3) Para búsqueda exacta por código, agregar un método con WHERE p.codigo=? y reutilizar mapear. 4) Probar coincidencia y ausencia sin concatenar entrada en SQL.
