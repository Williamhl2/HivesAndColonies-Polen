package com.hivesandcolonies.hccharacters.bootstrap;

import org.slf4j.Logger;

import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.character.polen.command.PolenDebugCommands;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue.PolenPrologueManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.singularity.PolenSingularityManager;
import com.hivesandcolonies.hccharacters.character.soa.world.SoaMarjorieEncounterManager;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModCreativeTabs;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(HcCharacters.MODID)
public class HcCharacters {
    public static final String MODID = "hc_characters";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("unused")
    public HcCharacters(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, HcCharactersGameplayConfig.SPEC, "hc-characters-common.toml");

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(PolenDebugCommands::register);
        NeoForge.EVENT_BUS.addListener(PolenSingularityManager::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(PolenPrologueManager::onServerStarted);
        NeoForge.EVENT_BUS.addListener(PolenPrologueManager::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(SoaMarjorieEncounterManager::onServerTick);
    }
}
