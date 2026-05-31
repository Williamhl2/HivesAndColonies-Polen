# Changelog

All notable changes to this project will be documented in this file.

## Unreleased - Item Progression Foundation

### Added

- Added typed item metadata for family, progression stage, and uniqueness.
- Added reusable item bases for story, material, focus, and colony item families.
- Added `docs/dev/ITEM_PROGRESSION.md` to define long-term item scaling rules.

### Changed

- Reclassified current items into explicit progression roles instead of leaving them as flat standalone entries.
- Grouped item registration and creative-tab ordering by family intent.

## Unreleased - First Functional Item Wave

### Added

- Added `bloom_focus` as a reusable focus item for helping Polen remember flowers, hives, and source-like resonances.
- Added `settlement_charm` as a colony item for marking safe resting places once shelter progression exists.
- Added `source_touched_petal` and `resonant_wax` as early scalable materials for future Ars and bee-related systems.

### Changed

- Added a dedicated item interaction controller so new item behaviors route through existing memory, affinity, and story systems.
- Added first-pass survival recipes and item-family datapack tags for the new item layer.

## Unreleased - Ars Magic Follow-up

### Added

- Added `PolenMagicController` for emergency blink relocation and subtle quiet-activity spell effects.
- Added new ambient magic dialogue lines for both Spanish and English localization.

### Changed

- Emergency relocation now uses a magical blink with particles and sound instead of a silent raw teleport.
- Singing and drawing can now produce small Ars-inspired spell feedback while Polen is calm.
- Updated lore and AI documentation to treat Polen's magic as instinctive, intimate Ars Nouveau behavior rather than late-game combat magic.

## Unreleased - Dialogue and Debug Follow-up

### Changed

- Split dialogue responsibilities into dedicated resolvers for speaker, chapter lines, ambient tone, and ambient key construction.
- Expanded `/polen ai get` to expose mood reason and safety-related debug state.

### Technical

- Added JUnit 5 test infrastructure.
- Added initial pure-logic unit tests for dialogue resolvers and danger-memory math.

## Unreleased

### Changed

- Continued the Polen entity refactor to keep AI responsibilities split by concern.
- Moved quiet activity logic out of `PolenEntity` into `PolenQuietActivityController`.
- Moved reusable `BlockPos` NBT serialization into `PolenNbtHelper`.
- Moved routine target selection out of `PolenEntity` into `PolenRoutinePlanner`.
- Moved safety escape planning out of `PolenEntity` into `PolenSafetyNavigator`.
- Moved player interaction flow out of `PolenEntity` into `PolenInteractionController`.
- Moved ambient dialogue emission out of `PolenEntity` into `PolenAmbientDialogueController`.
- Moved dangerous spot memory handling out of `PolenEntity` into `PolenDangerMemoryTracker`.
- Moved goal registration out of `PolenEntity` into `PolenGoalRegistry`.
- Tightened unsafe dialogue rules so cave-escape lines are no longer used for normal outdoor positions.
- Improved safety escape replanning so Polen can recover better when the first route is blocked or awkward.

### Technical

- `PolenEntity` now delegates passive hobby timing and client particle feedback to a dedicated controller.
- Entity NBT persistence is prepared for reuse by future extracted systems.
- Goals now depend more directly on planners/controllers instead of routing through extra public facades on `PolenEntity`.
- Escape search now prefers reachable safe spots and gives extra weight to upward/open destinations when leaving enclosed unsafe areas.

## v0.0.8 - Entity Refactor Update

### Added

- `PolenSafetyEvaluator` for evaluating safe and dangerous positions in the environment.
- `PolenSafetyNavigator` for handling escape route planning and replanning when stuck.
- `PolenRoutinePlanner` for selecting routine targets based on mood and memory.
- `PolenMoodController` for calculating Polen's mood based on context.
- `PolenMemoryHandler` for managing Polen's memories of flowers, hives, resting spots and nearby environment.
- `PolenQuietActivityController` for managing quiet activity selection and synchronized hobby timers.
- `PolenNbtHelper` for reusable NBT serialization logic.

### Changed

- Refactored `PolenEntity` to delegate specific responsibilities to the new classes, keeping it focused on coordinating entity state, interaction flow and routine target selection.
- Improved safety behavior to avoid false-positive cave dialogue outdoors and to allow escape replanning when the first route is blocked.
- Adjusted AI behavior to better reflect mood and memory states through the new controllers.

### Fixed

- Prevented false-positive cave escape dialogue when Polen is exposed outdoors.
- Improved escape behavior to avoid getting stuck when the initial escape route is blocked.

## v0.0.7 - Living Polen Update

### Added

- Added Polen memory system.
- Added first memory triggers:
  - `FIRST_FLOWER`
  - `FIRST_HIVE`
  - `FIRST_SOURCE`
  - `FIRST_COLONY`
