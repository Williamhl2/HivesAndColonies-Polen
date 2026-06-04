package com.hivesandcolonies.characters.character.polen.entity.ai.world.identity;

import com.hivesandcolonies.characters.bootstrap.Characters;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public enum PolenWorldAffinity {
    NONE(0, "none"),
    APIARIST(1, "apiarist"),
    ARCANE(2, "arcane"),
    COLONIAL(3, "colonial"),
    HARVEST(4, "harvest"),
    ARTISAN(5, "artisan"),
    WAYFARER(6, "wayfarer");

    private final int id;
    private final String serializedName;

    PolenWorldAffinity(int id, String serializedName) {
        this.id = id;
        this.serializedName = serializedName;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public ResourceLocation getCharmTexture() {
        return ResourceLocation.fromNamespaceAndPath(
                Characters.MODID,
                "textures/entity/accessory/" + this.serializedName + "_charm.png"
        );
    }

    public static PolenWorldAffinity fromId(int id) {
        for (PolenWorldAffinity affinity : values()) {
            if (affinity.id == id) {
                return affinity;
            }
        }
        return NONE;
    }

    public static PolenWorldAffinity byName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        for (PolenWorldAffinity affinity : values()) {
            if (affinity.serializedName.equals(normalized) || affinity.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return affinity;
            }
        }
        return NONE;
    }
}
