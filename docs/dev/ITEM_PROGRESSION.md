# Item Progression

## Objective

Create item growth without turning every new mechanic into "one more random item".

The item layer should support:

- story pacing
- colony progression
- Ars Nouveau integration
- future crafting and ritual systems

It should not become a second quest log or a dumping ground for logic that belongs elsewhere.

## Core Rules

### 1. Every item needs a single primary role

Allowed primary roles:

- `story`
- `material`
- `focus`
- `colony`

If an item tries to be all of them at once, split it.

### 2. Behavior does not belong inside the item by default

The item can:

- carry metadata
- display tooltip and flavor
- trigger an intent on use

The item should not own:

- chapter logic
- affinity progression logic
- memory unlock rules
- colony state rules
- safety or AI behavior

Those belong in dedicated managers/controllers.

### 3. Not every item needs its own concrete class

Use a dedicated class only when the item has behavior.

If the item only needs:

- localization
- tooltips
- progression metadata

then a reusable base item is enough.

## Families

Implemented item families:

- `STORY`
- `MATERIAL`
- `FOCUS`
- `COLONY`

Current base classes:

- `PolenLoreItem`
- `PolenMaterialItem`
- `PolenUsableFocusItem`
- `PolenColonyItem`

These sit on top of `PolenTypedItem`, which stores:

- family
- progression stage
- unique vs repeatable intent

Current package split:

- `item/base`
- `item/meta`
- `item/story`
- `item/material`

Future behavior-heavy items should add:

- `item/focus`
- `item/colony`

## Stages

Implemented stages:

- `PROLOGUE`
- `ACT_I_FOUNDATION`
- `ACT_II_DISCOVERY`
- `ACT_III_COMPANIONSHIP`
- `ACT_IV_RESTORATION`
- `POSTGAME`

This stage metadata is not meant to hard-lock the player by itself.

Its purpose is to:

- organize content
- drive future loot tables and rewards
- prevent late-game item pollution in early chapters
- keep writers and coders aligned

## Current Item Mapping

### Story

- `princess_seal`
  - stage: `PROLOGUE`
  - unique: yes
  - role: identity clue
- `princess_letter`
  - stage: `PROLOGUE`
  - unique: yes
  - role: fragmented memory/lore bridge
- `polen_journal`
  - stage: `ACT_I_FOUNDATION`
  - unique: yes
  - role: intimate record and future reading anchor

### Material

- `royal_pollen`
  - stage: `ACT_II_DISCOVERY`
  - unique: no
  - role: rare material for future rituals, crafting or symbolic exchange
- `source_touched_petal`
  - stage: `ACT_II_DISCOVERY`
  - unique: no
  - role: soft Ars-linked ingredient
- `resonant_wax`
  - stage: `ACT_II_DISCOVERY`
  - unique: no
  - role: bee-linked ritual and crafting base

### Focus

- `bloom_focus`
  - stage: `ACT_II_DISCOVERY`
  - unique: no
  - role: helps Polen lock flowers, hives, and source-like resonances into memory

### Colony

- `settlement_charm`
  - stage: `ACT_I_FOUNDATION`
  - unique: no
  - role: lets the player propose a safe resting place for Polen inside a real settlement

## Planned Escalation

### Prologue / Act I

Focus:

- intimacy
- trust
- clues
- non-industrial objects

Recommended item types:

- letters
- seals
- sketches
- pressed flowers
- wax fragments
- simple keepsakes

### Act II

Focus:

- observation
- source
- early Ars resonance
- first colony-linked materials

Recommended item types:

- source-touched petals
- resonant wax
- marked comb fragments
- memory sketches
- simple magical foci

### Act III

Focus:

- active participation
- colony integration
- bee/magic synthesis

Recommended item types:

- colony tokens
- ritual chalk
- focus charms
- recovery fragments

### Act IV and Postgame

Focus:

- restoration
- legacy
- intentional magic
- long-term systems

Recommended item types:

- royal relics
- restoration seals
- advanced foci
- late-game symbolic materials

## Registration Rules

`ModItems` should stay grouped by family, not by accidental creation order.

Current grouping:

- story items
- material items
- debug/spawn support

When focus and colony items are added later, they should get their own grouped blocks and helper registrations.

## Data Tags

Item families are also exposed as datapack tags:

- `polen:story_items`
- `polen:material_items`
- `polen:focus_items`
- `polen:colony_items`

That gives future systems a stable lookup surface without hardcoding individual item ids.

## First Survival Recipes

The first functional wave now has simple base recipes so the system can be tested in survival:

- `royal_pollen`
  - honeycomb + dandelion + glowstone dust
- `source_touched_petal`
  - poppy + amethyst shard
- `resonant_wax`
  - honeycomb + royal pollen
- `bloom_focus`
  - amethyst shard + source-touched petals + royal pollen + stick
- `settlement_charm`
  - string + paper + resonant wax + royal pollen

These are intentionally provisional.

The goal is to make the item layer playable now, then tune balance after in-game testing.

## Practical Next Additions

The next safe batch should be small:

1. Two `story` items
   - one memory clue
   - one colony-adjacent keepsake
2. Two `material` items
   - one bee-linked
   - one Ars-linked
3. One `focus` item
   - low-power, intimate, non-combat
4. One `colony` item
   - settlement/social meaning, not just crafting filler

This keeps progression readable and avoids item inflation.
