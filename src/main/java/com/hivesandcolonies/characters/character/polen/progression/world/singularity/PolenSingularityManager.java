package com.hivesandcolonies.characters.character.polen.progression.world.singularity;

import com.hivesandcolonies.characters.bootstrap.Characters;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStorySavedData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.UUID;

/**
 * Enforces the project's central rule: a save belongs to one Polen only.
 *
 * The first Polen that enters a world becomes the canonical Polen. Future Polen
 * entities with a different UUID are rejected, while the canonical entity can
 * still load normally from disk.
 */
public final class PolenSingularityManager {
    private PolenSingularityManager() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof PolenEntity polen)) {
            return;
        }

        Level level = event.getLevel();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!accept(serverLevel, polen)) {
            event.setCanceled(true);
            polen.discard();
        }
    }

    public static boolean accept(ServerLevel level, PolenEntity polen) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        PolenWorldStoryData data = savedData.getData();
        UUID knownUuid = data.getPolenEntityUuid();

        if (knownUuid == null) {
            PolenWorldStateManager.ensureFor(level, polen);
            savedData.setDirty();
            Characters.LOGGER.info("Bound unique Polen entity {} to this world.", polen.getUUID());
            return true;
        }

        if (knownUuid.equals(polen.getUUID())) {
            PolenWorldStateManager.ensureFor(level, polen);
            return true;
        }

        Characters.LOGGER.warn(
                "Rejected duplicate Polen entity {} because world is already bound to {}.",
                polen.getUUID(),
                knownUuid
        );
        return false;
    }
}
