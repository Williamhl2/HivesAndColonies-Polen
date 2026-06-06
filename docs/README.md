# Documentation

## Public narrative docs

### Spanish

- [Story bible](es/STORY.md)
- [Narrative chapters](es/NARRATIVE_CHAPTERS.md)
- [Polen prologue site](es/POLEN_PROLOGUE_SITE.md)
- [Polen residence](es/POLEN_RESIDENCE.md)

### English

- [Story overview](en/STORY.md)
- [Character notes](en/CHARACTERS.md)

## Developer docs

- [Project overview](dev/PROJECT_OVERVIEW.md)
- [Resumen tecnico en espanol](dev/PROJECT_OVERVIEW_ES.md)
- [Modpack context](dev/MODPACK_CONTEXT.md)
- [Contexto del modpack en espanol](dev/MODPACK_CONTEXT_ES.md)
- [Codebase map](dev/CODEBASE_MAP.md)
- [Character architecture](dev/CHARACTER_ARCHITECTURE.md)
- [Progression system](dev/PROGRESSION_SYSTEM.md)
- [Sistema de progresion en espanol](dev/PROGRESSION_SYSTEM_ES.md)
- [Polen AI](dev/POLEN_AI.md)
- [Polen character arc](dev/POLEN_CHARACTER_ARC.md)
- [Memoria de Polen en espanol](dev/POLEN_MEMORY_ES.md)
- [Dialogue localization](dev/DIALOGUE_LOCALIZATION.md)
- [Localizacion de dialogos en espanol](dev/DIALOGUE_LOCALIZATION_ES.md)
- [Item progression](dev/ITEM_PROGRESSION.md)
- [Content pipeline](dev/CONTENT_PIPELINE.md)

## Scope note

The repository is still structured around Polen as the first playable character.

Narrative scope is now broader than that:

- Hive is a multi-species world with no native humans
- Polen's lost memories and future "promised queen" role are canon
- Befsh, Cosmic, Luna, Noia, Noris, Jeff, and Vanilla are part of the active continuity
- a public mod identity is now `Hives & Colonies: Characters`

When updating docs, preserve the difference between:

- full canon
- current playable implementation
- planned but not fully wired systems

## Language rule

Spanish is the primary language for canon and internal narrative direction.

English should exist as a maintained mirror for:

- public sharing
- external collaborators
- broader player-facing communication

When lore, progression, dialogue structure, or character continuity changes:

- update the Spanish version first or in the same pass
- update the English version in the same pass whenever that doc has a public or shared counterpart

## Recommended reading order

For new developers:

1. Read [Project overview](dev/PROJECT_OVERVIEW.md).
2. Read [Modpack context](dev/MODPACK_CONTEXT.md).
3. Read [Codebase map](dev/CODEBASE_MAP.md).
4. Read [Progression system](dev/PROGRESSION_SYSTEM.md).
5. If you are touching Polen behavior, read [Polen AI](dev/POLEN_AI.md).
6. If you are touching dialogue or language files, read [Dialogue localization](dev/DIALOGUE_LOCALIZATION.md).
7. If you are touching items or progression rewards, read [Item progression](dev/ITEM_PROGRESSION.md).
8. If you are touching narrative, read [Story bible](es/STORY.md) and [Character notes](en/CHARACTERS.md).

## Maintenance rule

When implementation, canon, or public-facing fantasy changes, update:

- `README.md`
- the relevant `docs/dev/*` file
- the public narrative docs in English or Spanish
- the Spanish and English mirrors when both exist
- dialogue workflow notes if the change affects how lines are authored or loaded

The docs should describe:

- what is canon
- what is in the current build
- what is still in transition

They should not describe a planned version as if it were already playable.
