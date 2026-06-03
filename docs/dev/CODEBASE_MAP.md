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

### `entity/ai/brain/state`

- `PolenAiState`
  - memoria espacial, needs, intent y estado de luz

### `entity/ai/core`

- `PolenAutonomyController`
  - tick lento de autonomia

### `entity/ai/brain/need`

- `PolenNeed`
- `PolenNeedState`
- `PolenNeedSnapshot`
- `PolenNeedController`

### `entity/ai/brain/intent`

- `PolenIntent`
- `PolenIntentState`
- `PolenIntentSnapshot`
- `PolenIntentController`

### `entity/ai/brain/task`

- `PolenTaskType`
- `PolenTaskStatus`
- `PolenTaskState`
- `PolenTaskSnapshot`
- `PolenTaskController`

### `entity/ai/brain/mood`

- `PolenMood`
- `PolenMoodAnalysis`
- `PolenMoodController`

### `entity/ai/brain/action`

- `PolenAutonomousActionType`
- `PolenAutonomousActionPlan`
- `PolenAutonomousActionPlanner`

### `entity/ai/expression/activity`

- `PolenQuietActivityController`

### `entity/ai/expression/gesture`

- `PolenGesture`
- `PolenGestureController`

### `entity/ai/brain/interest`

- `PolenInterestType`
- `PolenInterestTarget`
- `PolenInterestLocator`

### `entity/ai/brain/memory`

- `PolenMemoryHandler`

### `entity/ai/world/affordance`

- `PolenAffordanceType`
- `PolenAffordanceTarget`
- `PolenAffordanceResolver`

Responsabilidad:

- traducir lugares concretos a usos semanticos como refugio, descanso, luz, interes o residencia

### `entity/ai/world/observation`

- `PolenObservationFocus`
- `PolenObservationDisposition`
- `PolenObservationController`

Responsabilidad:

- registrar que esta observando Polen y con que disposicion

### `entity/ai/world/comfort`

- `PolenComfortCategory`
- `PolenComfortProfile`
- `PolenComfortRank`
- `PolenComfortRule`
- `PolenComfortReport`
- `PolenComfortRules`
- `PolenComfortSignal`
- `PolenComfortEvaluator`

Responsabilidad:

- puntuar lugares por comodidad semantica y no solo por distancia o navegacion

### `entity/ai/world/identity`

- `PolenAffinity`
- `PolenWorldAffinity`
- `PolenIdentity`
- `PolenAffinityFactory`

Responsabilidad:

- identidad de Polen, afinidades base y sesgos suaves de comportamiento

### `entity/ai/world/interests`

- `PolenInterest`
- `PolenInterestProfile`
- `PolenInterestGenerator`
- `PolenAffinityBehaviorHooks`

Responsabilidad:

- generar intereses a partir de afinidad y contexto del mundo

### `entity/ai/ability/magic`

- `PolenMagicController`
  - blink
  - attunement
  - reflection
  - managed light

### `entity/ai/brain/routine`

- `PolenRoutinePlanner`
  - targets de rutina y quiet creation

### `entity/ai/navigation/search`

- `PolenSearchPlanner`
- `PolenSearchProfile`
- `PolenSearchDomain`
- `PolenSearchType`
- `PolenSearchStatus`
- `PolenSpotSelectionHelper`
- `light/PolenLightSpotHelper`

### `entity/ai/navigation/safety`

- `PolenSafetyEvaluator`
- `PolenSafetyNavigator`
- `PolenDangerMemoryMath`

### `entity/ai/world/home`

- `PolenResidenceStage`
- `PolenResidenceTarget`
- `PolenResidenceValidation`
- `PolenResidenceValidator`
- `PolenHomeManager`

### `entity/ai/world/story`

- `PolenWorldMemory`
- `PolenStoryStage`

Responsabilidad:

- puentes entre memoria narrativa local y lectura de etapa del mundo

### `entity/ai/navigation/goal`

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

### `item/affinity`

- `AffinityCharmItem`

### `compat/curios`

- `PolenCuriosBridge`

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

- hobbies y microconductas nuevas entran por `entity/ai/brain/action`
- ejecucion visible entra por `activity`, `magic`, `gesture` o `goal`

### Hogar

- residencia y pertenencia entran por `entity/ai/world/home`
- descanso improvisado sigue en `routine` y `restingPos`
- no mezclar ambos conceptos en `item` o `goal`

### Mundo semantico

- affordance, observation, comfort, identity e interests deben vivir en `entity/ai/world/*`
- si una feature describe como Polen entiende un lugar, no debe caer por accidente en `goal`, `magic` o `entity`

### Accesorios

- toda nueva pieza reusable debe apoyarse en `item/accessory`
- efectos especiales de accesorios no deben vivir embebidos en el item si pueden aislarse
- integraciones de equipamiento externo como `Curios` deben quedar en `compat/*`

### PolenEntity

- solo debe conservar estado sincronizado, persistencia y ciclo base
- no debe volver a absorber planners o logica de decision
