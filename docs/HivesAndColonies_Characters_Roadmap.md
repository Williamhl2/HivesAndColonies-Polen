# Hives & Colonies: Characters — Development Roadmap

> Language: Spanish-first development notes with English code/package names preserved where useful.  
> Scope: `Hives & Colonies: Characters`, currently centered on Polen and SoaMarjorie.

---

## 0. One-page direction summary

**Hives & Colonies: Characters** should become the narrative character layer of the Hives & Colonies ecosystem: not villagers, pets, or generic followers, but readable characters with memory, relationship progression, behavior, routines, world interaction, and character-specific fantasy.

The project direction is broader than Polen alone. Polen remains the first persistent story companion, while SoaMarjorie proves the value of focused encounter-based characters. Future characters should reuse common systems instead of copying one-off logic.

### Core fantasy

- **Polen**: persistent companion, lost survivor, cautious, vulnerable, memory recovery, bees, nature, Source, healing, home/refuge, trust, emotional progression.
- **SoaMarjorie**: temporary encounter character, legendary miner, mentor, humor, mining, Bountiful boards, caves, practical trust, limited rewards.
- **Future characters**: each gets a strong loop and personality, but uses shared foundations for memory, relationship, feedback UI, persistence, and safe interaction patterns.

### Current strategic problem

SoaMarjorie feels stronger than Polen despite much less development time because Soa has a clear loop:

```text
appear -> do something concrete -> react to player -> reward/affinity -> despawn
```

Polen has many sophisticated systems, but they are not yet connected into a clear playable loop:

```text
find Polen -> earn minimum trust -> guide her to safety -> assign her bed/refuge -> build relationship -> unlock memories/routines
```

The next major development goal is **Polen Functional Core**, not more story content yet.

---

## 1. Non-negotiable project rules

These decisions are locked unless explicitly revisited.

### 1.1 SoaMarjorie visual lock

Do not modify the final approved transforms for:

- belt pickaxe
- belt axe
- backpack
- hand item visual states
- tool belt layer positioning/rotation/scale

The axe and pickaxe placement is approved. Future Soa work should focus on behavior, balance, dialogue, mining, combat, affinity, and spawn rules only.

### 1.2 No heavy world scans from items

Items must never do expensive work from:

- `inventoryTick`
- every server tick
- repeated right-click without cooldown
- huge global entity scans
- large unbounded biome/block searches

Known failure: the old Hiveheart/Polen compass behavior froze the server by doing excessive work and still failed to find Polen.

Allowed patterns:

- one-shot bounded search
- cached `SavedData`
- known UUID lookup
- known last position lookup
- small-radius checks with cooldown
- fail fast if data is unavailable

### 1.3 Polen home/refuge must be player-driven

Polen must not automatically generate or regenerate houses.

Locked rule:

```text
Polen's Bee Bed is her real refuge/home.
```

The player should create or place the refuge. Polen should recognize and use it. No manager should rebuild houses after chunk reloads, entity reloads, or Carry On movement.

### 1.4 Hiveheart is a one-shot prologue activator

The Hiveheart Charm should be simple:

```text
first use -> find nearby cherry grove -> spawn Polen -> save UUID/position -> become dormant
```

After Polen exists in the world, the item is no longer a general locator or content generator for that save.

### 1.5 Affinity must be earned through meaningful events

Repeated dialogue/right-click must not grant unlimited affinity.

Affinity should come from event hooks with cooldowns, caps, or one-time flags:

- first meeting
- assigning refuge
- safe follow/trust walk success
- gifts with daily/typed cooldowns
- defending Polen
- unlocking memory
- mining together with Soa
- receiving Soa's shared mining drops
- attacking a character lowers affinity

### 1.6 Common systems first, character logic second

If a system is useful for more than one character, it should live in a common package and expose character-specific hooks.

Common systems should include:

- relationship/affinity storage
- memory flags and counters
- HUD overlay notifications
- cooldown helpers
- interaction routing
- gift handling foundation
- follow/escort foundation if reusable
- safe bounded search helpers

Character-specific systems should include:

- tone and dialogue
- accepted gifts and weights
- event names/reasons
- AI goals and behavior profile
- story progression
- special abilities

---

## 2. Current implemented/near-current state

This section reflects the development direction and patches discussed so far.

### 2.1 SoaMarjorie beta state

SoaMarjorie is the current successful vertical slice.

