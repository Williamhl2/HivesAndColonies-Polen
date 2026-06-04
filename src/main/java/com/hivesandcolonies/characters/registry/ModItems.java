package com.hivesandcolonies.characters.registry;

import com.hivesandcolonies.characters.Characters;
import com.hivesandcolonies.characters.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.characters.item.affinity.AffinityCharmItem;
import com.hivesandcolonies.characters.item.colony.ResidenceCharmItem;
import com.hivesandcolonies.characters.item.colony.SettlementCharmItem;
import com.hivesandcolonies.characters.item.focus.BloomFocusItem;
import com.hivesandcolonies.characters.item.material.RoyalPollenItem;
import com.hivesandcolonies.characters.item.material.ResonantWaxItem;
import com.hivesandcolonies.characters.item.material.SourceTouchedPetalItem;
import com.hivesandcolonies.characters.item.spawn.UniquePolenSpawnEggItem;
import com.hivesandcolonies.characters.item.story.PolenJournalItem;
import com.hivesandcolonies.characters.item.story.PrincessLetterItem;
import com.hivesandcolonies.characters.item.story.PrincessSealItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Characters.MODID);

    private ModItems() {}

    // Story items: unique objects tied to identity, trust and chapter progression.
    public static final DeferredItem<Item> PRINCESS_SEAL =
            registerStoryItem("princess_seal", PrincessSealItem::new);
    public static final DeferredItem<Item> PRINCESS_LETTER =
            registerStoryItem("princess_letter", PrincessLetterItem::new);
    public static final DeferredItem<Item> POLEN_JOURNAL =
            registerStoryItem("polen_journal", PolenJournalItem::new);

    // Material items: repeatable resources for future rituals, crafting and colony integration.
    public static final DeferredItem<Item> ROYAL_POLLEN =
            registerMaterialItem("royal_pollen", RoyalPollenItem::new);
    public static final DeferredItem<Item> SOURCE_TOUCHED_PETAL =
            registerMaterialItem("source_touched_petal", SourceTouchedPetalItem::new);
    public static final DeferredItem<Item> RESONANT_WAX =
            registerMaterialItem("resonant_wax", ResonantWaxItem::new);

    // Focus items: direct Ars-adjacent interaction tools for Polen and remembered places.
    public static final DeferredItem<Item> BLOOM_FOCUS =
            registerFocusItem("bloom_focus", BloomFocusItem::new);

    // Affinity charms: unique visual equipment generated from Polen's world affinity.
    public static final DeferredItem<Item> APIARIST_CHARM =
            registerTypedItem("apiarist_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.APIARIST), new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> ARCANE_CHARM =
            registerTypedItem("arcane_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.ARCANE), new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> COLONIAL_CHARM =
            registerTypedItem("colonial_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.COLONIAL), new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> HARVEST_CHARM =
            registerTypedItem("harvest_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.HARVEST), new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> ARTISAN_CHARM =
            registerTypedItem("artisan_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.ARTISAN), new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> WAYFARER_CHARM =
            registerTypedItem("wayfarer_charm", properties -> new AffinityCharmItem(properties, PolenWorldAffinity.WAYFARER), new Item.Properties().stacksTo(1));

    // Colony items: player-facing tools for integrating Polen into safe settlement spaces.
    public static final DeferredItem<Item> SETTLEMENT_CHARM =
            registerColonyItem("settlement_charm", SettlementCharmItem::new);
    public static final DeferredItem<Item> RESIDENCE_CHARM =
            registerColonyItem("residence_charm", ResidenceCharmItem::new);
    public static final DeferredItem<Item> POLEN_LANTERN =
            ITEMS.registerItem(
                    "polen_lantern",
                    properties -> new BlockItem(ModBlocks.POLEN_LANTERN.get(), properties)
            );

    public static final DeferredItem<DeferredSpawnEggItem> POLEN_SPAWN_EGG =
        ITEMS.registerItem(
                "polen_spawn_egg",
                UniquePolenSpawnEggItem::new
        );

    private static DeferredItem<Item> registerStoryItem(
            String name,
            Function<Item.Properties, ? extends Item> factory
    ) {
        return registerTypedItem(name, factory, new Item.Properties());
    }

    private static DeferredItem<Item> registerMaterialItem(
            String name,
            Function<Item.Properties, ? extends Item> factory
    ) {
        return registerTypedItem(name, factory, new Item.Properties());
    }

    private static DeferredItem<Item> registerFocusItem(
            String name,
            Function<Item.Properties, ? extends Item> factory
    ) {
        return registerTypedItem(name, factory, new Item.Properties());
    }

    private static DeferredItem<Item> registerColonyItem(
            String name,
            Function<Item.Properties, ? extends Item> factory
    ) {
        return registerTypedItem(name, factory, new Item.Properties());
    }

    @SuppressWarnings("unchecked")
    private static DeferredItem<Item> registerTypedItem(
            String name,
            Function<Item.Properties, ? extends Item> factory,
            Item.Properties properties
    ) {
        DeferredHolder<Item, ? extends Item> holder = ITEMS.registerItem(name, factory, properties);
        return (DeferredItem<Item>) holder;
    }
}
