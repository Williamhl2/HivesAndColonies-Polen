package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.comfort.PolenComfortCategory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.comfort.PolenComfortProfile;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.comfort.PolenComfortSignal;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

/**
 * Small gameplay hooks derived from the affinity charm carried by the unique Polen.
 *
 * This class intentionally lives in world/interests: it translates the visual affinity
 * into world interpretation, while equipment, Curios and rendering stay elsewhere.
 */
public final class PolenAffinityBehaviorHooks {
    private static final int OBSERVATION_RADIUS = 10;
    private static final int OBSERVATION_HEIGHT = 3;
    private static final int COMFORT_BONUS = 5;

    private PolenAffinityBehaviorHooks() {
    }

    public static String affinityReason(PolenWorldAffinity affinity) {
        return switch (safe(affinity)) {
            case APIARIST -> "bees are dominant interest";
            case ARCANE -> "magic is dominant interest";
            case COLONIAL -> "colonies are dominant interest";
            case HARVEST -> "food is dominant interest";
            case ARTISAN -> "decoration is dominant interest";
            case WAYFARER -> "exploration is dominant interest";
            case NONE -> "no affinity charm equipped";
        };
    }

    public static PolenAffordanceTarget findAffinityInterestTarget(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        PolenWorldAffinity affinity = safe(polen.getEquippedAffinityCharm());
        if (affinity == PolenWorldAffinity.NONE) {
            return null;
        }

        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        PolenAffordanceTarget best = null;
        double bestScore = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-OBSERVATION_RADIUS, -OBSERVATION_HEIGHT, -OBSERVATION_RADIUS),
                origin.offset(OBSERVATION_RADIUS, OBSERVATION_HEIGHT, OBSERVATION_RADIUS)
        )) {
            BlockState state = level.getBlockState(candidate);
            int strength = matchStrength(affinity, state);
            if (strength <= 0) {
                continue;
            }

            BlockPos usePos = resolveUsePos(polen, candidate);
            if (usePos == null) {
                continue;
            }

            double score = usePos.distSqr(origin) - strength * 2.0D;
            if (score < bestScore) {
                bestScore = score;
                best = new PolenAffordanceTarget(
                        candidate.immutable(),
                        usePos.immutable(),
                        affordanceType(affinity),
                        contextKey(affinity, state)
                );
            }
        }

        return best;
    }

    public static void collectComfortSignals(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        if (polen == null || level == null || origin == null || profile == null || signals == null) {
            return;
        }

        PolenWorldAffinity affinity = safe(polen.getEquippedAffinityCharm());
        if (affinity == PolenWorldAffinity.NONE) {
            return;
        }

        boolean found = false;
        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-profile.scanRadius(), -profile.verticalRadius(), -profile.scanRadius()),
                origin.offset(profile.scanRadius(), profile.verticalRadius(), profile.scanRadius())
        )) {
            if (matchStrength(affinity, level.getBlockState(candidate)) > 0) {
                found = true;
                break;
            }
        }

        if (found) {
            signals.add(new PolenComfortSignal(comfortCategory(affinity), "affinity_" + affinity.getSerializedName(), COMFORT_BONUS));
        }
    }

    public static String describeActiveTarget(PolenAffordanceType type, String context) {
        if (type == null) {
            return "none";
        }

        return switch (type) {
            case INTEREST_HIVE -> "apiarist:hive";
            case INTEREST_MAGIC -> "arcane:" + safeContext(context);
            case INTEREST_COLONY -> "colonial:" + safeContext(context);
            case INTEREST_FOOD -> "harvest:" + safeContext(context);
            case INTEREST_DECORATION -> "artisan:" + safeContext(context);
            case INTEREST_EXPLORATION -> "wayfarer:" + safeContext(context);
            case INTEREST_FLOWER -> "nature:flower";
            case INTEREST_SOURCE -> "source:" + safeContext(context);
            case EXISTING_LIGHT, MAGIC_LIGHT -> "light:" + safeContext(context);
            default -> safeContext(context);
        };
    }

    private static PolenWorldAffinity safe(PolenWorldAffinity affinity) {
        return affinity == null ? PolenWorldAffinity.NONE : affinity;
    }

    private static String safeContext(String context) {
        return context == null || context.isBlank() ? "none" : context;
    }

    private static BlockPos resolveUsePos(PolenEntity polen, BlockPos focusPos) {
        if (focusPos == null) {
            return null;
        }

        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = focusPos.offset(dx, dy, dz);
                    if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)) {
                        continue;
                    }

                    double score = candidate.distSqr(polen.blockPosition()) + candidate.distSqr(focusPos) * 0.35D;
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }

        return best;
    }

    private static int matchStrength(PolenWorldAffinity affinity, BlockState state) {
        if (state == null || state.isAir()) {
            return 0;
        }

        String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().toLowerCase(Locale.ROOT);
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);

        return switch (affinity) {
            case APIARIST -> scoreApiarist(state, namespace, path);
            case ARCANE -> scoreArcane(state, namespace, path);
            case COLONIAL -> scoreColonial(namespace, path);
            case HARVEST -> scoreHarvest(state, namespace, path);
            case ARTISAN -> scoreArtisan(namespace, path);
            case WAYFARER -> scoreWayfarer(state, namespace, path);
            case NONE -> 0;
        };
    }

    private static int scoreApiarist(BlockState state, String namespace, String path) {
        if (state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE)) {
            return 10;
        }
        if (namespace.equals("productivebees") || namespace.equals("the_bumblezone")) {
            return 8;
        }
        return containsAny(path, "hive", "apiary", "honey", "comb", "bee") ? 6 : 0;
    }

    private static int scoreArcane(BlockState state, String namespace, String path) {
        if (state.is(Blocks.ENCHANTING_TABLE) || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.BUDDING_AMETHYST) || state.is(Blocks.AMETHYST_CLUSTER)) {
            return 10;
        }
        if (namespace.equals("ars_nouveau") || namespace.equals("ars_elemental") || namespace.equals("ars_creo")) {
            return 8;
        }
        return containsAny(path, "rune", "glyph", "source", "mana", "arcane", "crystal", "amethyst") ? 6 : 0;
    }

    private static int scoreColonial(String namespace, String path) {
        if (namespace.equals("minecolonies") || namespace.equals("domum_ornamentum") || namespace.equals("structurize")) {
            return 8;
        }
        return containsAny(path, "colony", "townhall", "citizen", "builder", "warehouse", "guard", "tavern", "barrack") ? 6 : 0;
    }

    private static int scoreHarvest(BlockState state, String namespace, String path) {
        if (state.is(BlockTags.CROPS) || state.is(Blocks.FARMLAND) || state.is(Blocks.COMPOSTER)
                || state.is(Blocks.CAKE) || state.is(Blocks.SMOKER)) {
            return 8;
        }
        if (namespace.equals("farmersdelight") || namespace.equals("brewinandchewin") || namespace.equals("moredelight")
                || namespace.equals("farmers_pizzeria") || namespace.equals("ars_nouveau_flavors_delight")) {
            return 8;
        }
        return containsAny(path, "kitchen", "stove", "cooking", "feast", "pie", "cake", "crop", "wheat", "bread", "food") ? 6 : 0;
    }

    private static int scoreArtisan(String namespace, String path) {
        if (namespace.equals("handcrafted") || namespace.equals("chipped") || namespace.startsWith("mcw")
                || namespace.equals("decorative_blocks") || namespace.equals("decorative_blocks_reborn")) {
            return 8;
        }
        return containsAny(path, "chair", "table", "sofa", "bench", "carpet", "curtain", "ornament", "decor", "shelf") ? 6 : 0;
    }

    private static int scoreWayfarer(BlockState state, String namespace, String path) {
        if (state.is(Blocks.DIRT_PATH)) {
            return 7;
        }
        if (namespace.equals("waystones") || namespace.equals("repurposed_structures") || namespace.equals("ctov")) {
            return 8;
        }
        return containsAny(path, "waystone", "path", "road", "marker", "map", "compass", "dungeon", "mineshaft", "tower") ? 6 : 0;
    }

    private static PolenAffordanceType affordanceType(PolenWorldAffinity affinity) {
        return switch (affinity) {
            case APIARIST -> PolenAffordanceType.INTEREST_HIVE;
            case ARCANE -> PolenAffordanceType.INTEREST_MAGIC;
            case COLONIAL -> PolenAffordanceType.INTEREST_COLONY;
            case HARVEST -> PolenAffordanceType.INTEREST_FOOD;
            case ARTISAN -> PolenAffordanceType.INTEREST_DECORATION;
            case WAYFARER -> PolenAffordanceType.INTEREST_EXPLORATION;
            case NONE -> PolenAffordanceType.INTEREST_SOURCE;
        };
    }

    private static PolenComfortCategory comfortCategory(PolenWorldAffinity affinity) {
        return switch (affinity) {
            case APIARIST, WAYFARER -> PolenComfortCategory.NATURE;
            case ARCANE -> PolenComfortCategory.MAGIC;
            case COLONIAL -> PolenComfortCategory.COLONY;
            case HARVEST -> PolenComfortCategory.FOOD;
            case ARTISAN -> PolenComfortCategory.DECORATION;
            case NONE -> PolenComfortCategory.SPACE;
        };
    }

    private static String contextKey(PolenWorldAffinity affinity, BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        return switch (affinity) {
            case APIARIST -> "affinity_apiarist_" + compactContext(path);
            case ARCANE -> "affinity_arcane_" + compactContext(path);
            case COLONIAL -> "affinity_colonial_" + compactContext(path);
            case HARVEST -> "affinity_harvest_" + compactContext(path);
            case ARTISAN -> "affinity_artisan_" + compactContext(path);
            case WAYFARER -> "affinity_wayfarer_" + compactContext(path);
            case NONE -> "affinity_none";
        };
    }

    private static String compactContext(String path) {
        if (path == null || path.isBlank()) {
            return "block";
        }
        return path.length() <= 28 ? path : path.substring(0, 28);
    }

    private static boolean containsAny(String text, String... fragments) {
        for (String fragment : fragments) {
            if (text.contains(fragment)) {
                return true;
            }
        }
        return false;
    }
}
