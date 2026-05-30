# Content Pipeline

## Registries

Archivos:

- [ModItems.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModItems.java)
- [ModEntities.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModEntities.java)
- [ModCreativeTabs.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModCreativeTabs.java)
- [ModEntityAttributes.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModEntityAttributes.java)

## Añadir un item narrativo

1. Crear clase en `item/` si necesita tooltip o lógica propia.
2. Registrar en `ModItems`.
3. Añadir modelo en `assets/polen/models/item`.
4. Añadir textura en `assets/polen/textures/item`.
5. Añadir claves de traducción en `lang`.
6. Si debe verse en creativo, agregarlo a `ModCreativeTabs`.

## Tooltips traducibles

Base:

- [TranslatableTooltipItem.java](../../src/main/java/com/hivesandcolonies/polen/item/TranslatableTooltipItem.java)

Uso:

- heredar de `TranslatableTooltipItem`
- definir líneas de tooltip mediante claves de `lang`

## Añadir una linea de dialogo

1. Registrar la clave en `es_es.json` y `en_us.json`.
2. Conectarla a `PolenDialogueManager` o `PolenStoryEventManager`.
3. Si cambia progresión, dispararla desde un evento y no solo desde diálogo normal.

## Añadir un advancement

1. Crear JSON en `src/main/resources/data/polen/advancement/...`
2. Declarar `ResourceLocation` en `PolenAdvancementManager`
3. Crear método `grant...`
4. Conectarlo desde el evento o manager correcto

## Añadir una entidad o renderer

1. Registrar tipo en `ModEntities`
2. Registrar atributos en `ModEntityAttributes`
3. Crear renderer cliente
4. Conectar en `PolenClient`

## Localizacion

Archivos actuales:

- `src/main/resources/assets/polen/lang/es_es.json`
- `src/main/resources/assets/polen/lang/en_us.json`

Regla recomendada:

- toda nueva clave visible al jugador debe existir en ambos archivos

## Documentacion narrativa

Antes de tocar contenido narrativo, revisar:

- [docs/es/STORY.md](../es/STORY.md)
- [docs/es/NARRATIVE_CHAPTERS.md](../es/NARRATIVE_CHAPTERS.md)

Si un cambio técnico contradice esos documentos, el contenido va a quedar inconsistente aunque compile.
