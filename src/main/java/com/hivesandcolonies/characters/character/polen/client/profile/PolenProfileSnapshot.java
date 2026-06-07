package com.hivesandcolonies.characters.character.polen.client.profile;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskStatus;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.characters.bootstrap.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Client-only presentation data for the Polen profile screen.
 *
 * This deliberately reads already-synced entity state and does not add new fields
 * to PolenEntity. Full server-backed interest synchronization can be added later
 * through a dedicated network packet without changing the screen API.
 */
public final class PolenProfileSnapshot {
    private final Component displayName;
    private final PolenWorldAffinity affinity;
    private final PolenMood mood;
    private final PolenTaskType task;
    private final PolenTaskStatus taskStatus;
    private final ItemStack charmStack;
    private final EnumMap<ProfileInterest, Integer> visibleInterests;

    private PolenProfileSnapshot(
            Component displayName,
            PolenWorldAffinity affinity,
            PolenMood mood,
            PolenTaskType task,
            PolenTaskStatus taskStatus,
            ItemStack charmStack,
            EnumMap<ProfileInterest, Integer> visibleInterests
    ) {
        this.displayName = displayName;
        this.affinity = affinity;
        this.mood = mood;
        this.task = task;
        this.taskStatus = taskStatus;
        this.charmStack = charmStack;
        this.visibleInterests = visibleInterests;
    }

    public static PolenProfileSnapshot from(PolenEntity polen) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        if (affinity == null || affinity == PolenWorldAffinity.NONE) {
            affinity = PolenWorldAffinity.WAYFARER;
        }

        return new PolenProfileSnapshot(
                polen.getDisplayName(),
                affinity,
                polen.getMood(),
                polen.getCurrentTask(),
                polen.getCurrentTaskStatus(),
                charmStackFor(affinity),
                interestsFor(affinity)
        );
    }

    public Component displayName() {
        return displayName;
    }

    public PolenWorldAffinity affinity() {
        return affinity;
    }

    public PolenMood mood() {
        return mood;
    }

    public PolenTaskType task() {
        return task;
    }

    public PolenTaskStatus taskStatus() {
        return taskStatus;
    }

    public ItemStack charmStack() {
        return charmStack;
    }

    public Map<ProfileInterest, Integer> visibleInterests() {
        return visibleInterests;
    }

    public String titleName() {
        return switch (affinity) {
            case APIARIST -> "The Apiarist";
            case ARCANE -> "The Arcane";
            case COLONIAL -> "The Settler";
            case HARVEST -> "The Harvester";
            case ARTISAN -> "The Artisan";
            case WAYFARER -> "The Wayfarer";
            default -> "The Awakened";
        };
    }

    public String affinityName() {
        return switch (affinity) {
            case APIARIST -> "Apiarist";
            case ARCANE -> "Arcane";
            case COLONIAL -> "Colonial";
            case HARVEST -> "Harvest";
            case ARTISAN -> "Artisan";
            case WAYFARER -> "Wayfarer";
            default -> "Unknown";
        };
    }

    public String storyStageName() {
        return "Awakening";
    }

    private static ItemStack charmStackFor(PolenWorldAffinity affinity) {
        return switch (affinity) {
            case APIARIST -> new ItemStack(ModItems.APIARIST_CHARM.get());
            case ARCANE -> new ItemStack(ModItems.ARCANE_CHARM.get());
            case COLONIAL -> new ItemStack(ModItems.COLONIAL_CHARM.get());
            case HARVEST -> new ItemStack(ModItems.HARVEST_CHARM.get());
            case ARTISAN -> new ItemStack(ModItems.ARTISAN_CHARM.get());
            case WAYFARER -> new ItemStack(ModItems.WAYFARER_CHARM.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static EnumMap<ProfileInterest, Integer> interestsFor(PolenWorldAffinity affinity) {
        EnumMap<ProfileInterest, Integer> values = new EnumMap<>(ProfileInterest.class);
        for (ProfileInterest interest : ProfileInterest.values()) {
            values.put(interest, 42);
        }

        switch (affinity) {
            case APIARIST -> {
                values.put(ProfileInterest.BEES, 92);
                values.put(ProfileInterest.NATURE, 70);
                values.put(ProfileInterest.EXPLORATION, 58);
            }
            case ARCANE -> {
                values.put(ProfileInterest.MAGIC, 92);
                values.put(ProfileInterest.EXPLORATION, 64);
                values.put(ProfileInterest.DECORATION, 56);
            }
            case COLONIAL -> {
                values.put(ProfileInterest.COLONIES, 92);
                values.put(ProfileInterest.DECORATION, 62);
                values.put(ProfileInterest.FOOD, 55);
            }
            case HARVEST -> {
                values.put(ProfileInterest.FOOD, 92);
                values.put(ProfileInterest.NATURE, 68);
                values.put(ProfileInterest.COLONIES, 55);
            }
            case ARTISAN -> {
                values.put(ProfileInterest.DECORATION, 92);
                values.put(ProfileInterest.MAGIC, 58);
                values.put(ProfileInterest.COLONIES, 56);
            }
            case WAYFARER -> {
                values.put(ProfileInterest.EXPLORATION, 92);
                values.put(ProfileInterest.MAGIC, 58);
                values.put(ProfileInterest.NATURE, 54);
            }
            default -> values.put(ProfileInterest.EXPLORATION, 68);
        }

        return values;
    }
}
