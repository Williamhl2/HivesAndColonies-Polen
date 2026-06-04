package com.hivesandcolonies.characters.character.polen.client.profile;

import com.hivesandcolonies.characters.integration.curios.PolenCuriosBridge;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Client-only read model for the Polen profile screen.
 *
 * Keep this class as a presentation adapter only: it reads already-synced state
 * from PolenEntity and derives display labels. It must not own gameplay state.
 */
public final class PolenProfileView {
    private final String displayName;
    private final PolenWorldAffinity affinity;
    private final PolenMood mood;
    private final PolenTaskType task;
    private final ItemStack charmStack;
    private final List<InterestBar> interestBars;

    private PolenProfileView(String displayName, PolenWorldAffinity affinity, PolenMood mood, PolenTaskType task, ItemStack charmStack, List<InterestBar> interestBars) {
        this.displayName = displayName;
        this.affinity = affinity;
        this.mood = mood;
        this.task = task;
        this.charmStack = charmStack;
        this.interestBars = interestBars;
    }

    public static PolenProfileView from(PolenEntity polen) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        return new PolenProfileView(
                polen.getDisplayName().getString(),
                affinity,
                polen.getMood(),
                polen.getCurrentTask(),
                PolenCuriosBridge.stackForAffinity(affinity),
                barsFor(affinity)
        );
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
