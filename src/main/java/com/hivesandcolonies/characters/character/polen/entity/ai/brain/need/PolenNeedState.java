package com.hivesandcolonies.characters.character.polen.entity.ai.brain.need;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class PolenNeedState {

    private static final String TAG_SAFETY = "Safety";
    private static final String TAG_SOCIAL = "Social";
    private static final String TAG_CURIOSITY = "Curiosity";
    private static final String TAG_REST = "Rest";
    private static final String TAG_MAGIC = "Magic";

    private int safety = 24;
    private int social = 36;
    private int curiosity = 44;
    private int rest = 28;
    private int magic = 24;

    public int safety() {
        return this.safety;
    }

    public int social() {
        return this.social;
    }

    public int curiosity() {
        return this.curiosity;
    }

    public int rest() {
        return this.rest;
    }

    public int magic() {
        return this.magic;
    }

    public void adjustSafety(int delta) {
        this.safety = clamp(this.safety + delta);
    }

    public void adjustSocial(int delta) {
        this.social = clamp(this.social + delta);
    }

    public void adjustCuriosity(int delta) {
        this.curiosity = clamp(this.curiosity + delta);
    }

    public void adjustRest(int delta) {
        this.rest = clamp(this.rest + delta);
    }

    public void adjustMagic(int delta) {
        this.magic = clamp(this.magic + delta);
    }

    public void save(CompoundTag tag, String rootKey) {
        CompoundTag needsTag = new CompoundTag();
        needsTag.putInt(TAG_SAFETY, this.safety);
        needsTag.putInt(TAG_SOCIAL, this.social);
        needsTag.putInt(TAG_CURIOSITY, this.curiosity);
        needsTag.putInt(TAG_REST, this.rest);
        needsTag.putInt(TAG_MAGIC, this.magic);
        tag.put(rootKey, needsTag);
    }

    public void load(CompoundTag tag, String rootKey) {
        if (!tag.contains(rootKey, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag needsTag = tag.getCompound(rootKey);
        this.safety = clamp(needsTag.getInt(TAG_SAFETY));
        this.social = clamp(needsTag.getInt(TAG_SOCIAL));
        this.curiosity = clamp(needsTag.getInt(TAG_CURIOSITY));
        this.rest = clamp(needsTag.getInt(TAG_REST));
        this.magic = clamp(needsTag.getInt(TAG_MAGIC));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
