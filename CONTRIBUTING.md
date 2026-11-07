# Guía de trabajo en equipo

Cómo trabajamos los cuatro sobre este repositorio: ramas, commits, pruebas y Pull Requests.
El objetivo es que el historial quede **limpio, ordenado y con el aporte de cada integrante visible**.

---

## 1. Primera vez (cada integrante, una sola vez)

```bash
# 1. Clonar el repositorio
git clone https://github.com/AndresContrerasss/bordados-jmym.git
cd bordados-jmym

# 2. Decir quién eres (sale en cada commit)
git config user.name  "Tu Nombre Completo"
git config user.email "tucorreo@ejemplo.com"

# 3. Crear tu config.properties a partir de la plantilla
cp src/main/resources/config.properties.ejemplo src/main/resources/config.properties
#   ...y editarlo con los datos reales de Neon (ese archivo NO se sube).

# 4. Probar que todo compila y pasan las pruebas
mvn test
```

---

## 2. Ramas (branches)

Nunca se trabaja directamente sobre `main`. Cada tarea va en su propia rama.

| Rama | Para qué |
|------|----------|
| `main` | Versión estable. Solo entra código revisado y con las pruebas en verde. |
| `feature/<modulo>-<tarea>` | Una funcionalidad nueva. Ej.: `feature/pagos-comprobante` |
| `fix/<modulo>-<tarea>` | Corrección de un error. Ej.: `fix/ordenes-saldo-negativo` |
| `test/<modulo>` | Pruebas unitarias de un módulo. Ej.: `test/reportes` |

```bash
git switch main
git pull origin main              # siempre partir de lo último
git switch -c feature/pagos-comprobante
# ...trabajas, haces commits...
git push -u origin feature/pagos-comprobante
```

---

## 3. Commits — Conventional Commits (en español)

Formato: **`tipo(alcance): descripción corta en minúscula`**

```
feat(pagos): registrar abono y calcular saldo
fix(ordenes): evitar entregar una orden con saldo pendiente
test(seguridad): pruebas de hash y verificación de contraseña
docs(readme): explicar cómo configurar Neon
refactor(dao): unificar el manejo de errores SQL
```

**Tipos permitidos:**

| Tipo | Cuándo |
|------|--------|
| `feat` | Una funcionalidad nueva |
| `fix` | Corrección de un error |
| `test` | Agregar o cambiar pruebas |
| `docs` | Solo documentación |
| `refactor` | Reorganizar código sin cambiar lo que hace |
| `style` | Formato, sangrías, nombres (sin cambiar lógica) |
| `chore` | Configuración, Maven, archivos de apoyo |

**Alcance (`alcance`)** = el módulo: `auth`, `ordenes`, `clientes`, `pagos`, `produccion`, `reportes`, `sedes`, `dao`, `bd`, `ui`.

**Reglas simples**
- Un commit = un cambio con sentido. No mezclar cosas que no tienen relación.
- Descripción en **imperativo y minúscula**, sin punto final: "agrega", "corrige", "ajusta".
- Commits pequeños y frecuentes mejor que uno gigante.
- Si cierra un issue: `feat(reportes): agrega reporte de ventas (closes #12)`.

---

## 4. Antes de abrir el Pull Request

```bash
mvn test        # TODAS las pruebas deben pasar
```

Luego sube tu rama y abre el PR en GitHub **hacia `main`**:
- Título con el mismo estilo de los commits.
- Describe qué hiciste (usa la plantilla que aparece sola).
- Pide revisión a **otro** integrante. Nadie aprueba su propio PR.
- El PR se fusiona solo cuando: pasa la CI (GitHub Actions) y hay al menos **1 aprobación**.

```bash
# Si main avanzó mientras trabajabas, actualiza tu rama antes:
git switch main && git pull origin main
git switch feature/mi-rama
git merge main         # resuelves conflictos si los hay, luego vuelves a correr mvn test
```

---

## 5. Pruebas unitarias

- Van en `src/test/java/...`, en el mismo paquete que la clase que prueban.
- Se nombran `NombreDeLaClaseTest.java` y usan **JUnit 5**.
- Cada integrante escribe las pruebas de **su** módulo (ver `docs/REPARTO_DE_TRABAJO.md`).
- Correr todo: `mvn test`. Cobertura: abrir `target/site/jacoco/index.html`.

Regla de oro: **si agregas lógica nueva, agregas su prueba.** `main` no recibe código sin pruebas en verde.