- Added new world story flags for Polen memories.
- Added debug commands for unlocking Polen memories.
- Added debug command to inspect Polen's current mood.
- Added new Polen moods:
  - `CONFIDENT`
  - `JOYFUL`
- Added memory dialogue entries in `es_es.json` and `en_us.json`.

### Changed

- Expanded Polen's mood logic to better reflect world progression.
- `CONFIDENT` now represents trust in the environment/world state.
- `JOYFUL` represents stronger emotional comfort near trusted players.
- Polen can now associate flowers, hives and resting places with narrative progression.
- Improved the connection between Polen's AI behavior and story flags.

### Fixed

- Prevented memory dialogue from repeating after a memory has already been unlocked.
- Improved debug testing flow for story memories and mood inspection.

### Notes

- This update does not add Polen's residence yet.
- This update does not add deep MineColonies, Productive Bees or Ars Nouveau integration yet.
- Those systems are planned for later narrative/content updates.

## [v0.0.6] - 2026-05-30

### Added

- Initial content pipeline documentation
  - Item addition process
  - Dialogue line addition process
  - Advancement addition process
  - Entity and renderer addition process
  - Localization guidelines

- Documentation coverage for content authoring workflow
  - Registration checklist structure
  - Asset file placement references
  - Translation update reminders

### Changed

- Changelog versioning for 2026-05-30 changes is now tracked under v0.0.6

- Documentation structure refined for faster onboarding when adding Polen content

### Technical

- Repository documentation now better reflects the current content pipeline layout

## [v0.0.5] - 2026-05-29

### Added

- Persistent story data system
  - World story SavedData
  - Player relationship data
  - Automatic world save integration

- World story architecture
  - Shared chapter progression
  - Shared story flags
  - Persistent world narrative state

- Player relationship architecture
  - Persistent affinity
  - Interaction tracking
  - Relationship progression framework

- World story data
  - Current chapter persistence
  - Story flag persistence
  - Future Polen entity tracking support
  - Future spawn state support

- Debug utilities
  - /polen relationship get
  - /polen worlddata get

### Changed

- Refactored progression system into:
  - World Story
  - Player Relationship

- Chapter progression is now shared across the entire world

- Story flags are now global world events

- Affinity is now tracked individually per player

- PolenEntity responsibilities reduced
  - Interaction handling only
  - Narrative logic delegated to story systems

- Narrative event logic moved to PolenStoryEventManager

- Dialogue architecture prepared for long-term chapter expansion

- Advancement files moved to proper datapack location:
  - data/polen/advancement/story

- Advancement titles and descriptions now use translation keys

- Narrative event dialogue moved to language files

- Item tooltip implementation consolidated through reusable tooltip item base class

### Fixed

- Advancement resource structure
- Story progression persistence after world reload
- World state synchronization for multiplayer environments

### Technical

- Introduced SavedData-based persistence layer
- Separated global story progression from player-specific relationship data
- Prepared architecture for future multiplayer story progression

## [v0.0.4] - 2026-05-29

### Added

- Story advancement framework
  - Root advancement
  - First Meeting advancement
  - First Trust advancement
  - Name Reveal advancement
  - Chapter 0 Complete advancement
  - Player Has Shelter advancement

- Story flag system
  - NAME_REVEALED
  - CHAPTER_0_COMPLETE
  - PLAYER_HAS_SHELTER

- Chapter progression framework
  - Chapter storage
  - Chapter advancement
  - Chapter reset support

- Affinity level system
  - FIRST_TRUST
  - NAME_REVEAL
  - FRIEND
  - CLOSE_FRIEND
  - TRUSTED

- Story event manager

- Debug command framework
  - /polen affinity
  - /polen flag
  - /polen chapter

- Name reveal narrative event

- Dynamic NPC naming system

- Chapter 0 completion flow

- Chapter 1 initialization

### Changed

- Polen now appears as "???" before the name reveal event
- Dialogue system now supports dynamic speaker names
- Dialogue text separated from speaker identity
- Progression logic moved away from entity-specific implementation
- Chapter progression is now driven by story milestones

## [v0.0.3] - 2026-05-29

### Added

- Polen entity registration

- Polen spawn egg

- Polen renderer

- Initial Polen skin

- Entity attributes

- Basic AI goals
  - Random stroll
  - Look at player
  - Random look around
  - Float in water

- First NPC interaction system

- Chapter-based dialogue manager

- Progression framework
  - PolenChapterManager
  - PolenAffinityManager

### Changed

- Replaced single dialogue message with randomized chapter dialogue system

## [v0.0.2] - 2026-05-29

### Added

- Polen creative tab
- Princess Seal
- Princess Letter
- Polen Journal
- ES/EN translations
- Narrative tooltips
- Initial project structure
