# Modpack Context

## Why this exists

`Hives And Colonies: Polen` is not being designed in isolation.
It is part of the broader `Hives & Colonies` modpack ecosystem:

- Modpack page: `https://www.curseforge.com/minecraft/modpacks/hives-colonies`
- Mod list snapshot used here: user-provided on `2026-06-02`

This matters because Polen should not grow as if vanilla Minecraft were the only environment she will ever inhabit.
Her items, affinities, home logic, comfort logic, memory recovery, and future interactions should make sense inside the actual pack.

## Scope note

The repository is still Polen-first.

The canon is already wider than that:

- Hive is a multi-species world
- Polen carries lost memories from before the current world
- her backstory includes war, healer training, and named relationships
- a future rename such as `Hives & Colonies: Characters` is under consideration

So modpack-aware design should not assume that every system exists only to support a single shy companion loop forever.

## Design rule

When adding new systems, assume Polen may need to:

- recognize modded furniture, light, beds, doors, workspaces, and decorations
- react to modded food, farming, bees, and magic blocks
- wear or sync accessories through `Curios`
- coexist with colony structures, village overhauls, and player-built interiors
- associate places and objects with memory fragments, healing, grief, or belonging

Do not hardcode the future around vanilla-only assumptions if the pack already provides richer signals.

## Major integration domains

### Colony and settlement

- `MineColonies`
- `Structurize`
- `Domum Ornamentum`
- `BlockUI`
- `TownTalk`
- `FTB Teams`
- `FTB Quests`
- `ExtraQuests`

Planning impact:

- Polen should increasingly understand colony spaces as meaningful places, not generic shelters.
- Residence, comfort, and belonging should eventually recognize colony houses, decorated interiors, and team-owned spaces.
- Dialogue and progression can later acknowledge settlement growth.

### Magic and subtle arcana

- `Ars Nouveau`
- `Ars Elemental`
- `Ars Creo`

Planning impact:

- Source attunement, magical curiosity, healing identity, and future accessories should be able to react to Ars-adjacent blocks or spaces.
- Arcane identity should remain soft and character-driven, not become a combat kit by default.

### Bees, nature, and world identity

- `Productive Bees`
- `The Bumblezone`
- `Biolith`
- `Regions Unexplored`
- `Lithostitched`

Planning impact:

- Polen's apiarist and nature-facing identity should eventually read modded bee and biome content as real interests.
- Future world affinity rules should not stop at vanilla hives or flowers.
- Bee and flower motifs can also act as calming cues or memory triggers.

### Food, home, and daily life

- `Farmer's Delight`
- `More Delight`
- `Brewin' And Chewin'`
- `Farmer's Pizzeria`
- `Ars Nouveau's Flavors & Delight`
- `Macaw's Furniture`
- `Macaw's Doors`
- `Handcrafted`
- `Chipped`
- `Decorative Blocks Reborn`

Planning impact:

- Comfort and home evaluation should eventually recognize modded kitchens, tables, chairs, doors, storage, and decorated interiors.
- Polen's future hobbies, resting behavior, home dialogue, and recovery scenes should benefit from these richer domestic spaces.

### Travel and exploration

- `Waystones`
- `Xaero's Minimap`
- `Xaero's World Map`
- `ChoiceTheorem's Overhauled Village`
- `YUNG's Better Mineshafts`
- `YUNG's Better Dungeons`
- `Repurposed Structures`

Planning impact:

- Wayfinding, curiosity, and long-term travel can later anchor to waystones, villages, and exploration landmarks.
- Village-aware shelter logic should remain compatible with modded village layouts.

### Storage, logistics, and building workflow

- `Tom's Simple Storage`
- `Sophisticated Backpacks`
- `Sophisticated Core`
- `Soko Sort`
- `Pipez`
- `Create`
- `Carry On`
- `Multi-Piston`

Planning impact:

- Future Polen interactions with player bases should assume more than chest + furnace.
- If she ever manipulates or comments on player setups, she should understand that modded workspaces exist.

### Accessories and equipment

- `Curios API`
- `Relics`

Planning impact:

- Accessories should be first-class content, not an afterthought.
- Polen already uses affinity charms and a Curios bridge; future rings, necklaces, belts, and character accessories should keep building on that layer.

## What the codebase already reflects

The current implementation already has early pieces of this direction:

- `entity/ai/world/comfort/*`
  - semantic comfort scoring for places
- `entity/ai/world/identity/*`
  - world affinity and identity shaping
- `entity/ai/world/interests/*`
  - interest generation hooks
- `item/affinity/*`
  - affinity-themed charms
- `compat/curios/*`
  - Curios integration bridge

These systems should be treated as the foundation for future modpack-aware behavior.

## Planning rules

### Items

- Prefer item families over isolated one-off items.
- When adding wearable progression items, decide early whether the feature belongs in `accessory`, `affinity`, `colony`, or a future dedicated family.

### AI

- Shelter, comfort, and home logic should keep moving toward semantic places, not raw block checks.
- If a mod adds doors, beds, lights, furniture, apiaries, magical workspaces, or decorated interiors, assume Polen may eventually need to understand them.

### Dialogue

- Contextual dialogue should increasingly name the kind of place or object Polen understands.
- Good examples:
  - house
  - shelter
  - lantern
  - apiary
  - workshop
  - waystone

### Memory

- Modpack-aware spaces should eventually help stage memory recovery, not only utility behavior.
- Good triggers can include:
  - healing spaces
  - quiet decorated interiors
  - bee-heavy spaces
  - arcane study areas

### Scalability

- New integrations should usually land in `world/*`, `item/*`, or `compat/*`, not inside unrelated goals or entity code.
- If a feature only makes sense because the pack contains a certain category of mod, document that dependency here.
