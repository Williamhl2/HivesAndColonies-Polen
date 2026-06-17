package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.hccharacters.common.network.ClientboundPolenProfilePayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Client-only read model for the Polen profile screen.
 *
 * Keep this class as a presentation adapter only: it reads already-synced state
 * from PolenEntity and derives display labels. It must not own gameplay state.
 */
public final class PolenProfileView {
    private final int entityId;
    private final String displayName;
    private final PolenWorldAffinity affinity;
    private final PolenMood mood;
    private final PolenTaskType task;
    private final ItemStack charmStack;
    private final List<InterestBar> interestBars;
    private final boolean hasHome;
    private final boolean trustWalkActive;
    private final boolean trustWalkUnlocked;
    private final boolean giftsOnCooldown;
    private final int relationshipAffinity;
    private final int nextAffinityThreshold;
    private final int interactionCount;
    private final int currentChapter;
    private final String relationshipRankText;
    private final String storyStageKey;
    private final boolean firstFlowerMemory;
    private final boolean firstHiveMemory;
    private final boolean firstSourceMemory;
    private final boolean firstColonyMemory;
    private final boolean firstResidenceMemory;
    private final int safety;
    private final int social;
    private final int curiosity;
    private final int rest;
    private final int magic;

    private PolenProfileView(
            int entityId,
            String displayName,
            PolenWorldAffinity affinity,
            PolenMood mood,
            PolenTaskType task,
            ItemStack charmStack,
            List<InterestBar> interestBars,
            boolean hasHome,
            boolean trustWalkActive,
            boolean trustWalkUnlocked,
            boolean giftsOnCooldown,
            int relationshipAffinity,
            int nextAffinityThreshold,
            int interactionCount,
            int currentChapter,
            String relationshipRankText,
            String storyStageKey,
            boolean firstFlowerMemory,
            boolean firstHiveMemory,
            boolean firstSourceMemory,
            boolean firstColonyMemory,
            boolean firstResidenceMemory,
            int safety,
            int social,
            int curiosity,
            int rest,
            int magic
    ) {
        this.entityId = entityId;
        this.displayName = displayName;
        this.affinity = affinity;
        this.mood = mood;
        this.task = task;
        this.charmStack = charmStack;
        this.interestBars = interestBars;
        this.hasHome = hasHome;
        this.trustWalkActive = trustWalkActive;
        this.trustWalkUnlocked = trustWalkUnlocked;
        this.giftsOnCooldown = giftsOnCooldown;
        this.relationshipAffinity = relationshipAffinity;
        this.nextAffinityThreshold = nextAffinityThreshold;
        this.interactionCount = interactionCount;
        this.currentChapter = currentChapter;
        this.relationshipRankText = relationshipRankText == null ? "" : relationshipRankText;
        this.storyStageKey = storyStageKey == null || storyStageKey.isBlank()
                ? "screen.polen.profile.story.awakening"
                : storyStageKey;
        this.firstFlowerMemory = firstFlowerMemory;
        this.firstHiveMemory = firstHiveMemory;
        this.firstSourceMemory = firstSourceMemory;
        this.firstColonyMemory = firstColonyMemory;
        this.firstResidenceMemory = firstResidenceMemory;
        this.safety = safety;
        this.social = social;
        this.curiosity = curiosity;
        this.rest = rest;
        this.magic = magic;
    }

    public static PolenProfileView from(PolenEntity polen) {
        return from(polen, null);
    }

