# Changelog

All notable changes to this project will be documented in this file.

## Unreleased - Polen Safety and Dialogue Integration

### Added

- Added contextual ambient dialogue beats for Polen:
  - ranged danger
  - rain pressure
  - claustrophobic unsafe spaces
  - open-night exposure
  - active escape
  - emergency light placement
  - settled light state
  - magic source attunement
  - home / returning reflection
  - hive / flower curiosity
- Added dialogue anti-repeat state in `PolenAiState` so Polen can avoid repeating the same dialogue family and variation back to back.
- Added dialogue policy resolvers so ambient speech cadence can differ between urgent, quiet, night, and observational situations.
- Added build support for `hc_characters` split localization sources through `mergeHcCharactersLang`.

### Changed

- Reworked Polen ambient dialogue selection from a mostly linear tone-only flow into a contextual cue system with weighted beat selection.
- Ambient dialogue broadcasts now resolve one shared dialogue key per event instead of potentially giving different lines to each nearby player.
- Shelter-related ambient dialogue now requires confirmed shelter context before Polen talks as if she already has a roof or real refuge.
- `processResources` now regenerates runtime language files for both `characters` and `hc_characters` namespaces.

### Fixed

- Fixed false-positive shelter/night dialogue where Polen could speak as if she had a bed, roof, or refuge when the surrounding space was not actually valid shelter.
- Fixed Polen safety movement to apply lateral weaving when exposed to ranged threats during escape.
- Fixed the previous dialogue authoring/build mismatch where `hc_characters/lang_parts` changes could be present in source but not properly represented as a dedicated merge task in the build.

## [v1.0.1] SoaMarjorie Beta - 2026-06-09

## Added

- Added SoaMarjorie as a new encounter-based NPC focused on mining.
- Added temporary encounter system for SoaMarjorie:
  - Bountiful board encounters.
  - Cave mining encounters.
  - Timed despawn after each encounter.
- Added Bountiful board integration:
  - SoaMarjorie can appear near Bountiful bounty boards.
  - She stays near the board for a limited time.
  - She can give short dialogues and small rewards.
  - Board encounters now use player and board cooldowns.
- Added cave encounter system:
  - SoaMarjorie can appear underground from Y 0 and below.
  - Cave encounters only trigger when exposed mineable ores are nearby.
  - Cave spawn rules check darkness, sky visibility, valid floor, and safe space.
- Added real mining behavior for SoaMarjorie:
  - She can mine exposed ores.
  - She uses her pickaxe while mining.
  - She shares part of the mined resources with a nearby player.
  - Mining is limited per encounter for balance.
  - Mining respects `mobGriefing`.
- Added tool usage states:
  - Pickaxe while mining.
  - Axe while defending herself.
  - Torch while placing light.
- Added torch placement during cave encounters.
- Added defensive behavior:
  - SoaMarjorie can respond to hostile mobs.
  - SoaMarjorie can defend herself against players.
  - SoaMarjorie is highly resistant to damage during encounters.
- Added persistent cooldown data for SoaMarjorie encounters.
- Added common NPC relationship/affinity system.
- Added persistent relationship data per player and character.
- Added affinity memories/flags for future character-specific behavior.
- Added SoaMarjorie relationship integration:
  - Board visit memory.
  - Board reward memory.
  - Cave encounter memory.
  - Shared ore memory.
  - Attack memory.
- Added graphical affinity notification overlay.
- Added compact HUD feedback when affinity changes.
- Added relationship level names and personality-specific dialogue hooks for SoaMarjorie.
- Added mineable ore block tag for SoaMarjorie:
  - `hc_characters:soa_marjorie_mineable`
- Added new gameplay config options for SoaMarjorie:
  - Encounter toggles.
  - Board visit duration.
  - Cave encounter duration.
  - Board player cooldown.
  - Board position cooldown.
  - Cave player cooldown.
  - Cave max Y.
  - Spawn chance divisors.
  - Mining limit.
  - Torch placement toggle.
  - Affinity notification options.

## Changed

- Renamed Soa’s canonical entity ID to `hc_characters:soa_marjorie`.
- Updated SoaMarjorie assets to use `soa_marjorie` naming.
- Reworked SoaMarjorie from a natural persistent spawn into controlled temporary encounters.
- Removed natural biome modifier spawning for SoaMarjorie.
- Rebalanced SoaMarjorie board rewards to avoid progression-breaking items.
- Reduced rare reward power for beta release.
- Reduced mining output by limiting mined blocks per encounter.
- Changed cave spawn layer from Y 48 and below to Y 0 and below.
- Improved cave encounter validation so SoaMarjorie does not spawn where she has nothing to mine.
- Improved affinity progression so normal repeated dialogue no longer grants unlimited affinity.
- Reduced and compacted affinity overlay text size.
- Added message wrapping to affinity overlay.
- Adjusted SoaMarjorie dialogue tone toward mentor-style mining humor.
- Preserved final approved visual positions of SoaMarjorie’s belt pickaxe, belt axe, backpack, and rendered tools.

## Fixed

