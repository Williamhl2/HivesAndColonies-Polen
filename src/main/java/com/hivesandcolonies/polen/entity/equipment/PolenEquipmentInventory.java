package com.hivesandcolonies.polen.entity.equipment;

import com.hivesandcolonies.polen.entity.ai.world.identity.PolenWorldAffinity;
import net.minecraft.nbt.CompoundTag;

public final class PolenEquipmentInventory {
    private static final String TAG_AFFINITY_CHARM = "AffinityCharm";

    private PolenWorldAffinity affinityCharm = PolenWorldAffinity.NONE;

    public PolenWorldAffinity getAffinityCharm() {
        return this.affinityCharm;
    }

    public boolean hasAffinityCharm() {
        return this.affinityCharm != PolenWorldAffinity.NONE;
    }

    public void setAffinityCharm(PolenWorldAffinity affinityCharm) {
        this.affinityCharm = affinityCharm == null ? PolenWorldAffinity.NONE : affinityCharm;
    }

    public void save(CompoundTag tag) {
        tag.putString(TAG_AFFINITY_CHARM, this.affinityCharm.getSerializedName());
    }

    public void load(CompoundTag tag) {
        this.affinityCharm = PolenWorldAffinity.byName(tag.getString(TAG_AFFINITY_CHARM));
    }
}
