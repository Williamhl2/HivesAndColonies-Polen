package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStorySavedData;
import com.hivesandcolonies.hccharacters.character.polen.world.PolenSingletonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class PolenPrologueManager {
    private static final String HIVEHEART_CHARM_GRANTED_FLAG = "prologue.hiveheart_charm_granted";

    private PolenPrologueManager() {
    }

    public static void onServerStarted(ServerStartedEvent event) {
        // Prologue content is not generated during server boot. The first
        // Hiveheart use is the explicit one-shot trigger for the encounter site.
    }

    public static void ensurePrologueContent(ServerLevel level) {
        if (level == null) {
            return;
        }

        PolenEntity polen = PolenSingletonManager.findLivingPolen(level);
        if (polen != null) {
            PolenWorldStateManager.ensureFor(level, polen);
        }
    }

    public static BlockPos spawnPolenAtNearbyCherryGrove(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        ServerLevel overworld = player.serverLevel().getServer().overworld();
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(overworld);
        PolenWorldStoryData data = savedData.getData();

        PolenEntity existing = PolenSingletonManager.findLivingPolen(overworld);
        if (existing != null) {
            return existing.blockPosition().immutable();
        }
        if (data.getPolenEntityUuid() != null || data.isPolenSpawned()) {
            return data.getPrologueClearingCenter();
        }

        BlockPos origin = player.serverLevel().dimension().equals(Level.OVERWORLD)
                ? player.blockPosition()
                : overworld.getSharedSpawnPos();
        BlockPos clearingCenter = PolenPrologueSiteLocator.findHiveheartCherrySpawnPos(overworld, origin);
        if (clearingCenter == null) {
            return null;
        }

        BlockPos shelterPos = PolenPrologueSiteBuilder.prepareSite(overworld, data, clearingCenter);
        BlockPos spawnPos = PolenPrologueSiteLocator.resolvePrologueSpawnPos(overworld, clearingCenter, shelterPos);
        if (spawnPos == null) {
            return null;
        }

        data.setPrologueClearingCenter(clearingCenter);
        data.setPrologueShelterPos(shelterPos);
        data.setPrologueBeeBedPos(null);
        savedData.setDirty();

        PolenEntity polen = spawnPolen(overworld, spawnPos);
        if (polen == null) {
            return null;
        }

        PolenPrologueRuntime.applyTemporaryResidence(polen, shelterPos, null);
        polen.syncProfileState();
        PolenWorldStateManager.ensureFor(overworld, polen);
        return polen.blockPosition().immutable();
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
        return createOpeningClueMap();
    }

    public static boolean isLocatorDormant(ServerLevel level) {
        return level == null || PolenPrologueRuntime.isLocatorDormant(level, PolenWorldStateManager.get(level));
    }

    public static boolean hasReceivedOpeningClueMap(ServerPlayer player) {
        return player != null && PolenPlayerRelationshipManager.hasPlayerFlag(player, HIVEHEART_CHARM_GRANTED_FLAG);
    }

    public static boolean shouldOfferOpeningClueMap(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        ServerLevel overworld = player.serverLevel().getServer().overworld();
        return overworld != null
                && !PolenPrologueRuntime.isLocatorDormant(overworld, PolenWorldStateManager.get(overworld))
                && !player.getInventory().contains(new ItemStack(ModItems.HIVEHEART_CHARM.get()));
    }

    public static BlockPos resolveLocatorTarget(ServerLevel level) {
        if (level == null) {
            return null;
        }
        return PolenPrologueRuntime.resolveLocatorTarget(level, PolenWorldStateManager.get(level));
    }

    private static boolean grantOpeningClueMapIfNeeded(ServerPlayer player, ServerLevel overworld) {
        if (player == null || overworld == null) {
            return false;
        }

        if (!shouldOfferOpeningClueMap(player)) {
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

        if (!PolenPlayerRelationshipManager.hasPlayerFlag(player, HIVEHEART_CHARM_GRANTED_FLAG)) {
            PolenPlayerRelationshipManager.addPlayerFlag(player, HIVEHEART_CHARM_GRANTED_FLAG);
        }
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
}
