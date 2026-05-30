# Hives And Colonies: Polen

Mod narrativo en NeoForge centrado en Polen, una NPC con progresión de afinidad, capítulos de historia y comportamiento propio.

Estado actual: desarrollo temprano.

## Que incluye hoy

- Polen como entidad persistente con nombre revelado por progreso.
- Sistema base de afinidad por jugador.
- Sistema base de capítulos y flags de historia por mundo.
- Ítems narrativos iniciales.
- Diálogos y eventos de historia tempranos.
- IA inicial de personalidad para Polen:
  - timidez ante desconocidos
  - curiosidad por flores y colmenas
  - hobbies pasivos de dibujo y canto
  - mood básico, rutina contextual y memoria simple de lugares

## Documentacion

- [Indice de documentacion](docs/README.md)
- [Vision narrativa en español](docs/es/STORY.md)
- [Capitulos narrativos](docs/es/NARRATIVE_CHAPTERS.md)
- [Arquitectura tecnica](docs/dev/PROJECT_OVERVIEW.md)
- [IA de Polen](docs/dev/POLEN_AI.md)

## Estructura rapida

- `src/main/java/com/hivesandcolonies/polen`
  - entrada del mod, registros, cliente, entidad, progresión, historia y comandos debug
- `src/main/resources/assets/polen`
  - `lang`, texturas, modelos e identificadores visuales
- `src/main/resources/data/polen`
  - advancements y contenido de datos
- `docs/es`
  - canon narrativo y estructura de capítulos
- `docs/dev`
  - documentación técnica para desarrolladores

## Desarrollo

Compilar:

```powershell
./gradlew.bat compileJava
```

Comandos debug útiles en juego:

```text
/polen affinity get
/polen chapter get
/polen flag get
/polen relationship get
/polen worlddata get
/polen ai get
```

## Objetivo del proyecto

La meta no es crear un aldeano con diálogos.

La meta es construir un personaje persistente, legible y expandible, cuya narrativa, comportamiento y progresión se sientan conectados.