Implemented/planned as beta-ready:

- canonical ID: `hc_characters:soa_marjorie`
- encounter-based NPC, not a permanent natural spawn
- Bountiful board encounters in villages
- cave encounters from `Y <= 0`
- no biome modifier natural spawning
- temporary encounter duration and despawn
- persistent cooldowns by player and board/location
- real limited mining of exposed ores
- no Fortune/Silk Touch
- partial reward sharing with nearby player
- torch placement in cave mode
- pickaxe state while mining
- axe state while defending
- defensive behavior vs hostile mobs and attacking players
- nearly indestructible during encounters
- shared NPC affinity overlay support
- dialogue tone: legendary mining mentor with humor

Soa's design rule:

```text
The player may earn respect, but never surpass SoaMarjorie as the world's best miner.
```

### 2.2 Polen current state

Polen has many systems, but the player-facing core is not functional enough yet.

Existing systems/features in codebase or documentation:

- persistent entity
- story-based name reveal
- per-player relationship/affinity
- world-level story flags and chapter data
- profile UI opened by right-click
- needs/intent/task/mood/autonomy systems
- quiet activities: singing, drawing, reflecting, source attunement
- safety behavior, danger memory, blink escape
- Polen lantern behavior
- residence/home-related classes and items
- Curios/accessory support
- multiple item families: focus, colony, material, story, accessory
- debug commands for AI/progression/world data

Current issues to fix before expanding story:

- UI affinities are visible but not meaningfully used in gameplay.
- Polen cannot reliably receive items/gifts.
- Polen cannot be asked to follow the player to a safe place.
- Polen's Bee Bed is not yet the clear canonical refuge loop.
- Many items exist but lack clear purpose/tooltips/interaction outcomes.
- Old automatic prologue/refuge behavior caused lag and duplicate houses.
- Some systems attempt to be too autonomous before the basic loop is understandable.

---

## 3. Desired Polen vertical slice

Polen needs a clear loop equal in clarity to Soa's loop, but matching Polen's identity.

### 3.1 Target player experience

```text
1. Player uses Hiveheart Charm.
2. Polen appears once in a nearby cherry grove.
3. Polen is cautious and does not fully trust the player.
4. Player interacts gently, offers suitable gifts, and avoids danger.
5. At minimum trust, Polen accepts a short guided walk.
6. Player places or prepares Polen's Bee Bed.
7. Player binds the Bee Bed as Polen's refuge/home.
8. Polen begins using the refuge for safety, rest, routines, and emotional stability.
9. As trust grows, Polen unlocks memories, calmer routines, and small useful behaviors.
```

### 3.2 Polen should feel different from Soa

Soa is action, mastery, mining, humor.

Polen is:

- cautious
- soft
- observant
- healing-oriented
- tied to bees/nature/Source
- emotionally progressive
- persistent
- home/refuge-centered
- memory-driven

Polen should not become a combat follower or resource generator.

---

## 4. Technical architecture targets

### 4.1 Common relationship system

Keep or complete a common system similar to:

```text
common/npc/relationship/NpcRelationshipManager.java
common/npc/relationship/NpcRelationshipSavedData.java
common/npc/relationship/NpcRelationshipRecord.java
common/npc/relationship/NpcRelationshipLevels.java
```

Storage key:

```text
characterId + playerUuid
```

Examples:

```text
hc_characters:polen -> player UUID -> relationship record
hc_characters:soa_marjorie -> player UUID -> relationship record
```

Each record should support:

- affinity value
- interaction count
- last interaction game time
- flags/memories
- counters
- cooldown timestamps
- last seen character position if relevant

Character-specific wrappers may remain temporarily for compatibility:

- `PolenPlayerRelationshipManager`
- `SoaMarjorieRelationship`

But the long-term direction is common storage with character-specific adapters.

### 4.2 Affinity HUD overlay

Common overlay should remain reusable for all characters.

Requirements:

- compact text
- message wrapping
- small enough to fit in HUD
- queue multiple notifications
- show positive/negative delta
- show relationship progress bar
- show level-up state
- configurable duration
- can be disabled in config

Config goals:

```toml
[relationship_feedback]
show_affinity_notifications = true
show_affinity_debug_chat = false
affinity_notification_duration_ticks = 90
```

### 4.3 Interaction routing

Characters need predictable interaction routing.

For Polen:

