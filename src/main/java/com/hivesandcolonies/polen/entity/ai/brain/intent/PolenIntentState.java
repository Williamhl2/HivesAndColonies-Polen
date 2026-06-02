package com.hivesandcolonies.polen.entity.ai.brain.intent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class PolenIntentState {

    private static final String TAG_INTENT = "Intent";
    private static final String TAG_REASON = "Reason";
    private static final String TAG_LOCKED_UNTIL = "LockedUntil";

    private PolenIntent currentIntent = PolenIntent.WANDER_SAFE;
    private String currentReason = "initial_wander";
    private long lockedUntilGameTime;

    public PolenIntent currentIntent() {
        return this.currentIntent;
    }

    public String currentReason() {
        return this.currentReason;
    }

    public long lockedUntilGameTime() {
        return this.lockedUntilGameTime;
    }

    public void set(PolenIntent intent, String reason, long lockedUntilGameTime) {
        this.currentIntent = intent;
        this.currentReason = reason;
        this.lockedUntilGameTime = lockedUntilGameTime;
    }

    public void save(CompoundTag tag, String rootKey) {
        CompoundTag intentTag = new CompoundTag();
        intentTag.putString(TAG_INTENT, this.currentIntent.name());
        intentTag.putString(TAG_REASON, this.currentReason);
        intentTag.putLong(TAG_LOCKED_UNTIL, this.lockedUntilGameTime);
        tag.put(rootKey, intentTag);
    }

    public void load(CompoundTag tag, String rootKey) {
        if (!tag.contains(rootKey, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag intentTag = tag.getCompound(rootKey);
        this.currentReason = intentTag.getString(TAG_REASON);
        this.lockedUntilGameTime = intentTag.getLong(TAG_LOCKED_UNTIL);

        try {
            this.currentIntent = PolenIntent.valueOf(intentTag.getString(TAG_INTENT));
        } catch (IllegalArgumentException ignored) {
            this.currentIntent = PolenIntent.WANDER_SAFE;
            this.currentReason = "initial_wander";
            this.lockedUntilGameTime = 0L;
        }
    }
}
