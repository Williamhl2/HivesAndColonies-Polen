package com.hivesandcolonies.characters.bootstrap.registry;

import com.hivesandcolonies.characters.bootstrap.Characters;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Characters.MODID);

    private ModCreativeTabs() {}

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POLEN_TAB =
            CREATIVE_MODE_TABS.register("polen_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.characters"))
                    .icon(() -> ModItems.PRINCESS_SEAL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Story progression items.
                        output.accept(ModItems.PRINCESS_SEAL.get());
                        output.accept(ModItems.PRINCESS_LETTER.get());
                        output.accept(ModItems.POLEN_JOURNAL.get());

                        // Reusable materials and future systems.
                        output.accept(ModItems.ROYAL_POLLEN.get());
                        output.accept(ModItems.SOURCE_TOUCHED_PETAL.get());
                        output.accept(ModItems.RESONANT_WAX.get());

                        // Focus and colony tools.
                        output.accept(ModItems.BLOOM_FOCUS.get());
                        output.accept(ModItems.SETTLEMENT_CHARM.get());
                        output.accept(ModItems.RESIDENCE_CHARM.get());
                        output.accept(ModItems.APIARIST_CHARM.get());
                        output.accept(ModItems.ARCANE_CHARM.get());
                        output.accept(ModItems.COLONIAL_CHARM.get());
                        output.accept(ModItems.HARVEST_CHARM.get());
                        output.accept(ModItems.ARTISAN_CHARM.get());
                        output.accept(ModItems.WAYFARER_CHARM.get());
                        output.accept(ModItems.POLEN_LANTERN.get());

                        // Debug / spawn support.
                        output.accept(ModItems.POLEN_SPAWN_EGG.get());
                        output.accept(ModItems.BEFSH_SPAWN_EGG.get());
                        output.accept(ModItems.LUNA_SPAWN_EGG.get());
                        output.accept(ModItems.VANILLA_SPAWN_EGG.get());
                        output.accept(ModItems.NOIA_SPAWN_EGG.get());
                        output.accept(ModItems.NORIS_SPAWN_EGG.get());

                    })
                    .build());
}
