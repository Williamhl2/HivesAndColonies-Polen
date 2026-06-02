# Polen AI

## Estado actual

La IA de Polen ya no se organiza como una coleccion de `goal` sueltos.
El flujo real ahora esta separado en capas pequenas y escalables:

1. `needs`
2. `intent`
3. `task arbitration`
4. `autonomous action`
5. `goal execution`
6. `gesture + client pose`

La idea es que cada feature nueva entre por una capa concreta en vez de inflar `PolenEntity` o la raiz de `entity/ai`.

## Mapa rapido

- `entity/ai/core/PolenAiFacade`
  - punto unico de entrada entre `PolenEntity` y la IA
  - tickea autonomia, quiet activity, magia persistente y gestos
- `entity/ai/brain/state/PolenAiState`
  - memoria espacial, needs, intent, task state, light management y danger memory
- `entity/ai/core/PolenAutonomyController`
  - ordena el tick lento de autonomia
- `entity/ai/brain/need/*`
  - estado y ajuste gradual de necesidades internas
- `entity/ai/brain/intent/*`
  - intencion dominante con razon y lock temporal
- `entity/ai/brain/task/*`
  - puente entre intent y goals con tarea actual, tarea deseada, estado, fallos recientes y recovery
- `entity/ai/brain/action/*`
  - planner de acciones autonomas de bajo ruido
- `entity/ai/expression/activity/*`
  - quiet activities, particulas y puentes con magia sutil
- `entity/ai/navigation/goal/*`
  - movimiento y reaccion fisica
- `entity/ai/navigation/search/*`
  - perfiles reutilizables de busqueda, shortlist, resolucion de alcanzabilidad y estado observable
- `entity/ai/navigation/safety/*`
  - evaluacion de spots, rutas de escape, lluvia, noche y refugio
- `entity/ai/ability/magic/*`
  - blink, attunement, reflection y gestion de la lampara
- `entity/ai/expression/gesture/*`
  - capa de gesto sincronizado para animacion
- `client/model/PolenModel`
  - modelo tipo player
- `client/animation/PolenGesturePoseApplier`
  - poses cliente derivadas del gesto actual

## Needs actuales

`PolenNeedState` mantiene cinco tensiones blandas:

- `SAFETY`
- `SOCIAL`
- `CURIOSITY`
- `REST`
- `MAGIC`

No son barras de jugador.
Son presiones internas que ayudan a que Polen mantenga continuidad entre decisiones.

## Intent actuales

`PolenIntentController` puede elegir:

- `SEEK_SAFETY`
- `KEEP_DISTANCE`
- `APPROACH_TRUSTED_PLAYER`
- `INVESTIGATE_INTEREST`
- `SEEK_REST`
- `QUIET_CREATION`
- `WANDER_SAFE`

Cada intent se bloquea por una ventana corta de ticks para evitar jitter.

## Task arbitration

`PolenTaskController` toma el intent dominante y lo convierte en una tarea observable.

La capa de task agrega:

- `desired task`
- `current task`
- `status`
- `note`
- `recent failed task`
- `recovery cooldown`

Con eso Polen deja de insistir en la misma accion fallida cada pocos ticks cuando el problema no es urgente.
Las tareas urgentes como `SEEK_SAFETY` y `KEEP_DISTANCE` ignoran ese cooldown.

## Autonomous actions

`PolenAutonomousActionPlanner` desacopla hobbies y microconductas de los goals.

Acciones actuales:

- `SING`
- `DRAW`
- `ATTUNE_SOURCE`
- `ILLUMINATE_AREA`
- `REFLECT`

Cada accion define:

- quiet activity asociada
- grupo de dialogo ambiental
- duracion minima
- duracion maxima

## Quiet activities

`PolenQuietActivityController` sincroniza la actividad visible y la conecta con magia sutil.

Actividades actuales:

- `none`
- `singing`
- `drawing`
- `attuning`
- `illuminating`
- `reflecting`

Notas:

- `attuning` usa source recordado o local
- `illuminating` coloca `polen_lantern`
- `reflecting` funciona cerca de resting spot o una luz manejada por Polen
- `restingPos` y `residence` ya no significan lo mismo

## Seguridad

`PolenSafetyNavigator` y `PolenSafetyEvaluator` ya no tratan todos los problemas como la misma "cueva mala".

Se distinguen tres niveles:

- `unsafe area`
  - lugar incomodo para rutina pasiva o hobbies
- `should seek safety`
  - lugar que exige salir activamente