```text
right-click empty hand -> dialogue/profile UI
shift + right-click empty hand -> toggle Trust Walk if unlocked
right-click with known item -> item interaction handler
right-click with unknown item -> dialogue/no-recognition response
```

Avoid one controller doing everything inline. Suggested layout:

```text
character/polen/entity/PolenInteractionController.java
character/polen/item/interaction/PolenItemInteractionController.java
character/polen/entity/ai/follow/PolenTrustWalkController.java
character/polen/entity/ai/world/home/PolenHomeManager.java
```

### 4.4 SavedData rules

World-level persistent data should store:

- unique Polen UUID
- last known Polen position
- whether Hiveheart has already awakened Polen
- whether auto prologue generation is disabled/completed
- Polen's home/refuge dimension and block position
- any chapter/story flags
- any migration markers

Do not depend only on entity NBT for world-critical state.

---

## 5. Polen roadmap in phases

## Phase P0 — Stabilization and cleanup

Goal: stop Polen from breaking worlds/servers.

### Tasks

- Ensure Hiveheart does not call heavy logic in `inventoryTick`.
- Ensure Hiveheart does not scan huge areas repeatedly.
- Ensure Hiveheart cannot spawn duplicate Polen if a UUID/known world state already exists.
- Ensure prologue managers do not regenerate houses.
- Ensure old house/refuge generation code is disabled or gated behind explicit debug-only command.
- Save Polen's last known position periodically or on meaningful events.
- Make `/characters locate` use saved UUID/last position safely.
- Confirm chunk unload/reload does not create duplicates.
- Confirm Carry On movement does not trigger new house generation.

### Acceptance criteria

- Using Hiveheart never freezes the server.
- Polen spawns at most once per world through normal gameplay.
- Moving Polen away from her original cherry grove never creates another house.
- Leaving and returning to Polen's chunk never creates another house.
- If Polen is unloaded, systems fail gracefully instead of scanning the world.

### Test scenarios

1. New world -> use Hiveheart -> Polen appears once.
2. Use Hiveheart again -> dormant/no effect.
3. Move 500+ blocks away -> return -> no duplicate house.
4. Move Polen with Carry On -> reload area -> no duplicate house.
5. Restart server -> use Hiveheart -> no duplicate.
6. `/characters locate` works or reports known unloaded position without lag.

---

## Phase P1 — Polen Functional Core

Goal: make Polen's first playable loop functional.

### Tasks

- Define minimum trust threshold for guided walk.
- Add `Trust Walk` / gentle follow mode.
- Add clear player action to assign Polen's Bee Bed as refuge.
- Make Polen recognize her assigned bed in UI and AI.
- Connect refuge assignment to affinity and memory.
- Add visible feedback when refuge is assigned.
- Add dialogue for no home, home assigned, home unsafe, and home remembered.

### Trust Walk behavior

Activation:

```text
shift + right-click empty hand on Polen
```

Conditions:

- player has minimum trust
- Polen is not panicking
- Polen is not sleeping
- Polen is not recently hurt
- no immediate hostile threat nearby

Behavior:

- Polen follows at cautious distance
- temporary duration, e.g. 2-5 minutes
- cancels if player goes too far
- cancels if danger appears
- cancels if Polen reaches assigned bed/refuge
- should not teleport except maybe emergency anti-stuck with strict cooldown

Messages:

```text
Polen seems willing to follow for a little while.
Polen hesitates. She does not trust this yet.
Polen stops following and looks for safety.
```

### Acceptance criteria

- Player can guide Polen from cherry grove to a base without Carry On.
- Polen does not behave like a pet; she follows cautiously and temporarily.
- Player can assign a bed/refuge and see it in her UI.
- Polen returns to or stays near the bed when calm/resting.

---

## Phase P2 — Polen Bee Bed as canonical refuge

Goal: make the existing bed item matter.

### Canonical rule

```text
Polen Bee Bed = Polen's home/refuge anchor.
```

### Interaction options

Option A: Residence Charm binds the bed.

```text
Use Residence Charm on Polen Bee Bed while Polen is nearby -> bind refuge
```

Option B: Shift-click Polen Bee Bed directly.

```text
Shift-right-click Polen Bee Bed while Polen is nearby -> bind refuge
```

Recommended: support both if not too complex, with Residence Charm as the lore-friendly route.

### Validation

A valid refuge should check:

