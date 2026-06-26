package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;

public final class PolenProfileDisplayData {
    private final String displayName;
    private final String relationshipRankText;
    private final int relationshipAffinity;
    private final int nextAffinityThreshold;
    private final String currentStatusKey;
    private final String currentPurposeTitleKey;
    private final String currentPurposeBodyKey;
    private final int unlockedMemoryCount;

    private PolenProfileDisplayData(
            String displayName,
            String relationshipRankText,
            int relationshipAffinity,
            int nextAffinityThreshold,
            String currentStatusKey,
            String currentPurposeTitleKey,
            String currentPurposeBodyKey,
            int unlockedMemoryCount
    ) {
        this.displayName = displayName;
        this.relationshipRankText = relationshipRankText;
        this.relationshipAffinity = relationshipAffinity;
        this.nextAffinityThreshold = nextAffinityThreshold;
        this.currentStatusKey = currentStatusKey;
        this.currentPurposeTitleKey = currentPurposeTitleKey;
        this.currentPurposeBodyKey = currentPurposeBodyKey;
        this.unlockedMemoryCount = unlockedMemoryCount;
    }

    public static PolenProfileDisplayData from(
            String displayName,
            String relationshipRankText,
            int relationshipAffinity,
            int nextAffinityThreshold,
            PolenTaskType task,
            boolean hasHome,
            boolean trustWalkActive,
            boolean trustWalkUnlocked,
            int unlockedMemoryCount
    ) {
        String currentStatusKey;
        if (trustWalkActive) {
            currentStatusKey = "screen.polen.profile.status.guided_walk";
        } else {
            currentStatusKey = switch (task) {
                case SEEK_SAFETY -> "screen.polen.profile.status.seeking_safety";
                case KEEP_DISTANCE -> "screen.polen.profile.status.keeping_distance";
                case APPROACH_TRUSTED_PLAYER -> "screen.polen.profile.status.approaching";
                case INVESTIGATE_INTEREST -> "screen.polen.profile.status.observing";
                case SEEK_REST -> hasHome
                        ? "screen.polen.profile.status.returning_home"
                        : "screen.polen.profile.status.seeking_rest";
                case QUIET_CREATION -> "screen.polen.profile.status.quiet_creation";
                case WANDER_SAFE -> hasHome
                        ? "screen.polen.profile.status.wandering_home"
                        : "screen.polen.profile.status.wandering_carefully";
            };
        }

        String currentPurposeTitleKey;
        String currentPurposeBodyKey;
        if (task.isUrgent()) {
            currentPurposeTitleKey = "screen.polen.profile.purpose.recovering.title";
            currentPurposeBodyKey = "screen.polen.profile.purpose.recovering.body";
        } else if (!hasHome) {
            currentPurposeTitleKey = trustWalkUnlocked
                    ? "screen.polen.profile.purpose.needs_refuge.title"
                    : "screen.polen.profile.purpose.cautious.title";
            currentPurposeBodyKey = trustWalkUnlocked
                    ? "screen.polen.profile.purpose.needs_refuge.body"
                    : "screen.polen.profile.purpose.cautious.body";
        } else if (trustWalkActive) {
            currentPurposeTitleKey = "screen.polen.profile.purpose.guided.title";
            currentPurposeBodyKey = "screen.polen.profile.purpose.guided.body";
        } else if (unlockedMemoryCount <= 2) {
            currentPurposeTitleKey = "screen.polen.profile.purpose.learning_world.title";
            currentPurposeBodyKey = "screen.polen.profile.purpose.learning_world.body";
        } else {
            currentPurposeTitleKey = "screen.polen.profile.purpose.settled.title";
            currentPurposeBodyKey = "screen.polen.profile.purpose.settled.body";
        }

        return new PolenProfileDisplayData(
                displayName,
                relationshipRankText == null ? "" : relationshipRankText,
                relationshipAffinity,
                nextAffinityThreshold,
                currentStatusKey,
                currentPurposeTitleKey,
                currentPurposeBodyKey,
                unlockedMemoryCount
        );
    }

    public String displayName() {
        return this.displayName;
    }

    public String relationshipRankText() {
        return this.relationshipRankText;
    }

    public int relationshipAffinity() {
        return this.relationshipAffinity;
    }

    public int nextAffinityThreshold() {
        return this.nextAffinityThreshold;
    }

    public String currentStatusKey() {
        return this.currentStatusKey;
    }

    public String currentPurposeTitleKey() {
        return this.currentPurposeTitleKey;
    }

    public String currentPurposeBodyKey() {
        return this.currentPurposeBodyKey;
    }

    public int unlockedMemoryCount() {
        return this.unlockedMemoryCount;
    }
}
