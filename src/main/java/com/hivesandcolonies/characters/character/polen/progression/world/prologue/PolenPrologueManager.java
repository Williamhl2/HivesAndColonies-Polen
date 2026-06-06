package com.hivesandcolonies.characters.character.polen.progression.world.prologue;

import com.mojang.datafixers.util.Pair;
import com.hivesandcolonies.characters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.characters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.characters.bootstrap.registry.ModItems;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenResidenceValidation;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenResidenceValidator;
import com.hivesandcolonies.characters.character.polen.item.focus.HiveheartCharmItem;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStorySavedData;
import com.hivesandcolonies.characters.character.polen.world.PolenSingletonManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;

public final class PolenPrologueManager {
    private static final String HIVEHEART_CHARM_GRANTED_FLAG = "prologue.hiveheart_charm_granted";
    private static final int BIOME_LOCATE_INITIAL_RANGE = 4096;
    private static final int BIOME_LOCATE_MAX_RANGE = 65536;
    private static final int BIOME_LOCATE_VERTICAL_RANGE = 96;
    private static final int BIOME_LOCATE_STEP = 32;
    private static final int BIOME_SAMPLE_STEPS = 24;
    private static final int LOCAL_SITE_SCAN_RADIUS = 192;
    private static final int LOCAL_SITE_SCAN_STEP = 12;
    private static final int CLEARING_RADIUS = 6;
    private static final int FLOWER_RING_RADIUS = 8;
    private static final int SITE_WATER_RADIUS = 5;
    private static final int SHELTER_SEARCH_RADIUS = 2;
    private static final int SHELTER_MIN_DX = -3;
    private static final int SHELTER_MAX_DX = 3;
    private static final int SHELTER_MIN_DZ = -3;
    private static final int SHELTER_MAX_DZ = 2;

    private PolenPrologueManager() {
    }

    public static void onServerStarted(ServerStartedEvent event) {
        ensurePrologueContent(event.getServer().overworld());
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel overworld = player.serverLevel().getServer().overworld();
        ensurePrologueContent(overworld);
    }

    public static void ensurePrologueContent(ServerLevel level) {
        if (level == null) {
            return;
        }

        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        PolenWorldStoryData data = savedData.getData();
        PolenEntity polen = PolenSingletonManager.findLivingPolen(level);
        boolean prologueResolved = PolenStoryFlagsManager.hasFlag(level, PolenStoryFlag.NAME_REVEALED)
                || PolenStoryFlagsManager.hasFlag(level, PolenStoryFlag.CHAPTER_0_COMPLETE);

        BlockPos clearingCenter = data.getPrologueClearingCenter();
        if (clearingCenter == null || !prologueResolved && !isStoredPrologueSiteUsable(level, clearingCenter, data.getPrologueShelterPos(), data.getPrologueBeeBedPos())) {
            clearingCenter = polen != null ? polen.blockPosition() : findPrologueClearing(level);
            if (clearingCenter == null) {
                clearingCenter = fallbackClearing(level);
            }
            data.setPrologueClearingCenter(clearingCenter);
            data.setPrologueShelterPos(null);
            data.setPrologueBeeBedPos(null);
            savedData.setDirty();
        }

        BlockPos shelterPos = data.getPrologueShelterPos();
        boolean shelterRefreshed = false;
        if (shelterPos == null || !prologueResolved && !isShelterStillPresent(level, shelterPos)) {
            shelterPos = buildOrRefreshPrologueSite(level, clearingCenter);
            shelterRefreshed = true;
        }
        if (shelterPos != null && !shelterPos.equals(data.getPrologueShelterPos())) {
            data.setPrologueShelterPos(shelterPos);
            savedData.setDirty();
        }

        BlockPos beeBedPos = shelterPos == null ? null : data.getPrologueBeeBedPos();
        if (shelterPos != null && (shelterRefreshed || !isBeeBedStillPresent(level, beeBedPos))) {
            beeBedPos = resolveBeeBedMarkerPos(level, shelterPos);
        }
        if (beeBedPos == null && data.getPrologueBeeBedPos() != null
                || beeBedPos != null && !beeBedPos.equals(data.getPrologueBeeBedPos())) {
            data.setPrologueBeeBedPos(beeBedPos);
            savedData.setDirty();
        }

        if (polen == null) {
            BlockPos spawnPos = beeBedPos != null ? beeBedPos : shelterPos == null ? clearingCenter : shelterPos;
            polen = spawnPolen(level, spawnPos);
        }

        if (polen != null) {
            rememberTemporaryResidence(polen, shelterPos, beeBedPos);
            PolenWorldStateManager.ensureFor(level, polen);
        }
    }