- Overworld or allowed dimension
- block is a Polen Bee Bed
- solid/safe access nearby
- no lava/fire immediately adjacent
- enough space for Polen
- not inside water
- not blocked
- optional comfort bonuses for flowers, light, bee-related blocks

Avoid overly strict validation. The player should understand why it fails.

### Data to save

- bed position
- dimension
- assigned by player UUID
- assigned game time
- comfort score at assignment
- whether Polen has slept/rested there

### UI behavior

Polen's profile HOME tab should show:

- no home assigned
- home position/dimension
- comfort/safety state
- whether Polen can path to it if loaded
- last time Polen used it

### Acceptance criteria

- Bed assignment is obvious and reliable.
- Polen UI updates after assignment.
- Polen uses the bed/refuge for rest/safety behavior.
- Breaking/removing bed marks home invalid rather than crashing or regenerating structures.

---

## Phase P3 — Item interaction and gift system

Goal: make existing items meaningful without removing them.

### Interaction router

Create or complete `PolenItemInteractionController`.

Flow:

```text
player right-clicks Polen with item
-> identify item type/tag
-> check cooldown/trust/conditions
-> apply result
-> consume item if appropriate
-> send dialogue/overlay
```

### Gift rules

- repeated gifts should have diminishing returns
- daily cap for relationship gain
- separate cooldowns by item category
- some items increase interests more than general trust
- rare story items may unlock memories instead of raw affinity
- no gift spam exploit

### Suggested gift categories

| Category | Example items | Effect |
|---|---|---|
| Gentle food | bread, sweet berries, honey bottle | small trust, comfort |
| Bees | honeycomb, honey bottle, resonant wax | bee interest, trust |
| Nature | flowers, petals, saplings | nature interest |
| Source | source-touched petal, amethyst-like/magic items | Source interest, memories |
| Home | lantern, candle, bed-related items | home/safety interest |
| Story | journal, letter, seal | chapter/memory progression |

### Suggested tags

```text
data/hc_characters/tags/items/polen_gifts_bees.json
data/hc_characters/tags/items/polen_gifts_nature.json
data/hc_characters/tags/items/polen_gifts_source.json
data/hc_characters/tags/items/polen_gifts_food.json
data/hc_characters/tags/items/polen_gifts_home.json
```

### Acceptance criteria

- Polen accepts at least 5 meaningful vanilla/mod items.
- The UI/overlay shows when an item affects trust or interests.
- Repeating the same item cannot max affinity quickly.
- Unknown items produce a gentle response or normal dialogue, not a bug.

---

## Phase P4 — Make Polen's UI actionable

Goal: the profile screen should explain and reflect gameplay.

### Current issue

Polen has a UI showing affinities/interests, but those values are not yet strongly connected to behavior or actions.

### Profile screen should show

- relationship level with current player
- next relationship threshold
- major memories unlocked
- home/refuge status
- current mood/intention summary
- interest levels and what they influence
- current needs in readable form
- whether Trust Walk is available
- whether gifts are on cooldown

### UI should not be debug-only

Technical data may remain behind a debug toggle, but player-facing UI should answer:

```text
What does Polen need?
Does she trust me?
Does she have a safe home?
What has she remembered?
What can I do next?
```

### Acceptance criteria

- A player can understand Polen's state without reading source code.
- The UI exposes at least one clear next action.
- Affinities/interests shown in UI influence behavior or unlocks.

---

## Phase P5 — Connect affinities/interests to behavior

Goal: make Polen's displayed affinities matter.

### Recommended conceptual split

Relationship with player:

- trust/confidence toward that player
- unlocks follow, gifts, closeness, story moments

Polen's interests/affinities:

- bees
- nature
- Source
- home/refuge
- safety
- memory/story

### Behavior hooks

#### Bees affinity

- more likely to observe hives/bee nests
- accepts bee-related gifts better
- unlocks bee memory fragments
- may calm around friendly bee spaces

#### Nature affinity

- more likely to observe flowers, cherry groves, gardens
- accepts flowers/petals better
- unlocks nature memory fragments

#### Source affinity

- more likely to attune/reflect near magical blocks/items
- unlocks Source memory fragments
- may trigger soft visual/ambient behaviors later

#### Home/refuge affinity

- more likely to use bed/refuge
- panic recovery faster near home
- nighttime routines become calmer

#### Safety affinity

- reacts less fearfully around trusted player
- safer follow availability
- reduced panic after danger if player protected her

