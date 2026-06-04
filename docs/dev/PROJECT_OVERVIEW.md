# Project Overview

## What this project is

`Hives & Colonies: Characters` is a NeoForge mod for Minecraft 1.21.1 built for a broader cast of narrative characters, with Polen currently serving as the first implemented companion.

The repository and current implementation are still Polen-centric, but the narrative scope is expanding toward a wider cast. The project now uses the public title `Hives & Colonies: Characters` so additional story-relevant characters can be added without the mod identity revolving around a single one.

The project currently combines five layers:

1. Polen as an in-world companion entity.
2. Story and chapter progression.
3. Per-player relationship and affinity.
4. Character-oriented AI, safety, and soft magic.
5. Item and block content that supports narrative, colony, and future accessory systems.

## Narrative foundation

The current canon to preserve across docs and implementation is:

- Hive is a multi-species planet with no native humans.
- Humans later invade Hive.
- Polen survives that history but loses her memories.
- Those memories return gradually in the new world where gameplay begins.
- Polen was trained in healing magic.
- Befsh, Cosmic, Luna, Noia, Noris, Jeff, and Vanilla are part of her real continuity.
- Polen's long-term arc includes becoming the "promised queen" or legendary queen of the new world.

Important rule:

- early gameplay should feel intimate and grounded
- full canon should still be documented and preserved
- docs must not deny later truths just because the player has not learned them yet

## Modpack context

This project is being built as part of the `Hives & Colonies` modpack ecosystem, not as a standalone vanilla-only design exercise.

That has two direct consequences:

- Polen should increasingly understand modded homes, colony spaces, lights, doors, furniture, magic-adjacent areas, and apiary-oriented spaces as meaningful world signals.
- Item, dialogue, memory, and accessory planning should assume long-term integration with `Curios`, colony-oriented content, and the broader building/decor ecosystem already present in the pack.

See [MODPACK_CONTEXT.md](MODPACK_CONTEXT.md) for the working integration map based on the user-provided pack snapshot from `2026-06-02`.

## Technical goal

The codebase should let Polen and future related characters grow without turning the mod into:

- a pile of one-off triggers
- a single bloated entity class
- a quest script disguised as gameplay

That implies:

- world data for shared story progress
- player data for affinity and trust
- AI split into small readable domains
- dialogue that can scale beyond a single monolithic language file
- client animation that can evolve independently from server AI
- content families that can scale over time

## Stack

- Java 21
- NeoForge
- Gradle
- standard Minecraft `assets` and `data`

## Main entrypoints

- [Characters.java](../../src/main/java/com/hivesandcolonies/characters/Characters.java)
  - mod entrypoint
  - registers items, blocks, entities, attributes, creative tabs, and commands
- `CharactersClient.java`
  - client bootstrap
  - connects renderer and client-only behavior

Recommended next architecture doc:

- [CHARACTER_ARCHITECTURE.md](CHARACTER_ARCHITECTURE.md)
  - target split between shared systems and per-character implementations
  - directory strategy for future characters like Luna and Vanilla

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
- `entity/ai/brain/*`
- `entity/ai/navigation/*`
- `entity/ai/expression/*`
- `entity/ai/ability/*`
- `entity/ai/world/*`

Responsibilities:

- internal pressure model
- intent selection
- task arbitration and short recovery after failed non-urgent behaviors
- quiet autonomous actions
- reusable location search profiles and reachability resolution
- movement and reaction goals
- safety and shelter logic
- blink and subtle magic
- animation-facing gesture state
- world-facing affordances, comfort, home semantics, observation, memory, and affinity shaping

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
- memory reveals
- story event sequencing
- advancement and progression hooks

Important status note:

- dialogue text now lives in split source files and is merged into runtime language files during resource processing

### Content

- `registry/ModItems.java`
- `registry/ModBlocks.java`
- `registry/ModEntities.java`
- `registry/ModCreativeTabs.java`
- `registry/ModEntityAttributes.java`
- `item/*`
- `src/main/resources/assets/characters/*`
- `src/main/resources/data/characters/*`

Responsibilities:

- item and block registration
- lang keys
- models and blockstates
- recipes, tags, loot, advancements

## Current design rules

- Polen must feel like a character, not a generic villager.
- Intimacy should come before spectacle.
- AI should grow by layers, not by adding random conditionals to `PolenEntity`.
- Public behavior should stay consistent with character arc and story stage.
- Full canon and current implementation state must both be documented clearly.
- Item growth should follow families and progression stages, not accidental creation order.

## Current implementation reality

This version is still closer to:

- first encounters
- trust building
- shelter and routine
- early ambient behavior
- first hints of memory recovery

Known mismatch to keep documented:

- Polen's AI currently has a regression where she can lock into flower-watching behavior and stop moving as intended

## Where to start

- AI work: [POLEN_AI.md](POLEN_AI.md)
- progression work: [PROGRESSION_SYSTEM.md](PROGRESSION_SYSTEM.md)
- content work: [ITEM_PROGRESSION.md](ITEM_PROGRESSION.md) and [CONTENT_PIPELINE.md](CONTENT_PIPELINE.md)
- dialogue work: [DIALOGUE_LOCALIZATION.md](DIALOGUE_LOCALIZATION.md)
- narrative work: [../es/STORY.md](../es/STORY.md) and [../en/CHARACTERS.md](../en/CHARACTERS.md)
