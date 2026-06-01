# Progression System

## Summary

Progression is intentionally split into two axes:

- world-level story progress
- per-player relationship progress

That split is structural, not optional.

## World progress

Files:

- [PolenChapterManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenChapterManager.java)
- [PolenStoryFlagsManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenStoryFlagsManager.java)
- [PolenWorldStorySavedData.java](../../src/main/java/com/hivesandcolonies/polen/progression/world/PolenWorldStorySavedData.java)
- [PolenWorldStoryData.java](../../src/main/java/com/hivesandcolonies/polen/progression/world/PolenWorldStoryData.java)

Stores:

- current chapter
- world story flags
- Polen UUID
- spawn state

Persistence lives in overworld `SavedData`.

## Per-player relationship

Files:

- [PolenAffinityLevels.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAffinityLevels.java)
- [PolenAffinityManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAffinityManager.java)
- [PolenPlayerRelationshipManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/player/PolenPlayerRelationshipManager.java)
- [PolenPlayerRelationshipData.java](../../src/main/java/com/hivesandcolonies/polen/progression/player/PolenPlayerRelationshipData.java)

Stores:

- affinity
- interaction count
- completed tasks
- last interaction time
- player-specific relationship flags

## Story flags

See [PolenStoryFlag.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenStoryFlag.java).

Current baseline includes:

- `NAME_REVEALED`
- `CHAPTER_0_COMPLETE`
- `PLAYER_HAS_SHELTER`

These are world flags, not player flags.

## Event orchestration

File:

- [PolenStoryEventManager.java](../../src/main/java/com/hivesandcolonies/polen/story/PolenStoryEventManager.java)

Responsibilities:

- run dialogue sequences
- mark story flags
- advance chapters
- grant advancements

## Why this matters to AI

Progression now informs more than dialogue.
It also shapes how Polen should evolve over time:

- trust and comfort with specific players
- tone and openness
- willingness to approach or stay near
- future room for accessory use, residence ownership, and stronger autonomous behavior

The rule is simple:

- story progression decides macro growth
- affinity decides personal closeness
- AI reads both indirectly through controllers and managers

## Advancements

File:

- [PolenAdvancementManager.java](../../src/main/java/com/hivesandcolonies/polen/progression/PolenAdvancementManager.java)

This should remain a service layer.
Do not move story branching logic into the advancement manager.

## Recommended workflow for new progression features

1. Decide whether the new state belongs to the world or to a player.
2. If it changes chapters or shared milestones, store it in world progression.
3. If it changes trust or personal history, store it in player relationship data.
4. If it needs a scene, put the orchestration in `PolenStoryEventManager`.
5. If it has visible reward or milestone feedback, hook an advancement.
6. If it changes Polen's behavior over time, document the AI-facing implication too.

## Common mistakes to avoid

- storing affinity in global flags
- putting progression logic in `PolenEntity.tick()`
- bypassing story event orchestration with ad-hoc dialogue conditions
- duplicating chapter or flag constants outside their managers
- changing public behavior without updating docs
