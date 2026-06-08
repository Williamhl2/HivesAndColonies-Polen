package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests;

import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

public final class PolenInterestProfile {
    private final EnumMap<PolenInterest, Integer> scores;

    public PolenInterestProfile() {
        this.scores = new EnumMap<>(PolenInterest.class);
        for (PolenInterest interest : PolenInterest.values()) {
            scores.put(interest, 50);
        }
    }

    public PolenInterestProfile(Map<PolenInterest, Integer> scores) {
        this();
        for (Map.Entry<PolenInterest, Integer> entry : scores.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public int get(PolenInterest interest) {
        return scores.getOrDefault(interest, 50);
    }

    public void set(PolenInterest interest, int value) {
        if (interest == null) {
            return;
        }
        scores.put(interest, clamp(value));
    }

    public void add(PolenInterest interest, int amount) {
        set(interest, get(interest) + amount);
    }

    public PolenInterest dominantInterest() {
        PolenInterest best = PolenInterest.EXPLORATION;
        int bestScore = Integer.MIN_VALUE;
        for (PolenInterest interest : PolenInterest.values()) {
            int score = get(interest);
            if (score > bestScore) {
                best = interest;
                bestScore = score;
            }
        }
        return best;
    }

    public EnumMap<PolenInterest, Integer> copyScores() {
        return new EnumMap<>(scores);
    }

    public String summary() {
        StringJoiner joiner = new StringJoiner(", ");
        for (PolenInterest interest : PolenInterest.values()) {
            joiner.add(interest.name().toLowerCase() + "=" + get(interest));
        }
        return joiner.toString();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
