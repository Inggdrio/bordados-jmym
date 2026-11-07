# Cómo crear el repositorio en GitHub y empezar a trabajar en equipo

Esto lo hace **una sola persona** (el dueño del repo: **Andrés**, que lo creará en su cuenta de
GitHub). Los demás entran como colaboradores en el paso 3.

## 1. Crear el repositorio (desde esta carpeta)

```bash
cd bordados-jmym

git init
git add .
git commit -m "chore(repo): estructura inicial del proyecto con pruebas y CI"

git branch -M main
git remote add origin https://github.com/AndresContrerasss/bordados-jmym.git
git push -u origin main
```

> Antes crea el repositorio vacío en GitHub: **New repository → nombre `bordados-jmym` → sin README**
> (porque este proyecto ya lo trae). Si GitHub te pide usuario/clave, usa un **token** (Settings →
> Developer settings → Personal access tokens) o la app **GitHub CLI** (`gh auth login`).

## 2. Verificar que la CI quedó activa
Entra a la pestaña **Actions** del repositorio en GitHub. Debió correr el flujo *CI* y quedar en
verde (compila y pasa `mvn test`).

## 3. Agregar a los otros integrantes
En GitHub: **Settings → Collaborators → Add people** e invita a German, Samir y Sebastián con sus
usuarios de GitHub. Cada uno acepta la invitación por correo.

## 4. Proteger la rama `main` (recomendado)
**Settings → Branches → Add branch ruleset** (o *Branch protection rule*) para `main`:
- ✅ Require a pull request before merging (al menos **1 aprobación**).
- ✅ Require status checks to pass → marca el check **CI**.
- ✅ Do not allow bypassing the above settings.

Así nadie puede empujar directo a `main`: todo entra por Pull Request revisado y con las pruebas
en verde. Es exactamente lo que pide la rúbrica de "buenas prácticas".

## 5. Cada integrante empieza su primera tarea
Cada uno clona, se identifica y trabaja en **su** módulo (ver `docs/REPARTO_DE_TRABAJO.md`).
Ejemplo de un primer aporte real por persona:

```bash
# Andrés — ajusta algo en órdenes
git switch -c feature/ordenes-validacion-fecha
#   (edita, agrega su prueba)
git add .
git commit -m "test(ordenes): agrega casos de borde para el avance de estado"
git push -u origin feature/ordenes-validacion-fecha
#   abre el Pull Request en GitHub
```

La idea es que el historial muestre commits de **los cuatro**, cada uno en su módulo, entrando a
`main` por Pull Request. Eso es lo que evalúa el profesor: trabajo distribuido y ordenado.

---

## Comandos del día a día (chuleta)

```bash
git switch main && git pull origin main      # ponerse al día
git switch -c feature/<modulo>-<tarea>       # nueva rama
mvn test                                      # probar antes de subir
git add -A && git commit -m "feat(<modulo>): ..."
git push -u origin feature/<modulo>-<tarea>  # subir y abrir PR
```
