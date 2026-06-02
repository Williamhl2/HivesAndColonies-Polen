# Hives And Colonies: Polen

Narrative NeoForge mod for Minecraft 1.21.1 centered on Polen, a persistent companion character with memory, moods, autonomous behavior, relationship progression, and story-driven growth.

Current status: active early development.

## What the mod already includes

- Polen as a persistent entity with story-based name reveal.
- Per-player affinity and relationship tracking.
- World-level chapter and story flag progression.
- Contextual dialogue and early story events.
- Autonomous AI built around needs, intent, task arbitration, quiet actions, search, and safety.
- Soft magic behaviors:
  - blink escapes
  - source attunement
  - reflective quiet moments
  - night lighting with a managed `polen_lantern`
- Gesture-driven player-like animation layer.
- Early item families for story, material, focus, colony, and accessory growth.

## Design direction

The goal is not to build "a villager with dialogue".

The goal is to build a readable companion whose:

- behavior
- emotional state
- progression
- narrative role

all feel connected.

Polen should increasingly feel like another player in the world, not a static quest NPC.

## Documentation

- [Documentation index](docs/README.md)
- [Technical overview](docs/dev/PROJECT_OVERVIEW.md)
- [Polen AI architecture](docs/dev/POLEN_AI.md)
- [Codebase map](docs/dev/CODEBASE_MAP.md)
- [Spanish story bible](docs/es/STORY.md)
- [English story overview](docs/en/STORY.md)

## Quick structure

- `src/main/java/com/hivesandcolonies/polen`
  - mod entrypoint, entity, AI, progression, story, items, registries, commands
- `src/main/resources/assets/polen`
  - lang, models, blockstates, textures
- `src/main/resources/data/polen`
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
/polen affinity get
/polen chapter get
/polen flag get
/polen relationship get
/polen worlddata get
/polen ai get
```

## Current implementation notes

- Polen now differentiates between generic danger, rain shelter, and night-light relocation.
- Quiet activities already include singing, drawing, attuning, illuminating, and reflecting.
- AI structure is split into `core`, `brain`, `navigation`, `expression`, and `ability`, with reusable search profiles inside navigation and a task layer between intent and goals.
- `/polen ai get` now exposes intent, task, search state, and recent task failure recovery so in-world debugging is much less blind.
- The animation layer already uses a `PlayerModel`-based setup so future richer animation systems can hook into gestures cleanly.
