package com.hivesandcolonies.hccharacters.character.soa.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
    private static final int BOARD_SPAWN_CHANCE = 8;
    private static final int CAVE_SPAWN_CHANCE = 12;

    private static final ResourceLocation[] BOUNTIFUL_BOARD_IDS = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath("bountiful", "bountyboard"),
            ResourceLocation.fromNamespaceAndPath("bountiful", "bounty_board")
    };

    private static final Map<UUID, Integer> BOARD_PLAYER_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Integer> CAVE_PLAYER_COOLDOWNS = new HashMap<>();
    private static final Map<Long, Integer> BOARD_POSITION_COOLDOWNS = new HashMap<>();


    private SoaMarjorieEncounterManager() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % MANAGER_INTERVAL_TICKS != 0) {
            return;
        }

        tickCooldowns(BOARD_PLAYER_COOLDOWNS, MANAGER_INTERVAL_TICKS);
        tickCooldowns(CAVE_PLAYER_COOLDOWNS, MANAGER_INTERVAL_TICKS);
        tickCooldowns(BOARD_POSITION_COOLDOWNS, MANAGER_INTERVAL_TICKS);

        if (!HcCharactersGameplayConfig.soaMarjorieEncountersEnabled()) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator()) {
                    continue;
                }
                tryBoardVisit(level, player);
                tryCaveEncounter(level, player);
            }
        }
    }

    private static <K> void tickCooldowns(Map<K, Integer> cooldowns, int amount) {
        cooldowns.replaceAll((key, value) -> Math.max(0, value - amount));
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    private static void tryBoardVisit(ServerLevel level, ServerPlayer player) {
        if (!HcCharactersGameplayConfig.soaMarjorieBoardVisitsEnabled()) {
            return;
        }
        if (BOARD_PLAYER_COOLDOWNS.containsKey(player.getUUID())) {
            return;
        }
        if (player.getRandom().nextInt(BOARD_SPAWN_CHANCE) != 0) {
            return;
        }
        BlockPos boardPos = findNearestBountifulBoard(level, player.blockPosition());
        if (boardPos == null || BOARD_POSITION_COOLDOWNS.containsKey(boardPos.asLong()) || hasNearbySoa(level, boardPos)) {
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
        soa.startBoardVisit(boardPos, HcCharactersGameplayConfig.soaMarjorieBoardVisitDurationTicks());
        level.addFreshEntity(soa);
        BOARD_PLAYER_COOLDOWNS.put(player.getUUID(), HcCharactersGameplayConfig.soaMarjorieBoardPlayerCooldownTicks());
        BOARD_POSITION_COOLDOWNS.put(boardPos.asLong(), HcCharactersGameplayConfig.soaMarjorieBoardPositionCooldownTicks());
    }

    private static void tryCaveEncounter(ServerLevel level, ServerPlayer player) {
        if (!HcCharactersGameplayConfig.soaMarjorieCaveMiningEncountersEnabled()) {
            return;
        }
        if (CAVE_PLAYER_COOLDOWNS.containsKey(player.getUUID())) {
            return;
        }
        if (player.blockPosition().getY() > 48) {
            return;
        }
        if (level.canSeeSky(player.blockPosition())) {
            return;
        }
        if (player.getRandom().nextInt(CAVE_SPAWN_CHANCE) != 0) {
            return;
        }
        BlockPos spawnPos = findCaveSpawnNear(level, player.blockPosition());
        if (spawnPos == null || hasNearbySoa(level, spawnPos)) {
            return;
        }
        SoaMarjorieEntity soa = ModEntities.SOA_MARJORIE.get().create(level);
        if (soa == null) {
            return;
        }
        soa.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        soa.startCaveMiningEncounter(HcCharactersGameplayConfig.soaMarjorieCaveEncounterDurationTicks());
        level.addFreshEntity(soa);
        CAVE_PLAYER_COOLDOWNS.put(player.getUUID(), HcCharactersGameplayConfig.soaMarjorieCavePlayerCooldownTicks());
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
            if (distanceSqr(pos, origin) < 7.0D * 7.0D) {
                continue;
            }
            if (!canStandAt(level, pos)) {
                continue;
            }
            if (level.canSeeSky(pos) || level.getMaxLocalRawBrightness(pos) > 10) {
                continue;
            }
            return pos.immutable();
        }
        return null;
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
