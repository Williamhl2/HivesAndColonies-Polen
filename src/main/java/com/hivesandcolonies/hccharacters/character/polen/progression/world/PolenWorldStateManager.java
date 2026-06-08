package com.hivesandcolonies.hccharacters.character.polen.progression.world;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenAffinityFactory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenIdentity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterestGenerator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterestProfile;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenShelterValidator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.story.PolenStoryStage;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.story.PolenWorldMemory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;

import net.minecraft.core.BlockPos;
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

        ServerLevel level = (ServerLevel) polen.level();
        boolean dirty = false;
        if (data.getPrologueClearingCenter() == null) {
            data.setPrologueClearingCenter(polen.blockPosition());
            dirty = true;
        }

        if (data.getPrologueShelterPos() == null) {
            BlockPos shelterPos = PolenShelterValidator.findStoryShelter(
                    polen,
                    data.getPrologueClearingCenter() == null ? polen.blockPosition() : data.getPrologueClearingCenter()
            );
            if (shelterPos != null) {
                data.setPrologueShelterPos(shelterPos);
                dirty = true;
            }
        }

        if (data.getPrologueBeeBedPos() == null) {
            BlockPos searchOrigin = data.getPrologueShelterPos() != null
                    ? data.getPrologueShelterPos()
                    : data.getPrologueClearingCenter();
            BlockPos beeBedPos = PolenShelterContextResolver.findNearbyBeeBed(level, searchOrigin);
            if (beeBedPos != null) {
                data.setPrologueBeeBedPos(beeBedPos);
                dirty = true;
            }
        }

        return dirty;
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
}
