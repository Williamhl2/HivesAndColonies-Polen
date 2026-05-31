# Development Log

## 2026-05-31 (Item Progression Foundation)

### Added

- Item family metadata:
  - `PolenItemFamily`
  - `PolenProgressionStage`
- Typed item bases:
  - `PolenTypedItem`
  - `PolenLoreItem`
  - `PolenMaterialItem`
  - `PolenUsableFocusItem`
  - `PolenColonyItem`
- Item progression design document:
  - `docs/dev/ITEM_PROGRESSION.md`

### Changed

- Migrated current narrative items onto the new typed item hierarchy.
- Marked story items as unique and mapped them to explicit progression stages.
- Mapped `royal_pollen` as a reusable material aligned with later discovery content.
- Grouped `ModItems` and the creative tab by family intent instead of flat growth.

### Result

The item layer now has a durable foundation for scaling without turning every future feature into an isolated one-off item.

## 2026-05-31 (First Functional Item Wave)

### Added

- `bloom_focus`
  - can mark flowers and hives for Polen's memory
  - can resonate with source-like blocks to trigger the first source memory
- `settlement_charm`
  - can mark a safe resting place for Polen once shelter progression exists
- `source_touched_petal`
- `resonant_wax`

### Changed

- Added an item interaction controller so functional items can reuse progression, memory and affinity systems without embedding too much logic in the item classes themselves.
- Creative tab and item progression docs now include the first focus and colony layers.
- Added first-pass survival recipes and item family tags so the item layer is testable without creative-only spawning.

## 2026-05-30 (Ars Magic Integration)

### Added

- `PolenMagicController` for:
  - emergency blink relocation with particles and teleport sound
  - subtle idle spell effects during singing and drawing
- Ambient magic dialogue lines in both `es_es.json` and `en_us.json`

### Changed

- Emergency safety relocation now uses Polen's own magic instead of a raw teleport.
- Quiet activities now have a low-key magical layer that reflects the Ars Nouveau side of the story.
- Narrative documentation now states that Polen retains instinctive magic even before fully remembering her past.

### Result

Polen's behavior now reflects Ars Nouveau directly in gameplay:

- she can escape impossible enclosed spawns without looking bugged
- she can show small magical responses while calm instead of only ordinary idle particles

## 2026-05-30 (Dialogue, Debug and Tests)

### Added

- Dialogue resolvers for:
  - speaker identity
  - chapter key selection
  - ambient tone selection
  - ambient key construction
- AI debug snapshot support
- Initial JUnit 5 unit test setup for pure logic

### Result

`PolenDialogueManager` is now more of a facade than a container for every dialogue concern.

`/polen ai get` also exposes more reasoning signal, which should make future AI balancing faster.

## 2026-05-30 (Entity Refactor)

### Goal

Continue reducing `PolenEntity` before it turns into a single oversized AI file.

### Refactor Progress

Extracted or consolidated responsibilities:

- `PolenInteractionController`
  - player interaction flow
  - name reveal trigger
- `PolenAmbientDialogueController`
  - ambient dialogue cooldown
  - nearby player broadcast
- `PolenDangerMemoryTracker`
  - dangerous spot memory state
  - expiration and NBT persistence
- `PolenGoalRegistry`
  - goal priority registration outside the entity
- `PolenSafetyEvaluator`
  - safe spots
  - dangerous spots
  - shelter-aware standing checks
- `PolenMemoryHandler`
  - remembered flower, hive and resting spots
  - nearby environment seeding
  - first-interest memory unlock flow
- `PolenMoodController`
  - contextual mood calculation
- `PolenRoutinePlanner`
  - contextual routine target selection
  - remembered spot validation
- `PolenSafetyNavigator`
  - escape decisions
  - reachable safe spot search
  - path replanning when the first escape route fails
- `PolenQuietActivityController`
  - quiet activity selection
  - synchronized hobby timers
  - client particle feedback
- `PolenNbtHelper`
  - reusable `BlockPos` save/load logic

### Result

`PolenEntity` remains the coordinator of:

- synced entity state
- base tick lifecycle
- shared state used by goals and controllers

This keeps behavior split by concern instead of growing one monolithic AI file.

### Safety Behavior Revision

The escape logic was tightened to avoid false-positive cave dialogue outdoors.

Current behavior:

- ordinary outside exposure no longer triggers cave-like ambient escape dialogue by itself
- cave-like dark enclosed spaces can still trigger discomfort and escape
- escape now searches for reachable safer spots and can replan when Polen gets stuck below a ledge or small climb

## 2026-05-30 (Narrative Direction)

### Architecture Revision

The progression system has been redesigned to support multiplayer and long-term narrative development.

Core rule:

Story belongs to the world.

Relationship belongs to the player.

### World Story Layer

Shared by all players.

Current responsibilities:

- Chapter progression
- Story flags
- Global narrative state
- Future kingdom progression

Examples:

- NAME_REVEALED
- CHAPTER_0_COMPLETE
- PLAYER_HAS_SHELTER

### Player Relationship Layer

Stored individually per player.

Current responsibilities:

- Affinity
- Interaction tracking
- Personal relationship progression

This allows Polen to react differently to individual players while maintaining a shared world narrative.

### Persistence

Story progression now survives:

- World reloads
- Server restarts
- Singleplayer sessions
- Multiplayer sessions

### Narrative Systems

The narrative event system has been separated from entity logic.

PolenEntity now focuses on:

- Interaction
- Presentation
- Event triggering

Story execution is handled by:

- PolenStoryEventManager

Dialogue content is now stored in language resources instead of Java code.

### Current Story Status

Completed:

- Chapter 0: The Girl in the Clearing

Implemented:

- First Meeting
- First Trust
- Name Reveal
- Chapter 0 Complete
- Foundation Chapter Start

In Progress:

- Chapter 1: Foundation

Next Major Goal:

- Shelter Recognition System
- First meaningful player-built settlement milestone

## 2026-05-29

### Narrative Direction

Polen is not the Queen of Bees.

She is the rightful heir to the Bee Kingdom and the daughter of the missing Queen.

The player will gradually help Polen rebuild the kingdom and grow into her future role as queen.

### Design Philosophy

The mod is narrative-driven.

Polen should not behave like a quest dispenser.

She should evolve as a character throughout the player's progression.

### Current Narrative Status

Chapter 0 (The Girl in the Clearing) is now fully implemented.

Story flow:

- Meet the mysterious girl
- Build trust
- Discover her name
- Complete Chapter 0
- Begin Chapter 1 (Foundation)

Polen initially appears as "???".

Her true name is revealed through a dedicated story event.

### Progression Architecture

Current systems:

- Dialogue Manager
- Chapter Manager
- Affinity Manager
- Affinity Levels
- Story Flag Manager
- Story Event Manager
- Advancement Manager
- Debug Command Framework

Current story milestones:

- First Meeting
- First Trust
- Name Reveal
- Chapter 0 Complete
- Player Has Shelter

Future integrations:

- Persistent player story data
- FTB Quests progression
- Relationship system
- Dynamic dialogue expansion
- Colony progression
- Chapter-specific events
- Kingdom restoration systems

### Technical Notes

This section is kept as historical context.

Persistence has now been implemented for core story progression systems.

Persisted systems:

- Affinity
- Story Flags
- Chapter Progression

Current focus for v0.0.5+ is expansion of relationship depth, chapter events, and settlement-driven story progression.