    public static PolenProfileView from(PolenEntity polen, ClientboundPolenProfilePayload payload) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        return new PolenProfileView(
                polen.getId(),
                polen.getDisplayName().getString(),
                affinity,
                polen.getMood(),
                polen.getCurrentTask(),
                PolenCuriosBridge.stackForAffinity(affinity),
                barsFor(affinity, payload),
                polen.hasAssignedHome(),
                polen.isTrustWalkSyncedActive(),
                payload != null && payload.trustWalkUnlocked(),
                payload != null && payload.giftsOnCooldown(),
                payload != null ? payload.affinity() : 0,
                payload != null ? payload.nextThreshold() : 10,
                payload != null ? payload.interactionCount() : 0,
                payload != null ? payload.currentChapter() : 0,
                payload != null ? payload.relationshipRankText() : "",
                payload != null ? payload.storyStageKey() : "screen.polen.profile.story.awakening",
                payload != null && payload.firstFlowerMemory(),
                payload != null && payload.firstHiveMemory(),
                payload != null && payload.firstSourceMemory(),
                payload != null && payload.firstColonyMemory(),
                payload != null && payload.firstResidenceMemory(),
                polen.getProfileNeedSafety(),
                polen.getProfileNeedSocial(),
                polen.getProfileNeedCuriosity(),
                polen.getProfileNeedRest(),
                polen.getProfileNeedMagic()
        );
    }

    public int entityId() {
        return this.entityId;
    }

    public String displayName() {
        return this.displayName;
    }

    public PolenWorldAffinity affinity() {
        return this.affinity;
    }

    public PolenMood mood() {
        return this.mood;
    }

    public PolenTaskType task() {
        return this.task;
    }

    public ItemStack charmStack() {
        return this.charmStack;
    }

    public List<InterestBar> interestBars() {
        return this.interestBars;
    }

    public boolean hasHome() {
        return this.hasHome;
    }

    public boolean trustWalkActive() {
        return this.trustWalkActive;
    }

    public boolean trustWalkUnlocked() {
        return this.trustWalkUnlocked;
    }

    public boolean giftsOnCooldown() {
        return this.giftsOnCooldown;
    }

    public int relationshipAffinity() {
        return this.relationshipAffinity;
    }

    public int nextAffinityThreshold() {
        return this.nextAffinityThreshold;
    }

    public int interactionCount() {
        return this.interactionCount;
    }

    public int currentChapter() {
        return this.currentChapter;
    }

    public String relationshipRankText() {
        return this.relationshipRankText;
    }

    public String storyStageKey() {
        return this.storyStageKey;
    }

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

    public String strongestNeedKey() {
        int lowest = this.safety;
        String key = "safety";
        if (this.rest < lowest) {
            lowest = this.rest;
            key = "rest";
        }
        if (this.social < lowest) {
            lowest = this.social;
            key = "social";
        }
        if (this.curiosity < lowest) {
            lowest = this.curiosity;
            key = "curiosity";
        }
        if (this.magic < lowest) {
            key = "magic";
        }
        return key;
    }

    public String giftHintKey() {
        String strongestInterest = strongestInterestKey();
        if (!strongestInterest.isEmpty()) {
            return switch (strongestInterest) {
                case "bees" -> "bees";
                case "magic" -> "source";
                case "food" -> "food";
                case "colonies", "decoration" -> "home";
                case "exploration" -> "nature";
                default -> "";
            };
        }
        return switch (strongestNeedKey()) {
            case "safety", "rest" -> "home";
            case "social" -> "food";
            case "curiosity" -> "nature";
            case "magic" -> "source";
            default -> "bees";
        };
    }

    public int relationshipProgressPercent() {
        if (this.nextAffinityThreshold <= 0) {
            return this.relationshipAffinity;
        }
        return Math.max(0, Math.min(100, this.relationshipAffinity));
    }

    public int unlockedMemoryCount() {
        int count = 1;
        if (this.firstFlowerMemory) {
            count++;
        }
        if (this.firstHiveMemory) {
            count++;
        }
        if (this.firstSourceMemory) {
            count++;
        }
        if (this.firstColonyMemory) {
            count++;
        }
        if (this.firstResidenceMemory) {
            count++;
        }
        return count;
    }

    public List<MemoryEntry> memoryEntries() {
        return List.of(
                new MemoryEntry("screen.polen.profile.memories.first_awakening", true),
                new MemoryEntry("screen.polen.profile.memories.first_flower", this.firstFlowerMemory),
                new MemoryEntry("screen.polen.profile.memories.first_hive", this.firstHiveMemory),
                new MemoryEntry("screen.polen.profile.memories.first_source", this.firstSourceMemory),
                new MemoryEntry("screen.polen.profile.memories.first_colony", this.firstColonyMemory),
                new MemoryEntry("screen.polen.profile.memories.first_residence", this.firstResidenceMemory)
        );
    }

    public String trustWalkStateKey() {
        if (this.trustWalkActive) {
            return "screen.polen.profile.actions.follow.active";
        }
        return this.trustWalkUnlocked
                ? "screen.polen.profile.actions.follow.ready"
                : "screen.polen.profile.actions.follow.locked";
    }

    public String giftsCooldownKey() {
        return this.giftsOnCooldown
                ? "screen.polen.profile.gifts.cooldown.active"
                : "screen.polen.profile.gifts.cooldown.ready";
    }

    public String nextActionKey() {
        if (!this.trustWalkUnlocked) {
            return "screen.polen.profile.next_action.raise_trust";
        }
        if (!this.hasHome) {
            return "screen.polen.profile.next_action.assign_bed";
        }
        if (this.giftsOnCooldown) {
            return "screen.polen.profile.next_action.wait_gifts";
        }
        return "screen.polen.profile.next_action.memory";
    }

    public boolean canAskToFollow() {
        return this.trustWalkUnlocked || this.trustWalkActive;
    }

    public boolean canReturnHome() {
        return this.hasHome;
    }

    public String followSummaryKey() {
        if (this.trustWalkActive) {
            return "screen.polen.profile.state.follow.active";
        }
        return this.trustWalkUnlocked
                ? "screen.polen.profile.state.follow.ready"
                : "screen.polen.profile.state.follow.locked";
    }

    public String homeSummaryKey() {
        return this.hasHome
                ? "screen.polen.profile.state.home.assigned"
                : "screen.polen.profile.state.home.missing";
    }

    public String giftSummaryKey() {
        return this.giftsOnCooldown
                ? "screen.polen.profile.state.gifts.cooldown"
                : "screen.polen.profile.state.gifts.ready";
    }

    public String currentStatusKey() {
        if (this.trustWalkActive) {
            return "screen.polen.profile.status.guided_walk";
        }
        return switch (this.task) {
            case SEEK_SAFETY -> "screen.polen.profile.status.seeking_safety";
            case KEEP_DISTANCE -> "screen.polen.profile.status.keeping_distance";
            case APPROACH_TRUSTED_PLAYER -> "screen.polen.profile.status.approaching";
            case INVESTIGATE_INTEREST -> "screen.polen.profile.status.observing";
            case SEEK_REST -> this.hasHome
                    ? "screen.polen.profile.status.returning_home"
                    : "screen.polen.profile.status.seeking_rest";
            case QUIET_CREATION -> "screen.polen.profile.status.quiet_creation";
            case WANDER_SAFE -> this.hasHome
                    ? "screen.polen.profile.status.wandering_home"
                    : "screen.polen.profile.status.wandering_carefully";
        };
    }

    public String currentPurposeTitleKey() {
        if (this.task.isUrgent()) {
            return "screen.polen.profile.purpose.recovering.title";
        }
        if (!this.hasHome) {
            return this.trustWalkUnlocked
                    ? "screen.polen.profile.purpose.needs_refuge.title"
                    : "screen.polen.profile.purpose.cautious.title";
        }
        if (this.trustWalkActive) {
            return "screen.polen.profile.purpose.guided.title";
        }
        if (this.unlockedMemoryCount() <= 2) {
            return "screen.polen.profile.purpose.learning_world.title";
        }
        return "screen.polen.profile.purpose.settled.title";
    }

    public String currentPurposeBodyKey() {
        if (this.task.isUrgent()) {
            return "screen.polen.profile.purpose.recovering.body";
        }
        if (!this.hasHome) {
            return this.trustWalkUnlocked
                    ? "screen.polen.profile.purpose.needs_refuge.body"
                    : "screen.polen.profile.purpose.cautious.body";
        }
        if (this.trustWalkActive) {
            return "screen.polen.profile.purpose.guided.body";
        }
        if (this.unlockedMemoryCount() <= 2) {
            return "screen.polen.profile.purpose.learning_world.body";
        }
        return "screen.polen.profile.purpose.settled.body";
    }

    public String focusHintKey() {
        return switch (strongestInterestKey()) {
            case "bees" -> "screen.polen.profile.focus.bees";
            case "magic" -> "screen.polen.profile.focus.magic";
            case "colonies", "decoration" -> "screen.polen.profile.focus.home";
            case "food" -> "screen.polen.profile.focus.food";
            case "exploration" -> "screen.polen.profile.focus.nature";
            default -> "screen.polen.profile.focus.general";
        };
    }

    public List<InterestBar> strongestInterests(int limit) {
        if (limit <= 0 || this.interestBars.isEmpty()) {
            return List.of();
        }
        return this.interestBars.subList(0, Math.min(limit, this.interestBars.size()));
    }

    public String affinityTitle() {
        return switch (this.affinity) {
            case APIARIST -> "The Apiarist";
            case ARCANE -> "The Arcane";
            case COLONIAL -> "The Settler";
            case HARVEST -> "The Harvester";
            case ARTISAN -> "The Artisan";
            case WAYFARER -> "The Wayfarer";
            case NONE -> "Awakening";
        };
    }

    public String affinityLabel() {
        return switch (this.affinity) {
            case APIARIST -> "Apiarist";
            case ARCANE -> "Arcane";
            case COLONIAL -> "Colonial";
            case HARVEST -> "Harvest";
            case ARTISAN -> "Artisan";
            case WAYFARER -> "Wayfarer";
            case NONE -> "Unknown";
        };
    }

    private String strongestInterestKey() {
        return this.interestBars.isEmpty() ? "" : this.interestBars.get(0).label();
    }

    private static List<InterestBar> barsFor(PolenWorldAffinity affinity, ClientboundPolenProfilePayload payload) {
        if (payload != null) {
            List<InterestBar> bars = new ArrayList<>(List.of(
                    new InterestBar("bees", payload.beesInterest()),
                    new InterestBar("magic", payload.magicInterest()),
                    new InterestBar("colonies", payload.coloniesInterest()),
                    new InterestBar("food", payload.foodInterest()),
                    new InterestBar("decoration", payload.decorationInterest()),
                    new InterestBar("exploration", payload.explorationInterest())
            ));
            bars.sort(Comparator.comparingInt(InterestBar::value).reversed().thenComparing(InterestBar::label));
            return List.copyOf(bars);
        }

        return switch (affinity) {
            case APIARIST -> List.of(
                    new InterestBar("bees", 92), new InterestBar("nature", 76), new InterestBar("food", 54),
                    new InterestBar("magic", 42), new InterestBar("colonies", 38), new InterestBar("exploration", 34));
            case ARCANE -> List.of(
                    new InterestBar("magic", 94), new InterestBar("exploration", 68), new InterestBar("decoration", 52),
                    new InterestBar("bees", 44), new InterestBar("food", 36), new InterestBar("colonies", 30));
            case COLONIAL -> List.of(
                    new InterestBar("colonies", 94), new InterestBar("food", 68), new InterestBar("decoration", 58),
                    new InterestBar("exploration", 48), new InterestBar("bees", 34), new InterestBar("magic", 30));
            case HARVEST -> List.of(
                    new InterestBar("food", 94), new InterestBar("bees", 64), new InterestBar("decoration", 54),
                    new InterestBar("colonies", 48), new InterestBar("exploration", 38), new InterestBar("magic", 28));
            case ARTISAN -> List.of(
                    new InterestBar("decoration", 94), new InterestBar("colonies", 64), new InterestBar("magic", 52),
                    new InterestBar("food", 44), new InterestBar("exploration", 40), new InterestBar("bees", 32));
            case WAYFARER -> List.of(
                    new InterestBar("exploration", 94), new InterestBar("magic", 62), new InterestBar("colonies", 50),
                    new InterestBar("bees", 46), new InterestBar("food", 38), new InterestBar("decoration", 32));
            case NONE -> List.of(
                    new InterestBar("bees", 50), new InterestBar("magic", 50), new InterestBar("colonies", 50),
                    new InterestBar("food", 50), new InterestBar("decoration", 50), new InterestBar("exploration", 50));
        };
    }

    public record InterestBar(String label, int value) {
    }

    public record MemoryEntry(String titleKey, boolean unlocked) {
    }
}
