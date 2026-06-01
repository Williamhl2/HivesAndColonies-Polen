# Polen Character Arc

## Objective

Polen should not remain frozen as "the shy girl" for the entire mod.

Her initial caution is part of her context, not her full identity.

With trust, safety, belonging, and discovery, she should become:

- more expressive
- more proactive
- more affectionate with trusted players
- more visibly present inside the colony

without turning into a generic upbeat NPC.

## Central rule

Polen changes, but she does not flatten.

Her growth is filtered through three layers:

1. world story progress
2. per-player affinity
3. immediate environmental safety

## Current implemented mood vocabulary

The code currently exposes these moods:

- `CALM`
- `TIMID`
- `CURIOUS`
- `INSPIRED`
- `UNSETTLED`
- `CONFIDENT`
- `JOYFUL`

These are not the full narrative arc by themselves.
They are the visible surface the AI can express right now.

## Broad progression line

```text
Distrust
-> Safety
-> Shared curiosity
-> Belonging
-> Warmth
-> Active confidence
-> Responsibility
-> Full presence
```

## Stage notes

### Stage 1 - The Unknown Girl

Approximate scope:

- prologue
- early foundation

Core traits:

- shy
- cautious
- private
- sensitive to proximity

Behavior expectations:

- keeps distance from untrusted players
- prefers safe and simple spaces
- reacts poorly to dark, enclosed, or exposed conditions
- only uses magic in subtle or emergency ways

Likely visible moods:

- `TIMID`
- `UNSETTLED`
- `CALM`

### Stage 2 - The Visitor

Approximate scope:

- early trust
- first safe routines

Core traits:

- calmer
- observant
- curious
- still guarded

Behavior expectations:

- explores safe places
- watches flowers, hives, and source-like spots
- may sing or draw when unwatched
- starts using memory and routine more clearly

Likely visible moods:

- `CALM`
- `CURIOUS`
- `INSPIRED`

### Stage 3 - The Companion

Approximate scope:

- belonging phase
- residence acceptance

Core traits:

- more expressive
- affectionate with trusted players
- more anchored to colony spaces

Behavior expectations:

- uses remembered places
- seeks better shelter on her own
- places light at night when needed
- reflects, attunes, and manages quiet magic intentionally

Likely visible moods:

- `CURIOUS`
- `INSPIRED`
- `CONFIDENT`

### Stage 4 - The Seeker

Approximate scope:

- deeper discovery
- stronger contact with the lost legacy

Core traits:

- determined
- more emotionally honest
- still vulnerable around painful memories

Behavior expectations:

- stronger source interest
- more active investigation behavior
- seeks safe places after difficult revelations

Likely visible moods:

- `INSPIRED`
- `CURIOUS`
- `UNSETTLED`
- `CONFIDENT`

### Stage 5 - The Heir or Keeper

Approximate scope:

- acceptance of legacy

Core traits:

- introspective
- sincere
- less evasive

Behavior expectations:

- stops fleeing from the meaning of her past
- speaks more clearly about memory and duty
- becomes steadier in public presence

Likely visible moods:

- `CONFIDENT`
- `CALM`
- occasional `UNSETTLED`

### Stage 6 - Full Polen

Approximate scope:

- late story
- postgame

Core traits:

- secure
- warm
- expressive
- protective
- capable of leadership without losing softness

Behavior expectations:

- initiates proximity more often with trusted players
- uses the colony more freely
- shows quiet joy in what was built together
- feels like a companion presence rather than a fragile visitor

Likely visible moods:

- `JOYFUL`
- `CONFIDENT`
- `INSPIRED`
- `CALM`

## Relationship rule

Story progress does not erase per-player affinity.

Even in late stages:

- low affinity players should still meet boundaries
- medium affinity players should get warmth with caution
- high affinity players should get the fullest companion side of Polen

## AI-facing note

When adding new visible behavior, ask:

- does this fit Polen's current stage
- does it require trust
- does it require safety
- does it make her feel more like a companion instead of a scripted prop

If the answer is unclear, the feature probably needs to be simplified or moved to a later progression stage.
