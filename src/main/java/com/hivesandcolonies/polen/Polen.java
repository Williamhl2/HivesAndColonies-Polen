package com.hivesandcolonies.polen;

import org.slf4j.Logger;

import com.hivesandcolonies.polen.registry.ModCreativeTabs;
import com.hivesandcolonies.polen.registry.ModItems;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Polen.MODID)
public class Polen {
    public static final String MODID = "polen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Polen(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("Polen mod loaded.");
    }
}