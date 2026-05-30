# Codebase Map

## Refactor actual de Polen

La IA ya no vive solo en `PolenEntity`.

Extracciones activas:

- `entity/ai/activity/PolenQuietActivityController`
- `entity/ai/memory/PolenMemoryHandler`
- `entity/ai/mood/PolenMoodController`
- `entity/ai/routine/PolenRoutinePlanner`
- `entity/ai/safety/PolenSafetyEvaluator`
- `entity/ai/safety/PolenSafetyNavigator`
- `util/PolenNbtHelper`

Objetivo:

- mantener `PolenEntity` como coordinadora
- evitar que hobbies, mood, memoria y serializacion terminen mezclados en un solo archivo

## Paquetes principales

### `com.hivesandcolonies.polen`

- `Polen`
  - entrada principal del mod
- `PolenClient`
  - registro de renderers cliente

### `com.hivesandcolonies.polen.client`

- `PolenRenderer`
  - renderer base de Polen usando `HumanoidModel`

### `com.hivesandcolonies.polen.command`

- `PolenDebugCommands`
  - comandos `/polen ...` para inspección y pruebas

### `com.hivesandcolonies.polen.dialogue`

- `PolenDialogueManager`
  - selecciona líneas por capítulo

### `com.hivesandcolonies.polen.entity`

- `PolenEntity`
  - entidad principal
  - punto central de IA, mood, hobbies y memoria ligera

### `com.hivesandcolonies.polen.entity.ai`

- `PolenMood`
  - enum de estado emocional actual

### `com.hivesandcolonies.polen.entity.ai.goal`

- `PolenKeepDistanceGoal`
  - timidez ante jugadores demasiado cercanos
- `PolenRoutineGoal`
  - rutina contextual usando tiempo, clima y memoria de lugares
- `PolenIdleHobbyGoal`
  - dibujo y canto sin interacción directa
- `PolenCuriousInterestGoal`
  - interés por flores, colmenas y nidos de abejas

### `com.hivesandcolonies.polen.item`

- `TranslatableTooltipItem`
  - base para tooltips traducibles
- `PrincessSealItem`
- `PrincessLetterItem`
- `PolenJournalItem`

### `com.hivesandcolonies.polen.progression`

- `PolenAffinityLevels`
  - umbrales de afinidad
- `PolenAffinityManager`
  - acceso de alto nivel a afinidad
- `PolenChapterManager`
  - control de capítulos del mundo
- `PolenStoryFlag`
  - enum de flags narrativos
- `PolenStoryFlagsManager`
  - lectura/escritura de flags de historia
- `PolenAdvancementManager`
  - otorga advancements ligados a progreso

### `com.hivesandcolonies.polen.progression.player`

- `PolenPlayerRelationshipData`
  - DTO de afinidad, interacciones y flags por jugador
- `PolenPlayerRelationshipManager`
  - `SavedData` por jugador almacenado en overworld

### `com.hivesandcolonies.polen.progression.world`

- `PolenWorldStoryData`
  - DTO de capítulo, flags y estado de spawn
- `PolenWorldStorySavedData`
  - `SavedData` de historia global del mundo

### `com.hivesandcolonies.polen.registry`

- `ModItems`
- `ModEntities`
- `ModCreativeTabs`
- `ModEntityAttributes`

### `com.hivesandcolonies.polen.story`

- `PolenStoryEventManager`
  - secuencias narrativas de nombre revelado y refugio

## Recursos

### `src/main/resources/assets/polen/lang`

- `es_es.json`
- `en_us.json`

Contiene:

- nombres de ítems
- tooltips
- diálogos
- textos de advancements

### `src/main/resources/assets/polen/textures`

- `entity/polen.png`
- `item/*.png`

### `src/main/resources/assets/polen/models/item`

- modelos de ítems y spawn egg

### `src/main/resources/data/polen`

- advancements narrativos

## Flujo basico de una interaccion

1. El jugador interactúa con `PolenEntity`.
2. `PolenPlayerRelationshipManager` registra interacción.
3. `PolenEntity` consulta capítulo y afinidad.
4. Si se cumple una condición de evento, `PolenStoryEventManager` dispara secuencia.
5. Si no, `PolenDialogueManager` devuelve una línea normal.

## Flujo basico de IA

1. `PolenEntity` actualiza nombre, mood y memorias en `tick`.
2. Los goals compiten por prioridad.
3. La prioridad alta protege personalidad:
   - timidez
   - rutina contextual
   - hobbies
   - curiosidad
4. El renderer solo dibuja; la lógica vive en entidad y goals.
