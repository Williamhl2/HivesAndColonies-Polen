package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.hccharacters.common.network.ClientboundPolenProfilePayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Client-only presentation model for the Polen profile screen.
 *
 * This class builds immutable UI-facing slices from already-synced entity state.
 * It should assemble view data, not own gameplay logic or duplicate screen rules.
 */
public final class PolenProfileView {
    private final int entityId;
    private final PolenProfileDisplayData display;
    private final PolenProfileActionState actions;
    private final PolenProfileHelpState help;

    private PolenProfileView(
            int entityId,
            PolenProfileDisplayData display,
            PolenProfileActionState actions,
            PolenProfileHelpState help
    ) {
        this.entityId = entityId;
        this.display = display;
        this.actions = actions;
        this.help = help;
    }

    public static PolenProfileView from(PolenEntity polen) {
        return from(polen, null);
    }

    public static PolenProfileView from(PolenEntity polen, ClientboundPolenProfilePayload payload) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        List<PolenProfileInterestMetric> interestMetrics = barsFor(affinity, payload);
        List<PolenProfileMemoryEntry> memoryEntries = memoryEntriesFor(
                payload != null && payload.firstFlowerMemory(),
                payload != null && payload.firstHiveMemory(),
                payload != null && payload.firstSourceMemory(),
                payload != null && payload.firstColonyMemory(),
                payload != null && payload.firstResidenceMemory()
        );
        boolean hasHome = polen.hasAssignedHome();
        boolean trustWalkActive = polen.isTrustWalkSyncedActive();
        boolean trustWalkUnlocked = payload != null && payload.trustWalkUnlocked();
        boolean giftsOnCooldown = payload != null && payload.giftsOnCooldown();
        int relationshipAffinity = payload != null ? payload.affinity() : 0;
        int nextThreshold = payload != null ? payload.nextThreshold() : 10;
        int unlockedMemoryCount = unlockedMemoryCount(memoryEntries);

        PolenProfileDisplayData display = PolenProfileDisplayData.from(
                polen.getDisplayName().getString(),
                payload != null ? payload.relationshipRankText() : "",
                relationshipAffinity,
                nextThreshold,
                polen.getCurrentTask(),
                hasHome,
                trustWalkActive,
                trustWalkUnlocked,
                unlockedMemoryCount
        );
        PolenProfileActionState actions = PolenProfileActionState.from(
                trustWalkActive,
                trustWalkUnlocked,
                hasHome,
                giftsOnCooldown
        );
        PolenProfileHelpState help = PolenProfileHelpState.from(
                interestMetrics,
                memoryEntries,
                hasHome,
                polen.getProfileNeedSafety(),
                polen.getProfileNeedSocial(),
                polen.getProfileNeedCuriosity(),
                polen.getProfileNeedRest(),
                polen.getProfileNeedMagic()
        );

        return new PolenProfileView(polen.getId(), display, actions, help);
    }

    public int entityId() {
        return this.entityId;
    }

    public PolenProfileDisplayData display() {
        return this.display;
    }

    public PolenProfileActionState actions() {
        return this.actions;
    }

    public PolenProfileHelpState help() {
        return this.help;
    }

    private static List<PolenProfileInterestMetric> barsFor(PolenWorldAffinity affinity, ClientboundPolenProfilePayload payload) {
        if (payload != null) {
            List<PolenProfileInterestMetric> bars = new ArrayList<>(List.of(
                    new PolenProfileInterestMetric("bees", payload.beesInterest()),
                    new PolenProfileInterestMetric("magic", payload.magicInterest()),
                    new PolenProfileInterestMetric("colonies", payload.coloniesInterest()),
                    new PolenProfileInterestMetric("food", payload.foodInterest()),
                    new PolenProfileInterestMetric("decoration", payload.decorationInterest()),
                    new PolenProfileInterestMetric("exploration", payload.explorationInterest())
            ));
            bars.sort(Comparator.comparingInt(PolenProfileInterestMetric::value).reversed().thenComparing(PolenProfileInterestMetric::label));
            return List.copyOf(bars);
        }

        return switch (affinity) {
            case APIARIST -> List.of(
                    new PolenProfileInterestMetric("bees", 92),
                    new PolenProfileInterestMetric("exploration", 76),
                    new PolenProfileInterestMetric("food", 54),
                    new PolenProfileInterestMetric("magic", 42),
                    new PolenProfileInterestMetric("colonies", 38),
                    new PolenProfileInterestMetric("decoration", 34));
            case ARCANE -> List.of(
                    new PolenProfileInterestMetric("magic", 94),
                    new PolenProfileInterestMetric("exploration", 68),
                    new PolenProfileInterestMetric("decoration", 52),
                    new PolenProfileInterestMetric("bees", 44),
                    new PolenProfileInterestMetric("food", 36),
                    new PolenProfileInterestMetric("colonies", 30));
            case COLONIAL -> List.of(
                    new PolenProfileInterestMetric("colonies", 94),
                    new PolenProfileInterestMetric("food", 68),
                    new PolenProfileInterestMetric("decoration", 58),
                    new PolenProfileInterestMetric("exploration", 48),
                    new PolenProfileInterestMetric("bees", 34),
                    new PolenProfileInterestMetric("magic", 30));
            case HARVEST -> List.of(
                    new PolenProfileInterestMetric("food", 94),
                    new PolenProfileInterestMetric("bees", 64),
                    new PolenProfileInterestMetric("decoration", 54),
                    new PolenProfileInterestMetric("colonies", 48),
                    new PolenProfileInterestMetric("exploration", 38),
                    new PolenProfileInterestMetric("magic", 28));
            case ARTISAN -> List.of(
                    new PolenProfileInterestMetric("decoration", 94),
                    new PolenProfileInterestMetric("colonies", 64),
                    new PolenProfileInterestMetric("magic", 52),
                    new PolenProfileInterestMetric("food", 44),
                    new PolenProfileInterestMetric("exploration", 40),
                    new PolenProfileInterestMetric("bees", 32));
            case WAYFARER -> List.of(
                    new PolenProfileInterestMetric("exploration", 94),
                    new PolenProfileInterestMetric("magic", 62),
                    new PolenProfileInterestMetric("colonies", 50),
                    new PolenProfileInterestMetric("bees", 46),
                    new PolenProfileInterestMetric("food", 38),
                    new PolenProfileInterestMetric("decoration", 32));
            case NONE -> List.of(
                    new PolenProfileInterestMetric("bees", 50),
                    new PolenProfileInterestMetric("magic", 50),
                    new PolenProfileInterestMetric("colonies", 50),
                    new PolenProfileInterestMetric("food", 50),
                    new PolenProfileInterestMetric("decoration", 50),
                    new PolenProfileInterestMetric("exploration", 50));
        };
    }

    private static List<PolenProfileMemoryEntry> memoryEntriesFor(
            boolean firstFlowerMemory,
            boolean firstHiveMemory,
            boolean firstSourceMemory,
            boolean firstColonyMemory,
            boolean firstResidenceMemory
    ) {
        return List.of(
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_awakening", true),
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_flower", firstFlowerMemory),
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_hive", firstHiveMemory),
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_source", firstSourceMemory),
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_colony", firstColonyMemory),
                new PolenProfileMemoryEntry("screen.polen.profile.memories.first_residence", firstResidenceMemory)
        );
    }

    private static int unlockedMemoryCount(List<PolenProfileMemoryEntry> memoryEntries) {
        int count = 0;
        for (PolenProfileMemoryEntry entry : memoryEntries) {
            if (entry.unlocked()) {
                count++;
            }
        }
        return count;
    }
}
