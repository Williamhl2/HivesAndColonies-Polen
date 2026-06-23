# Character Architecture

## Problema actual

La estructura actual ya esta ordenada por subdominios tecnicos, pero casi todo sigue estando nombrado y modelado como si el mod solo fuera a tener a Polen.

Eso hoy funciona porque solo existe un personaje vivo dentro del build.

Va a empezar a romperse cuando entren personajes como `Luna` o `Vanilla`, porque:

- duplicariamos controladores, resolvers, estados e items base que en realidad describen sistemas reutilizables
- mezclariamos comportamiento comun de companions con identidad canonica de Polen
- terminariamos con una falsa capa "generic" hecha a la fuerza si intentamos abstraer demasiado tarde

La meta no es borrar a Polen del diseño.

La meta es separar:

- framework comun del mod
- runtime reutilizable de companions
- implementaciones especificas de cada personaje

## Regla central

Si una clase sigue teniendo sentido con `Luna` o `Vanilla` sin renombrar conceptos canonicos, debe vivir fuera de `Polen`.

Si una clase depende de:

- recuerdos de Polen
- afinidades propias de Polen
- magia propia de Polen
- dialogo propio de Polen
- arco narrativo propio de Polen

entonces debe vivir en el paquete del personaje.

## Estructura objetivo

```text
src/main/java/com/hivesandcolonies/characters/
  bootstrap/
    Characters.java
    CharactersClient.java
    registry/
      CharactersRegistryBootstrap.java

  common/
    ai/
      runtime/
      navigation/
      world/
      debug/
    client/
      profile/
      animation/
      render/
    dialogue/
      runtime/
      format/
    entity/
      companion/
      equipment/
      sync/
    item/
      base/
      accessory/
    progression/
      player/
      world/
    story/
      runtime/
    util/

  character/
    polen/
      entity/
      ai/
      dialogue/
      story/
      progression/
      item/
      client/
      registry/
      world/

    luna/
      entity/
      ai/
      dialogue/
      story/
      progression/
      item/
      client/
      registry/
      world/

    vanilla/
      entity/
      ai/
      dialogue/
      story/
      progression/
      item/
      client/
      registry/
      world/

  integration/
    curios/
```

## Que entra en `bootstrap`

`bootstrap` solo debe conocer:

- el `mod_id`
- el arranque del mod
- la composicion de registries
- el cableado de personajes disponibles

No debe contener logica de Polen, Luna o Vanilla.

## Que entra en `common`

`common` es para piezas reutilizables entre companions.

Debe poder usarse sin importar si el personaje es:

- Polen
- Luna
- Vanilla
- otro que aun no existe

Ejemplos correctos de `common`:

- estado generico de task, cooldowns, search y observation
- runtime de dialogue formatting
- helpers de NBT
- inventario de equipment
- clases base de accessories
- persistencia world/player por `characterId`
- goal bases o utilidades de navegacion compartidas

Ejemplos que no deben entrar en `common`:

- `PolenWorldAffinity`
- recuerdos ligados a flores o colmenas de Polen
- lineas de dialogo de name reveal
- magia blink propia de Polen
- item ids como `polen_lantern`

## Que entra en `character/<id>`

Cada personaje debe tener su implementacion completa bajo su propio paquete.

Eso incluye:

- entidad concreta
- politicas de IA
- dialogos y resolvers
- story flags y memories propios
- items y assets propios
- renderer, model y profile view especificos
- registrars del personaje

La idea es que agregar `Luna` o `Vanilla` no exija tocar el paquete de `Polen`, salvo para integracion narrativa compartida si realmente existe.

## Frontera real de la IA

La IA no debe generalizarse completa de una vez.

La separacion correcta es:

### Runtime comun

Debe vivir en `common/ai/*`.

Esto incluye:

- pipeline `need -> intent -> task -> goal`
- contenedores de estado
- cooldowns
- recovery ante fallos
- search planner
- observation state
- comfort evaluation engine
- home/residence primitives
- debug snapshot base

### Politica por personaje

Debe vivir en `character/<id>/ai/*`.

Esto incluye:

- tipos concretos de need
- intents concretos
- moods concretos
- actividades tranquilas concretas
- affordances que el personaje reconoce como importantes
- reglas de comfort segun personalidad
- abilities especiales
- heuristicas de seguridad, curiosidad y socializacion

## Que de la IA actual parece comun

Hoy, por como esta escrita la IA, estas piezas son candidatas claras a extraccion:

- `PolenTaskState`
- `PolenTaskSnapshot`
- `PolenTaskStatus`
- `PolenSearchPlanner`
- `PolenSearchProfile`
- `PolenSearchStatus`
- `PolenSpotSelectionHelper`
- `PolenObservationController`
- `PolenComfortEvaluator`
- `PolenResidenceValidation`
- `PolenEquipmentInventory`
- `TranslatableTooltipItem`

No significa moverlas tal cual.

Significa redisenarlas como tipos sin marca Polen.

## Que de la IA actual debe seguir siendo especifico

Estas piezas todavia pertenecen a `character/polen`:

- `PolenMood`
- `PolenIntent`
- `PolenNeed`
- `PolenWorldAffinity`
- `PolenAffinityFactory`
- `PolenMemoryType`
- `PolenMagicController`
- `PolenDialogueManager`
- `PolenStoryEventManager`
- `PolenRoutinePlanner`

La razon es simple:

- describen identidad
- describen fantasia jugable
- describen narrativa

No conviene volverlas generic solo porque existe la posibilidad futura de otro personaje.

## Split recomendado para `PolenAiState`

`PolenAiState` hoy mezcla tres tipos de cosas:

