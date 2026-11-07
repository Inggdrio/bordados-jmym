# Tareas por integrante (código propio de cada uno)

Para que el trabajo sea **justo** y cada quien tenga código suyo, además de las pruebas cada
integrante implementa estas mejoras **en las clases de su módulo** (ver `REPARTO_DE_TRABAJO.md`).
Cada tarea = una rama + un Pull Request revisado por **otro** compañero.

Reglas:
- **Tú escribes el código de tu tarea** (es tu aporte individual). Si te atoras, pides ayuda en el PR.
- Cada tarea toca solo archivos de tu módulo → no choca con los demás.
- Antes del PR: `mvn test` en verde y, si agregaste lógica, su prueba.
- Mensajes de commit con el formato `tipo(alcance): descripción` (ver `CONTRIBUTING.md`).

Resultado por persona: **1 PR de pruebas + 3 PR de funcionalidad** = aporte parejo y visible.

---

## 👤 German — Autenticación / Seguridad

**T1 · `feature/auth-clave-segura`** — Validar la fortaleza de la contraseña
- Archivos: `util/Validador.java`, `controlador/UsuarioControlador.java`, `util/ValidadorTest.java`
- Qué: método `claveSegura(String)` que exija **mínimo 8 caracteres, al menos una letra y un número**;
  lanzar `NegocioException` con mensaje claro. Úsalo donde se asigna/cambia la contraseña del usuario.
- Aceptación: no deja crear un usuario con clave débil; la prueba cubre un caso válido y uno inválido.
- Commit: `feat(auth): valida fortaleza de la contraseña al crear usuarios`

**T2 · `feature/auth-ver-clave`** — Mostrar/ocultar la contraseña en el login
- Archivos: `vista/VentanaLogin.java`
- Qué: un checkbox "Mostrar contraseña" que cambia el `echoChar` del campo de clave.
- Aceptación: marcado se ve el texto; desmarcado se oculta.
- Commit: `feat(auth): permite mostrar u ocultar la contraseña en el login`

**T3 · `feature/auth-auditoria-login`** — Registrar en la auditoría los intentos de login fallidos
- Archivos: `controlador/LoginControlador.java`, `dao/AuditoriaDAO.java`
- Qué: cuando falle un inicio de sesión, dejar un registro en la tabla `auditoria` (correo y fecha).
- Aceptación: tras un login fallido aparece una fila nueva en `auditoria`.
- Commit: `feat(auth): registra en auditoría los intentos de login fallidos`

---

## 👤 Andrés — Clientes / Órdenes

**T1 · `feature/ordenes-validar-vacia`** — Exigir al menos un bordado para guardar
- Archivos: `controlador/OrdenControlador.java`
- Qué: al crear/guardar una orden sin bordados, lanzar `NegocioException("Agregue al menos un bordado.")`.
- Aceptación: no se guarda una orden vacía; con un bordado sí.
- Commit: `feat(ordenes): exige al menos un bordado para guardar la orden`

**T2 · `feature/ordenes-resumen`** — Resumen de la orden (con prueba)
- Archivos: `modelo/Orden.java`, `modelo/OrdenTest.java`
- Qué: `cantidadTotalBordados()` (suma de cantidades) y `resumen()` → ej. `"3 bordados · $ 45.000"`.
- Aceptación: prueba unitaria que verifica la suma de cantidades y el total.
- Commit: `feat(ordenes): agrega resumen y cantidad total de bordados en Orden`

**T3 · `feature/ordenes-buscar`** — Buscar/filtrar órdenes por cédula o nombre del cliente
- Archivos: `vista/PanelListaOrdenes.java` (o el panel de lista de tu módulo)
- Qué: un campo de búsqueda que filtre la tabla de órdenes por cédula o nombre del cliente.
- Aceptación: al escribir, la lista muestra solo las órdenes que coinciden.
- Commit: `feat(ordenes): busca y filtra órdenes por cédula o nombre del cliente`

---

## 👤 Samir — Pagos / Producción

**T1 · `feature/pagos-abono-total`** — Botón para abonar el saldo completo
- Archivos: `vista/PanelPagos.java`
- Qué: botón "Pagar saldo" que rellena el valor con el saldo pendiente y muestra el **% pagado**
  de la orden (puedes apoyarte en `PagoControlador`).
- Aceptación: al pulsarlo, el valor = saldo; registra el pago y la factura queda cerrada.
- Commit: `feat(pagos): agrega botón para abonar el saldo completo`

**T2 · `feature/produccion-confirmar-estado`** — Confirmar antes de avanzar el estado
- Archivos: `vista/PanelProduccion.java`
- Qué: un `JOptionPane` de confirmación ("¿Marcar como …?") antes de avanzar el estado de la orden.
- Aceptación: pide confirmación; si el usuario cancela, el estado no cambia.
- Commit: `feat(produccion): pide confirmación antes de avanzar el estado de la orden`

**T3 · `feature/pagos-comprobante-txt`** — Guardar el comprobante de pago en un archivo .txt
- Archivos: `vista/PanelPagos.java` (usa `PagoControlador.comprobante(...)`, que ya devuelve el texto)
- Qué: botón "Guardar comprobante" que escribe el comprobante en un `.txt` con `JFileChooser`.
- Aceptación: genera un archivo de texto legible con los datos del pago.
- Commit: `feat(pagos): guarda el comprobante de pago en un archivo de texto`

> Recuerda que las pruebas de integración con BD de tu módulo quedaron como esqueleto `@Disabled`
> en `PagoControladorIT`: cuando tengas la BD de prueba, puedes implementarlas (PR extra).

---

## 👤 Sebastián — Reportes / Sedes

**T1 · `feature/reportes-exportar-csv`** — Exportar el reporte a CSV
- Archivos: `vista/PanelReportes.java`
- Qué: botón "Exportar CSV" que guarda las columnas y filas del `ResultadoReporte` en un `.csv`
  usando `JFileChooser`.
- Aceptación: genera un archivo abrible en Excel con exactamente lo que muestra la tabla.
- Commit: `feat(reportes): exporta el reporte mostrado a un archivo CSV`

**T2 · `feature/sedes-resumen`** — Resumen corto de la sede (con prueba)
- Archivos: `modelo/Sede.java`, `modelo/SedeTest.java`
- Qué: método `resumen()` → ej. `"The Police — Cra 5 (tel. 6021111)"`.
- Aceptación: prueba unitaria que verifica el texto del resumen.
- Commit: `feat(sedes): agrega resumen corto de la sede`

**T3 · `feature/reportes-totales`** — Fila de totales al pie del reporte
- Archivos: `vista/PanelReportes.java`
- Qué: agregar una fila final con los totales (suma de las columnas numéricas del `ResultadoReporte`).
- Aceptación: al generar un reporte aparece una fila "TOTAL" con las sumas correctas.
- Commit: `feat(reportes): agrega fila de totales al pie del reporte`

---

## Cómo queda el reparto final (resumen)

| Integrante | Pruebas (PR 1) | Funcionalidad (PR 2, 3 y 4) |
|------------|----------------|------------------------------|
| German | validación, seguridad, roles | clave segura · ver contraseña · auditoría de login |
| Andrés | bordado, orden, cliente | orden no vacía · resumen de orden · buscar órdenes |
| Samir | pago (+ integración @Disabled) | abono total · confirmar producción · comprobante .txt |
| Sebastián | formato, constantes, sede | exportar CSV · resumen de sede · totales del reporte |

Cada uno: **4 PR revisados por otro compañero** (1 de pruebas + 3 de funcionalidad). Eso es trabajo
en equipo repartido, justo y con código propio de cada integrante.
