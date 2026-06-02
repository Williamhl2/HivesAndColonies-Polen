package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryBonus;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryBonusType;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryItem;
import com.hivesandcolonies.polen.item.accessory.PolenAccessorySlot;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryTarget;
import com.hivesandcolonies.polen.item.base.TranslatableTooltipItem;
import com.hivesandcolonies.polen.item.colony.ResidenceCharmItem;
import com.hivesandcolonies.polen.item.colony.SettlementCharmItem;
import com.hivesandcolonies.polen.item.focus.BloomFocusItem;
import com.hivesandcolonies.polen.item.material.RoyalPollenItem;
import com.hivesandcolonies.polen.item.material.ResonantWaxItem;
import com.hivesandcolonies.polen.item.material.SourceTouchedPetalItem;
import com.hivesandcolonies.polen.item.story.PolenJournalItem;
import com.hivesandcolonies.polen.item.story.PrincessLetterItem;
import com.hivesandcolonies.polen.item.story.PrincessSealItem;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Polen.MODID);

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


    // Accessory items: Curios-compatible equipment for the player and future Polen equipment logic.
    public static final DeferredItem<Item> HONEY_SIGNET_RING =
            registerAccessoryItem(
                    "honey_signet_ring",
                    PolenAccessorySlot.RING,
                    PolenAccessoryTarget.BOTH,
                    PolenProgressionStage.ACT_I_FOUNDATION,
                    false,
                    List.of(
                            new PolenAccessoryBonus(PolenAccessoryBonusType.SAFETY, 1),
                            new PolenAccessoryBonus(PolenAccessoryBonusType.SOCIAL, 1)
                    )
            );

    public static final DeferredItem<Item> SOURCE_PETAL_NECKLACE =
            registerAccessoryItem(
                    "source_petal_necklace",
                    PolenAccessorySlot.NECKLACE,
                    PolenAccessoryTarget.BOTH,
                    PolenProgressionStage.ACT_I_FOUNDATION,
                    false,
                    List.of(
                            new PolenAccessoryBonus(PolenAccessoryBonusType.MAGIC, 1),
                            new PolenAccessoryBonus(PolenAccessoryBonusType.SOURCE_ATTUNEMENT, 1)
                    )
            );

    public static final DeferredItem<Item> WAYFINDER_BELT =
            registerAccessoryItem(
                    "wayfinder_belt",
                    PolenAccessorySlot.BELT,
                    PolenAccessoryTarget.PLAYER,
                    PolenProgressionStage.ACT_I_FOUNDATION,
                    false,
                    List.of(
                            new PolenAccessoryBonus(PolenAccessoryBonusType.LIGHT_REACH, 1),
                            new PolenAccessoryBonus(PolenAccessoryBonusType.CURIOSITY, 1)
                    )
            );

    public static final DeferredItem<Item> HIVEHEART_CHARM =
            registerAccessoryItem(
                    "hiveheart_charm",
                    PolenAccessorySlot.CHARM,
                    PolenAccessoryTarget.POLEN,
                    PolenProgressionStage.ACT_I_FOUNDATION,
                    true,
                    List.of(
                            new PolenAccessoryBonus(PolenAccessoryBonusType.REST, 2),
                            new PolenAccessoryBonus(PolenAccessoryBonusType.SAFETY, 1)
                    )
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


    private static DeferredItem<Item> registerAccessoryItem(
            String name,
            PolenAccessorySlot slot,
            PolenAccessoryTarget target,
            PolenProgressionStage progressionStage,
            boolean unique,
            List<PolenAccessoryBonus> bonuses
    ) {
        return registerTypedItem(
                name,
                properties -> new PolenAccessoryItem(
                        properties,
                        progressionStage,
                        slot,
                        target,
                        unique,
                        bonuses,
                        new TranslatableTooltipItem.TooltipLine("tooltip.polen." + name + ".line1", ChatFormatting.GOLD),
                        new TranslatableTooltipItem.TooltipLine("tooltip.polen." + name + ".line2", ChatFormatting.GRAY),
                        new TranslatableTooltipItem.TooltipLine("tooltip.polen." + name + ".line3", ChatFormatting.DARK_GRAY)
                ),
                new Item.Properties().stacksTo(1)
        );
    }

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
