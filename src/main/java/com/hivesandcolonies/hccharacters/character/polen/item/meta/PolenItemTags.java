package com.hivesandcolonies.hccharacters.character.polen.item.meta;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class PolenItemTags {

    public static final TagKey<Item> STORY_ITEMS = create("story_items");
    public static final TagKey<Item> MATERIAL_ITEMS = create("material_items");
    public static final TagKey<Item> FOCUS_ITEMS = create("focus_items");
    public static final TagKey<Item> COLONY_ITEMS = create("colony_items");
    public static final TagKey<Item> ACCESSORY_ITEMS = create("accessory_items");
    public static final TagKey<Item> POLEN_GIFTS_BEES = create("polen_gifts_bees");
    public static final TagKey<Item> POLEN_GIFTS_NATURE = create("polen_gifts_nature");
    public static final TagKey<Item> POLEN_GIFTS_SOURCE = create("polen_gifts_source");
    public static final TagKey<Item> POLEN_GIFTS_FOOD = create("polen_gifts_food");
    public static final TagKey<Item> POLEN_GIFTS_HOME = create("polen_gifts_home");

    private PolenItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, path));
    }
}
