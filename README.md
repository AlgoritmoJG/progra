# Sistema de Punto de Venta para Comercios Locales
Proyecto académico de Programación II: Java, MySQL, JDBC y MVC. Fases 1, 2 y 3: aplicación gráfica de escritorio Swing con consola de apoyo.

## Documentación
- [Requerimientos y alcance](docs/fase1/requerimientos.md)
- [UML visual](docs/fase1/diagrama-clases.svg) · [fuente PlantUML](docs/fase1/diagrama-clases.puml) y [diccionario de clases](docs/fase1/diccionario-clases.md)
- [E-R y decisiones SQL](docs/fase2/diagrama-er.md) · [Esquema MySQL](sql/01_schema.sql)
- [Ejecución: PDF de una página](docs/fase2/ejecucion.pdf) · [versión editable](docs/fase2/ejecucion.md)
- [Arquitectura y preparación de defensa](docs/fase2/arquitectura-y-defensa.md)
- [Guion de evidencia](docs/fase2/guion-evidencia.md)
- [Resultados de validación](docs/fase2/validacion.md) · [Checklist por fase](docs/checklist.md)

## Inicio rápido Linux/Docker
Con JDK completo >=21, Docker, Python >=3.12 y curl:
```bash
scripts/mysql-local.sh
scripts/swing.sh
# Crear administrador inicial desde Login si todavía no hay usuarios.
scripts/test-mysql.sh
```
Las claves locales se generan en `.local/`, ignorado por Git; no hay credenciales compartidas. Las pruebas MySQL usan exclusivamente `pos_test`, cuyos datos se borran. La aplicación usa `pos_dev`.

## Interfaz Swing y defensa
- [Pantallas, archivos y ejecución en NetBeans](docs/fase3/interfaz-y-ejecucion.md)
- [Guion de defensa: 3 minutos de demo + 7 de preguntas/cambio](docs/fase3/defensa.md)
- [Pruebas gráficas y capturas](docs/fase3/validacion.md)

El main predeterminado es `com.progra.pos.view.swing.SwingApp`. Figma es opcional: la interfaz entregada funciona en Java Swing y guarda datos en MySQL. Para ejecutar la consola anterior: `scripts/run.sh`.

## Estructura
`model`: dominio · `view`: Swing y consola · `controller`: permisos y coordinación · `dao`: JDBC · `util`: conexión, validación y contraseñas. Código bajo `src/main/java/com/progra/pos`; pruebas bajo `src/test/java/com/progra/pos`.

Precio con IVA incluido sin desglose. Perecederos: 10 % de descuento de 0 a 3 días inclusive; vencidos no se venden. Confirmación con commit/rollback y bloqueo de stock; historial conserva importes aplicados. No hay procesamiento bancario ni facturación fiscal.
