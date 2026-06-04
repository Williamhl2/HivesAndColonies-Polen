package com.hivesandcolonies.characters.progression.world;

import com.hivesandcolonies.characters.Characters;
import com.hivesandcolonies.characters.entity.ai.world.identity.PolenIdentity;
import com.hivesandcolonies.characters.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.characters.entity.ai.world.interests.PolenInterestProfile;
import com.hivesandcolonies.characters.entity.ai.world.story.PolenStoryStage;
import com.hivesandcolonies.characters.entity.ai.world.story.PolenWorldMemory;
import com.hivesandcolonies.characters.progression.PolenStoryFlag;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.EnumMap;
import java.util.UUID;

public final class PolenWorldStorySavedData extends SavedData {
    private static final String FILE_ID = Characters.MODID + "_world_story";
    private static final String TAG_CURRENT_CHAPTER = "currentChapter";
    private static final String TAG_WORLD_FLAGS = "worldFlags";
    private static final String TAG_POLEN_ENTITY_UUID = "polenEntityUuid";
    private static final String TAG_POLEN_SPAWNED = "polenSpawned";
    private static final String TAG_IDENTITY = "identity";
    private static final String TAG_IDENTITY_ID = "identityId";
    private static final String TAG_FIRST_SPAWN_GAME_TIME = "firstSpawnGameTime";
    private static final String TAG_FIRST_SPAWN_DAY = "firstSpawnDay";
    private static final String TAG_PERSONALITY_SEED = "personalitySeed";
    private static final String TAG_ORIGIN_DIMENSION = "originDimension";
    private static final String TAG_INTEREST_PROFILE = "interestProfile";
    private static final String TAG_STORY_STAGE = "storyStage";
    private static final String TAG_WORLD_MEMORIES = "worldMemories";

    private final PolenWorldStoryData data;

    public PolenWorldStorySavedData() {
        this(new PolenWorldStoryData());
    }

    private PolenWorldStorySavedData(PolenWorldStoryData data) {
        this.data = data;
    }

    public static SavedData.Factory<PolenWorldStorySavedData> factory() {
        return new SavedData.Factory<>(
                PolenWorldStorySavedData::new,
                PolenWorldStorySavedData::load,
                DataFixTypes.LEVEL
        );
    }

    public static PolenWorldStorySavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        DimensionDataStorage dataStorage = overworld.getDataStorage();
        return dataStorage.computeIfAbsent(factory(), FILE_ID);
    }

    public static PolenWorldStorySavedData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        PolenWorldStoryData data = new PolenWorldStoryData();
        data.setCurrentChapter(tag.getInt(TAG_CURRENT_CHAPTER));

        ListTag flags = tag.getList(TAG_WORLD_FLAGS, Tag.TAG_STRING);
        for (Tag flagTag : flags) {
            if (!(flagTag instanceof StringTag stringTag)) {
                continue;
            }

            try {
                data.setFlag(PolenStoryFlag.valueOf(stringTag.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (tag.hasUUID(TAG_POLEN_ENTITY_UUID)) {
            data.setPolenEntityUuid(tag.getUUID(TAG_POLEN_ENTITY_UUID));
        }

        data.setPolenSpawned(tag.getBoolean(TAG_POLEN_SPAWNED));

        if (tag.contains(TAG_IDENTITY, Tag.TAG_COMPOUND)) {
            CompoundTag identityTag = tag.getCompound(TAG_IDENTITY);
            if (identityTag.hasUUID(TAG_IDENTITY_ID)) {
                UUID identityId = identityTag.getUUID(TAG_IDENTITY_ID);
                long firstSpawnGameTime = identityTag.getLong(TAG_FIRST_SPAWN_GAME_TIME);
                long firstSpawnDay = identityTag.getLong(TAG_FIRST_SPAWN_DAY);
                long personalitySeed = identityTag.getLong(TAG_PERSONALITY_SEED);
                String originDimension = identityTag.getString(TAG_ORIGIN_DIMENSION);
                data.setIdentity(new PolenIdentity(identityId, firstSpawnGameTime, firstSpawnDay, personalitySeed, originDimension));
            }
        }

        if (tag.contains(TAG_INTEREST_PROFILE, Tag.TAG_COMPOUND)) {
            CompoundTag interestTag = tag.getCompound(TAG_INTEREST_PROFILE);
            EnumMap<PolenInterest, Integer> scores = new EnumMap<>(PolenInterest.class);
            for (PolenInterest interest : PolenInterest.values()) {
                if (interestTag.contains(interest.name())) {
                    scores.put(interest, interestTag.getInt(interest.name()));
                }
            }
            data.setInterestProfile(new PolenInterestProfile(scores));
        }

        if (tag.contains(TAG_STORY_STAGE, Tag.TAG_STRING)) {
            try {
                data.setStoryStage(PolenStoryStage.valueOf(tag.getString(TAG_STORY_STAGE)));
            } catch (IllegalArgumentException ignored) {
                data.setStoryStage(PolenStoryStage.AWAKENING);
            }
        }

        ListTag memories = tag.getList(TAG_WORLD_MEMORIES, Tag.TAG_STRING);
        for (Tag memoryTag : memories) {
            if (!(memoryTag instanceof StringTag stringTag)) {
                continue;
            }

            try {
                data.remember(PolenWorldMemory.valueOf(stringTag.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new PolenWorldStorySavedData(data);
    }

    public PolenWorldStoryData getData() {
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_CURRENT_CHAPTER, data.getCurrentChapter());

        ListTag flags = new ListTag();
        for (PolenStoryFlag flag : data.getWorldFlags()) {
            flags.add(StringTag.valueOf(flag.name()));
        }
        tag.put(TAG_WORLD_FLAGS, flags);

        if (data.getPolenEntityUuid() != null) {
            tag.putUUID(TAG_POLEN_ENTITY_UUID, data.getPolenEntityUuid());
        }

        tag.putBoolean(TAG_POLEN_SPAWNED, data.isPolenSpawned());

        if (data.hasIdentity()) {
            PolenIdentity identity = data.getIdentity();
            CompoundTag identityTag = new CompoundTag();
            identityTag.putUUID(TAG_IDENTITY_ID, identity.identityId());
            identityTag.putLong(TAG_FIRST_SPAWN_GAME_TIME, identity.firstSpawnGameTime());
            identityTag.putLong(TAG_FIRST_SPAWN_DAY, identity.firstSpawnDay());
            identityTag.putLong(TAG_PERSONALITY_SEED, identity.personalitySeed());
            identityTag.putString(TAG_ORIGIN_DIMENSION, identity.originDimension());
            tag.put(TAG_IDENTITY, identityTag);
        }

        CompoundTag interestTag = new CompoundTag();
        for (PolenInterest interest : PolenInterest.values()) {
            interestTag.putInt(interest.name(), data.getInterestScore(interest));
        }
        tag.put(TAG_INTEREST_PROFILE, interestTag);

        tag.putString(TAG_STORY_STAGE, data.getStoryStage().name());

        ListTag memories = new ListTag();
        for (PolenWorldMemory memory : data.getWorldMemories()) {
            memories.add(StringTag.valueOf(memory.name()));
        }
        tag.put(TAG_WORLD_MEMORIES, memories);

        return tag;
    }
}