- `unsafe dialogue`
  - linea ambiental reservada para contextos realmente opresivos

Casos actuales de seguridad:

- spot fisicamente malo
- oscuridad subterranea fuerte
- hostiles cercanos
- exposicion a lluvia
- noche en oscuridad sin un punto valido para iluminar

Comportamiento esperado:

- bajo lluvia Polen intenta ir a un spot con techo real
- de noche intenta llegar a un area plana donde pueda colocar luz
- si queda atascada puede usar `blink`
- el planner evita reusar el mismo spot actual como falso destino de escape

## Luz y magia

`PolenMagicController` cubre dos familias:

1. magia sutil durante quiet activity
2. magia utilitaria

Magia utilitaria actual:

- `blinkToSafety`
- `blinkToward`
- gestion de `polen_lantern`

Magia sutil actual:

- singing spell
- drawing spell
- attunement spell
- illumination spell
- reflection spell

Las particulas usan paleta verde y morado.

## Gestos y animacion

El gesto ya es una capa real de estado, no solo una pose hardcodeada.

`PolenGesture` guarda:

- `id`
- `animationKey`
- `looping`
- `suggestedDurationTicks`

`PolenGestureController` resuelve:

- gestos pasivos por quiet activity
- gestos reactivos por task o mood
- triggers cortos para goals concretos

Gestos actuales:

- `IDLE`
- `SINGING`
- `DRAWING`
- `ATTUNING`
- `ILLUMINATING`
- `REFLECTING`
- `CURIOUS`
- `APPROACHING`
- `WITHDRAWN`
- `STARTLED`

Esto deja lista una futura integracion con una capa mas rica de animacion sin rehacer la IA.

## Modelo cliente

`PolenModel` ahora usa `PlayerModel`, no `HumanoidModel` plano.

Consecuencias:

- mejor lectura visual como companera y no como mob generico
- capas externas sincronizadas con cabeza, mangas y piernas
- poses mas compatibles con una futura integracion estilo Fresh Animations

## Goals actuales

Los goals principales ya no dependen solo de `intent`.
Ahora reportan `planned`, `active`, `completed` o `failed` a la capa de task.

- `PolenKeepDistanceGoal`
  - se aleja de jugadores no confiables
- `PolenApproachTrustedPlayerGoal`
  - se acerca a jugadores con confianza suficiente
- `PolenCuriousInterestGoal`
  - inspecciona flores, colmenas y source
- `PolenRoutineGoal`
  - movimiento contextual para `SEEK_REST` y `QUIET_CREATION`
- `PolenIdleHobbyGoal`
  - ejecuta hobbies y quiet activities
- `PolenSeekSafetyGoal`
  - refugio, escape, repath y blink
- `PolenSafeStrollGoal`
  - paseo ligero solo en contexto estable

## Hogar y residencia

`entity/ai/world/home` ahora separa hogar de descanso improvisado:

- `PolenResidenceValidator`
  - valida techo real, cama, acceso y espacio habitable
- `PolenHomeManager`
  - guarda la residencia elegida y la vuelve preferible para descanso y refugio
- `PolenResidenceStage`
  - deja preparada la progresion: `BORROWED_SHELTER`, `OWN_SPACE`, `INTEGRATED_RESIDENCE`, `LIVING_ARCHIVE`

Narrativamente:

- `settlement_charm` marca descanso seguro
- `residence_charm` marca pertenencia

## Items y escalabilidad

El arbol de items ya esta preparado para crecer sin mezclar todo:

- `item/base/*`
- `item/meta/*`
- `item/material/*`
- `item/focus/*`
- `item/colony/*`
- `item/accessory/*`

`item/accessory/*` ya contiene la base para:

- slot
- target
- bonus
- item reusable

Esto prepara anillos, collares, cinturones y piezas que Polen pueda equipar o que el jugador pueda usar.

## Debug

La capa de debug sigue viva en:

- `entity/ai/debug/PolenAiDebugInspector`
- `entity/ai/debug/PolenAiDebugSnapshot`

Sirve para inspeccionar:

- mood y su razon
- intent y su razon
- quiet activity
- needs
- estado de seguridad
- memoria espacial relevante

## Regla de mantenimiento

Si una feature nueva puede vivir como:

- planner
- controller
- goal
- debug helper
- item family
- gesture

entonces no debe agregarse directo a `PolenEntity`.

La entidad debe seguir siendo coordinadora de estado y ciclo de vida, no un archivo omnibus.
