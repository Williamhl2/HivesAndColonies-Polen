package com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort;

import com.hivesandcolonies.characters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.characters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.interests.PolenAffinityBehaviorHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Locale;

final class PolenComfortRules {
    private PolenComfortRules() {
    }

    static final List<PolenComfortRule> DEFAULT = List.of(
            PolenComfortRules::collectSafety,
            PolenComfortRules::collectShelter,
            PolenComfortRules::collectLight,
            PolenComfortRules::collectSpace,
            PolenComfortRules::collectNearbyBlocks,
            PolenComfortRules::collectModdedSignals,
            PolenComfortRules::collectAffinitySignals
    );

    private static void collectSafety(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        if (!PolenSafetyEvaluator.isStandableSpot(polen, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SAFETY, "not_standable", -80));
        }
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SAFETY, "unsafe_standing_spot", -55));
        }
        if (PolenDangerMemoryTracker.isDangerousMemorySpot(polen, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SAFETY, "remembered_danger", -70));
        }
        if (level.canSeeSky(origin.above()) && level.isRaining()) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SAFETY, "exposed_to_rain", -20));
        }
    }

    private static void collectShelter(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        if (PolenSafetyEvaluator.isRainShelteredStandingSpot(level, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "rain_sheltered", 14));
        }
        if (PolenSafetyEvaluator.hasOverheadCover(level, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "overhead_cover", 8));
        }
        if (!level.canSeeSky(origin.above())) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "closed_sky", 6));
        }

        PolenShelterKind kind = PolenShelterContextResolver.resolveShelterKind(level, origin);
        if (kind == PolenShelterKind.HOUSE) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "house", 30));
        } else if (kind == PolenShelterKind.ROOF) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "roof", 16));
        } else if (kind == PolenShelterKind.TREE) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.NATURE, "tree_cover", 8));
        }

        if (PolenShelterContextResolver.isStrongHouseInterior(level, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "strong_interior", 22));
        }
        if (PolenShelterContextResolver.hasNearbyDoor(level, origin)) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SHELTER, "near_door", 6));
        }
    }

    private static void collectLight(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        int brightness = level.getMaxLocalRawBrightness(origin);
        if (brightness >= 13) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.LIGHT, "warm_bright", 16));
        } else if (brightness >= 9) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.LIGHT, "soft_light", 10));
        } else if (brightness <= 4) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.LIGHT, "too_dark", -10));
        }
    }

    private static void collectSpace(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        int habitable = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = origin.offset(dx, 0, dz);
                if (PolenSafetyEvaluator.isStandableSpot(polen, candidate)) {
                    habitable++;
                }
            }
        }

        if (habitable >= 7) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SPACE, "open_room", 14));
        } else if (habitable >= 4) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SPACE, "small_room", 8));
        } else {
            signals.add(new PolenComfortSignal(PolenComfortCategory.SPACE, "cramped", -10));
        }
    }

    private static void collectNearbyBlocks(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        boolean hasBed = false;
        boolean hasPolenLight = false;
        boolean hasCrafting = false;
        boolean hasStorage = false;
        boolean hasFood = false;
        boolean hasDecoration = false;
        boolean hasHive = false;
        boolean hasFlower = false;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-profile.scanRadius(), -profile.verticalRadius(), -profile.scanRadius()),
                origin.offset(profile.scanRadius(), profile.verticalRadius(), profile.scanRadius())
        )) {
            BlockState state = level.getBlockState(candidate);
            String path = state.getBlock().builtInRegistryHolder().key().location().getPath().toLowerCase(Locale.ROOT);

            hasBed = hasBed || state.is(BlockTags.BEDS);
            hasPolenLight = hasPolenLight || state.is(ModBlocks.POLEN_LANTERN.get());
            hasCrafting = hasCrafting || state.is(Blocks.CRAFTING_TABLE) || path.contains("workbench");
            hasStorage = hasStorage || path.contains("chest") || path.contains("barrel") || path.contains("shelf") || path.contains("cabinet") || path.contains("storage");
            hasFood = hasFood || path.contains("kitchen") || path.contains("stove") || path.contains("cooking") || path.contains("feast") || path.contains("pie") || path.contains("cake") || path.contains("honey");
            hasDecoration = hasDecoration || path.contains("chair") || path.contains("table") || path.contains("sofa") || path.contains("bench") || path.contains("carpet") || path.contains("curtain") || path.contains("shelf");
            hasHive = hasHive || path.contains("hive") || path.contains("nest") || path.contains("apiary");
            hasFlower = hasFlower || state.is(BlockTags.FLOWERS);
        }

        if (hasBed) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.REST, "near_bed", 22));
        }
        if (hasPolenLight) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.LIGHT, "polen_light", 20));
        }
        if (hasCrafting) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.COLONY, "work_surface", 5));
        }
        if (hasStorage) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.STORAGE, "near_storage", 5));
        }
        if (hasFood) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.FOOD, "food_or_kitchen", 8));
        }
        if (hasDecoration) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.DECORATION, "decorated", 6));
        }
        if (hasHive) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.NATURE, "near_hive", 10));
        }
        if (hasFlower) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.NATURE, "near_flowers", 5));
        }
    }

    private static void collectAffinitySignals(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        PolenAffinityBehaviorHooks.collectComfortSignals(polen, level, origin, profile, signals);
    }

    private static void collectModdedSignals(
            PolenEntity polen,
            Level level,
            BlockPos origin,
            PolenComfortProfile profile,
            List<PolenComfortSignal> signals
    ) {
        if (!profile.includeModdedSignals()) {
            return;
        }

        boolean colony = false;
        boolean magic = false;
        boolean handcrafted = false;
        boolean create = false;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-profile.scanRadius(), -profile.verticalRadius(), -profile.scanRadius()),
                origin.offset(profile.scanRadius(), profile.verticalRadius(), profile.scanRadius())
        )) {
            String namespace = level.getBlockState(candidate).getBlock().builtInRegistryHolder().key().location().getNamespace();
            colony = colony || namespace.equals("minecolonies") || namespace.equals("domum_ornamentum") || namespace.equals("structurize");
            magic = magic || namespace.equals("ars_nouveau") || namespace.equals("ars_elemental") || namespace.equals("ars_creo");
            handcrafted = handcrafted || namespace.equals("handcrafted") || namespace.equals("chipped") || namespace.startsWith("mcw") || namespace.equals("decorative_blocks");
            create = create || namespace.equals("create");
        }

        if (colony) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.COLONY, "colony_block", 12));
        }
        if (magic) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.MAGIC, "arcane_block", 8));
        }
        if (handcrafted) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.DECORATION, "modded_decoration", 8));
        }
        if (create) {
            signals.add(new PolenComfortSignal(PolenComfortCategory.COLONY, "create_machine", 4));
        }
    }
}
