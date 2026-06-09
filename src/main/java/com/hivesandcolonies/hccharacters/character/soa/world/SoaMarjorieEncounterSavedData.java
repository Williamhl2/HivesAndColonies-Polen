package com.hivesandcolonies.hccharacters.character.soa.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class SoaMarjorieEncounterSavedData extends SavedData {
    private static final String FILE_ID = HcCharacters.MODID + "_soa_marjorie_encounters";

    private static final String TAG_BOARD_PLAYER_COOLDOWNS = "boardPlayerCooldowns";
    private static final String TAG_CAVE_PLAYER_COOLDOWNS = "cavePlayerCooldowns";
    private static final String TAG_BOARD_POSITION_COOLDOWNS = "boardPositionCooldowns";
    private static final String TAG_PLAYER_UUID = "playerUuid";
    private static final String TAG_BOARD_POS = "boardPos";
    private static final String TAG_UNTIL = "until";

    private final Map<UUID, Long> boardPlayerCooldowns;
    private final Map<UUID, Long> cavePlayerCooldowns;
    private final Map<Long, Long> boardPositionCooldowns;

    private SoaMarjorieEncounterSavedData() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private SoaMarjorieEncounterSavedData(
            Map<UUID, Long> boardPlayerCooldowns,
            Map<UUID, Long> cavePlayerCooldowns,
            Map<Long, Long> boardPositionCooldowns
    ) {
        this.boardPlayerCooldowns = boardPlayerCooldowns;
        this.cavePlayerCooldowns = cavePlayerCooldowns;
        this.boardPositionCooldowns = boardPositionCooldowns;
    }

    public static SoaMarjorieEncounterSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(factory(), FILE_ID);
    }

    public static SavedData.Factory<SoaMarjorieEncounterSavedData> factory() {
        return new SavedData.Factory<>(
                SoaMarjorieEncounterSavedData::new,
                SoaMarjorieEncounterSavedData::load,
                DataFixTypes.LEVEL
        );
    }

    public boolean isBoardPlayerOnCooldown(UUID playerId, long now) {
        return this.boardPlayerCooldowns.getOrDefault(playerId, 0L) > now;
    }

    public boolean isCavePlayerOnCooldown(UUID playerId, long now) {
        return this.cavePlayerCooldowns.getOrDefault(playerId, 0L) > now;
    }

    public boolean isBoardPositionOnCooldown(long boardPos, long now) {
        return this.boardPositionCooldowns.getOrDefault(boardPos, 0L) > now;
    }

    public void setBoardPlayerCooldown(UUID playerId, long until) {
        this.boardPlayerCooldowns.put(playerId, until);
        this.setDirty();
    }

    public void setCavePlayerCooldown(UUID playerId, long until) {
        this.cavePlayerCooldowns.put(playerId, until);
        this.setDirty();
    }

    public void setBoardPositionCooldown(long boardPos, long until) {
        this.boardPositionCooldowns.put(boardPos, until);
        this.setDirty();
    }

    public void cleanup(long now) {
        boolean changed = removeExpired(this.boardPlayerCooldowns, now);
        changed |= removeExpired(this.cavePlayerCooldowns, now);
        changed |= removeExpired(this.boardPositionCooldowns, now);
        if (changed) {
            this.setDirty();
        }
    }

    private static <K> boolean removeExpired(Map<K, Long> cooldowns, long now) {
        boolean changed = false;
        Iterator<Map.Entry<K, Long>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    public static SoaMarjorieEncounterSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Map<UUID, Long> boardPlayerCooldowns = loadPlayerCooldowns(tag.getList(TAG_BOARD_PLAYER_COOLDOWNS, Tag.TAG_COMPOUND));
        Map<UUID, Long> cavePlayerCooldowns = loadPlayerCooldowns(tag.getList(TAG_CAVE_PLAYER_COOLDOWNS, Tag.TAG_COMPOUND));
        Map<Long, Long> boardPositionCooldowns = loadPositionCooldowns(tag.getList(TAG_BOARD_POSITION_COOLDOWNS, Tag.TAG_COMPOUND));
        return new SoaMarjorieEncounterSavedData(boardPlayerCooldowns, cavePlayerCooldowns, boardPositionCooldowns);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(TAG_BOARD_PLAYER_COOLDOWNS, savePlayerCooldowns(this.boardPlayerCooldowns));
        tag.put(TAG_CAVE_PLAYER_COOLDOWNS, savePlayerCooldowns(this.cavePlayerCooldowns));
        tag.put(TAG_BOARD_POSITION_COOLDOWNS, savePositionCooldowns(this.boardPositionCooldowns));
        return tag;
    }

    private static Map<UUID, Long> loadPlayerCooldowns(ListTag list) {
        Map<UUID, Long> cooldowns = new HashMap<>();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag entry && entry.hasUUID(TAG_PLAYER_UUID)) {
                cooldowns.put(entry.getUUID(TAG_PLAYER_UUID), entry.getLong(TAG_UNTIL));
            }
        }
        return cooldowns;
    }

    private static Map<Long, Long> loadPositionCooldowns(ListTag list) {
        Map<Long, Long> cooldowns = new HashMap<>();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag entry) {
                cooldowns.put(entry.getLong(TAG_BOARD_POS), entry.getLong(TAG_UNTIL));
            }
        }
        return cooldowns;
    }

    private static ListTag savePlayerCooldowns(Map<UUID, Long> cooldowns) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID(TAG_PLAYER_UUID, entry.getKey());
            tag.putLong(TAG_UNTIL, entry.getValue());
            list.add(tag);
        }
        return list;
    }

    private static ListTag savePositionCooldowns(Map<Long, Long> cooldowns) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Long> entry : cooldowns.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putLong(TAG_BOARD_POS, entry.getKey());
            tag.putLong(TAG_UNTIL, entry.getValue());
            list.add(tag);
        }
        return list;
    }
}