1. runtime reutilizable
2. semantica de companion
3. memoria especifica de Polen

La estructura objetivo deberia ser algo asi:

```text
common/ai/runtime/CharacterAiRuntimeState
  - task state
  - cooldowns
  - search state
  - observation state
  - debug state

common/ai/runtime/CharacterHomeState
  - residence anchor
  - residence use pos
  - residence stage

character/polen/ai/state/PolenMemoryState
  - favorite flower
  - favorite hive
  - favorite source
  - active light
  - Polen-specific remembered spots
```

La abstraccion correcta no es "hacer `PolenAiState` mas grande y parametrizable".

La abstraccion correcta es romperlo en capas.

## Split recomendado para `PolenEntity`

`PolenEntity` deberia terminar separada entre:

```text
common/entity/companion/BaseCharacterEntity
  - synced state generico
  - persistencia comun
  - interaction hooks
  - equipment inventory
  - client/server tick routing

character/polen/entity/PolenEntity
  - display name logic
  - Polen affinity wiring
  - Polen-specific save keys
  - Polen-specific behavior bridges
```

No hace falta llegar a esa base en un paso.

Pero si conviene empezar con esa direccion.

## Split recomendado para dialogo

La capa de dialogo deberia quedar asi:

```text
common/dialogue/runtime/
  DialogueComposer
  DialogueLineSelector
  CharacterSpeakerFormatter

character/polen/dialogue/
  PolenDialogueCatalog
  PolenAmbientDialogueResolver
  PolenChapterDialogueResolver
  PolenDialogueSituationResolver

character/luna/dialogue/
  LunaDialogueCatalog
  LunaAmbientDialogueResolver
  LunaChapterDialogueResolver
```

Lo comun compone y formatea.
Lo especifico decide:

- que situaciones existen
- que llaves usar
- como habla ese personaje

## Split recomendado para progresion

La progresion debe separarse en:

### Compartido

- persistencia world/player por personaje
- repositorios
- llaves de acceso
- utilidades para unlocks y consulta

### Especifico

- `PolenStoryFlag`
- `PolenChapterManager`
- `PolenAffinityManager`
- memories y eventos de Polen

Cuando llegue `Luna`, no deberia crearse otro `SavedData` entero copiado.
Deberia agregarse otra capa de datos encima del mismo runtime de persistence.

## Recursos y data packs

El namespace del mod sigue siendo `hc_characters`.

La separacion por personaje debe darse en el path.

### Objetivo para assets

```text
src/main/resources/assets/hc_characters/
  textures/
    character/polen/
    character/luna/
    character/vanilla/
    item/polen/
    item/luna/
    item/vanilla/

  models/
    item/polen/
    item/luna/
    item/vanilla/
    block/polen/

  lang_parts/<locale>/characters/
    polen/
      ambient.json
      chapters.json
      events.json
      memories.json
    lucy/
      scene.json
    soa/
      interaction.json
      encounters.json
```

### Objetivo para data

```text
src/main/resources/data/hc_characters/
  advancement/
    polen/story/
    luna/story/
    vanilla/story/

  recipe/
    polen/
    luna/
    vanilla/

  loot_table/
    polen/
    luna/

  tags/
    item/common/
    item/polen/
    item/luna/
```

Esto permite dos cosas:

- mantener el mod como un solo namespace
- evitar una raiz plana llena de contenido mezclado

## Ejemplo con `Luna`

Si `Luna` entra al mod:

Debe reutilizar de `common`:

- runtime de task
- search planner
- observation
- comfort engine
- base entity hooks
- dialogue formatter
- saved data por personaje
- accessory framework

Debe definir en `character/luna`:

- sus moods
- sus intents
- sus recuerdos
- sus lineas
- su renderer
- sus items
- su magia o ausencia de magia
- sus criterios de lugares seguros o agradables

No deberia tocar el paquete `Polen`, salvo si ambas comparten una escena o flag global del mundo.

## Ejemplo con `Vanilla`

`Vanilla` podria incluso reutilizar casi todo el runtime y aun asi tener una fantasia muy distinta.

Ejemplo:

- mismo pipeline de IA
- distinta politica de socializacion
- cero blink magic
- otros intereses
- otra forma de resolver dialogo contextual
- otros accesorios o ninguno

Eso confirma la regla:

No compartimos personalidad.
Compartimos infraestructura.

## Regla de nombres

Regla fuerte para el refactor:

- si una clase en `common` empieza por `Polen`, esta en el lugar incorrecto
- si una clase en `common` importa `character.polen`, esta en el lugar incorrecto
- si una clase de `Luna` necesita copiar y pegar una clase entera de `Polen`, nos falta extraccion

## Orden correcto del refactor

### Fase 1

Separar paquetes sin cambiar gameplay:

- `bootstrap`
- `common`
- `character/polen`

### Fase 2

Extraer runtime comun:

- task state
- search state
- observation state
- equipment
- tooltip/item base

### Fase 3

Extraer servicios reutilizables:

- dialogue composition
- persistence repositories
- registry composition por personaje

### Fase 4

Mover politica de Polen a su propio modulo:

- moods
- intents
- memories
- magic
- story events

### Fase 5

Agregar `Luna` usando la nueva frontera.

Si para agregar `Luna` hay que tocar veinte clases de `Polen`, la arquitectura sigue mal.

## Decision practica

La siguiente etapa no deberia ser "renombrar todo a generic".

La siguiente etapa deberia ser:

1. crear la frontera fisica `common` vs `character/polen`
2. mover primero las piezas que ya son claramente reutilizables
3. dejar la personalidad y narrativa de Polen donde pertenece
4. usar `Luna` y `Vanilla` como prueba de diseño, no como excusa para sobreabstraer
