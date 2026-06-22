# Dialogue localization

Polen's dialogue workflow now uses split source files instead of editing one large language file directly.

## Target source of truth

Use these files when adding or changing dialogue:

```text
src/main/resources/assets/hc_characters/lang_parts/<locale>/ambient.json
src/main/resources/assets/hc_characters/lang_parts/<locale>/chapters.json
src/main/resources/assets/hc_characters/lang_parts/<locale>/events.json
src/main/resources/assets/hc_characters/lang_parts/<locale>/memories.json
```

Non-dialogue translations live in:

```text
src/main/resources/assets/hc_characters/lang_base/<locale>.json
```

The final Minecraft files still live at:

```text
src/main/resources/assets/hc_characters/lang/<locale>.json
```

Minecraft and NeoForge still expect the final `lang/*.json` files at runtime.

## Current status

This workflow is now wired into the build:

- split dialogue files are the authoring source
- `lang_base/*.json` holds non-dialogue keys
- final `lang/*.json` files remain the runtime output
- `mergeHcCharactersLang` regenerates those runtime files automatically

The runtime side is also now wired:

- chapter lines are used for direct interaction when no stronger context takes priority
- ambient lines can fire from passive nearby behavior, not only from one-off scripted triggers
- interaction can prefer contextual lines when Polen is actively resting, observing, hiding, approaching, or doing a quiet activity
- memory lines can unlock from environmental discovery instead of only item-driven moments
- ambient dialogue can now resolve contextual beats inside one situation instead of relying only on affinity tone
- ambient broadcast now resolves one shared line per event, avoiding mismatched lines for nearby players
- anti-repeat state now reduces immediate reuse of the same ambient family / variation

## Build task

```text
./gradlew mergeHcCharactersLang
```

`processResources` depends on both `mergeCharactersLang` and `mergeHcCharactersLang`, so normal builds regenerate the final language files automatically.

## Practical rule

When touching dialogue:

1. Edit the split source files if they already exist for that locale.
2. Keep `lang/*.json` aligned with the generated output.
3. Verify the game-side code actually consumes the changed lines.
4. Update docs if the new lines change canon, memory reveals, or character relationships.

If you add a new dialogue family or a new situational file, update the runtime resolver too. Split files are only authoring sources until code maps gameplay states to those keys.

If you add a new ambient beat under an existing situation, update:

- `PolenDialogueBeatResolver`
- `PolenAmbientDialogueResolver` only if key structure changes
- the locale `ambient.json` files for every supported locale

Do not hand-edit `lang/*.json` unless you are intentionally repairing generated output after changing the split sources.

## Lore references

The dialogue continuity now needs to account for:

- Befsh
- Cosmic
- Luna
- Noia
- Noris
- Jeff
- Vanilla
- Polen's lost memories
- the war on Hive
- Polen's future "promised queen" role

These references should usually enter through:

- memory fragments
- affinity-gated dialogue
- ambient lines
- story events

The player should not receive the full backstory in one dump.

## Current runtime hooks

Gameplay currently routes dialogue through these channels:

- `chapters.json`: baseline interaction dialogue by chapter
- `ambient.json`: passive contextual speech and state-sensitive interaction lines
- `events.json`: fixed narrative sequences such as shelter recognition and name reveal
- `memories.json`: memory fragment lines unlocked by world discovery or story-adjacent actions

Ambient dialogue specifically now has three layers:

- situation selection
- beat selection inside that situation
- affinity tone fallback when a beat-specific key is not being used
