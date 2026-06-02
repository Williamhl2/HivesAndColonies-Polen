package com.hivesandcolonies.polen.progression.world;

import com.hivesandcolonies.polen.entity.ai.world.identity.PolenIdentity;
import com.hivesandcolonies.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.polen.entity.ai.world.interests.PolenInterestGenerator;
import com.hivesandcolonies.polen.entity.ai.world.interests.PolenInterestProfile;
import com.hivesandcolonies.polen.entity.ai.world.story.PolenStoryStage;
import com.hivesandcolonies.polen.entity.ai.world.story.PolenWorldMemory;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;

import java.util.EnumSet;
import java.util.UUID;

public final class PolenWorldStoryData {
    private int currentChapter;
    private final EnumSet<PolenStoryFlag> worldFlags;
    private UUID polenEntityUuid;
    private boolean polenSpawned;
    private PolenIdentity identity;
    private PolenInterestProfile interestProfile;
    private PolenStoryStage storyStage;
    private final EnumSet<PolenWorldMemory> worldMemories;

    public PolenWorldStoryData() {
        this.currentChapter = 0;
        this.worldFlags = EnumSet.noneOf(PolenStoryFlag.class);
        this.interestProfile = new PolenInterestProfile();
        this.storyStage = PolenStoryStage.AWAKENING;
        this.worldMemories = EnumSet.noneOf(PolenWorldMemory.class);
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

    public PolenIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(PolenIdentity identity) {
        this.identity = identity;
        if (identity != null && interestProfile == null) {
            this.interestProfile = PolenInterestGenerator.generate(identity.personalitySeed());
        }
    }

    public boolean hasIdentity() {
        return identity != null;
    }

    public PolenInterestProfile getInterestProfile() {
        if (interestProfile == null) {
            interestProfile = identity == null
                    ? new PolenInterestProfile()
                    : PolenInterestGenerator.generate(identity.personalitySeed());
        }
        return interestProfile;
    }

    public void setInterestProfile(PolenInterestProfile interestProfile) {
        this.interestProfile = interestProfile == null ? new PolenInterestProfile() : interestProfile;
    }

    public int getInterestScore(PolenInterest interest) {
        return getInterestProfile().get(interest);
    }

    public void adjustInterest(PolenInterest interest, int amount) {
        getInterestProfile().add(interest, amount);
    }

    public PolenStoryStage getStoryStage() {
        return storyStage == null ? PolenStoryStage.AWAKENING : storyStage;
    }

    public void setStoryStage(PolenStoryStage storyStage) {
        this.storyStage = storyStage == null ? PolenStoryStage.AWAKENING : storyStage;
    }

    public EnumSet<PolenWorldMemory> getWorldMemories() {
        return EnumSet.copyOf(worldMemories);
    }

    public boolean hasMemory(PolenWorldMemory memory) {
        return worldMemories.contains(memory);
    }

    public boolean remember(PolenWorldMemory memory) {
        return worldMemories.add(memory);
    }

    public boolean forget(PolenWorldMemory memory) {
        return worldMemories.remove(memory);
    }
}
