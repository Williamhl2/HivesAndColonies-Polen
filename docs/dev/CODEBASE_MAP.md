# Codebase Map

## Direccion actual

El proyecto esta migrando a una estructura donde Polen se construye por capas pequenas:

- entidad y wiring
- IA por subdominio
- render/animacion cliente
- items por familia
- registro y recursos desacoplados

La raiz de `entity/ai` debe mantenerse limpia.
Cada nueva capacidad debe caer en un subdirectorio con responsabilidad clara.

## Paquetes principales

### `com.hivesandcolonies.polen`

- `Polen`
  - entrada principal del mod
- `PolenClient`
  - registro cliente

### `com.hivesandcolonies.polen.client`

- `PolenRenderer`
  - renderer de Polen

### `com.hivesandcolonies.polen.client.model`

- `PolenModel`
  - modelo basado en `PlayerModel`

### `com.hivesandcolonies.polen.client.animation`

- `PolenGesturePoseApplier`
  - traduce gestos a poses cliente

### `com.hivesandcolonies.polen.command`

- `PolenDebugCommands`
  - comandos `/polen ...`

### `com.hivesandcolonies.polen.dialogue`

- `PolenDialogueManager`
- `PolenSpeakerResolver`
- `PolenChapterDialogueResolver`
- `PolenAmbientToneResolver`
- `PolenAmbientDialogueResolver`

### `com.hivesandcolonies.polen.entity`

- `PolenEntity`
  - entidad principal y estado sincronizado
- `PolenInteractionController`
  - interaccion con jugador
- `PolenAmbientDialogueController`
  - emision de lineas ambientales
- `PolenDangerMemoryTracker`
  - memoria de spots peligrosos
- `PolenGoalRegistry`
  - prioridades de goals

## IA de Polen

### `entity/ai/core`

- `PolenAiFacade`
  - fachada central del wiring de IA

### `entity/ai/state`

- `PolenAiState`
  - memoria espacial, needs, intent y estado de luz

### `entity/ai/autonomy`

- `PolenAutonomyController`
  - tick lento de autonomia

### `entity/ai/need`

- `PolenNeed`
- `PolenNeedState`
- `PolenNeedSnapshot`
- `PolenNeedController`

### `entity/ai/intent`

- `PolenIntent`
- `PolenIntentState`
- `PolenIntentSnapshot`
- `PolenIntentController`

### `entity/ai/mood`

- `PolenMood`
- `PolenMoodAnalysis`
- `PolenMoodController`

### `entity/ai/action`

- `PolenAutonomousActionType`
- `PolenAutonomousActionPlan`
- `PolenAutonomousActionPlanner`

### `entity/ai/activity`

- `PolenQuietActivityController`

### `entity/ai/gesture`

- `PolenGesture`
- `PolenGestureController`

### `entity/ai/interest`

- `PolenInterestType`
- `PolenInterestTarget`
- `PolenInterestLocator`

### `entity/ai/memory`

- `PolenMemoryHandler`

### `entity/ai/magic`

- `PolenMagicController`
  - blink
  - attunement
  - reflection
  - managed light

### `entity/ai/routine`

- `PolenRoutinePlanner`
  - targets de rutina y quiet creation

### `entity/ai/safety`

- `PolenSafetyEvaluator`
- `PolenSafetyNavigator`
- `PolenDangerMemoryMath`

### `entity/ai/goal`

- `PolenKeepDistanceGoal`
- `PolenApproachTrustedPlayerGoal`
- `PolenCuriousInterestGoal`
- `PolenRoutineGoal`
- `PolenIdleHobbyGoal`
- `PolenSeekSafetyGoal`
- `PolenSafeStrollGoal`

### `entity/ai/debug`

- `PolenAiDebugInspector`
- `PolenAiDebugSnapshot`

## Items

### `item/base`

- `TranslatableTooltipItem`
- `PolenTypedItem`
- `PolenLoreItem`
- `PolenMaterialItem`
- `PolenUsableFocusItem`
- `PolenColonyItem`

### `item/meta`

- `PolenItemFamily`
- `PolenProgressionStage`
- `PolenItemTags`

### `item/material`

- materiales base de Polen

### `item/focus`

- focos usables

### `item/colony`

- items de asentamiento y progreso

### `item/accessory`

- `PolenAccessorySlot`
- `PolenAccessoryTarget`
- `PolenAccessoryBonusType`
- `PolenAccessoryBonus`
- `PolenAccessoryItem`

## Progression

### `progression`

- `PolenAffinityLevels`
- `PolenAffinityManager`
- `PolenChapterManager`
- `PolenStoryFlag`
- `PolenStoryFlagsManager`
- `PolenAdvancementManager`

### `progression/player`

- `PolenPlayerRelationshipData`
- `PolenPlayerRelationshipManager`

### `progression/world`

- `PolenWorldStoryData`
- `PolenWorldStorySavedData`

## Registry

### `registry`

- `ModItems`
- `ModBlocks`
- `ModEntities`
- `ModCreativeTabs`
- `ModEntityAttributes`

## Story

### `story`

- `PolenStoryEventManager`
- memoria narrativa y eventos de descubrimiento

## Recursos

### `assets/polen/lang`

- `es_es.json`
- `en_us.json`

### `assets/polen/blockstates`

- blockstates del mod

### `assets/polen/models/block`

- modelos de bloques

### `assets/polen/models/item`

- modelos de items

### `data/polen/tags/item`

- familias de item
- incluye `accessory_items`

### `data/polen/loot_table`

- loot tables del mod

## Reglas de crecimiento

### IA

- no agregar clases nuevas en la raiz de `entity/ai`
- usar subdirectorios por dominio
- si una capacidad es visible en cliente, separar estado servidor de pose cliente

### Acciones

- hobbies y microconductas nuevas entran por `entity/ai/action`
- ejecucion visible entra por `activity`, `magic`, `gesture` o `goal`

### Accesorios

- toda nueva pieza reusable debe apoyarse en `item/accessory`
- efectos especiales de accesorios no deben vivir embebidos en el item si pueden aislarse

### PolenEntity

- solo debe conservar estado sincronizado, persistencia y ciclo base
- no debe volver a absorber planners o logica de decision
