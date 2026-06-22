package com.hivesandcolonies.hccharacters.character.soa.world;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.util.LevelBrightnessHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class SoaMarjorieEncounterManager {
    private static final int MANAGER_INTERVAL_TICKS = 100;
    private static final int BOARD_SCAN_RADIUS = 24;
    private static final int BOARD_SCAN_Y_RADIUS = 8;
    private static final int BOARD_SPAWN_RADIUS = 7;
    private static final int CAVE_SPAWN_HORIZONTAL_RADIUS = 18;
    private static final int CAVE_SPAWN_VERTICAL_RADIUS = 5;
    private static final int EXISTING_SOA_RADIUS = 56;
    private static final int CAVE_ORE_SCAN_HORIZONTAL_RADIUS = 14;
    private static final int CAVE_ORE_SCAN_VERTICAL_RADIUS = 6;

    private static final ResourceLocation[] BOUNTIFUL_BOARD_IDS = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath("bountiful", "bountyboard"),
            ResourceLocation.fromNamespaceAndPath("bountiful", "bounty_board")
    };
    private static final TagKey<Block> SOA_MINEABLE_ORES = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "soa_marjorie_mineable")
    );

    private SoaMarjorieEncounterManager() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % MANAGER_INTERVAL_TICKS != 0) {
            return;
        }

        if (!HcCharactersGameplayConfig.soaMarjorieEncountersEnabled()) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            SoaMarjorieEncounterSavedData savedData = SoaMarjorieEncounterSavedData.get(level);
            savedData.cleanup(level.getGameTime());
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator()) {
                    continue;
                }
                tryBoardVisit(level, player);
                tryCaveEncounter(level, player);
            }
        }
    }


    private static void tryBoardVisit(ServerLevel level, ServerPlayer player) {
        if (!HcCharactersGameplayConfig.soaMarjorieBoardVisitsEnabled()) {
            return;
        }
        SoaMarjorieEncounterSavedData savedData = SoaMarjorieEncounterSavedData.get(level);
        long now = level.getGameTime();
        if (savedData.isBoardPlayerOnCooldown(player.getUUID(), now)) {
            return;
        }
        if (player.getRandom().nextInt(HcCharactersGameplayConfig.soaMarjorieBoardSpawnChanceDivisor()) != 0) {
            return;
        }
        BlockPos boardPos = findNearestBountifulBoard(level, player.blockPosition());
        if (boardPos == null || savedData.isBoardPositionOnCooldown(boardPos.asLong(), now) || hasNearbySoa(level, boardPos)) {
            return;
        }
        BlockPos spawnPos = findSpawnNear(level, boardPos, BOARD_SPAWN_RADIUS);
        if (spawnPos == null) {
            return;
        }
        SoaMarjorieEntity soa = ModEntities.SOA_MARJORIE.get().create(level);
        if (soa == null) {
            return;
        }
        soa.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        soa.startBoardVisit(boardPos, HcCharactersGameplayConfig.soaMarjorieBoardVisitDurationTicks(), player);
        level.addFreshEntity(soa);
        savedData.setBoardPlayerCooldown(player.getUUID(), now + HcCharactersGameplayConfig.soaMarjorieBoardPlayerCooldownTicks());
        savedData.setBoardPositionCooldown(boardPos.asLong(), now + HcCharactersGameplayConfig.soaMarjorieBoardPositionCooldownTicks());
    }

    private static void tryCaveEncounter(ServerLevel level, ServerPlayer player) {
        if (!HcCharactersGameplayConfig.soaMarjorieCaveMiningEncountersEnabled()) {
            return;
        }
        SoaMarjorieEncounterSavedData savedData = SoaMarjorieEncounterSavedData.get(level);
        long now = level.getGameTime();
        if (savedData.isCavePlayerOnCooldown(player.getUUID(), now)) {
            return;
        }
        if (player.blockPosition().getY() > HcCharactersGameplayConfig.soaMarjorieCaveMaxY()) {
            return;
        }
        if (level.canSeeSky(player.blockPosition())) {
            return;
        }
        if (player.getRandom().nextInt(HcCharactersGameplayConfig.soaMarjorieCaveSpawnChanceDivisor()) != 0) {
            return;
        }
        BlockPos spawnPos = findCaveSpawnNear(level, player.blockPosition());
        if (spawnPos == null || hasNearbySoa(level, spawnPos)) {
            return;
        }
        if (HcCharactersGameplayConfig.soaMarjorieCanMineBlocks() && !hasNearbyExposedOre(level, spawnPos)) {
            return;
        }
        SoaMarjorieEntity soa = ModEntities.SOA_MARJORIE.get().create(level);
        if (soa == null) {
            return;
        }
        soa.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        soa.startCaveMiningEncounter(HcCharactersGameplayConfig.soaMarjorieCaveEncounterDurationTicks(), player);
        level.addFreshEntity(soa);
        savedData.setCavePlayerCooldown(player.getUUID(), now + HcCharactersGameplayConfig.soaMarjorieCavePlayerCooldownTicks());
    }

    private static boolean hasNearbySoa(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(EXISTING_SOA_RADIUS);
        return !level.getEntitiesOfClass(SoaMarjorieEntity.class, area, SoaMarjorieEntity::isEncounterActive).isEmpty();
    }

    private static BlockPos findNearestBountifulBoard(ServerLevel level, BlockPos origin) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -BOARD_SCAN_Y_RADIUS; y <= BOARD_SCAN_Y_RADIUS; y++) {
            for (int x = -BOARD_SCAN_RADIUS; x <= BOARD_SCAN_RADIUS; x++) {
                for (int z = -BOARD_SCAN_RADIUS; z <= BOARD_SCAN_RADIUS; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!isBountifulBoard(level.getBlockState(cursor))) {
                        continue;
                    }
                    double distance = distanceSqr(origin, cursor);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static boolean isBountifulBoard(BlockState state) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        for (ResourceLocation boardId : BOUNTIFUL_BOARD_IDS) {
            if (boardId.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos findSpawnNear(ServerLevel level, BlockPos anchor, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -2; y <= 3; y++) {
                    BlockPos pos = anchor.offset(x, y, z);
                    if (!canStandAt(level, pos)) {
                        continue;
                    }
                    double distance = distanceSqr(anchor, pos);
                    if (distance < 4.0D || distance > radius * radius) {
                        continue;
                    }
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static BlockPos findCaveSpawnNear(ServerLevel level, BlockPos origin) {
        for (int attempts = 0; attempts < 36; attempts++) {
            int x = level.random.nextInt(CAVE_SPAWN_HORIZONTAL_RADIUS * 2 + 1) - CAVE_SPAWN_HORIZONTAL_RADIUS;
            int y = level.random.nextInt(CAVE_SPAWN_VERTICAL_RADIUS * 2 + 1) - CAVE_SPAWN_VERTICAL_RADIUS;
            int z = level.random.nextInt(CAVE_SPAWN_HORIZONTAL_RADIUS * 2 + 1) - CAVE_SPAWN_HORIZONTAL_RADIUS;
            BlockPos pos = origin.offset(x, y, z);
            if (pos.getY() > HcCharactersGameplayConfig.soaMarjorieCaveMaxY()) {
                continue;
            }
            if (distanceSqr(pos, origin) < 7.0D * 7.0D) {
                continue;
            }
            if (!canStandAt(level, pos)) {
                continue;
            }
            if (level.canSeeSky(pos) || LevelBrightnessHelper.maxLocalRawBrightness(level, pos) > 10) {
                continue;
            }
            return pos.immutable();
        }
        return null;
    }


    private static boolean hasNearbyExposedOre(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -CAVE_ORE_SCAN_VERTICAL_RADIUS; y <= CAVE_ORE_SCAN_VERTICAL_RADIUS; y++) {
            for (int x = -CAVE_ORE_SCAN_HORIZONTAL_RADIUS; x <= CAVE_ORE_SCAN_HORIZONTAL_RADIUS; x++) {
                for (int z = -CAVE_ORE_SCAN_HORIZONTAL_RADIUS; z <= CAVE_ORE_SCAN_HORIZONTAL_RADIUS; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (isMineableOre(level.getBlockState(cursor)) && hasExposedFace(level, cursor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasExposedFace(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMineableOre(BlockState state) {
        return state.is(SOA_MINEABLE_ORES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES);
    }

    private static double distanceSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }
}
