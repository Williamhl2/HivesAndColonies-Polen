package com.hivesandcolonies.characters.bootstrap;

import org.slf4j.Logger;

import com.hivesandcolonies.characters.bootstrap.config.CharactersGameplayConfig;
import com.hivesandcolonies.characters.character.polen.command.PolenDebugCommands;
import com.hivesandcolonies.characters.character.polen.progression.world.prologue.PolenPrologueManager;
import com.hivesandcolonies.characters.character.polen.progression.world.singularity.PolenSingularityManager;
import com.hivesandcolonies.characters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.characters.bootstrap.registry.ModCreativeTabs;
import com.hivesandcolonies.characters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.characters.bootstrap.registry.ModItems;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Characters.MODID)
public class Characters {
    public static final String MODID = "characters";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("unused")
    public Characters(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CharactersGameplayConfig.SPEC, "characters-common.toml");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(PolenDebugCommands::register);
        NeoForge.EVENT_BUS.addListener(PolenSingularityManager::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(PolenPrologueManager::onServerStarted);
        NeoForge.EVENT_BUS.addListener(PolenPrologueManager::onPlayerLoggedIn);
    }
}
