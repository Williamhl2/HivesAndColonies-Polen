package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NpcRelationshipRecord {
    private int affinity;
    private int interactions;
    private long lastInteractionGameTime;
    private long nextRewardGameTime;
    private int specialRewards;
    private final Set<String> flags = new HashSet<>();
    private final Map<String, Integer> counters = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    public int affinity() {
        return this.affinity;
    }

    public void setAffinity(int affinity) {
        this.affinity = NpcRelationshipLevels.clamp(affinity);
    }

    public int interactions() {
        return this.interactions;
    }

    public void setInteractions(int interactions) {
        this.interactions = Math.max(0, interactions);
    }

    public void incrementInteractions() {
        this.interactions += 1;
    }

    public long lastInteractionGameTime() {
        return this.lastInteractionGameTime;
    }

    public void setLastInteractionGameTime(long lastInteractionGameTime) {
        this.lastInteractionGameTime = Math.max(0L, lastInteractionGameTime);
    }

    public long nextRewardGameTime() {
        return this.nextRewardGameTime;
    }

    public void setNextRewardGameTime(long nextRewardGameTime) {
        this.nextRewardGameTime = Math.max(0L, nextRewardGameTime);
    }

    public int specialRewards() {
        return this.specialRewards;
    }

    public void setSpecialRewards(int specialRewards) {
        this.specialRewards = Math.max(0, specialRewards);
    }

    public void incrementSpecialRewards() {
        this.specialRewards += 1;
    }

    public boolean hasFlag(String flag) {
        return flag != null && this.flags.contains(flag);
    }

    public void setFlag(String flag) {
        if (flag != null && !flag.isBlank()) {
            this.flags.add(flag);
        }
    }

    public void clearFlag(String flag) {
        if (flag != null && !flag.isBlank()) {
            this.flags.remove(flag);
        }
    }

    public Set<String> flags() {
        return Set.copyOf(this.flags);
    }

    public int counter(String key) {
        return this.counters.getOrDefault(key, 0);
    }

    public void setCounter(String key, int value) {
        if (key != null && !key.isBlank()) {
            this.counters.put(key, Math.max(0, value));
        }
    }

    public int incrementCounter(String key) {
        int value = this.counter(key) + 1;
        this.setCounter(key, value);
        return value;
    }

    public Map<String, Integer> counters() {
        return Map.copyOf(this.counters);
    }

    public long cooldownUntil(String key) {
        return this.cooldowns.getOrDefault(key, 0L);
    }

    public boolean isCooldownActive(String key, long now) {
        return this.cooldownUntil(key) > now;
    }

    public void setCooldown(String key, long untilGameTime) {
        if (key != null && !key.isBlank()) {
            if (untilGameTime <= 0L) {
                this.cooldowns.remove(key);
            } else {
                this.cooldowns.put(key, untilGameTime);
            }
        }
    }

    public Map<String, Long> cooldowns() {
        return Map.copyOf(this.cooldowns);
    }

    public int trustTier() {
        return Math.min(3, NpcRelationshipLevels.rankIndex(this.affinity));
    }
}
