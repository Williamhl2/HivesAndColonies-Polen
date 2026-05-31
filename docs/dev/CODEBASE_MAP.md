# Codebase Map

## Ultimas extracciones

Se agregaron estas piezas fuera de `PolenEntity`:

- `entity/ai/PolenAiFacade`
- `entity/PolenInteractionController`
- `entity/PolenAmbientDialogueController`
- `entity/PolenDangerMemoryTracker`
- `entity/PolenGoalRegistry`
- `entity/ai/autonomy/PolenAutonomyController`
- `entity/ai/need/*`
- `entity/ai/intent/*`
- `entity/ai/interest/*`
- `dialogue/PolenSpeakerResolver`
- `dialogue/PolenChapterDialogueResolver`
- `dialogue/PolenAmbientToneResolver`
- `dialogue/PolenAmbientDialogueResolver`

Tambien existe una capa de debug de IA para inspeccion local:

- `entity/ai/debug/PolenAiDebugInspector`
- `entity/ai/debug/PolenAiDebugSnapshot`

## Refactor actual de Polen

La IA ya no vive solo en `PolenEntity`.

Extracciones activas:

- `entity/PolenInteractionController`
- `entity/PolenAmbientDialogueController`
- `entity/PolenDangerMemoryTracker`
- `entity/PolenGoalRegistry`
- `entity/ai/activity/PolenQuietActivityController`
- `entity/ai/PolenAiFacade`
 - fachada central entre `PolenEntity` y la capa de IA
- `entity/ai/state/PolenAiState`
 - contenedor persistente del estado compartido de IA
- `entity/ai/autonomy/PolenAutonomyController`
 - coordina el tick lento de autonomia
- `entity/ai/interest/PolenInterestLocator`
 - localiza intereses recordados y locales
- `entity/ai/intent/PolenIntent`
 - enum de intenciones de alto nivel
- `entity/ai/intent/PolenIntentState`
 - estado persistente de intencion actual
- `entity/ai/intent/PolenIntentController`
 - selecciona la intencion dominante
- `entity/ai/memory/PolenMemoryHandler`
- `entity/ai/magic/PolenMagicController`
- `entity/ai/mood/PolenMoodController`
- `entity/ai/need/PolenNeed`
 - enum de necesidades internas
- `entity/ai/need/PolenNeedState`
 - estado persistente de necesidades
- `entity/ai/need/PolenNeedController`
 - ajusta necesidades segun entorno, social y magia
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
  - contenedor de estado sincronizado y ciclo base
- `PolenInteractionController`
  - interaccion con jugador y reveal del nombre
- `PolenAmbientDialogueController`
  - emision de dialogos ambientales cercanos
- `PolenDangerMemoryTracker`
  - memoria temporal de peligro y persistencia asociada
- `PolenGoalRegistry`
  - orden centralizado de goals de Polen

### `com.hivesandcolonies.polen.entity.ai`

- `PolenAiFacade`
  - entrada unica de wiring entre entidad y subsistemas de IA
- `PolenMood`
  - enum de estado emocional actual

### `com.hivesandcolonies.polen.entity.ai.state`

- `PolenAiState`
  - memoria espacial, danger memory, needs e intent en un solo contenedor persistente

### `com.hivesandcolonies.polen.entity.ai.autonomy`

- `PolenAutonomyController`
  - ordena el flujo `needs -> intent -> mood -> memory seeding`

### `com.hivesandcolonies.polen.entity.ai.interest`

- `PolenInterestType`
  - tipo de interes encontrado
- `PolenInterestTarget`
  - destino tipado reutilizable
- `PolenInterestLocator`
  - escaneo local y reutilizacion de intereses recordados

### `com.hivesandcolonies.polen.entity.ai.intent`

- `PolenIntent`
  - enum de voluntad inmediata de Polen
- `PolenIntentState`
  - estado persistido de intencion y lock temporal
- `PolenIntentController`
  - politica compacta de seleccion de intencion

### `com.hivesandcolonies.polen.entity.ai.magic`

- `PolenMagicController`
  - blink de seguridad con FX y sonido
  - microhechizos de quiet activity ligados al eje Ars Nouveau

### `com.hivesandcolonies.polen.entity.ai.goal`

- `PolenKeepDistanceGoal`
  - timidez ante jugadores demasiado cercanos
- `PolenRoutineGoal`
  - rutina contextual usando tiempo, clima y memoria de lugares
- `PolenIdleHobbyGoal`
  - dibujo y canto sin interacción directa
- `PolenCuriousInterestGoal`
  - interes por flores, colmenas y source

### `com.hivesandcolonies.polen.entity.ai.need`

- `PolenNeed`
  - enum de necesidades internas
- `PolenNeedState`
  - valores persistentes de seguridad, social, curiosidad, descanso y magia
- `PolenNeedController`
  - deriva presiones internas desde contexto y progresion

### `com.hivesandcolonies.polen.item`

- `base/TranslatableTooltipItem`
  - base para tooltips traducibles
- `base/PolenTypedItem`
  - base con familia, etapa de progresion y marca de unicidad
- `base/PolenLoreItem`
- `base/PolenMaterialItem`
- `base/PolenUsableFocusItem`
- `base/PolenColonyItem`
- `meta/PolenItemFamily`
- `meta/PolenProgressionStage`
- `meta/PolenItemTags`
- `story/PrincessSealItem`
- `story/PrincessLetterItem`
- `story/PolenJournalItem`
- `material/RoyalPollenItem`
- `material/SourceTouchedPetalItem`
- `material/ResonantWaxItem`
- `focus/BloomFocusItem`
- `colony/SettlementCharmItem`
- `interaction/PolenItemInteractionController`

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
- `recipe/*`
  - adquisicion base de items jugables
- `tags/item/*`
  - clasificacion por familia para sistemas futuros

## Flujo basico de una interaccion

1. El jugador interactúa con `PolenEntity`.
2. `PolenPlayerRelationshipManager` registra interacción.
3. `PolenEntity` consulta capítulo y afinidad.
4. Si se cumple una condición de evento, `PolenStoryEventManager` dispara secuencia.
5. Si no, `PolenDialogueManager` devuelve una línea normal.

## Flujo basico de IA

1. `PolenEntity` delega el tick lento a `PolenAutonomyController`.
2. `PolenNeedController` ajusta necesidades internas.
3. `PolenIntentController` decide la intencion dominante.
4. Los goals solo compiten si la intencion actual los habilita.
5. `PolenMoodController` traduce el estado interno a expresion emocional visible.
6. El renderer solo dibuja; la logica vive en controladores, planners y goals.
