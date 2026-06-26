package com.hivesandcolonies.hccharacters.character.polen.progression.world;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenAffinityFactory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenIdentity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterestGenerator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterestProfile;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.story.PolenStoryStage;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.story.PolenWorldMemory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.hccharacters.character.polen.world.PolenSingletonManager;

import net.minecraft.server.level.ServerLevel;

public final class PolenWorldStateManager {
    private PolenWorldStateManager() {
    }

    public static PolenWorldStoryData get(ServerLevel level) {
        return PolenWorldStorySavedData.get(level).getData();
    }

    public static PolenWorldStoryData ensureFor(ServerLevel level, PolenEntity polen) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        PolenWorldStoryData data = savedData.getData();
        boolean dirty = false;

        if (data.getPolenEntityUuid() == null) {
            data.setPolenEntityUuid(polen.getUUID());
            dirty = true;
        }

        if (!data.isPolenSpawned()) {
            data.setPolenSpawned(true);
            dirty = true;
        }

        if (!data.hasIdentity()) {
            data.setIdentity(PolenIdentity.create(
                    polen.getUUID(),
                    level.getGameTime(),
                    level.getDayTime(),
                    level.dimension().location().toString()
            ));
            data.setInterestProfile(PolenInterestGenerator.generate(data.getIdentity().personalitySeed()));
            data.remember(PolenWorldMemory.FIRST_AWAKENING);
            dirty = true;
        }

        if (ensurePrologueSite(data, polen)) {
            dirty = true;
        }

        ensureAffinityCharm(data, polen);

        if (dirty) {
            savedData.setDirty();
        }

        return data;
    }

    private static boolean ensurePrologueSite(PolenWorldStoryData data, PolenEntity polen) {
        if (data == null || polen == null) {
            return false;
        }

        // The first Hiveheart use now creates a one-shot prologue clearing with
        // Polen's improvised starter shelter. World state remembers that origin,
        // but it must still never infer, relocate, or regenerate the site later.
        //
        // This keeps the prologue aligned with the narrative docs while still
        // preventing duplicated shelters when Polen is unloaded/reloaded, moved
        // away from the original grove, or revisited after chunk reloads.
        if (data.getPrologueClearingCenter() == null) {
            data.setPrologueClearingCenter(polen.blockPosition());
            return true;
        }

        return false;
    }


    private static void ensureAffinityCharm(PolenWorldStoryData data, PolenEntity polen) {
        if (data == null || polen == null || !data.hasIdentity()) {
            return;
        }

        // Do not overwrite a later charm the player or story systems may equip.
        if (polen.getEquippedAffinityCharm() != PolenWorldAffinity.NONE) {
            return;
        }

        polen.equipAffinityCharm(PolenAffinityFactory.fromProfile(data.getInterestProfile()));
    }

    public static PolenInterestProfile interests(ServerLevel level) {
        return get(level).getInterestProfile();
    }

    public static int interest(ServerLevel level, PolenInterest interest) {
        return get(level).getInterestScore(interest);
    }

    public static void adjustInterest(ServerLevel level, PolenInterest interest, int amount) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        savedData.getData().adjustInterest(interest, amount);
        savedData.setDirty();
        syncEquippedAffinity(level);
    }

    public static PolenStoryStage storyStage(ServerLevel level) {
        return get(level).getStoryStage();
    }

    public static void setStoryStage(ServerLevel level, PolenStoryStage stage) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        if (savedData.getData().getStoryStage() != stage) {
            savedData.getData().setStoryStage(stage);
            savedData.setDirty();
        }
    }

    public static boolean remember(ServerLevel level, PolenWorldMemory memory) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        boolean changed = savedData.getData().remember(memory);
        if (changed) {
            savedData.setDirty();
        }
        return changed;
    }

    public static void rememberLastKnownPosition(ServerLevel level, net.minecraft.core.BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        savedData.getData().setLastKnownPolenPos(pos);
        savedData.setDirty();
    }

    public static void rememberPolenHomeBed(ServerLevel level, net.minecraft.core.BlockPos bedPos) {
        if (level == null || bedPos == null) {
            return;
        }
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        savedData.getData().setPrologueBeeBedPos(bedPos);
        savedData.setDirty();
    }

    private static void syncEquippedAffinity(ServerLevel level) {
        if (level == null) {
            return;
        }

        PolenEntity polen = PolenSingletonManager.findLivingPolen(level);
        if (polen == null) {
            return;
        }

        polen.equipAffinityCharm(PolenAffinityFactory.fromProfile(get(level).getInterestProfile()));
    }

}

