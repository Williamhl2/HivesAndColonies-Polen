package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public final class LucyVillageEncounterSavedData extends SavedData {
    private static final String FILE_ID = HcCharacters.MODID + "_lucy_village_encounters";

    private static final String TAG_PLAYER_RETRY_COOLDOWNS = "playerRetryCooldowns";
    private static final String TAG_PLAYER_UUID = "playerUuid";
    private static final String TAG_UNTIL = "until";
    private static final String TAG_ACTIVE_ANCHOR = "activeAnchor";
    private static final String TAG_ACTIVE_UNTIL = "activeUntil";
    private static final String TAG_ACTIVE_LUCY_UUID = "activeLucyUuid";
    private static final String TAG_ACTIVE_SOA_UUID = "activeSoaUuid";
    private static final String TAG_NEXT_AMBIENT_TICK = "nextAmbientTick";
    private static final String TAG_AMBIENT_STEP = "ambientStep";

    private final Map<UUID, Long> playerRetryCooldowns;
    private BlockPos activeAnchor;
    private long activeUntil;
    private UUID activeLucyUuid;
    private UUID activeSoaUuid;
    private long nextAmbientTick;
    private int ambientStep;

    private LucyVillageEncounterSavedData() {
        this(new HashMap<>(), null, 0L, null, null, 0L, 0);
    }

    private LucyVillageEncounterSavedData(
            Map<UUID, Long> playerRetryCooldowns,
            BlockPos activeAnchor,
            long activeUntil,
            UUID activeLucyUuid,
            UUID activeSoaUuid,
            long nextAmbientTick,
            int ambientStep
    ) {
        this.playerRetryCooldowns = playerRetryCooldowns;
        this.activeAnchor = activeAnchor;
        this.activeUntil = activeUntil;
        this.activeLucyUuid = activeLucyUuid;
        this.activeSoaUuid = activeSoaUuid;
        this.nextAmbientTick = nextAmbientTick;
        this.ambientStep = ambientStep;
    }

    public static LucyVillageEncounterSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(factory(), FILE_ID);
    }

    public static SavedData.Factory<LucyVillageEncounterSavedData> factory() {
        return new SavedData.Factory<>(
                LucyVillageEncounterSavedData::new,
                LucyVillageEncounterSavedData::load,
                DataFixTypes.LEVEL
        );
    }

    public boolean isPlayerOnRetryCooldown(UUID playerId, long now) {
        return this.playerRetryCooldowns.getOrDefault(playerId, 0L) > now;
    }

    public void setPlayerRetryCooldown(UUID playerId, long until) {
        this.playerRetryCooldowns.put(playerId, until);
        this.setDirty();
    }

    public void setActiveScene(BlockPos anchor, UUID lucyUuid, UUID soaUuid, long activeUntil, long nextAmbientTick) {
        this.activeAnchor = anchor == null ? null : anchor.immutable();
        this.activeLucyUuid = lucyUuid;
        this.activeSoaUuid = soaUuid;
        this.activeUntil = activeUntil;
        this.nextAmbientTick = nextAmbientTick;
        this.ambientStep = 0;
        this.setDirty();
    }

    public void clearActiveScene() {
        if (this.activeAnchor == null && this.activeLucyUuid == null && this.activeSoaUuid == null && this.activeUntil == 0L) {
            return;
        }
        this.activeAnchor = null;
        this.activeLucyUuid = null;
        this.activeSoaUuid = null;
        this.activeUntil = 0L;
        this.nextAmbientTick = 0L;
        this.ambientStep = 0;
        this.setDirty();
    }

    public boolean hasActiveScene() {
        return this.activeAnchor != null && this.activeUntil > 0L && this.activeLucyUuid != null && this.activeSoaUuid != null;
    }

    public BlockPos getActiveAnchor() {
        return this.activeAnchor;
    }

    public long getActiveUntil() {
        return this.activeUntil;
    }

    public UUID getActiveLucyUuid() {
        return this.activeLucyUuid;
    }

    public UUID getActiveSoaUuid() {
        return this.activeSoaUuid;
    }

    public long getNextAmbientTick() {
        return this.nextAmbientTick;
    }

    public int getAmbientStep() {
        return this.ambientStep;
    }

    public void advanceAmbient(long nextTick, int step) {
        this.nextAmbientTick = nextTick;
        this.ambientStep = step;
        this.setDirty();
    }

    public void cleanup(long now) {
        boolean changed = removeExpired(this.playerRetryCooldowns, now);
        if (this.activeUntil > 0L && this.activeUntil <= now && this.activeAnchor == null) {
            this.activeUntil = 0L;
            this.activeLucyUuid = null;
            this.activeSoaUuid = null;
            this.nextAmbientTick = 0L;
            this.ambientStep = 0;
            changed = true;
        }
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

    public static LucyVillageEncounterSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Map<UUID, Long> retryCooldowns = loadPlayerCooldowns(tag.getList(TAG_PLAYER_RETRY_COOLDOWNS, Tag.TAG_COMPOUND));
        BlockPos activeAnchor = tag.contains(TAG_ACTIVE_ANCHOR) ? BlockPos.of(tag.getLong(TAG_ACTIVE_ANCHOR)) : null;
        long activeUntil = tag.getLong(TAG_ACTIVE_UNTIL);
        UUID activeLucyUuid = tag.hasUUID(TAG_ACTIVE_LUCY_UUID) ? tag.getUUID(TAG_ACTIVE_LUCY_UUID) : null;
        UUID activeSoaUuid = tag.hasUUID(TAG_ACTIVE_SOA_UUID) ? tag.getUUID(TAG_ACTIVE_SOA_UUID) : null;
        long nextAmbientTick = tag.getLong(TAG_NEXT_AMBIENT_TICK);
        int ambientStep = tag.getInt(TAG_AMBIENT_STEP);
        return new LucyVillageEncounterSavedData(
                retryCooldowns,
                activeAnchor,
                activeUntil,
                activeLucyUuid,
                activeSoaUuid,
                nextAmbientTick,
                ambientStep
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(TAG_PLAYER_RETRY_COOLDOWNS, savePlayerCooldowns(this.playerRetryCooldowns));
        if (this.activeAnchor != null) {
            tag.putLong(TAG_ACTIVE_ANCHOR, this.activeAnchor.asLong());
        }
        if (this.activeLucyUuid != null) {
            tag.putUUID(TAG_ACTIVE_LUCY_UUID, this.activeLucyUuid);
        }
        if (this.activeSoaUuid != null) {
            tag.putUUID(TAG_ACTIVE_SOA_UUID, this.activeSoaUuid);
        }
        tag.putLong(TAG_ACTIVE_UNTIL, this.activeUntil);
        tag.putLong(TAG_NEXT_AMBIENT_TICK, this.nextAmbientTick);
        tag.putInt(TAG_AMBIENT_STEP, this.ambientStep);
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
}
