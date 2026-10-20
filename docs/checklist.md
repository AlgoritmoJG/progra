# Checklist de entrega y evaluación
Este checklist vincula evidencia con los criterios oficiales; no predice una nota.

## Fase 1 — análisis, modelado y POO
- [x] Título: Sistema de Punto de Venta para Comercios Locales.
- [x] UML con atributos privados, tipos, métodos, multiplicidades y visibilidad.
- [x] Herencia, composición, agregación y asociaciones explícitas y explicadas.
- [x] Problema, alcance, actores, reglas de negocio y requerimientos técnicos.
- [x] Diccionario de clases alineado con el modelo Java.
- [x] Preguntas, respuestas y modificaciones guiadas por bloque documental.

| Criterio | Evidencia |
|---|---|
| Funcionamiento técnico — 40 % | Requerimientos con criterios de aceptación y límites explícitos. En esta fase no se exige interfaz ejecutable. |
| Conceptos del curso — 40 % | UML y diccionario: encapsulamiento, dos jerarquías, polimorfismo y relaciones. |
| Documentación y defensa — 20 % | Requerimientos, explicación de decisiones, preguntas y ejercicios. |

## Fase 2 — lógica, BD y MVC
- [x] Java compilable con JDK completo >=21; probado con JDK 25.0.4.1, release 21.
- [x] Clases encapsuladas, herencia, polimorfismo, enums y ArrayList.
- [x] E-R y SQL de seis tablas con PK, FK, NOT NULL, UNIQUE, CHECK y acciones referenciales justificadas.
- [x] CRUD JDBC de categorías, productos, clientes y usuarios; confirmación y consulta de ventas.
- [x] PreparedStatement en todas las consultas de aplicación; entradas parametrizadas.
- [x] Validaciones de vacíos, tipos, rangos, vencimiento y existencias.
- [x] try-catch, try-with-resources y traducción de SQLException.
- [x] BCrypt con salt, permisos en controladores y credenciales MySQL fuera de Git.
- [x] Venta transaccional: commit/rollback y bloqueo para prevenir sobreventa.
- [x] Historial por fechas, detalles y aislamiento por usuario.
- [x] Vista de consola separada del modelo, controlador y DAO.
- [x] Guía PDF de una página con conexión y creación de credenciales propias de prueba.
- [x] Guion de video CRUD de 1–2 minutos y capturas sugeridas.
- [x] Pruebas de modelo y MySQL, incluida concurrencia y rollback, sin omisiones.
- [ ] Ejecutar en el NetBeans 31 del estudiante y confirmar su versión del servidor MySQL.
- [ ] Grabar video o capturas: se entrega el guion, no una grabación ficticia.

| Criterio | Evidencia |
|---|---|
| Funcionamiento técnico — 40 % | CRUD persistente, consola, transacción, historial y pruebas reales en MySQL. |
| Conceptos del curso — 40 % | POO, ArrayList, MVC, JDBC, PreparedStatement y manejo de excepciones. |
| Documentación y defensa — 20 % | E-R, SQL, guía de una página, guion y ejercicios por bloque. |

## Fase 3 — interfaz y preparación de defensa
- [x] Login Swing y menú según rol; administrador inicial desde Login.
- [x] Productos, categorías, Caja, historial y usuarios; clientes desde Caja.
- [x] Tablas de consulta, formularios validados y selección correcta al ordenar filas.
- [x] JDBC y BCrypt fuera del hilo de eventos, usando SwingWorker.
- [x] Mensajes de error, estado de operación y protección contra doble clic.
- [x] Confirmación antes de borrar, vaciar o salir con carrito.
- [x] Protección contra sobrescribir stock desde un formulario desactualizado.
- [x] Pruebas gráficas con Robot y MySQL real; capturas de ventanas ejecutadas.
- [x] Guion de 3 minutos de demo y 7 minutos de preguntas/cambio de código.
- [x] Preguntas con respuestas y modificaciones guiadas por bloque documental.
- [ ] Ejecutar en el NetBeans 31 del estudiante y verificar su versión de MySQL.
- [ ] Grabar evidencia y practicar personalmente las modificaciones en vivo.

| Criterio | Evidencia |
|---|---|
| Funcionamiento técnico — 40 % | Pantallas Swing conectadas a controladores y MySQL; pruebas gráficas y de transacción. |
| Conceptos del curso — 40 % | MVC, POO, ArrayList, eventos, SwingWorker y selección de filas con modelo/vista. |
| Documentación y defensa — 20 % | Guía de pantallas/ejecución, capturas, guion cronometrado y ejercicios. |
