package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.item.PrincessLetterItem;
import com.hivesandcolonies.polen.item.PolenJournalItem;
import com.hivesandcolonies.polen.item.PrincessSealItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Polen.MODID);

    private ModItems() {}

     public static final DeferredItem<Item> PRINCESS_SEAL = ITEMS.registerItem(
            "princess_seal",
            PrincessSealItem::new,
            new Item.Properties()
    );
    
    public static final DeferredItem<Item> PRINCESS_LETTER = ITEMS.registerItem(
            "princess_letter",
            PrincessLetterItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<Item> POLEN_JOURNAL = ITEMS.registerItem(
            "polen_journal",
            PolenJournalItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<DeferredSpawnEggItem> POLEN_SPAWN_EGG =
        ITEMS.registerItem(
                "polen_spawn_egg",
                properties -> new DeferredSpawnEggItem(
                        ModEntities.POLEN,
                        0xF4C430,
                        0x7B3F98,
                        properties
                )
        );
}
