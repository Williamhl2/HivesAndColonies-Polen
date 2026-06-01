# Project Overview

## Que es este proyecto

`Hives And Colonies: Polen` es un mod de NeoForge para Minecraft 1.21.1 centrado en una NPC narrativa llamada Polen.

El proyecto mezcla cuatro capas:

1. Entidad y comportamiento de Polen.
2. Progresión narrativa por capítulos.
3. Relación jugador <-> Polen.
4. Contenido narrativo base: ítems, diálogos, eventos y advancements.

## Objetivo tecnico

La base técnica debe permitir que Polen evolucione como personaje sin convertir el mod en una colección de triggers sueltos.

Eso implica:

- datos de mundo para historia compartida
- datos por jugador para afinidad y relación
- IA legible y extensible
- recursos narrativos desacoplados en `lang` y `data`

## Stack

- Java 21
- NeoForge
- Gradle
- Recursos estándar de Minecraft: `assets` y `data`

## Puntos de entrada principales

- [Polen.java](../../src/main/java/com/hivesandcolonies/polen/Polen.java)
  - entrada del mod
  - registra ítems, creative tab, entidades y comandos debug
- [PolenClient.java](../../src/main/java/com/hivesandcolonies/polen/PolenClient.java)
  - bootstrap cliente para renderers

## Subsistemas

### Entidad

- `entity/PolenEntity.java`
- `client/PolenRenderer.java`
- `entity/ai/...`

Responsabilidad:

- presencia física de Polen en mundo
- nombre visible según progreso
- goals de IA
- estado de actividad tranquila, mood y memoria ligera

### Progresion

- `progression/PolenChapterManager.java`
- `progression/PolenStoryFlagsManager.java`
- `progression/PolenAffinityManager.java`
- `progression/player/...`
- `progression/world/...`

Responsabilidad:

- capítulos de historia por mundo
- flags de progreso narrativo
- afinidad por jugador
- persistencia en `SavedData`

### Historia y dialogo

- `dialogue/PolenDialogueManager.java`
- `story/PolenStoryEventManager.java`

Responsabilidad:

- líneas de diálogo normales
- líneas ambientales contextuales por jugador
- secuencias de eventos narrativos
- avance de flags y advancements

### Contenido

- `registry/ModItems.java`
- `registry/ModEntities.java`
- `registry/ModCreativeTabs.java`
- `registry/ModEntityAttributes.java`
- `item/...`
- `src/main/resources/assets/polen/...`
- `src/main/resources/data/polen/...`

Responsabilidad:

- registrar contenido
- tooltips traducibles
- assets
- advancements

### Debug y soporte de desarrollo

- `command/PolenDebugCommands.java`

Responsabilidad:

- inspección manual de afinidad, capítulos, flags, relación, world data e IA

## Reglas de diseño que ya existen

- Polen no debe sentirse como un aldeano genérico.
- La narrativa debe llegar antes que la épica.
- La progresión del jugador y la conducta de Polen deben estar conectadas.
- Los sistemas deben ser expandibles por capas, no por hardcode disperso.

## Donde empezar segun la tarea

- Cambios de IA: [POLEN_AI.md](POLEN_AI.md)
- Cambios de progreso: [PROGRESSION_SYSTEM.md](PROGRESSION_SYSTEM.md)
- Cambios de assets y contenido: [CONTENT_PIPELINE.md](CONTENT_PIPELINE.md)
- Cambios narrativos: `docs/es`
