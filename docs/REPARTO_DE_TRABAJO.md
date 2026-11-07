# Reparto de trabajo — Bordados J.M.Y.M.

El sistema está dividido en **módulos** alineados con los objetivos específicos del proyecto.
Cada integrante es **responsable (owner)** de su módulo: su código, sus pruebas unitarias y
la revisión de los PR que toquen esa zona. Las clases compartidas (`modelo`, `util`) las puede
tocar cualquiera, pero avisando en el PR.

> La arquitectura es **MVC + DAO**: `vista → controlador → dao → base de datos`.
> La vista nunca habla directamente con la base de datos.

---

## Integrantes y módulos

### 👤 German Darío Bolaños — Autenticación, Seguridad e Infraestructura
Objetivo específico 1: *autenticación y control de acceso por roles.*

| Capa | Archivos |
|------|----------|
| Infra | `conexion/ConexionBD.java`, `dao/BaseDAO.java`, `App.java` |
| Util | `util/Validador.java`, `util/SeguridadUtil.java`, `util/Constantes.java`, `util/Formato.java`, `util/NegocioException.java` |
| Modelo | `modelo/Persona.java`, `Usuario.java`, `Gerente.java`, `Vendedor.java`, `Disenador.java`, `UsuarioFactory.java` |
| Controlador | `LoginControlador.java`, `Sesion.java`, `UsuarioControlador.java` |
| DAO | `UsuarioDAO.java`, `AuditoriaDAO.java` |
| Vista | `VentanaLogin.java`, `PanelUsuarios.java` |
| **Pruebas** | `ValidadorTest`, `SeguridadUtilTest`, `UsuarioTest` |

### 👤 Andrés Julián Contreras Tombe — Clientes y Órdenes · Coordinador del repositorio
Objetivo específico 2: *gestión de clientes y órdenes.*

| Capa | Archivos |
|------|----------|
| Modelo | `modelo/Cliente.java`, `Orden.java`, `Bordado.java` |
| Controlador | `OrdenControlador.java` |
| DAO | `OrdenDAO.java`, `ClienteDAO.java` |
| Vista | `DialogoOrden.java`, `DialogoDetalle.java`, `PanelListaOrdenes.java`, `PanelOrdenesVendedor.java`, `PanelOrdenesGerente.java`, `Formulario.java` |
| **Pruebas** | `BordadoTest`, `OrdenTest`, `ClienteTest` |
| **Extra** | Crea el repositorio y el commit inicial; configuración de Maven (`pom.xml`), CI (GitHub Actions) y reglas del repo |

### 👤 Samir Correa Álvarez — Pagos y Producción
Objetivos específicos 3 y 4: *seguimiento del estado de las órdenes* y *pagos con cálculo de saldo.*

| Capa | Archivos |
|------|----------|
| Modelo | `modelo/Pago.java` |
| Controlador | `PagoControlador.java` |
| DAO | `PagoDAO.java` |
| Vista | `PanelPagos.java`, `PanelProduccion.java`, `PanelHistorial.java` |
| **Pruebas** | `PagoTest` + (a futuro) pruebas de integración de `PagoControlador` y el avance de estados, que requieren base de datos o *mocks*. |

### 👤 Sebastián Lasso — Reportes y Sedes
Objetivo específico 5: *reportes* + administración de sedes.

| Capa | Archivos |
|------|----------|
| Modelo | `modelo/Sede.java`, `ResultadoReporte.java` |
| Controlador | `ReporteControlador.java`, `SedeControlador.java` |
| DAO | `ReporteDAO.java`, `SedeDAO.java` |
| Vista | `PanelReportes.java`, `PanelSedes.java`, `PanelInicio.java` |
| **Pruebas** | `FormatoTest`, `ConstantesTest`, `SedeTest` |

### Compartido (todo el equipo)
`vista/Estilo.java`, `vista/Tabla.java`, `vista/PanelBase.java`, `vista/VentanaPrincipal.java`,
el esquema SQL de `sql/` y la base de datos en Neon.

---

## Pruebas unitarias actuales (10 clases, sin base de datos)

| Clase de prueba | Qué cubre | Responsable |
|-----------------|-----------|-------------|
| `ValidadorTest` | vacío, solo dígitos, correo, dinero, enteros | German |
| `SeguridadUtilTest` | hash con sal, verificación de contraseña | German |
| `UsuarioTest` | roles, módulos por rol, fábrica de usuarios | German |
| `BordadoTest` | validaciones, subtotal, firma | Andrés |
| `OrdenTest` | total, saldo, avance de estado, fecha de cierre | Andrés |
| `ClienteTest` | cédula, nombre, teléfono, descripción | Andrés |
| `PagoTest` | datos del abono | Samir |
| `FormatoTest` | dinero y fechas | Sebastián |
| `ConstantesTest` | listas del negocio y búsqueda por posición | Sebastián |
| `SedeTest` | validación e identidad por código | Sebastián |

Se ejecutan con `mvn test`. El reporte de cobertura queda en `target/site/jacoco/index.html`.

---

## Qué NO se prueba con pruebas unitarias (y por qué)
Las capas `dao/` y la parte de los `controlador/` que consulta la base de datos **no** se prueban
con pruebas unitarias puras, porque dependen de una conexión real a Neon y de la sesión iniciada.
Eso se valida con **pruebas manuales** (las 7 cuentas y 30 clientes de prueba del `README`) y queda
como tarea futura de **pruebas de integración**. Las pruebas unitarias se concentran en la **lógica
de negocio pura**: validaciones, cálculos (total, saldo, subtotal), reglas de estado y seguridad.
