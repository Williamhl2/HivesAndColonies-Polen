# Project Overview

## What this project is

`Hives And Colonies: Polen` is a NeoForge mod for Minecraft 1.21.1 built around a narrative companion character named Polen.

The project currently combines five layers:

1. Polen as an in-world companion entity.
2. Story and chapter progression.
3. Per-player relationship and affinity.
4. Companion-oriented AI, safety, and soft magic.
5. Item and block content that supports narrative, colony, and future accessory systems.

## Technical goal

The codebase should let Polen grow as a character without turning the mod into:

- a pile of one-off triggers
- a single bloated entity class
- a quest script disguised as gameplay

That implies:

- world data for shared story progress
- player data for affinity and trust
- AI split into small readable domains
- client animation that can evolve independently from server AI
- content families that can scale over time

## Stack

- Java 21
- NeoForge
- Gradle
- standard Minecraft `assets` and `data`

## Main entrypoints

- [Polen.java](../../src/main/java/com/hivesandcolonies/polen/Polen.java)
  - mod entrypoint
  - registers items, blocks, entities, attributes, creative tabs, and commands
- `PolenClient.java`
  - client bootstrap
  - connects renderer and client-only behavior

## Main subsystems

### Entity and presentation

- `entity/PolenEntity.java`
- `client/PolenRenderer.java`
- `client/model/PolenModel.java`
- `client/animation/PolenGesturePoseApplier.java`

Responsibilities:

- physical in-world presence
- synced state
- player-like rendering
- gesture-driven visible behavior

### AI and autonomy

- `entity/ai/core/*`
- `entity/ai/state/*`
- `entity/ai/need/*`
- `entity/ai/intent/*`
- `entity/ai/action/*`
- `entity/ai/activity/*`
- `entity/ai/goal/*`
- `entity/ai/safety/*`
- `entity/ai/magic/*`
- `entity/ai/gesture/*`

Responsibilities:

- internal pressure model
- intent selection
- quiet autonomous actions
- movement and reaction goals
- safety and shelter logic
- blink and subtle magic
- animation-facing gesture state

### Progression

- `progression/PolenChapterManager.java`
- `progression/PolenStoryFlagsManager.java`
- `progression/PolenAffinityManager.java`
- `progression/player/*`
- `progression/world/*`

Responsibilities:

- world story chapters
- story flags
- player trust and affinity
- persistence via `SavedData`

### Dialogue and story events

- `dialogue/PolenDialogueManager.java`
- `story/PolenStoryEventManager.java`

Responsibilities:

- normal dialogue
- ambient dialogue
- story event sequencing
- advancement and progression hooks

### Content

- `registry/ModItems.java`
- `registry/ModBlocks.java`
- `registry/ModEntities.java`
- `registry/ModCreativeTabs.java`
- `registry/ModEntityAttributes.java`
- `item/*`
- `src/main/resources/assets/polen/*`
- `src/main/resources/data/polen/*`

Responsibilities:

- item and block registration
- lang keys
- models and blockstates
- recipes, tags, loot, advancements

## Current design rules

- Polen must feel like a companion, not a generic villager.
- Intimacy should come before spectacle.
- AI should grow by layers, not by adding random conditionals to `PolenEntity`.
- Public behavior should stay consistent with character arc and story stage.
- Item growth should follow families and progression stages, not accidental creation order.

## Where to start

- AI work: [POLEN_AI.md](POLEN_AI.md)
- progression work: [PROGRESSION_SYSTEM.md](PROGRESSION_SYSTEM.md)
- content work: [ITEM_PROGRESSION.md](ITEM_PROGRESSION.md) and [CONTENT_PIPELINE.md](CONTENT_PIPELINE.md)
- narrative work: [../es/STORY.md](../es/STORY.md) and [../en/CHARACTERS.md](../en/CHARACTERS.md)
