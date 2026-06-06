package com.hivesandcolonies.characters.character.polen.progression.player;

import com.hivesandcolonies.characters.bootstrap.Characters;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class PolenPlayerRelationshipManager {
    private static final String FILE_ID = Characters.MODID + "_player_relationships";
    private static final String TAG_RELATIONSHIPS = "relationships";
    private static final String TAG_PLAYER_UUID = "playerUuid";
    private static final String TAG_AFFINITY = "affinity";
    private static final String TAG_INTERACTION_COUNT = "interactionCount";
    private static final String TAG_TASKS_COMPLETED = "tasksCompletedForPolen";
    private static final String TAG_LAST_INTERACTION_GAME_TIME = "lastInteractionGameTime";
    private static final String TAG_PLAYER_FLAGS = "playerFlags";

    private PolenPlayerRelationshipManager() {}

    public static PolenPlayerRelationshipData getRelationship(Player player) {
        SavedRelationships savedData = getSavedData(player);
        if (savedData == null) {
            return new PolenPlayerRelationshipData();
        }

        return savedData.getOrCreate(player.getUUID());
    }

    public static int getAffinity(Player player) {
        return getRelationship(player).getAffinity();
    }

    public static void setAffinity(Player player, int value) {
        mutate(player, data -> data.setAffinity(value));
    }

    public static void addAffinity(Player player, int amount) {
        mutate(player, data -> data.setAffinity(data.getAffinity() + amount));
    }

    public static void removeAffinity(Player player, int amount) {
        mutate(player, data -> data.setAffinity(data.getAffinity() - amount));
    }

    public static void resetAffinity(Player player) {
        mutate(player, PolenPlayerRelationshipData::resetAffinity);
    }

    public static void recordInteraction(Player player) {
        mutate(player, data -> {
            data.incrementInteractionCount();
            data.setLastInteractionGameTime(player.level().getGameTime());
        });
    }

    public static void incrementTasksCompleted(Player player) {
        mutate(player, PolenPlayerRelationshipData::incrementTasksCompletedForPolen);
    }

    public static boolean hasPlayerFlag(Player player, String flag) {
        if (flag == null || flag.isBlank()) {
            return false;
        }
        return getRelationship(player).getPlayerFlags().contains(flag);
    }

    public static void addPlayerFlag(Player player, String flag) {
        if (flag == null || flag.isBlank()) {
            return;
        }
        mutate(player, data -> data.addPlayerFlag(flag));
    }

    public static void clearPlayerFlag(Player player, String flag) {
        if (flag == null || flag.isBlank()) {
            return;
        }
        mutate(player, data -> data.clearPlayerFlag(flag));
    }

    private static void mutate(Player player, Consumer<PolenPlayerRelationshipData> consumer) {
        SavedRelationships savedData = getSavedData(player);
        if (savedData == null) {
            return;
        }

        consumer.accept(savedData.getOrCreate(player.getUUID()));
        savedData.setDirty();
    }

    private static SavedRelationships getSavedData(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        ServerLevel overworld = serverLevel.getServer().overworld();
        DimensionDataStorage dataStorage = overworld.getDataStorage();
        return dataStorage.computeIfAbsent(SavedRelationships.factory(), FILE_ID);
    }

    private static final class SavedRelationships extends SavedData {
        private final Map<UUID, PolenPlayerRelationshipData> relationships;

        private SavedRelationships() {
            this(new HashMap<>());
        }

        private SavedRelationships(Map<UUID, PolenPlayerRelationshipData> relationships) {
            this.relationships = relationships;
        }

        public static SavedData.Factory<SavedRelationships> factory() {
            return new SavedData.Factory<>(
                    SavedRelationships::new,
                    SavedRelationships::load,
                    DataFixTypes.LEVEL
            );
        }

        public static SavedRelationships load(
                CompoundTag tag,
                HolderLookup.Provider registries
        ) {
            Map<UUID, PolenPlayerRelationshipData> relationships = new HashMap<>();
            ListTag entries = tag.getList(TAG_RELATIONSHIPS, Tag.TAG_COMPOUND);

            for (Tag entryTag : entries) {
                if (!(entryTag instanceof CompoundTag entry)) {
                    continue;
                }

                if (!entry.hasUUID(TAG_PLAYER_UUID)) {
                    continue;
                }

                UUID playerUuid = entry.getUUID(TAG_PLAYER_UUID);
                PolenPlayerRelationshipData data = new PolenPlayerRelationshipData();
                data.setAffinity(entry.getInt(TAG_AFFINITY));
                data.setInteractionCount(entry.getInt(TAG_INTERACTION_COUNT));
                data.setTasksCompletedForPolen(entry.getInt(TAG_TASKS_COMPLETED));
                data.setLastInteractionGameTime(entry.getLong(TAG_LAST_INTERACTION_GAME_TIME));

                ListTag playerFlags = entry.getList(TAG_PLAYER_FLAGS, Tag.TAG_STRING);
                for (Tag flagTag : playerFlags) {
                    if (flagTag instanceof StringTag stringTag) {
                        data.addPlayerFlag(stringTag.getAsString());
                    }
                }

                relationships.put(playerUuid, data);
            }

            return new SavedRelationships(relationships);
        }

        public PolenPlayerRelationshipData getOrCreate(UUID playerUuid) {
            return relationships.computeIfAbsent(
                    playerUuid,
                    ignored -> new PolenPlayerRelationshipData()
            );
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag entries = new ListTag();

            for (Map.Entry<UUID, PolenPlayerRelationshipData> entry : relationships.entrySet()) {
                PolenPlayerRelationshipData data = entry.getValue();
                CompoundTag relationshipTag = new CompoundTag();
                ListTag playerFlags = new ListTag();

                relationshipTag.putUUID(TAG_PLAYER_UUID, entry.getKey());
                relationshipTag.putInt(TAG_AFFINITY, data.getAffinity());
                relationshipTag.putInt(TAG_INTERACTION_COUNT, data.getInteractionCount());
                relationshipTag.putInt(TAG_TASKS_COMPLETED, data.getTasksCompletedForPolen());
                relationshipTag.putLong(TAG_LAST_INTERACTION_GAME_TIME, data.getLastInteractionGameTime());

                for (String flag : data.getPlayerFlags()) {
                    playerFlags.add(StringTag.valueOf(flag));
                }

                relationshipTag.put(TAG_PLAYER_FLAGS, playerFlags);
                entries.add(relationshipTag);
            }

            tag.put(TAG_RELATIONSHIPS, entries);
            return tag;
        }
    }
}