### Acceptance criteria

- Each visible affinity has at least one gameplay effect.
- Interests are affected by gifts, places, and events.
- Polen's behavior visibly changes over time.

---

## Phase P6 — Polen narrative progression after functional core

Goal: only add more story once gameplay loop is stable.

### Story unlock triggers

- first meeting
- first safe gift
- first bed/refuge assignment
- first night slept safely
- first bee-related memory
- first Source-related memory
- first successful Trust Walk
- high trust threshold
- chapter threshold

### Memory fragments should be small

Avoid giant exposition early. Use fragments tied to visible actions.

Examples:

- near bees: remembers a sound, a title, a duty
- near bed/refuge: remembers safety or loss
- after gift: remembers kindness or ceremony
- after danger: remembers running, hiding, healing

### Acceptance criteria

- Story progress follows player actions.
- Memories are contextual and do not spam.
- Player understands why a memory triggered.

---

## Phase P7 — Polen AI polish

Goal: once core loop works, make Polen feel alive without adding instability.

### Priority routines

- stay near assigned refuge when calm
- move closer to trusted player only in safe contexts
- move away from unknown/aggressive players
- observe flowers/bee blocks
- sit/rest near bed
- light lantern at night if safe
- sing/draw rarely when calm
- blink away from danger
- return home if scared and path is reasonable

### Avoid for now

- complex global search for story sites
- automatic structure placement
- long-distance pathing to unloaded locations
- heavy AI every tick
- too many simultaneous activity controllers

### Acceptance criteria

- Polen's behavior is readable.
- No server lag from idle AI.
- Routines are visible but not noisy.

---

## 6. SoaMarjorie maintenance roadmap

Soa is beta-functional. Continue only polish and balance.

### Short-term checks

- Verify cave spawn only at `Y <= 0`.
- Verify Soa only spawns in caves when exposed ore exists nearby.
- Verify torch placement works in darkness.
- Verify mining goal reaches actual block breaking.
- Verify she uses pickaxe while mining.
- Verify she uses axe when attacked or threatened.
- Verify hostile mobs can target her but cannot realistically kill her.
- Verify board cooldown prevents immediate respawn.
- Verify cave cooldown prevents farming.
- Verify drops are shared at roughly 1/4 without duplication.

### Future polish

- Add more mining mentor dialogue by affinity tier.
- Add mining sound/particle pacing before block break.
- Add special ore cost weights: diamond/emerald count as more mining budget.
- Add optional config for mineable tag contents.
- Add debug logs for skipped encounter reasons.
- Add rare lines when player survives near lava/gravel/mobs.

### Do not change

- belt tool position
- belt axe orientation
- belt pickaxe orientation
- backpack
- visual layer transforms

---

## 7. Item purpose map

Existing items should not be removed casually. They should receive clear purposes, tooltips, and interactions.

| Item | Intended purpose | Status goal |
|---|---|---|
| `Hiveheart Charm` | One-shot Polen awakening in nearby cherry grove | Make dormant after Polen exists |
| `Polen Bee Bed` | Polen's canonical home/refuge anchor | Must become core loop item |
| `Residence Charm` | Bind Polen Bee Bed or safe location as residence | Should prioritize Polen Bee Bed |
| `Settlement Charm` | Temporary safe area/settlement marker | Define later; avoid overlap with bed |
| `Bloom Focus` | Mark/inspect natural/bee/flower points of interest | Connect to nature/bee interest |
| `Affinity Charm` / `PolenAffinityCharmItem` | Curios/accessory affecting expression/affinities | Keep, clarify effects |
| `Source-Touched Petal` | Source/magic gift or crafting material | Connect to Source interest/memory |
| `Resonant Wax` | Bee-related material/gift | Connect to bee interest/crafting |
| `Royal Pollen` | Rare story/crafting/progression item | Gate important memory/crafting |
| `Polen Journal` | Memory/story record | Should show unlocked memories |
| `Princess Letter` | Story item | Trigger/reflect narrative memory |
| `Princess Seal` | Advanced story item | Reserve for later chapter |
| `Polen Lantern` | Safe night lighting / comfort | Keep as Polen-safe utility |

### Tooltip standard

Every item should answer:

```text
What is this for?
Does it require Polen nearby?
Is it one-use, reusable, or dormant?
Does it affect trust, interest, home, or story?
```

---

## 8. Package/class areas to inspect

