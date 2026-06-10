package com.hivesandcolonies.hccharacters.character.polen.progression.player;

import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PolenPlayerRelationshipData {
    private int affinity;
    private int interactionCount;
    private int tasksCompletedForPolen;
    private long lastInteractionGameTime;
    private final Set<String> playerFlags;
    private final Map<String, Long> cooldowns;

    public PolenPlayerRelationshipData() {
        this.affinity = PolenAffinityLevels.STRANGER;
        this.playerFlags = new HashSet<>();
        this.cooldowns = new HashMap<>();
    }

    public int getAffinity() {
        return affinity;
    }

    public void setAffinity(int affinity) {
        this.affinity = PolenAffinityLevels.clamp(affinity);
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = Math.max(0, interactionCount);
    }

    public void incrementInteractionCount() {
        this.interactionCount++;
    }

    public int getTasksCompletedForPolen() {
        return tasksCompletedForPolen;
    }

    public void setTasksCompletedForPolen(int tasksCompletedForPolen) {
        this.tasksCompletedForPolen = Math.max(0, tasksCompletedForPolen);
    }

    public void incrementTasksCompletedForPolen() {
        this.tasksCompletedForPolen++;
    }

    public long getLastInteractionGameTime() {
        return lastInteractionGameTime;
    }

    public void setLastInteractionGameTime(long lastInteractionGameTime) {
        this.lastInteractionGameTime = Math.max(0L, lastInteractionGameTime);
    }

    public Set<String> getPlayerFlags() {
        return Set.copyOf(playerFlags);
    }

    public void addPlayerFlag(String flag) {
        playerFlags.add(flag);
    }

    public void clearPlayerFlag(String flag) {
        playerFlags.remove(flag);
    }

    public void resetAffinity() {
        this.affinity = PolenAffinityLevels.STRANGER;
    }

    public boolean isCooldownActive(String key, long gameTime) {
        return key != null && cooldowns.getOrDefault(key, 0L) > gameTime;
    }

    public void setCooldown(String key, long untilGameTime) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (untilGameTime <= 0L) {
            cooldowns.remove(key);
        } else {
            cooldowns.put(key, untilGameTime);
        }
    }

    public Map<String, Long> getCooldowns() {
        return Map.copyOf(cooldowns);
    }
}
