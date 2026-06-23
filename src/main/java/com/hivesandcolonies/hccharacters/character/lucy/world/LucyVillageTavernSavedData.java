package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.HashMap;
import java.util.Map;

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

public final class LucyVillageTavernSavedData extends SavedData {
    private static final String FILE_ID = HcCharacters.MODID + "_lucy_village_taverns";
    private static final String TAG_TAVERNS = "taverns";
    private static final String TAG_BELL = "bell";
    private static final String TAG_ANCHOR = "anchor";
    private static final String TAG_LUCY = "lucy";
    private static final String TAG_SOA = "soa";

    private final Map<Long, TavernSite> taverns;

    private LucyVillageTavernSavedData() {
        this(new HashMap<>());
    }

    private LucyVillageTavernSavedData(Map<Long, TavernSite> taverns) {
        this.taverns = taverns;
    }

    public static LucyVillageTavernSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(factory(), FILE_ID);
    }

    public static SavedData.Factory<LucyVillageTavernSavedData> factory() {
        return new SavedData.Factory<>(
                LucyVillageTavernSavedData::new,
                LucyVillageTavernSavedData::load,
                DataFixTypes.LEVEL
        );
    }

    public TavernSite getTavern(BlockPos bellPos) {
        return bellPos == null ? null : this.taverns.get(bellPos.asLong());
    }

    public void putTavern(TavernSite site) {
        if (site == null || site.bellPos() == null) {
            return;
        }
        this.taverns.put(site.bellPos().asLong(), site);
        this.setDirty();
    }

    public void removeTavern(BlockPos bellPos) {
        if (bellPos != null && this.taverns.remove(bellPos.asLong()) != null) {
            this.setDirty();
        }
    }

    public static LucyVillageTavernSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Map<Long, TavernSite> taverns = new HashMap<>();
        for (Tag entryTag : tag.getList(TAG_TAVERNS, Tag.TAG_COMPOUND)) {
            if (!(entryTag instanceof CompoundTag entry)
                    || !entry.contains(TAG_BELL)
                    || !entry.contains(TAG_ANCHOR)
                    || !entry.contains(TAG_LUCY)
                    || !entry.contains(TAG_SOA)) {
                continue;
            }

            BlockPos bellPos = BlockPos.of(entry.getLong(TAG_BELL));
            TavernSite site = new TavernSite(
                    bellPos,
                    BlockPos.of(entry.getLong(TAG_ANCHOR)),
                    BlockPos.of(entry.getLong(TAG_LUCY)),
                    BlockPos.of(entry.getLong(TAG_SOA))
            );
            taverns.put(bellPos.asLong(), site);
        }
        return new LucyVillageTavernSavedData(taverns);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (TavernSite site : this.taverns.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_BELL, site.bellPos().asLong());
            entry.putLong(TAG_ANCHOR, site.anchorPos().asLong());
            entry.putLong(TAG_LUCY, site.lucyPos().asLong());
            entry.putLong(TAG_SOA, site.soaPos().asLong());
            list.add(entry);
        }
        tag.put(TAG_TAVERNS, list);
        return tag;
    }

    public record TavernSite(BlockPos bellPos, BlockPos anchorPos, BlockPos lucyPos, BlockPos soaPos) {
        LucyVillageSceneLocator.SceneLocation toSceneLocation() {
            return new LucyVillageSceneLocator.SceneLocation(
                    this.bellPos.immutable(),
                    this.anchorPos.immutable(),
                    this.lucyPos.immutable(),
                    this.soaPos.immutable()
            );
        }
    }
}
