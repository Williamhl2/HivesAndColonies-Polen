package com.hivesandcolonies.hccharacters.bootstrap.registry;

import java.util.function.Function;
import java.util.function.Supplier;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.hccharacters.character.polen.item.affinity.AffinityCharmItem;
import com.hivesandcolonies.hccharacters.character.polen.item.colony.ResidenceCharmItem;
import com.hivesandcolonies.hccharacters.character.polen.item.colony.SettlementCharmItem;
import com.hivesandcolonies.hccharacters.character.polen.item.focus.BloomFocusItem;
import com.hivesandcolonies.hccharacters.character.polen.item.focus.HiveheartCharmItem;
import com.hivesandcolonies.hccharacters.character.polen.item.material.ResonantWaxItem;
import com.hivesandcolonies.hccharacters.character.polen.item.material.RoyalPollenItem;
import com.hivesandcolonies.hccharacters.character.polen.item.material.SourceTouchedPetalItem;
import com.hivesandcolonies.hccharacters.character.polen.item.spawn.UniquePolenSpawnEggItem;
import com.hivesandcolonies.hccharacters.character.polen.item.story.PolenJournalItem;
import com.hivesandcolonies.hccharacters.character.polen.item.story.PrincessLetterItem;
import com.hivesandcolonies.hccharacters.character.polen.item.story.PrincessSealItem;
import com.hivesandcolonies.hccharacters.common.item.spawn.SecondaryCharacterSpawnEggItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HcCharacters.MODID);

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
    public static final DeferredItem<Item> HIVEHEART_CHARM =
            registerStoryItem("hiveheart_charm", HiveheartCharmItem::new);

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
    public static final DeferredItem<Item> POLEN_BEE_BED =
            ITEMS.registerItem(
                    "polen_bee_bed",
                    properties -> new BedItem(ModBlocks.POLEN_BEE_BED.get(), properties.stacksTo(1))
            );

    public static final DeferredItem<DeferredSpawnEggItem> POLEN_SPAWN_EGG =
        ITEMS.registerItem(
                "polen_spawn_egg",
                UniquePolenSpawnEggItem::new
        );
    public static final DeferredItem<DeferredSpawnEggItem> BEFSH_SPAWN_EGG =
            registerSpawnEgg("befsh_spawn_egg", ModEntities.BEFSH, 0x6E3F2D, 0xD8B58A);
    public static final DeferredItem<DeferredSpawnEggItem> LUNA_SPAWN_EGG =
            registerSpawnEgg("luna_spawn_egg", ModEntities.LUNA, 0xC7E8FF, 0x7A92FF);
    public static final DeferredItem<DeferredSpawnEggItem> VANILLA_SPAWN_EGG =
            registerSpawnEgg("vanilla_spawn_egg", ModEntities.VANILLA, 0xF3E5C7, 0xC89F5D);
    public static final DeferredItem<DeferredSpawnEggItem> NOIA_SPAWN_EGG =
            registerSpawnEgg("noia_spawn_egg", ModEntities.NOIA, 0x97E7D7, 0xD86F9B);
    public static final DeferredItem<DeferredSpawnEggItem> NORIS_SPAWN_EGG =
            registerSpawnEgg("noris_spawn_egg", ModEntities.NORIS, 0x7E92D8, 0xD7E3FF);
    public static final DeferredItem<DeferredSpawnEggItem> SOA_MARJORIE_SPAWN_EGG =
            registerSpawnEgg("soa_marjorie_spawn_egg", ModEntities.SOA_MARJORIE, 0x4C4C4C, 0xD8A14A);
    public static final DeferredItem<DeferredSpawnEggItem> LUCY_SPAWN_EGG =
            registerSpawnEgg("lucy_spawn_egg", ModEntities.LUCY, 0xB56B5E, 0xEED2A7);

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

    private static DeferredItem<DeferredSpawnEggItem> registerSpawnEgg(
            String name,
            Supplier<? extends EntityType<? extends Mob>> entityType,
            int backgroundColor,
            int highlightColor
    ) {
        return ITEMS.registerItem(
                name,
                properties -> new SecondaryCharacterSpawnEggItem(entityType, backgroundColor, highlightColor, properties),
                new Item.Properties()
        );
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
