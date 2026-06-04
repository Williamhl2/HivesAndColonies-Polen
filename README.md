# Hives & Colonies: Characters

Narrative NeoForge mod for Minecraft 1.21.1 built for a broader cast of story characters, currently led by Polen as the first persistent companion with memory loss, autonomous behavior, relationship progression, and story-driven recovery.

The current codebase is still Polen-first, but the project scope is widening toward a broader cast. The mod now uses the public title `Hives & Colonies: Characters` so more story-relevant characters can be introduced without the title fighting the scope.

The mod is also being built as a character-driven part of the larger `Hives & Colonies` modpack ecosystem, so future items, shelter logic, affinities, accessories, memories, and character interactions are expected to integrate with a richer colony, magic, home, and Curios-heavy environment.

Current status: active early development with narrative expansion in progress and at least one known AI regression.

## What the mod already includes

- Polen as a persistent entity with story-based name reveal.
- Per-player affinity and relationship tracking.
- World-level chapter and story flag progression.
- Early contextual dialogue and story events.
- Autonomous AI built around needs, intent, task arbitration, quiet actions, search, and safety.
- Soft magic behaviors:
  - blink escapes
  - source attunement
  - reflective quiet moments
  - night lighting with a managed `polen_lantern`
  - residence-aware refuge and rest behavior through a dedicated `residence_charm`
- Gesture-driven player-like animation layer.
- Early item families for story, material, focus, colony, and accessory growth.

## Design direction

The goal is not to build "a villager with dialogue".

The goal is to build a readable character whose:

- behavior
- emotional state
- memory recovery
- relationships
- narrative role

all feel connected.

Polen should increasingly feel like another player-like presence in the world, not a static quest NPC.

## Narrative direction

The updated canon now treats Polen as:

- a survivor from Hive, a planet populated by many species and no native humans
- someone who lost her memories and will recover them gradually
- a healer by training
- the long-term "promised queen" or legendary queen figure of a new world
- one part of a wider cast that includes Befsh, Cosmic, Luna, Noia, Noris, Jeff, and Vanilla

The playable beginning should still stay intimate and grounded even though the larger backstory already exists.

## Documentation

- Primary lore and narrative direction should be documented in Spanish first.
- Public-facing and collaboration-facing documentation should also keep an English version in sync for wider sharing.

- [Documentation index](docs/README.md)
- [Technical overview](docs/dev/PROJECT_OVERVIEW.md)
- [Modpack context](docs/dev/MODPACK_CONTEXT.md)
- [Polen AI architecture](docs/dev/POLEN_AI.md)
- [Dialogue localization workflow](docs/dev/DIALOGUE_LOCALIZATION.md)
- [Codebase map](docs/dev/CODEBASE_MAP.md)
- [Spanish story bible](docs/es/STORY.md)
- [English story overview](docs/en/STORY.md)

## Quick structure

- `src/main/java/com/hivesandcolonies/characters`
  - mod entrypoint, entity, AI, progression, story, items, registries, commands
- `src/main/resources/assets/characters`
  - lang, models, blockstates, textures
- `src/main/resources/data/characters`
  - recipes, tags, loot, advancements
- `docs/dev`
  - technical documentation
- `docs/es`
  - narrative docs in Spanish
- `docs/en`
  - narrative docs in English

## Build

Compile:

```powershell
./gradlew.bat compileJava
```

Run tests:

```powershell
./gradlew.bat test
```

## Useful debug commands

```text
/characters affinity get
/characters locate
/characters chapter get
/characters flag get
/characters relationship get
/characters worlddata get
/characters ai get
```

## Current implementation notes

- Polen differentiates between generic danger, rain shelter, and night-light relocation.
- Quiet activities already include singing, drawing, attuning, illuminating, and reflecting.
- AI structure is split into `core`, `brain`, `navigation`, `expression`, `ability`, and `world`, with comfort, home, observation, affinity, and affordance layers separated from raw goals.
- Rest and shelter selection balances remembered residence against local comfort and travel distance, so Polen does not blindly prefer home from impractical ranges.
- `/characters ai get` exposes intent, task, search state, and recent task failure recovery.
- `/characters locate` reports where the unique living Polen currently is, even if she is far away or in another dimension.
- Early affinity charms already exist and sync through `Curios`, leaving room for future rings, necklaces, belts, and other character accessories.
- The animation layer already uses a `PlayerModel`-based setup so future richer animation systems can hook into gestures cleanly.
- Dialogue authoring now uses `lang_base/` plus split `lang_parts/` sources, merged automatically into runtime `lang/*.json` files during resource processing.
- Dialogue runtime now uses those split sources across interaction lines, passive ambient chatter, story events, and memory unlocks.
- Interest investigation now backs off from repeated or unreachable targets, so Polen is less likely to loop on the same flowers forever.