This list helps resume work quickly.

### Polen entity and interaction

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/PolenEntity.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/PolenInteractionController.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/PolenGoalRegistry.java
```

### Polen UI

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/client/profile/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/client/screen/PolenProfileScreen.java
```

### Polen home/refuge

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/block/PolenBeeBedBlock.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/world/home/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/colony/ResidenceCharmItem.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/colony/SettlementCharmItem.java
```

### Polen items

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/interaction/PolenItemInteractionController.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/focus/HiveheartCharmItem.java
src/main/java/com/hivesandcolonies/hccharacters/character/polen/item/focus/BloomFocusItem.java
```

### Polen progression

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/progression/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/progression/player/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/progression/world/
```

### Polen AI systems

```text
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/core/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/brain/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/navigation/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/world/
src/main/java/com/hivesandcolonies/hccharacters/character/polen/entity/ai/expression/
```

### SoaMarjorie

```text
src/main/java/com/hivesandcolonies/hccharacters/character/soa/entity/SoaMarjorieEntity.java
src/main/java/com/hivesandcolonies/hccharacters/character/soa/dialogue/SoaMarjorieDialogue.java
src/main/java/com/hivesandcolonies/hccharacters/character/soa/world/SoaMarjorieEncounterManager.java
src/main/java/com/hivesandcolonies/hccharacters/character/soa/client/layer/SoaMarjorieMiningGearLayer.java
```

Do not edit the visual transforms in `SoaMarjorieMiningGearLayer` unless explicitly requested.

---

## 9. Testing checklist before each beta release

### Build checks

```powershell
.\gradlew.bat clean build
```

If tests fail because of old package names, ensure tests use:

```text
com.hivesandcolonies.hccharacters
```

not:

```text
com.hivesandcolonies.characters
```

### Polen tests

1. New world: Hiveheart spawns Polen in/near cherry grove.
2. Hiveheart second use: dormant/no duplicate.
3. Move far away and return: no duplicate house.
4. Move Polen with Carry On: no duplicate house.
5. Restart server: no duplicate Polen/house.
6. Right-click empty hand: UI/dialogue works.
7. Shift-right-click after enough trust: Trust Walk toggles.
8. Shift-right-click before enough trust: fails with clear message.
9. Polen Bee Bed assignment works.
10. Removing bed invalidates home safely.
11. Gift item accepted only if valid and cooldown allows.
12. Repeated gift/right-click cannot max affinity.
13. UI shows home/trust/memories accurately.
14. No lag spikes from idle Polen.

### Soa tests

1. Bountiful board spawn works.
2. Board encounter gives only one reward.
3. Board cooldown prevents immediate respawn.
4. Cave spawn only occurs `Y <= 0`.
5. Cave spawn requires exposed ore nearby.
6. Soa places torches in darkness.
7. Soa mines exposed ore.
8. Soa uses pickaxe while mining.
9. Soa uses axe when attacked/threatened.
10. Hostile mobs can target Soa.
11. Soa survives hostile mobs easily.
12. Player gets partial shared drops, no duplication.
13. Soa despawns after encounter.
14. Affinity overlay appears only for meaningful events.

### Multiplayer/server checks

1. Two players near same board: no duplicate Soas.
2. Board cooldown persists after restart.
3. Player cooldown persists after restart.
4. Polen relationship is per-player.
5. Soa relationship is per-player.
6. No expensive scans when players hold items.
7. Spark or logs show no repeated heavy tick logic.

---

## 10. Performance rules

### Avoid

- huge `AABB` entity scans
- biome searches on every tick
- block scans without radius/cooldown
- pathing to unloaded or very distant targets
- item logic in `inventoryTick` beyond visual/simple counters
- automatic structure generation after initial setup
- repeated world modification from AI managers

### Prefer

- `SavedData` caches
- known UUID lookup
- known last position
- small-radius scans
- explicit player-triggered actions
- cooldowns
- debug logging for skipped reasons
- fast failure if data is missing

### Debug config suggestion

```toml
[debug]
characters_debug = false
polen_debug = false
soa_marjorie_debug = false
log_skipped_encounters = false
log_relationship_changes = false
```

---

## 11. Release milestone plan

### Beta A — SoaMarjorie public beta

Status: mostly complete.

Includes:

- Soa board encounters
- Soa cave mining
- Soa torch placement/combat
- Soa affinity overlay
- cooldowns and balance

### Beta B — Polen stabilization

Focus:

- Hiveheart one-shot
- no duplicate houses
- no global scan lag
- last known position
- no automatic structure regeneration

### Beta C — Polen Functional Core

Focus:

- Trust Walk
- Bee Bed home assignment
- item interaction router
- first gifts
- UI home/trust clarity

### Beta D — Polen Affinity Integration

Focus:

- common relationship integration
- UI values affect behavior
- interests: bees/nature/source/home/safety
- gift cooldowns and caps
- memory flags

### Beta E — Polen Memory & Routine Polish

Focus:

- first memory fragments tied to player actions
- refuge routines
- calm behaviors
- bee/nature observations
- Source attunement moments

### Beta F — Wider cast foundation

Focus:

- common character interfaces
- reusable relationship/memory/interaction APIs
- define first minimal shells for Befsh/Luna/Noia/Noris without overbuilding

---

## 12. Open design decisions

These need final decisions later.

1. Should Polen's Trust Walk require a specific affinity threshold, a story flag, or both?
2. Should the Residence Charm be required to bind the Bee Bed, or should direct bed interaction be enough?
3. Should Polen accept vanilla flowers as gifts from the start?
4. Should gifts consume the item always, or only when accepted?
5. Should Polen's UI show exact numbers or descriptive levels?
6. Should Polen's home be changeable freely, cooldown-gated, or require trust?
7. Should Polen teleport home only if unloaded/stuck, or never teleport?
8. Should Soa's Bountiful board spawn prefer boards with mining-related decrees later?
9. Should future characters be persistent like Polen, temporary like Soa, or mixed?
10. How much of Polen's old specific affinity system should be migrated to common relationship storage?

---

## 13. Documentation tasks

The public README already frames the mod as `Hives & Colonies: Characters`, a narrative NeoForge mod for Minecraft 1.21.1 that began Polen-first but is widening toward a broader cast. Keep documentation aligned with that direction.

### Required docs to maintain

```text
docs/ROADMAP.md
docs/dev/technical-overview.md
docs/dev/codebase-map.md
docs/dev/polen-functional-core.md
docs/dev/relationship-system.md
docs/es/story-bible.md
docs/en/story-overview.md
```

### Documentation policy

- Spanish first for lore and narrative direction.
- English also maintained for public/collaboration docs.
- Every gameplay item should have a doc entry and tooltip.
- Every character should have:
  - design fantasy
  - gameplay loop
  - relationship model
  - accepted interactions
  - forbidden behaviors
  - release readiness checklist

---

## 14. Quick resume instructions

If development is resumed after losing context, do this:

1. Do not touch SoaMarjorie's item visuals.
2. Verify current build with `./gradlew.bat clean build`.
3. Confirm Hiveheart does not lag and does not duplicate Polen.
4. Work on `Polen Functional Core` before adding more story.
5. Implement or finish Trust Walk.
6. Make Polen Bee Bed the canonical home/refuge.
7. Make item interactions/gifts work through a router.
8. Connect UI affinities to behavior and gift/event effects.
9. Add tooltips to unclear items.
10. Keep all heavy operations bounded and cached.

The guiding question for every new feature:

```text
Does this make the character more readable, more playable, or more alive without adding hidden server risk?
```

If the answer is no, defer it.

---

## 15. Short version for issue tracker

```text
Goal: Polen Functional Core

Polen must become a clear playable companion loop:
Hiveheart awakens Polen once -> player earns initial trust -> Polen can be guided with Trust Walk -> player assigns Polen Bee Bed as refuge -> UI shows home/trust/interests -> gifts and events affect relationship/interests -> memories unlock from meaningful actions.

Rules:
- no automatic house generation
- no global scans from items
- no affinity from spam-click dialogue
- use common relationship/HUD systems
- preserve existing items by giving them clear purpose
- SoaMarjorie visual item transforms are locked
```

---

## References

- Current public repository direction: `Hives & Colonies: Characters` is described as a narrative NeoForge mod for Minecraft 1.21.1, currently led by Polen but expanding to a wider cast.
- Existing docs/README direction emphasizes that the goal is not a villager with dialogue, but a readable character whose behavior, emotional state, memory recovery, relationships, and narrative role feel connected.
- Release history already introduced Polen entity/progression framework, AI systems, items, and documentation; SoaMarjorie is the newer beta direction discussed in development planning.
