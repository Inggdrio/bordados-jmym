# Sistema de Gestión de Pedidos de Bordados J.M.Y.M.

Programa de escritorio en **Java 21 + Swing**, con **Maven**, arquitectura **MVC** y base de datos
**PostgreSQL en Neon** (administrada con DBeaver). Única librería: el driver `postgresql`.

## 1. Qué necesitas instalar
JDK 21 • Maven 3.9 o superior • DBeaver • Una cuenta gratuita en https://neon.tech

## 2. Crear la base de datos en Neon
1. En Neon crea un proyecto y copia los datos de conexión (botón **Connect**).
2. En DBeaver: *Nueva conexión → PostgreSQL* con host, base (`neondb`), usuario y contraseña (SSL: `require`).
3. Abre `sql/bordados_jmym_postgresql.sql` y ejecútalo como script (**Alt + X**).
   Crea tablas, triggers, funciones, vistas, roles, las 3 sedes, las **7 cuentas** y **30 clientes de prueba** con sus órdenes.
   > Si ya tenías la base creada con una versión anterior, ejecuta solo `sql/migracion_v2_2_entrega_pagada.sql`
   > (incluye la regla de entrega, las cuentas de las sedes y los 30 clientes; se puede ejecutar varias veces sin duplicar nada).

## 3. Configurar la conexión
Copia la plantilla y edítala con tus datos reales de Neon:

    cp src/main/resources/config.properties.ejemplo src/main/resources/config.properties

    db.url=jdbc:postgresql://ep-xxxx.us-east-2.aws.neon.tech/neondb?sslmode=require
    db.usuario=tu_usuario
    db.clave=tu_clave

> El archivo `config.properties` **no se sube al repositorio** (está en `.gitignore`): cada
> integrante crea el suyo. Así las credenciales nunca quedan expuestas en GitHub.
> La primera conexión puede tardar unos segundos: Neon "duerme" la base cuando no se usa.

## 4. Ejecutar
    mvn compile exec:java            # ejecutar directamente
    mvn clean package                # crear el .jar
    java -jar target/bordados-jmym-1.0.jar

## 5. Cuentas de prueba (contraseña de todas: `Jmym2026`)
| Sede | Vendedor | Diseñador |
|------|----------|-----------|
| J.M.Y.M. | vendedor@jmym.com | disenador@jmym.com |
| The Police | vendedor.police@jmym.com | disenador.police@jmym.com |
| Lobo | vendedor.lobo@jmym.com | disenador.lobo@jmym.com |

El **Gerente** (`gerente@jmym.com`) supervisa las tres sedes.

| Rol | Menú |
|-----|------|
| Gerente | Inicio, Sedes, Usuarios, Órdenes, Reportes |
| Vendedor | Órdenes, Pagos, Historial |
| Diseñador | Producción (con nombre y teléfono del cliente), Historial |

**Cámbialas o elimínalas antes de usar el sistema de verdad.**

### 30 clientes de prueba (cédulas 1090000001 a 1090000030)
Repartidos en las 3 sedes: 9 **Pendiente** (algunos con abono) • 5 **En Producción** • 6 **Listo para Entrega**
(3 pagadas, que ya se pueden entregar, y 3 con saldo, que no) • 10 **Entregada** (todas pagadas).

## 6. Estructura (MVC) — 52 archivos, unas 2.400 líneas
    com.jmym
    ├── App.java          punto de entrada (main)
    ├── conexion/         ConexionBD → abre la conexión a Neon
    ├── modelo/           MODELO: Persona, Cliente, Usuario (Gerente, Vendedor, Disenador), Sede, Orden, Bordado, Pago
    ├── dao/              consultas SQL: BaseDAO (código JDBC común) y un DAO por tabla
    ├── controlador/      CONTROLADOR: validaciones y reglas de negocio
    ├── vista/            VISTA: Swing. Estilo, Tabla, Formulario, ventanas y un Panel por módulo
    └── util/             Constantes, Validador, Formato, SeguridadUtil, NegocioException

Flujo: **Vista → Controlador → DAO → Base de datos**. La vista nunca habla con la base de datos.
Los errores se lanzan como `NegocioException` y la vista los muestra en un aviso con `Estilo.intentar(...)`.

## 7. Dónde está cada concepto de POO
| Concepto | Dónde verlo |
|----------|-------------|
| Encapsulación | Atributos `private` + `get/set` con validación (`Persona`, `Bordado`, `Sede`) |
| Abstracción | `Persona`, `Usuario`, `BaseDAO`, `PanelBase`, `PanelListaOrdenes` (clases abstractas) |
| Herencia | `Cliente` y `Usuario` → `Persona`; `Gerente`/`Vendedor`/`Disenador` → `Usuario`; cada panel → `PanelBase` |
| Polimorfismo | `getRol()` / `getModulos()` / `descripcion()` cambian según la clase; `PanelListaOrdenes` define el molde y cada hija decide `datos`, `fila`, `acciones` y `accion` |
| Arreglos | `Constantes` (estados, roles, medios de pago), `getModulos()`, filas `Object[]` de reportes |
| ArrayList | `Orden.bordados`, listas de usuarios, sedes, órdenes y pagos |

## 8. Reglas importantes
- Contraseñas con sal + SHA-256; 3 intentos fallidos bloquean la cuenta; la sesión se cierra a los 10 min sin uso.
- Una orden solo se modifica o elimina mientras está **Pendiente**; el estado avanza sin saltos.
- **La factura se cierra al pagar el total: una orden con saldo pendiente no se puede entregar** (la opción "Entregar" no aparece y la base de datos también lo rechaza).
- El tipo de bordado se elige de la lista o se **escribe a mano**. Ya no se piden *uniforme* ni *descripción* (usa las **notas**).
- Las listas de órdenes tienen el botón **Actualizar** (menos el Historial del Diseñador). El Diseñador nunca ve precios. Al marcar una orden como lista, pasa a su **Historial**; el Vendedor ve en el suyo las órdenes **entregadas**.
- Los pagos no superan el saldo y no se editan ni se borran. Toda operación crítica queda en `auditoria`.
- El logo está en `src/main/resources/logo.png`: reemplázalo por el archivo original para que se vea con más nitidez.

## 9. Pruebas unitarias
El proyecto trae pruebas con **JUnit 5** sobre la lógica de negocio (validaciones, cálculos de
total y saldo, reglas de estado, seguridad). No necesitan base de datos.

    mvn test                     # corre todas las pruebas
    target/site/jacoco/index.html  # reporte de cobertura (se abre en el navegador)

Son 10 clases de prueba en `src/test/java/`. El detalle de qué cubre cada una y quién la mantiene
está en `docs/REPARTO_DE_TRABAJO.md`.

## 10. Trabajo en equipo
- Cómo clonar, crear ramas, escribir commits y abrir Pull Requests: **`CONTRIBUTING.md`**.
- Quién es responsable de cada módulo y de cada prueba: **`docs/REPARTO_DE_TRABAJO.md`**.
- Cada push y cada PR hacia `main` dispara la **CI** (`.github/workflows/ci.yml`), que compila y
  corre `mvn test` automáticamente.
