package com.hivesandcolonies.polen.progression.world;

import com.hivesandcolonies.polen.progression.PolenStoryFlag;

import java.util.EnumSet;
import java.util.UUID;

public final class PolenWorldStoryData {
    private int currentChapter;
    private final EnumSet<PolenStoryFlag> worldFlags;
    private UUID polenEntityUuid;
    private boolean polenSpawned;

    public PolenWorldStoryData() {
        this.currentChapter = 0;
        this.worldFlags = EnumSet.noneOf(PolenStoryFlag.class);
    }

    public int getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(int currentChapter) {
        this.currentChapter = currentChapter;
    }

    public EnumSet<PolenStoryFlag> getWorldFlags() {
        return EnumSet.copyOf(worldFlags);
    }

    public boolean hasFlag(PolenStoryFlag flag) {
        return worldFlags.contains(flag);
    }

    public boolean setFlag(PolenStoryFlag flag) {
        return worldFlags.add(flag);
    }

    public boolean clearFlag(PolenStoryFlag flag) {
        return worldFlags.remove(flag);
    }

    public void resetFlags() {
        worldFlags.clear();
    }

    public UUID getPolenEntityUuid() {
        return polenEntityUuid;
    }

    public void setPolenEntityUuid(UUID polenEntityUuid) {
        this.polenEntityUuid = polenEntityUuid;
    }

    public boolean isPolenSpawned() {
        return polenSpawned;
    }

    public void setPolenSpawned(boolean polenSpawned) {
        this.polenSpawned = polenSpawned;
    }
}
