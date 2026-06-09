package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class NpcRelationshipSavedData extends SavedData {
    private static final String FILE_ID = HcCharacters.MODID + "_npc_relationships";

    private static final String TAG_RELATIONSHIPS = "relationships";
    private static final String TAG_CHARACTER_ID = "characterId";
    private static final String TAG_PLAYER_UUID = "playerUuid";
    private static final String TAG_AFFINITY = "affinity";
    private static final String TAG_INTERACTIONS = "interactions";
    private static final String TAG_LAST_INTERACTION_GAME_TIME = "lastInteractionGameTime";
    private static final String TAG_NEXT_REWARD_GAME_TIME = "nextRewardGameTime";
    private static final String TAG_SPECIAL_REWARDS = "specialRewards";
    private static final String TAG_FLAGS = "flags";
    private static final String TAG_COUNTERS = "counters";
    private static final String TAG_COOLDOWNS = "cooldowns";
    private static final String TAG_KEY = "key";
    private static final String TAG_INT_VALUE = "intValue";
    private static final String TAG_LONG_VALUE = "longValue";

    private final Map<String, Map<UUID, NpcRelationshipRecord>> relationships;

    private NpcRelationshipSavedData() {
        this(new HashMap<>());
    }

    private NpcRelationshipSavedData(Map<String, Map<UUID, NpcRelationshipRecord>> relationships) {
        this.relationships = relationships;
    }

    public static NpcRelationshipSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        DimensionDataStorage dataStorage = overworld.getDataStorage();
        return dataStorage.computeIfAbsent(factory(), FILE_ID);
    }

    public static SavedData.Factory<NpcRelationshipSavedData> factory() {
        return new SavedData.Factory<>(
                NpcRelationshipSavedData::new,
                NpcRelationshipSavedData::load,
                DataFixTypes.LEVEL
        );
    }

    public static NpcRelationshipSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Map<String, Map<UUID, NpcRelationshipRecord>> relationships = new HashMap<>();
        ListTag entries = tag.getList(TAG_RELATIONSHIPS, Tag.TAG_COMPOUND);

        for (Tag entryTag : entries) {
            if (!(entryTag instanceof CompoundTag entry) || !entry.hasUUID(TAG_PLAYER_UUID)) {
                continue;
            }

            String characterId = entry.getString(TAG_CHARACTER_ID);
            if (characterId.isBlank()) {
                continue;
            }

            UUID playerUuid = entry.getUUID(TAG_PLAYER_UUID);
            NpcRelationshipRecord record = new NpcRelationshipRecord();
            record.setAffinity(entry.getInt(TAG_AFFINITY));
            record.setInteractions(entry.getInt(TAG_INTERACTIONS));
            record.setLastInteractionGameTime(entry.getLong(TAG_LAST_INTERACTION_GAME_TIME));
            record.setNextRewardGameTime(entry.getLong(TAG_NEXT_REWARD_GAME_TIME));
            record.setSpecialRewards(entry.getInt(TAG_SPECIAL_REWARDS));

            ListTag flags = entry.getList(TAG_FLAGS, Tag.TAG_STRING);
            for (Tag flagTag : flags) {
                if (flagTag instanceof StringTag stringTag) {
                    record.setFlag(stringTag.getAsString());
                }
            }

            ListTag counters = entry.getList(TAG_COUNTERS, Tag.TAG_COMPOUND);
            for (Tag counterTag : counters) {
                if (counterTag instanceof CompoundTag counter) {
                    record.setCounter(counter.getString(TAG_KEY), counter.getInt(TAG_INT_VALUE));
                }
            }

            ListTag cooldowns = entry.getList(TAG_COOLDOWNS, Tag.TAG_COMPOUND);
            for (Tag cooldownTag : cooldowns) {
                if (cooldownTag instanceof CompoundTag cooldown) {
                    record.setCooldown(cooldown.getString(TAG_KEY), cooldown.getLong(TAG_LONG_VALUE));
                }
            }

            relationships
                    .computeIfAbsent(characterId, ignored -> new HashMap<>())
                    .put(playerUuid, record);
        }

        return new NpcRelationshipSavedData(relationships);
    }

    public NpcRelationshipRecord getOrCreate(String characterId, UUID playerUuid) {
        return this.relationships
                .computeIfAbsent(characterId, ignored -> new HashMap<>())
                .computeIfAbsent(playerUuid, ignored -> new NpcRelationshipRecord());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();

        for (Map.Entry<String, Map<UUID, NpcRelationshipRecord>> characterEntry : this.relationships.entrySet()) {
            for (Map.Entry<UUID, NpcRelationshipRecord> playerEntry : characterEntry.getValue().entrySet()) {
                NpcRelationshipRecord record = playerEntry.getValue();
                CompoundTag entry = new CompoundTag();

                entry.putString(TAG_CHARACTER_ID, characterEntry.getKey());
                entry.putUUID(TAG_PLAYER_UUID, playerEntry.getKey());
                entry.putInt(TAG_AFFINITY, record.affinity());
                entry.putInt(TAG_INTERACTIONS, record.interactions());
                entry.putLong(TAG_LAST_INTERACTION_GAME_TIME, record.lastInteractionGameTime());
                entry.putLong(TAG_NEXT_REWARD_GAME_TIME, record.nextRewardGameTime());
                entry.putInt(TAG_SPECIAL_REWARDS, record.specialRewards());

                ListTag flags = new ListTag();
                for (String flag : record.flags()) {
                    flags.add(StringTag.valueOf(flag));
                }
                entry.put(TAG_FLAGS, flags);

                ListTag counters = new ListTag();
                for (Map.Entry<String, Integer> counterEntry : record.counters().entrySet()) {
                    CompoundTag counter = new CompoundTag();
                    counter.putString(TAG_KEY, counterEntry.getKey());
                    counter.putInt(TAG_INT_VALUE, counterEntry.getValue());
                    counters.add(counter);
                }
                entry.put(TAG_COUNTERS, counters);

                ListTag cooldowns = new ListTag();
                for (Map.Entry<String, Long> cooldownEntry : record.cooldowns().entrySet()) {
                    CompoundTag cooldown = new CompoundTag();
                    cooldown.putString(TAG_KEY, cooldownEntry.getKey());
                    cooldown.putLong(TAG_LONG_VALUE, cooldownEntry.getValue());
                    cooldowns.add(cooldown);
                }
                entry.put(TAG_COOLDOWNS, cooldowns);

                entries.add(entry);
            }
        }

        tag.put(TAG_RELATIONSHIPS, entries);
        return tag;
    }
}
