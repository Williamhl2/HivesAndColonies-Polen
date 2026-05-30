# Changelog

All notable changes to this project will be documented in this file.

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