    public static boolean grantOpeningClueMap(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return grantOpeningClueMapIfNeeded(player, player.serverLevel().getServer().overworld());
    }

    public static ItemStack createOpeningClueMap() {
        return new ItemStack(ModItems.HIVEHEART_CHARM.get());
    }

    public static ItemStack createOpeningClueMap(ServerLevel level) {
        ItemStack stack = createOpeningClueMap();
        if (level != null) {
            ensurePrologueContent(level);
            HiveheartCharmItem.bindTarget(stack, level, resolveLocatorTarget(level));
        }
        return stack;
    }

    public static BlockPos resolveLocatorTarget(ServerLevel level) {
        if (level == null) {
            return null;
        }

        PolenWorldStoryData data = PolenWorldStateManager.get(level);
        if (isBeeBedStillPresent(level, data.getPrologueBeeBedPos())) {
            return data.getPrologueBeeBedPos();
        }
        if (data.getPrologueShelterPos() != null) {
            BlockPos nearbyBeeBed = resolveBeeBedMarkerPos(level, data.getPrologueShelterPos());
            if (nearbyBeeBed != null) {
                return nearbyBeeBed;
            }
        }
        return data.getPrologueClearingCenter();
    }

    private static boolean grantOpeningClueMapIfNeeded(ServerPlayer player, ServerLevel overworld) {
        if (player == null || overworld == null) {
            return false;
        }

        if (PolenStoryFlagsManager.hasFlag(overworld, PolenStoryFlag.NAME_REVEALED)
                || PolenPlayerRelationshipManager.hasPlayerFlag(player, HIVEHEART_CHARM_GRANTED_FLAG)
                || player.getInventory().contains(new ItemStack(ModItems.HIVEHEART_CHARM.get()))) {
            return false;
        }

        ItemStack stack = createOpeningClueMap(overworld);
        boolean added = player.getInventory().add(stack);
        if (!added) {
            ItemEntity drop = player.drop(stack, false);
            if (drop != null) {
                drop.setNoPickUpDelay();
                drop.setTarget(player.getUUID());
            }
        }

        PolenPlayerRelationshipManager.addPlayerFlag(player, HIVEHEART_CHARM_GRANTED_FLAG);
        return true;
    }

    private static PolenEntity spawnPolen(ServerLevel level, BlockPos spawnPos) {
        PolenEntity polen = ModEntities.POLEN.get().create(level);
        if (polen == null || spawnPos == null) {
            return null;
        }

        polen.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        polen.refreshDisplayName();
        if (level.addFreshEntity(polen)) {
            PolenSingletonManager.remember(level, polen);
            return polen;
        }
        return null;
    }

    private static BlockPos buildOrRefreshPrologueSite(ServerLevel level, BlockPos clearingCenter) {
        if (level == null || clearingCenter == null) {
            return null;
        }

        BlockPos shelterPos = resolveShelterPos(level, clearingCenter);
        if (shelterPos == null) {
            return null;
        }
        clearClearing(level, clearingCenter);
        seedClearingFlowers(level, clearingCenter);
        flattenShelterFootprint(level, shelterPos);
        buildShelter(level, shelterPos);
        return shelterPos;
    }

    private static boolean isShelterStillPresent(ServerLevel level, BlockPos shelterPos) {
        if (level == null || shelterPos == null) {
            return false;
        }
        return !level.canSeeSky(shelterPos.above())
                && resolveBeeBedMarkerPos(level, shelterPos) != null
                && PolenShelterContextResolver.hasNearbyLight(level, shelterPos);
    }

