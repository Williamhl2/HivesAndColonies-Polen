# Item Progression

## Objective

Grow the content layer without turning every new mechanic into "one more random item".

The item system should support:

- story pacing
- colony progression
- soft Ars-linked magic
- future equipment and accessory systems

It should not become:

- a second quest log
- a dumping ground for AI logic
- a misc folder with no taxonomy

## Current families

Implemented families:

- `STORY`
- `MATERIAL`
- `FOCUS`
- `COLONY`
- `ACCESSORY`

Current reusable item foundations live under:

- `item/base`
- `item/meta`
- `item/story`
- `item/material`
- `item/focus`
- `item/colony`
- `item/accessory`

## Core rules

### 1. Every item needs one primary role

Allowed primary roles:

- story
- material
- focus
- colony
- accessory

If an item tries to be all of them at once, split it.

### 2. Behavior should not live inside the item by default

The item may:

- carry metadata
- show tooltip and flavor
- trigger a simple interaction

The item should not own:

- chapter logic
- affinity progression
- AI state transitions
- residence validation
- safety logic

Those belong in managers, controllers, planners, or registries.

### 3. Not every item needs a unique class

Only create a dedicated class when the item has meaningful behavior.
If it only needs:

- lang
- tooltip
- family
- progression stage

then a reusable base class is enough.

## Current bases

- `PolenLoreItem`
- `PolenMaterialItem`
- `PolenUsableFocusItem`
- `PolenColonyItem`
- `PolenAccessoryItem`

These sit on top of `PolenTypedItem`, which stores:

- family
- progression stage
- unique vs repeatable intent

## Accessory groundwork

Accessory support is now a real part of the item layer.

Current support classes:

- `PolenAccessorySlot`
- `PolenAccessoryTarget`
- `PolenAccessoryBonusType`
- `PolenAccessoryBonus`
- `PolenAccessoryItem`

This is the base for:

- rings
- necklaces
- belts
- Polen-only accessories
- player-wearable accessories

The intended rule is:

- equipment identity belongs in `item/accessory`
- effect logic can be delegated elsewhere if it grows complex

## Current mapping

### Story

- `princess_seal`
- `princess_letter`
- `polen_journal`

### Material

- `royal_pollen`
- `source_touched_petal`
- `resonant_wax`

### Focus

- `bloom_focus`

### Colony

- `settlement_charm`
- `residence_charm`

### Utility block/item

- `polen_lantern`
  - managed by Polen's night-light behavior
  - still belongs to content, even though AI places and removes it

## Progression stages

Implemented stages:

- `PROLOGUE`
- `ACT_I_FOUNDATION`
- `ACT_II_DISCOVERY`
- `ACT_III_COMPANIONSHIP`
- `ACT_IV_RESTORATION`
- `POSTGAME`

Stage metadata is not a hard lock by itself.
It exists to:

- organize content
- guide balancing
- keep narrative and implementation aligned
- prevent late-stage item sprawl in early progression

## Registration rules

`ModItems` should stay grouped by family and purpose, not by accidental insertion order.

`ModBlocks` should follow the same rule for persistent companion-facing blocks such as `polen_lantern`.

When new accessories arrive:

- group them clearly in `ModItems`
- expose them through tags
- document slot and target expectations

## Tags

Families should remain queryable through datapack tags.

Current examples include:

- `polen:story_items`
- `polen:material_items`
- `polen:focus_items`
- `polen:colony_items`
- `polen:accessory_items`

This gives future systems a stable lookup surface without hardcoding ids.

## Practical next additions

The next safe batch should remain small and structured:

1. One or two accessories with clear slot identity.
2. One colony-adjacent item that interacts with residence or belonging.
3. One focus or material item tied to source or bee memory.
4. No new item should be added without deciding its family first.

Status update:

- `residence_charm` now fills that colony-adjacent residence slot.
- Residence validation still belongs in AI/world managers, not in the item class.
