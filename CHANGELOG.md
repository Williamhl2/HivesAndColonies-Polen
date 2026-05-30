# Changelog

All notable changes to this project will be documented in this file.

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
