package com.hivesandcolonies.polen.item.meta;

import com.hivesandcolonies.polen.Polen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class PolenItemTags {

    public static final TagKey<Item> STORY_ITEMS = create("story_items");
    public static final TagKey<Item> MATERIAL_ITEMS = create("material_items");
    public static final TagKey<Item> FOCUS_ITEMS = create("focus_items");
    public static final TagKey<Item> COLONY_ITEMS = create("colony_items");

    private PolenItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Polen.MODID, path));
    }
}
