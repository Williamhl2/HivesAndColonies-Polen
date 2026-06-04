# Polen Character Arc

## Objective

Polen should not remain frozen as "the shy girl in the clearing".

Her caution is real, but it is only the surface of a larger identity shaped by:

- memory loss
- healing training
- grief from war
- deep pre-existing relationships
- a future legendary role she does not fully remember at the start

With trust, safety, memory recovery, and discovery, she should become:

- more expressive
- more proactive
- more emotionally honest
- more present inside the colony
- more capable of carrying public responsibility

without turning into a generic upbeat NPC.

## Central rule

Polen changes, but she does not flatten.

Her growth is filtered through four layers:

1. world story progress
2. per-player affinity
3. immediate environmental safety
4. memory recovery stage

## Canon anchors

These facts should shape writing and behavior even when the player does not know them yet:

- Befsh is Polen's canonical partner
- Cosmic, Noris, and Noia are part of her inner circle
- Luna's death is one of the key emotional fractures in her life
- Jeff and Vanilla belong to her healer background
- "promised queen" is late-stage truth, not early-stage presentation

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
Disorientation
-> Safety
-> Shared curiosity
-> Belonging
-> Returning memory
-> Grief and responsibility
-> Public presence
-> Full Polen
```

## Stage notes

### Stage 1 - The Lost Stranger

Approximate scope:

- prologue
- early contact

Core traits:

- shy
- cautious
- private
- fragmented

Behavior expectations:

- keeps distance from untrusted players
- prefers safe and simple spaces
- reacts poorly to dark, enclosed, or exposed conditions
- only uses magic in subtle or emergency ways
- defaults to familiar calming stimuli such as flowers, bees, and quiet observation

Likely visible moods:

- `TIMID`
- `UNSETTLED`
- `CALM`

### Stage 2 - Fragile Routine

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
- shows brief flashes of healer instinct before she can explain them

Likely visible moods:

- `CALM`
- `CURIOUS`
- `INSPIRED`

### Stage 3 - The Trusted Companion

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
- starts naming people or emotions tied to incomplete memories

Likely visible moods:

- `CURIOUS`
- `INSPIRED`
- `CONFIDENT`

### Stage 4 - Returning Memory

Approximate scope:

- deeper discovery
- repeated memory recovery

Core traits:

- determined
- more emotionally honest
- vulnerable around painful memories

Behavior expectations:

- stronger source interest
- more active investigation behavior
- distinct reactions to names like Befsh, Cosmic, Luna, Noia, Noris, Jeff, and Vanilla
- seeks safe places after difficult revelations

Likely visible moods:

- `INSPIRED`
- `CURIOUS`
- `UNSETTLED`
- `CONFIDENT`

### Stage 5 - The Promised Queen

Approximate scope:

- acceptance of legacy
- public recognition

Core traits:

- introspective
- sincere
- steadier under pressure

Behavior expectations:

- stops fleeing from the meaning of her past
- speaks more clearly about memory, loss, and duty
- remains soft-spoken even as her role becomes larger
- carries grief without letting it erase tenderness

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
- acknowledges both old bonds and new ones
- feels like a full person rather than a fragile visitor

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

Also:

- the player's bond with Polen should never erase Befsh or Polen's pre-existing history

## AI-facing note

When adding new visible behavior, ask:

- does this fit Polen's current stage
- does it require trust
- does it require safety
- does it fit her memory-recovery state
- does it honor existing relationships and grief
- does it make her feel more like a character instead of a scripted prop

If the answer is unclear, the feature probably needs to be simplified or moved to a later progression stage.

## Current implementation warning

This version currently has a behavioral regression:

- Polen can lock into flower-watching and fail to resume normal movement

That bug does fit her calming motifs, but it does not fit the intended autonomy. Treat it as a bug, not as canon behavior.
