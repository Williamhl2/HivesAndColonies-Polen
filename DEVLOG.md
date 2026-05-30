# Development Log

## 2026-05-30 (Entity Refactor)

### Goal

Continue reducing `PolenEntity` before it turns into a single oversized AI file.

### Refactor Progress

Extracted or consolidated responsibilities:

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
- interaction flow
- routine target selection
- ambient dialogue dispatch
- dangerous spot memory ownership

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
