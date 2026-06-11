package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import net.minecraft.world.item.ItemStack;

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
    private final int safety;
    private final int social;
    private final int curiosity;
    private final int rest;
    private final int magic;

    private PolenProfileView(int entityId, String displayName, PolenWorldAffinity affinity, PolenMood mood, PolenTaskType task, ItemStack charmStack, List<InterestBar> interestBars, boolean hasHome, boolean trustWalkActive, int safety, int social, int curiosity, int rest, int magic) {
        this.entityId = entityId;
        this.displayName = displayName;
        this.affinity = affinity;
        this.mood = mood;
        this.task = task;
        this.charmStack = charmStack;
        this.interestBars = interestBars;
        this.hasHome = hasHome;
        this.trustWalkActive = trustWalkActive;
        this.safety = safety;
        this.social = social;
        this.curiosity = curiosity;
        this.rest = rest;
        this.magic = magic;
    }

    public static PolenProfileView from(PolenEntity polen) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        return new PolenProfileView(
                polen.getId(),
                polen.getDisplayName().getString(),
                affinity,
                polen.getMood(),
                polen.getCurrentTask(),
                PolenCuriosBridge.stackForAffinity(affinity),
                barsFor(affinity),
                polen.hasAssignedHome(),
                polen.isTrustWalkSyncedActive(),
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
        return switch (strongestNeedKey()) {
            case "safety", "rest" -> "home";
            case "social" -> "food";
            case "curiosity" -> "nature";
            case "magic" -> "source";
            default -> "bees";
        };
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

    private static List<InterestBar> barsFor(PolenWorldAffinity affinity) {
        return switch (affinity) {
            case APIARIST -> List.of(
                    new InterestBar("Bees", 92), new InterestBar("Nature", 76), new InterestBar("Food", 54),
                    new InterestBar("Magic", 42), new InterestBar("Colonies", 38), new InterestBar("Exploration", 34));
            case ARCANE -> List.of(
                    new InterestBar("Magic", 94), new InterestBar("Exploration", 68), new InterestBar("Decoration", 52),
                    new InterestBar("Bees", 44), new InterestBar("Food", 36), new InterestBar("Colonies", 30));
            case COLONIAL -> List.of(
                    new InterestBar("Colonies", 94), new InterestBar("Food", 68), new InterestBar("Decoration", 58),
                    new InterestBar("Exploration", 48), new InterestBar("Bees", 34), new InterestBar("Magic", 30));
            case HARVEST -> List.of(
                    new InterestBar("Food", 94), new InterestBar("Bees", 64), new InterestBar("Decoration", 54),
                    new InterestBar("Colonies", 48), new InterestBar("Exploration", 38), new InterestBar("Magic", 28));
            case ARTISAN -> List.of(
                    new InterestBar("Decoration", 94), new InterestBar("Colonies", 64), new InterestBar("Magic", 52),
                    new InterestBar("Food", 44), new InterestBar("Exploration", 40), new InterestBar("Bees", 32));
            case WAYFARER -> List.of(
                    new InterestBar("Exploration", 94), new InterestBar("Magic", 62), new InterestBar("Colonies", 50),
                    new InterestBar("Bees", 46), new InterestBar("Food", 38), new InterestBar("Decoration", 32));
            case NONE -> List.of(
                    new InterestBar("Bees", 50), new InterestBar("Magic", 50), new InterestBar("Colonies", 50),
                    new InterestBar("Food", 50), new InterestBar("Decoration", 50), new InterestBar("Exploration", 50));
        };
    }

    public record InterestBar(String label, int value) {
    }
}