- Fixed old `hc_characters:soa` references.
- Fixed outdated Soa asset paths.
- Fixed SoaMarjorie cave mining goal stopping before mining.
- Fixed repeated right-click dialogue allowing players to quickly reach max affinity.
- Fixed SoaMarjorie respawning too quickly at the same Bountiful board.
- Fixed board cooldowns not persisting across world/server restarts.
- Fixed cave encounters spawning above the intended mining layer.
- Fixed oversized affinity overlay text.
- Fixed SoaMarjorie not consistently using the correct tool state while mining or defending.

## Balance Notes

- SoaMarjorie is intended as a rare narrative/mining encounter, not an automation system.
- She only mines exposed ores.
- She does not use Fortune or Silk Touch.
- She shares only a portion of the mined drops.
- Her board rewards are intentionally small and utility-focused.
- Bountiful remains the main bounty reward system; SoaMarjorie is a companion encounter around it.

## [1.0.0-beta.1] - 2026-06-07

### Added

- PolenAutonomyController to keep high-level autonomy flow out of PolenEntity.
- PolenAiFacade as the single AI-facing entry point used by PolenEntity.
- PolenAiState to store remembered places, danger memory, needs, and intent outside the entity class itself.
- Persistent need-state tracking for safety, social contact, curiosity, rest, and magic.
- High-level intent selection with short-lived decision locks so Polen can sustain actions instead of jittering between reactions.
- Reusable interest discovery for flowers, hives, and source-like locations.
- PolenShelterValidator for validating playable shelters as narrative milestones.
- prologueClearingCenter and prologueShelterPos persisted in world story data.
- docs/es/POLEN_PROLOGUE_SITE.md defining prologue clearing structure and canon rules.
- Typed item metadata for family, progression stage, and uniqueness.
- Reusable item bases: PolenLoreItem, PolenMaterialItem, PolenUsableFocusItem, PolenColonyItem.
- loom_focus as a reusable focus item for flower, hive, and source-like resonance memory.
- settlement_charm as a colony item for marking safe resting places.
- source_touched_petal and
  esonant_wax as early scalable materials.
- PolenMagicController for emergency blink relocation and subtle idle spell effects.
- New ambient magic dialogue lines for both Spanish and English localization.
- Dedicated item interaction controller routing new item behaviors through memory, affinity, and story systems.
- Dialogue resolvers for speaker identity, chapter key selection, ambient tone, and ambient key construction.
- JUnit 5 unit test infrastructure with initial pure-logic tests for dialogue resolvers and danger-memory math.

### Changed

- Polen's server-side AI now follows a compact
  eeds -> intent -> mood -> goals loop.
- Source-like locations are now remembered as first-class interests and can feed later autonomous behavior.
- Routine, hobby, curiosity, trusted approach, and safe wandering goals now activate through explicit intent gating.
- /characters ai get now exposes intent, dominant need, full need values, and remembered source data for balancing.
- Polen now prioritizes her
  estingPos and
  esidence more strongly when seeking shelter during rain or night rest.
- QUIET_CREATION selection now favors returning home before dispersing to other quiet spots when context is already safe.
- Item layer reclassified into explicit progression roles with family intent grouping.
- Emergency relocation now uses a magical blink with particles and sound instead of a silent raw teleport.
- Singing and drawing can now produce small Ars-inspired spell feedback while Polen is calm.
- Narrative documentation now treats Polen's magic as instinctive, intimate Ars Nouveau behavior.
- PolenDialogueManager refactored into a facade, splitting dialogue concerns into dedicated resolvers.
- hiveheart_charm defined as a non-crafteable prologue item in its first stage.

### Fixed

- Movement deadlock where Polen could keep a selected intent while the matching movement goal was still blocked.
- Interacting with Polen inside a valid shelter during FOUNDATION now correctly triggers PLAYER_HAS_SHELTER.
- settlement_charm and source discovery now reuse PolenWorldEventTriggers instead of duplicating memory unlocks.

### Removed

- POLEN_HAS_FOOD placeholder story flag with no active references.
- Premature crafteable recipe for hiveheart_charm.
- hiveheart_charm inclusion in the active Curios charm tag (removed until shelter progression is complete).

# Changelog

All notable changes to this project will be documented in this file.

## Unreleased - v0.0.11 Autonomy Update

### Added

- Added `PolenAutonomyController` to keep high-level autonomy flow out of `PolenEntity`.
- Added `PolenAiFacade` as the single AI-facing entry point used by `PolenEntity`.
- Added `PolenAiState` to store remembered places, danger memory, needs, and intent outside the entity class itself.
- Added persistent need-state tracking for safety, social contact, curiosity, rest, and magic.
- Added high-level intent selection with short-lived decision locks so Polen can sustain actions instead of jittering between reactions.
- Added reusable interest discovery for flowers, hives, and source-like locations.

### Changed

- Polen's server-side AI now follows a compact `needs -> intent -> mood -> goals` loop.
- Source-like locations are now remembered as first-class interests and can feed later autonomous behavior.
- Routine, hobby, curiosity, trusted approach, and safe wandering goals now activate through explicit intent gating.
- `/characters ai get` now exposes intent, dominant need, full need values, and remembered source data for balancing.
- Fixed a movement deadlock where Polen could keep a selected intent while the matching movement goal was still blocked, causing her to look around without walking.

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
- Expanded `/characters ai get` to expose mood reason and safety-related debug state.

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
  - /characters relationship get
  - /characters worlddata get

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
  - data/characters/advancement/story

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
  - /characters affinity
  - /characters flag
  - /characters chapter

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
