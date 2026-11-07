# Plan de commits paso a paso (para los 4)

Objetivo: que el repositorio quede **bien distribuido**, con el aporte de cada integrante visible,
y que **compile siempre** (nadie renombra clases ni rompe las conexiones).

## Idea clave (por qué este orden)
El código es **MVC muy conectado**: `App → VentanaLogin → VentanaPrincipal`, y `VentanaPrincipal`
crea los 9 paneles; además los controladores usan modelos de varios módulos. Por eso **no** se puede
partir el código ya hecho en 4 commits que compilen por separado. La solución:

1. **Andrés** sube una sola vez la **base que ya compila** (todo el código fuente, sin pruebas).
2. **Cada integrante** sube, por su propia rama y PR, **las pruebas de su módulo**.
3. De ahí en adelante, cada quien trabaja incrementalmente en **su** módulo (ver `REPARTO_DE_TRABAJO.md`).

Los nombres de clase quedan fijos desde el primer commit. Nadie los cambia.

---

## Paso 0 — Andrés crea el repositorio (una sola vez)

El arranque se sube en **4 commits temáticos** (no uno solo), para que se vea la construcción por
capas. Las pruebas NO entran aquí: las sube cada dueño en la Fase 1.

```bash
cd bordados-jmym
git init
git branch -M main

# Commit 1 — configuración, SQL, recursos y documentación
git add pom.xml .gitignore README.md CONTRIBUTING.md docs/ .github/ sql/ src/main/resources/
git commit -m "chore(repo): configuración de Maven, SQL, recursos y documentación"

# Commit 2 — conexión, DAO base, modelo y utilidades
git add src/main/java/com/jmym/conexion/ src/main/java/com/jmym/dao/ \
        src/main/java/com/jmym/modelo/ src/main/java/com/jmym/util/
git commit -m "feat(bd): conexión a PostgreSQL, BaseDAO, modelo y utilidades"

# Commit 3 — controladores y punto de entrada (App)
git add src/main/java/com/jmym/controlador/ src/main/java/com/jmym/App.java
git commit -m "feat(logica): controladores de login, órdenes, pagos, producción y reportes"

# Commit 4 — vista (ventanas y paneles Swing)
git add src/main/java/com/jmym/vista/
git commit -m "feat(ui): ventanas y paneles Swing"

git remote add origin https://github.com/AndresContrerasss/bordados-jmym.git
git push -u origin main
```

Luego: invita a los otros 3 como colaboradores y protege `main` (ver `SUBIR_A_GITHUB.md`).
Después de esto, `main` compila y `mvn test` corre sin errores (todavía sin pruebas).

> **Importante para evitar choques:** cada integrante, en su copia, agrega **solo** los archivos de
> prueba que le corresponden. No copies al repo pruebas que no son tuyas.

---

## Regla de pruebas (estructura)
Cada prueba va en `src/test/java/` en el **mismo paquete** que la clase que prueba, y se llama
`<Clase>Test.java`. Ya están escritas en el proyecto; cada quien sube las suyas:

```
src/test/java/com/jmym/
├── util/     ValidadorTest, SeguridadUtilTest, ConstantesTest, FormatoTest
├── modelo/   UsuarioTest, BordadoTest, OrdenTest, ClienteTest, PagoTest, SedeTest
└── controlador/  PagoControladorIT   (integración, @Disabled)
```

---

## Paso a paso por integrante

Cada PR es: crear rama → agregar tus archivos → `mvn test` → commit → push → abrir PR → pedir
revisión a otro integrante → fusionar cuando la CI esté verde y haya 1 aprobación.

### 👤 German — módulo Autenticación / Seguridad
```bash
git switch main && git pull origin main
git switch -c test/auth
# agrega: src/test/java/com/jmym/util/ValidadorTest.java
#         src/test/java/com/jmym/util/SeguridadUtilTest.java
#         src/test/java/com/jmym/modelo/UsuarioTest.java
mvn test
git add src/test/java/com/jmym/util/ValidadorTest.java \
        src/test/java/com/jmym/util/SeguridadUtilTest.java \
        src/test/java/com/jmym/modelo/UsuarioTest.java
git commit -m "test(auth): pruebas de validación, seguridad y roles de usuario"
git push -u origin test/auth
# abrir Pull Request hacia main
```

### 👤 Andrés — módulo Clientes y Órdenes
```bash
git switch main && git pull origin main
git switch -c test/ordenes
# agrega: src/test/java/com/jmym/modelo/BordadoTest.java
#         src/test/java/com/jmym/modelo/OrdenTest.java
#         src/test/java/com/jmym/modelo/ClienteTest.java
mvn test
git add src/test/java/com/jmym/modelo/BordadoTest.java \
        src/test/java/com/jmym/modelo/OrdenTest.java \
        src/test/java/com/jmym/modelo/ClienteTest.java
git commit -m "test(ordenes): pruebas de bordado, orden (total/saldo/estados) y cliente"
git push -u origin test/ordenes
# abrir Pull Request hacia main
```

### 👤 Samir — módulo Pagos y Producción
```bash
git switch main && git pull origin main
git switch -c test/pagos
# agrega: src/test/java/com/jmym/modelo/PagoTest.java
#         src/test/java/com/jmym/controlador/PagoControladorIT.java
mvn test
git add src/test/java/com/jmym/modelo/PagoTest.java \
        src/test/java/com/jmym/controlador/PagoControladorIT.java
git commit -m "test(pagos): pruebas del modelo Pago y esqueleto de integración (@Disabled)"
git push -u origin test/pagos
# abrir Pull Request hacia main
```

### 👤 Sebastián — módulo Reportes y Sedes
```bash
git switch main && git pull origin main
git switch -c test/reportes
# agrega: src/test/java/com/jmym/util/FormatoTest.java
#         src/test/java/com/jmym/util/ConstantesTest.java
#         src/test/java/com/jmym/modelo/SedeTest.java
mvn test
git add src/test/java/com/jmym/util/FormatoTest.java \
        src/test/java/com/jmym/util/ConstantesTest.java \
        src/test/java/com/jmym/modelo/SedeTest.java
git commit -m "test(reportes): pruebas de formato, constantes del negocio y sede"
git push -u origin test/reportes
# abrir Pull Request hacia main
```

---

## Orden recomendado de fusión
1. German (`test/auth`) primero.
2. Luego los otros tres en cualquier orden. Como cada uno toca archivos distintos, **no hay conflictos**.
3. Antes de abrir cada PR: `git switch main && git pull origin main && git merge main` en tu rama, y
   vuelve a correr `mvn test`.
4. Al fusionar cada PR en GitHub usa **"Rebase and merge"** (no "Squash"): conserva cada commit
   individual en `main`, así el historial muestra el trabajo de los cuatro sin ensuciarse con merges.

## De aquí en adelante (trabajo continuo)
Cuando ya estén las pruebas, cada quien sigue mejorando **su** módulo con el mismo flujo
(`feature/<modulo>-<tarea>` o `fix/<modulo>-<tarea>`), un PR por tarea. Así el historial muestra el
trabajo de los cuatro, repartido y ordenado.
