# Documentation

## Public narrative docs

### Spanish

- [Story bible](es/STORY.md)
- [Narrative chapters](es/NARRATIVE_CHAPTERS.md)
- [Polen residence](es/POLEN_RESIDENCE.md)

### English

- [Story overview](en/STORY.md)
- [Character notes](en/CHARACTERS.md)

## Developer docs

- [Project overview](dev/PROJECT_OVERVIEW.md)
- [Codebase map](dev/CODEBASE_MAP.md)
- [Progression system](dev/PROGRESSION_SYSTEM.md)
- [Polen AI](dev/POLEN_AI.md)
- [Polen character arc](dev/POLEN_CHARACTER_ARC.md)
- [Item progression](dev/ITEM_PROGRESSION.md)
- [Content pipeline](dev/CONTENT_PIPELINE.md)

## Recommended reading order

For new developers:

1. Read [Project overview](dev/PROJECT_OVERVIEW.md).
2. Read [Codebase map](dev/CODEBASE_MAP.md).
3. Read [Progression system](dev/PROGRESSION_SYSTEM.md).
4. If you are touching Polen behavior, read [Polen AI](dev/POLEN_AI.md).
5. If you are touching items or progression rewards, read [Item progression](dev/ITEM_PROGRESSION.md).
6. If you are touching narrative, read [Story bible](es/STORY.md) and [Character notes](en/CHARACTERS.md).

## Maintenance rule

When the implementation changes in a visible way, update:

- `README.md`
- the relevant `docs/dev/*` file
- the public narrative docs in English or Spanish if the player-facing fantasy changed

The docs should describe the current mod, not a planned version from several weeks ago.