    private static boolean isBeeBedStillPresent(ServerLevel level, BlockPos beeBedPos) {
        return level != null
                && beeBedPos != null
                && level.getBlockState(beeBedPos).is(ModBlocks.POLEN_BEE_BED.get());
    }

    private static BlockPos resolveBeeBedMarkerPos(ServerLevel level, BlockPos shelterPos) {
        return level == null || shelterPos == null ? null : PolenShelterContextResolver.findNearbyBeeBed(level, shelterPos);
    }

    private static void rememberTemporaryResidence(PolenEntity polen, BlockPos shelterPos, BlockPos beeBedPos) {
        if (polen == null || shelterPos == null) {
            return;
        }

        // The bee bed is Polen's temporary anchor inside the improvised shelter.
        // We still avoid placing her magical lantern here because that belongs to
        // later night-time behaviors, not to the physical refuge marker itself.
        PolenResidenceValidation residence = PolenResidenceValidator.validate(polen, shelterPos);
        if (residence.isSuccess() && residence.target() != null) {
            polen.getAiState().setResidenceState(
                    residence.target().anchorPos().immutable(),
                    residence.target().usePos().immutable(),
                    residence.target().context(),
                    residence.target().stage()
            );

            BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, residence.target().usePos());
            if (normalizedRestingPos != null) {
                polen.getAiState().setRestingPos(normalizedRestingPos.immutable());
            }
            return;
        }

