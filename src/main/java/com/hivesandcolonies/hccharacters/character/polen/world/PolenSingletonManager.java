package com.hivesandcolonies.hccharacters.character.polen.world;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStorySavedData;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.UUID;

/**
 * Enforces Polen as a single world-level character.
 *
 * The saved UUID lives in overworld saved data so the same Polen identity is
 * shared across dimensions. Spawn eggs, commands or loaded duplicate entities
 * must not create additional independent Polens.
 */
public final class PolenSingletonManager {
    private PolenSingletonManager() {}

    public static boolean hasLivingPolen(ServerLevel level) {
        return findLivingPolen(level) != null;
    }

    public static PolenEntity findLivingPolen(ServerLevel level) {
        PolenWorldStoryData data = PolenWorldStorySavedData.get(level).getData();
        UUID storedUuid = data.getPolenEntityUuid();
        MinecraftServer server = level.getServer();

        if (storedUuid != null) {
            PolenEntity storedPolen = findPolenByUuid(server, storedUuid);
            if (storedPolen != null && storedPolen.isAlive()) {
                return storedPolen;
            }
        }

        // Do not scan the whole world for Polen. A full-world entity AABB lookup can
        // freeze heavily modded servers when called by items, login handlers, or tick
        // paths. Polen is remembered by UUID when she spawns or loads, so a missing
        // UUID lookup simply means she is not currently loaded.
        return null;
    }

    public static boolean claimOrDiscardDuplicate(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return true;
        }

        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(serverLevel);
        PolenWorldStoryData data = savedData.getData();
        UUID currentUuid = polen.getUUID();
        UUID storedUuid = data.getPolenEntityUuid();

        if (storedUuid == null) {
            data.setPolenEntityUuid(currentUuid);
            data.setPolenSpawned(true);
            data.setLastKnownPolenPos(polen.blockPosition());
            savedData.setDirty();
            return true;
        }

        if (storedUuid.equals(currentUuid)) {
            data.setPolenSpawned(true);
            data.setLastKnownPolenPos(polen.blockPosition());
            savedData.setDirty();
            return true;
        }

        PolenEntity storedPolen = findPolenByUuid(serverLevel.getServer(), storedUuid);
        if (storedPolen == null || storedPolen.isRemoved() || !storedPolen.isAlive()) {
            data.setPolenEntityUuid(currentUuid);
            data.setPolenSpawned(true);
            data.setLastKnownPolenPos(polen.blockPosition());
            savedData.setDirty();
            return true;
        }

        polen.discard();
        return false;
    }

    public static void remember(ServerLevel level, PolenEntity polen) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        PolenWorldStoryData data = savedData.getData();
        data.setPolenEntityUuid(polen.getUUID());
        data.setPolenSpawned(true);
        data.setLastKnownPolenPos(polen.blockPosition());
        savedData.setDirty();
    }

    private static PolenEntity findPolenByUuid(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof PolenEntity polen) {
                return polen;
            }
        }
        return null;
    }
}
