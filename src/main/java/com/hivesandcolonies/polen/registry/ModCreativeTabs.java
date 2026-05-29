package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.Polen;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Polen.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POLEN_TAB =
            CREATIVE_MODE_TABS.register("polen_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.polen"))
                    .icon(() -> ModItems.PRINCESS_SEAL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PRINCESS_SEAL.get());
                        output.accept(ModItems.PRINCESS_LETTER.get());
                        output.accept(ModItems.POLEN_JOURNAL.get());
                    })
                    .build());
}