        BlockPos preferredRestingPos = beeBedPos != null ? beeBedPos : shelterPos;
        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, preferredRestingPos);
        if (normalizedRestingPos != null) {
            polen.getAiState().setRestingPos(normalizedRestingPos.immutable());
        }
    }

    private static boolean isStoredPrologueSiteUsable(ServerLevel level, BlockPos clearingCenter, BlockPos shelterPos, BlockPos beeBedPos) {
        if (level == null || clearingCenter == null) {
            return false;
        }
        if (!isAllowedPrologueBiome(level, clearingCenter)) {
            return false;
        }
        if (surfacePos(level, clearingCenter) == null || hasNearbyWater(level, clearingCenter, SITE_WATER_RADIUS)) {
            return false;
        }
        if (shelterPos != null) {
            if (beeBedPos != null && !isBeeBedStillPresent(level, beeBedPos) && !canPlaceShelterFootprint(level, shelterPos)) {
                return false;
            }
            return isShelterStillPresent(level, shelterPos) || canPlaceShelterFootprint(level, shelterPos);
        }
        return resolveShelterPos(level, clearingCenter) != null;
    }

    private static void clearClearing(ServerLevel level, BlockPos center) {
        for (int dx = -CLEARING_RADIUS; dx <= CLEARING_RADIUS; dx++) {
            for (int dz = -CLEARING_RADIUS; dz <= CLEARING_RADIUS; dz++) {
                if (dx * dx + dz * dz > CLEARING_RADIUS * CLEARING_RADIUS) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos standingPos = new BlockPos(x, Math.max(center.getY(), surfaceY), z);
                BlockPos groundPos = standingPos.below();
                BlockState groundState = level.getBlockState(groundPos);

                for (int y = standingPos.getY(); y <= standingPos.getY() + 4; y++) {
                    BlockPos airPos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(airPos);
                    if (!state.isAir() && (state.is(BlockTags.LEAVES) || state.canBeReplaced())) {
                        setBlock(level, airPos, Blocks.AIR.defaultBlockState());
                    }
                }

                if (groundState.is(Blocks.WATER) || groundState.is(Blocks.LAVA)) {
                    setBlock(level, groundPos, Blocks.DIRT.defaultBlockState());
                    groundState = level.getBlockState(groundPos);
                }

                if (groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT)) {
                    if (dx * dx + dz * dz <= 5) {
                        setBlock(level, groundPos, ((Math.abs(dx) + Math.abs(dz)) & 1) == 0
                                ? Blocks.DIRT_PATH.defaultBlockState()
                                : Blocks.COARSE_DIRT.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void seedClearingFlowers(ServerLevel level, BlockPos center) {
        List<Block> flowers = List.of(Blocks.ALLIUM, Blocks.WHITE_TULIP, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET);
        int index = 0;
        for (int dx = -FLOWER_RING_RADIUS; dx <= FLOWER_RING_RADIUS; dx++) {
            for (int dz = -FLOWER_RING_RADIUS; dz <= FLOWER_RING_RADIUS; dz++) {
                int distanceSq = dx * dx + dz * dz;
                if (distanceSq < 30 || distanceSq > FLOWER_RING_RADIUS * FLOWER_RING_RADIUS) {
                    continue;
                }
                if (((dx + dz) & 3) != 0) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int standingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos flowerPos = new BlockPos(x, standingY, z);
                BlockPos groundPos = flowerPos.below();
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)) {
                    continue;
                }
                if (!level.getBlockState(flowerPos).canBeReplaced()) {
                    continue;
                }
                setBlock(level, flowerPos, flowers.get(index % flowers.size()).defaultBlockState());
                index++;
            }
        }
    }

    private static void flattenShelterFootprint(ServerLevel level, BlockPos shelterPos) {
        int floorY = shelterPos.getY() - 1;
        for (int dx = SHELTER_MIN_DX; dx <= SHELTER_MAX_DX; dx++) {
            for (int dz = SHELTER_MIN_DZ; dz <= SHELTER_MAX_DZ; dz++) {
                BlockPos groundPos = shelterPos.offset(dx, -1, dz);
                for (int y = floorY - 2; y < floorY; y++) {
                    BlockPos fillPos = new BlockPos(groundPos.getX(), y, groundPos.getZ());
                    BlockState existing = level.getBlockState(fillPos);
                    if (existing.isAir() || existing.canBeReplaced() || existing.is(Blocks.WATER)) {
                        setBlock(level, fillPos, Blocks.DIRT.defaultBlockState());
                    }
                }

                setBlock(level, groundPos, dz >= 0 ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.CHERRY_PLANKS.defaultBlockState());
                for (int y = floorY + 1; y <= floorY + 4; y++) {
                    setBlock(level, new BlockPos(groundPos.getX(), y, groundPos.getZ()), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildShelter(ServerLevel level, BlockPos shelterPos) {
        placeLogColumn(level, shelterPos.offset(-2, 0, -2), 3);
        placeLogColumn(level, shelterPos.offset(2, 0, -2), 3);
        placeLogColumn(level, shelterPos.offset(-2, 0, 0), 2);
        placeLogColumn(level, shelterPos.offset(2, 0, 0), 2);

        setBlock(level, shelterPos.offset(-1, 0, -2), Blocks.CHERRY_PLANKS.defaultBlockState());
        setBlock(level, shelterPos.offset(0, 0, -2), Blocks.WHITE_WOOL.defaultBlockState());
        setBlock(level, shelterPos.offset(1, 0, -2), Blocks.CHERRY_PLANKS.defaultBlockState());

        setBlock(level, shelterPos.offset(-2, 0, -1), Blocks.CHERRY_PLANKS.defaultBlockState());
        setBlock(level, shelterPos.offset(2, 0, -1), Blocks.CHERRY_PLANKS.defaultBlockState());

        BlockState rearRoof = Blocks.CHERRY_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH)
                .setValue(StairBlock.HALF, Half.TOP);
        BlockState midRoof = Blocks.CHERRY_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState frontRoof = Blocks.CHERRY_SLAB.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, shelterPos.offset(dx, 3, -2), rearRoof);
            setBlock(level, shelterPos.offset(dx, 2, -1), midRoof);
            setBlock(level, shelterPos.offset(dx, 2, 0), frontRoof);
        }

        BlockState beeBed = ModBlocks.POLEN_BEE_BED.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
        setBlock(level, shelterPos.offset(0, 0, -1), beeBed);

        BlockState barrel = Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
        setBlock(level, shelterPos.offset(-1, 0, -1), Blocks.YELLOW_CARPET.defaultBlockState());
        setBlock(level, shelterPos.offset(1, 0, -1), Blocks.WHITE_CARPET.defaultBlockState());
        setBlock(level, shelterPos.offset(2, 0, -1), barrel);

        BlockState campfire = Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
        setBlock(level, shelterPos.offset(1, 0, 1), campfire);
    }

    private static void placeLogColumn(ServerLevel level, BlockPos basePos, int height) {
        for (int dy = 0; dy < height; dy++) {
            setBlock(level, basePos.above(dy), Blocks.STRIPPED_CHERRY_LOG.defaultBlockState());
        }
    }

    private static BlockPos findPrologueClearing(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        BlockPos preferred = locateClearingInBiome(level, spawn, Biomes.CHERRY_GROVE);
        return preferred != null ? preferred : locateClearingInBiome(level, spawn, Biomes.MEADOW);
    }

    private static BlockPos locateClearingInBiome(ServerLevel level, BlockPos origin, ResourceKey<Biome> biomeKey) {
        for (int searchRadius = BIOME_LOCATE_INITIAL_RANGE; searchRadius <= BIOME_LOCATE_MAX_RANGE; searchRadius *= 2) {
            Pair<BlockPos, Holder<Biome>> located = locateBiome(level, origin, biomeKey, searchRadius);
            if (located == null || located.getFirst() == null) {
                continue;
            }

            BlockPos nearbySite = findSiteNearBiome(level, located.getFirst(), biomeKey);
            if (nearbySite != null) {
                return nearbySite.immutable();
            }
        }
        return null;
    }

    private static Pair<BlockPos, Holder<Biome>> locateBiome(ServerLevel level, BlockPos origin, ResourceKey<Biome> biomeKey, int searchRadius) {
        return level.getChunkSource()
                .getGenerator()
                .getBiomeSource()
                .findClosestBiome3d(
                        origin,
                        searchRadius,
                        BIOME_LOCATE_VERTICAL_RANGE,
                        BIOME_LOCATE_STEP,
                        holder -> holder.is(biomeKey),
                        level.getChunkSource().randomState().sampler(),
                        level
                );
    }

    private static BlockPos findSiteNearBiome(ServerLevel level, BlockPos biomeCenter, ResourceKey<Biome> biomeKey) {
        BlockPos directSurface = surfacePos(level, biomeCenter);
        if (isValidArtificialClearingAnchor(level, directSurface, biomeKey, true)) {
            return directSurface.immutable();
        }

        for (int radius = LOCAL_SITE_SCAN_STEP; radius <= LOCAL_SITE_SCAN_RADIUS; radius += LOCAL_SITE_SCAN_STEP) {
            for (int step = 0; step < BIOME_SAMPLE_STEPS; step++) {
                double angle = step * (Math.PI * 2.0D / BIOME_SAMPLE_STEPS);
                int x = biomeCenter.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = biomeCenter.getZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos candidate = surfacePos(level, new BlockPos(x, biomeCenter.getY(), z));
                if (isValidArtificialClearingAnchor(level, candidate, biomeKey, true)) {
                    return candidate.immutable();
                }
            }
        }

        if (isValidArtificialClearingAnchor(level, directSurface, biomeKey, false)) {
            return directSurface.immutable();
        }
        return null;
    }

    private static boolean hasNearbyWater(ServerLevel level, BlockPos candidate, int radius) {
        if (level == null || candidate == null) {
            return true;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = candidate.getX() + dx;
                int z = candidate.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos standingPos = new BlockPos(x, y, z);
                BlockPos groundPos = standingPos.below();
                if (!level.getFluidState(standingPos).isEmpty()
                        || level.getBlockState(groundPos).is(Blocks.WATER)
                        || level.getBlockState(groundPos).is(Blocks.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValidArtificialClearingAnchor(ServerLevel level, BlockPos candidate, ResourceKey<Biome> biomeKey, boolean requireShelterFit) {
        if (level == null || candidate == null) {
            return false;
        }
        if (candidate.getY() < level.getSeaLevel() + 1 || candidate.getY() > 170) {
            return false;
        }
        if (!isBiomeMatch(level, candidate, biomeKey)) {
            return false;
        }
        if (!level.canSeeSky(candidate)) {
            return false;
        }

        BlockPos groundPos = candidate.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)
                || groundState.is(Blocks.WATER)
                || groundState.is(Blocks.LAVA)
                || hasNearbyWater(level, candidate, SITE_WATER_RADIUS)
                || hasVillageLikeClutter(level, candidate)) {
            return false;
        }

        return !requireShelterFit || resolveShelterPos(level, candidate) != null;
    }

    private static boolean isAllowedPrologueBiome(ServerLevel level, BlockPos pos) {
        return isBiomeMatch(level, pos, Biomes.CHERRY_GROVE) || isBiomeMatch(level, pos, Biomes.MEADOW);
    }

    private static boolean isBiomeMatch(ServerLevel level, BlockPos pos, ResourceKey<Biome> biomeKey) {
        return level != null && pos != null && biomeKey != null && level.getBiome(pos).is(biomeKey);
    }

    private static boolean hasVillageLikeClutter(ServerLevel level, BlockPos candidate) {
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -2; dy <= 3; dy++) {
                    BlockState state = level.getBlockState(candidate.offset(dx, dy, dz));
                    if (state.is(BlockTags.BEDS)
                            || state.is(BlockTags.DOORS)
                            || state.is(Blocks.BARREL)
                            || state.is(Blocks.CHEST)
                            || state.is(Blocks.BELL)
                            || state.is(Blocks.SMOKER)
                            || state.is(Blocks.BLAST_FURNACE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static BlockPos resolveShelterPos(ServerLevel level, BlockPos clearingCenter) {
        if (level == null || clearingCenter == null) {
            return null;
        }

        int[][] offsets = {
                {0, -1},
                {0, -2},
                {1, -1},
                {-1, -1},
                {1, -2},
                {-1, -2},
                {0, 0},
                {2, -1},
                {-2, -1}
        };

        for (int[] offset : offsets) {
            BlockPos candidate = surfacePos(level, clearingCenter.offset(offset[0], 0, offset[1]));
            if (candidate != null && candidate.closerToCenterThan(clearingCenter.getCenter(), CLEARING_RADIUS + 1.5D)
                    && canPlaceShelterFootprint(level, candidate)) {
                return candidate.immutable();
            }
        }

        for (int dx = -SHELTER_SEARCH_RADIUS; dx <= SHELTER_SEARCH_RADIUS; dx++) {
            for (int dz = -SHELTER_SEARCH_RADIUS; dz <= 0; dz++) {
                BlockPos candidate = surfacePos(level, clearingCenter.offset(dx, 0, dz));
                if (candidate != null && canPlaceShelterFootprint(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }

        return null;
    }

    private static BlockPos surfacePos(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    private static boolean canPlaceShelterFootprint(ServerLevel level, BlockPos shelterPos) {
        if (level == null || shelterPos == null || hasNearbyWater(level, shelterPos, 2)) {
            return false;
        }

        int floorY = shelterPos.getY() - 1;
        for (int dx = SHELTER_MIN_DX; dx <= SHELTER_MAX_DX; dx++) {
            for (int dz = SHELTER_MIN_DZ; dz <= SHELTER_MAX_DZ; dz++) {
                int x = shelterPos.getX() + dx;
                int z = shelterPos.getZ() + dz;
                int sampleY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Math.abs(sampleY - shelterPos.getY()) > 3) {
                    return false;
                }

                BlockPos standingPos = new BlockPos(x, sampleY, z);
                BlockPos groundPos = standingPos.below();
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)
                        || groundState.is(Blocks.WATER)
                        || groundState.is(Blocks.LAVA)
                        || !level.canSeeSky(standingPos)) {
                    return false;
                }

                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos checkPos = new BlockPos(x, floorY + 1 + dy, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static BlockPos fallbackClearing(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        BlockPos meadow = locateClearingInBiome(level, spawn, Biomes.MEADOW);
        return meadow != null ? meadow : locateClearingInBiome(level, spawn, Biomes.CHERRY_GROVE);
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, 3);
        }
    }
}
