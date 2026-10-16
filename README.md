# Sistema de Punto de Venta para Comercios Locales
Proyecto académico de Programación II: Java, MySQL, JDBC y MVC. Fases 1 y 2; Swing se implementará en Fase 3.

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
scripts/run.sh -Dexec.args=--crear-admin
scripts/run.sh
scripts/test-mysql.sh
```
Las claves locales se generan en `.local/`, ignorado por Git; no hay credenciales compartidas. Las pruebas MySQL usan exclusivamente `pos_test`, cuyos datos se borran. La aplicación usa `pos_dev`.

## Estructura
`model`: dominio · `view`: consola · `controller`: permisos y coordinación · `dao`: JDBC · `util`: conexión, validación y contraseñas. Código bajo `src/main/java/com/progra/pos`; pruebas bajo `src/test/java/com/progra/pos`.

Precio con IVA incluido sin desglose. Perecederos: 10 % de descuento de 0 a 3 días inclusive; vencidos no se venden. Confirmación con commit/rollback y bloqueo de stock; historial conserva importes aplicados. No hay procesamiento bancario ni facturación fiscal.
