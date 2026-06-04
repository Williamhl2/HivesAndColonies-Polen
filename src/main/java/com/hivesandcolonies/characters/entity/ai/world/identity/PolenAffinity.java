package com.hivesandcolonies.characters.entity.ai.world.identity;

import com.hivesandcolonies.characters.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.characters.Characters;
import net.minecraft.resources.ResourceLocation;

/**
 * Visual identity derived from the unique world's dominant Polen interest.
 *
 * This is intentionally separate from Nest Core. Nest Core belongs to Polen's
 * innate magic; the affinity charm is the first visible sign that this world's
 * Polen is different from another world's Polen.
 */
public enum PolenAffinity {
    APIARIST(PolenInterest.BEES, "apiarist_charm"),
    ARCANE(PolenInterest.MAGIC, "arcane_charm"),
    COLONIAL(PolenInterest.COLONIES, "colonial_charm"),
    HARVEST(PolenInterest.FOOD, "harvest_charm"),
    ARTISAN(PolenInterest.DECORATION, "artisan_charm"),
    WAYFARER(PolenInterest.EXPLORATION, "wayfarer_charm");

    private final PolenInterest interest;
    private final ResourceLocation itemId;

    PolenAffinity(PolenInterest interest, String itemPath) {
        this.interest = interest;
        this.itemId = ResourceLocation.fromNamespaceAndPath(Characters.MODID, itemPath);
    }

    public PolenInterest interest() {
        return interest;
    }

    public ResourceLocation itemId() {
        return itemId;
    }

    public String itemPath() {
        return itemId.getPath();
    }

    public static PolenAffinity fromInterest(PolenInterest interest) {
        for (PolenAffinity affinity : values()) {
            if (affinity.interest == interest) {
                return affinity;
            }
        }
        return WAYFARER;
    }
}
