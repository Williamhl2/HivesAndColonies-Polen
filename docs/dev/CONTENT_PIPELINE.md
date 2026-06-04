# Content Pipeline

## Registries

Main registry files:

- [ModItems.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModItems.java)
- [ModBlocks.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModBlocks.java)
- [ModEntities.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModEntities.java)
- [ModCreativeTabs.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModCreativeTabs.java)
- [ModEntityAttributes.java](../../src/main/java/com/hivesandcolonies/polen/registry/ModEntityAttributes.java)

## Adding a new item

1. Decide its family first.
2. Create a concrete class only if it has behavior.
3. Register it in `ModItems`.
4. Add model data in `assets/polen/models/item`.
5. Add lang keys in both English and Spanish.
6. If it belongs in creative mode, add it to `ModCreativeTabs`.
7. If it needs tags, add the correct `data/polen/tags/item/*` entry.

## Adding a new block

1. Register it in `ModBlocks`.
2. If it has a matching item form, register that in `ModItems`.
3. Add blockstate and block model files.
4. Add item model file if needed.
5. Add lang keys in both languages.
6. Add loot table if the block should drop something.

This is already the path used by `polen_lantern`.

## Tooltips and reusable item behavior

Relevant bases:

- `item/base/TranslatableTooltipItem`
- `item/base/PolenTypedItem`
- `item/accessory/PolenAccessoryItem`

Rule:

- prefer reusable base classes over one-off copies

## Adding dialogue

Read [DIALOGUE_LOCALIZATION.md](DIALOGUE_LOCALIZATION.md) first.

Current rule:

- the project now uses split dialogue source files
- final runtime still depends on `lang/*.json`

So when changing dialogue:

1. Update the split source files when they exist for that locale.
2. Keep the final generated `lang/*.json` output aligned.
3. Connect the key through `PolenDialogueManager` or the right event/controller.
4. If the line changes progression, trigger it from an event or manager, not from random render or item code.
5. Verify the line still matches current canon and current implementation state.

## Adding character-facing content

If a new item, block, line, or event affects Polen or the wider cast:

1. Register the content first if it is a game asset.
2. Decide whether the behavior belongs in:
- AI planner/controller
- progression manager
- interaction controller
- accessory/equipment layer
- dialogue or story event manager
3. Update the relevant docs immediately.

Example:

- `polen_lantern` is content
- placement/removal rules belong to AI and magic controllers

## Adding advancements

1. Create the JSON in `src/main/resources/data/polen/advancement/...`
2. Add the `ResourceLocation` in `PolenAdvancementManager`
3. Add a grant method
4. Trigger it from the correct manager or story event

## Localization

Current public lang files:

- `src/main/resources/assets/polen/lang/en_us.json`
- `src/main/resources/assets/polen/lang/es_es.json`
- `src/main/resources/assets/polen/lang/es_cl.json`

Rule:

- every visible player-facing key should exist in English and Spanish
- if a change alters public-facing fantasy, update docs too
- if a change introduces lore names, relationships, memory reveals, or titles, verify those references are consistent across locales
- if the documentation already has Spanish and English mirrors, keep both aligned in the same pass

## Narrative alignment

Before changing visible content, review:

- [docs/es/STORY.md](../es/STORY.md)
- [docs/en/STORY.md](../en/STORY.md)
- [docs/en/CHARACTERS.md](../en/CHARACTERS.md)

If the content changes Polen's visible identity or role, also review:

- [POLEN_CHARACTER_ARC.md](POLEN_CHARACTER_ARC.md)
- [POLEN_AI.md](POLEN_AI.md)

If the content touches:

- Befsh
- Cosmic
- Luna
- Noia
- Noris
- Jeff
- Vanilla
- memory loss
- war backstory
- the "promised queen" role

then treat it as continuity-sensitive content, not flavor text.

## Maintenance rule

If a new feature adds:

- a new family
- a new public mechanic
- a new AI-facing item or block
- a new accessory system hook
- a new memory reveal path
- a new named character reference

then code, lang, assets, tags, and docs must all move together in the same pass.
