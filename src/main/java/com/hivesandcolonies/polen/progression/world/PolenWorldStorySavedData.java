package com.hivesandcolonies.polen.progression.world;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class PolenWorldStorySavedData extends SavedData {
    private static final String FILE_ID = Polen.MODID + "_world_story";
    private static final String TAG_CURRENT_CHAPTER = "currentChapter";
    private static final String TAG_WORLD_FLAGS = "worldFlags";
    private static final String TAG_POLEN_ENTITY_UUID = "polenEntityUuid";
    private static final String TAG_POLEN_SPAWNED = "polenSpawned";

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
        return tag;
    }
}
