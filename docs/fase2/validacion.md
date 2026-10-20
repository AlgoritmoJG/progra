# Evidencia de validación de las fases 1 y 2

Documento de la entrega anterior. La interfaz Swing y las pruebas adicionales están en [validación de Fase 3](../fase3/validacion.md).

## Entorno ejecutado
- Linux; OpenJDK Temurin 25.0.4.1, compilación con release 21.
- Maven 3.9.11, Connector/J 9.3.0, jbcrypt 0.4.
- MySQL Community Server 8.4.11 en contenedor mysql:8.4, puerto local 3307.
- TLS con verificación de CA local mediante truststore PKCS12.
- pos_dev para la aplicación; pos_test desechable para pruebas. Las pruebas rechazan URLs que no nombren exclusivamente pos_test.

## Comprobaciones realizadas
| Comprobación | Resultado |
|---|---|
| ModeloTest | 8 pruebas, 0 fallos, 0 errores, 0 omitidas. |
| MySqlTest | 12 pruebas, 0 fallos, 0 errores, 0 omitidas. |
| Compilación y empaquetado | Correctos; target/punto-venta-1.0.0.jar. Ejecutar con Maven para incluir dependencias; no es un JAR autónomo. |
| Consola | Login, alta de categoría, consultas, historial, resumen vacío, rechazo de entrada y salida verificados con BD de pruebas. |
| Scripts MySQL | Arranque y repetición correctos; no se reimporta un esquema existente. |
| UML | PlantUML validó sintaxis y generó diagrama-clases.svg. |
| Guía | PDF con fuentes incrustadas, una página A4, inspeccionado visualmente. |

Las pruebas JDBC ejercitan CRUD de cuatro entidades, unicidad, búsqueda literal, login inválido, inyección como texto, permisos, desactivación de sesión, protección de CF y de la propia cuenta administradora, precio histórico, descuento, vencimiento, rollback, carrito agrupado, confirmación fallida sin vaciar carrito y CHECK de MySQL.

La prueba concurrente usa dos conexiones y dos cajeros que compiten por una unidad: exige exactamente un éxito, stock cero y una venta. La prueba de rollback exige que, al fallar el segundo producto, las existencias del primero permanezcan intactas y no exista venta parcial.

## Cómo reproducir
Desde la raíz con JDK completo y Docker:
```bash
scripts/mysql-local.sh
scripts/test-mysql.sh
scripts/mvn.sh -DskipTests package
```
El primer script crea o reinicia el contenedor y prepara CA/BD. El segundo configura la BD desechable y ejecuta ambas suites. Reportes: target/surefire-reports/. El último empaqueta sin repetir pruebas; no es otra ejecución de tests.

Sin POS_RUN_DB_TESTS=true, mvn test solo demuestra el modelo: MySQL se omite. No presentar ese resultado como validación de JDBC.

## Límites
No se ejecutó NetBeans ni una interfaz gráfica: no están instalados en esta nube. Verificar NetBeans 31 en el equipo del estudiante siguiendo ejecucion.pdf. No se probó exactamente JDK 25.0.3: se usó 25.0.4.1 con bytecode Java 21. “MySQL 26.7” sigue pendiente de identificar; el esquema requiere MySQL >=8.0.16 y está probado en 8.4.11. No se desarrolló Swing ni se grabó video: la interfaz corresponde a Fase 3 y se entrega el guion de evidencia.
