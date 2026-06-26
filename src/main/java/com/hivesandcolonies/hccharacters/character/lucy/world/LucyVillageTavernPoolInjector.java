package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.ArrayList;
import java.util.List;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Adds the Lucy + Soa tavern pieces to the vanilla village house pools at server load.
 *
 * Using a Java-side additive injection is safer than replacing the whole
 * minecraft:village/<biome>/houses template pool with a data-pack JSON file:
 * if another mod also edits the same vanilla pool, both additions can coexist.
 */
public final class LucyVillageTavernPoolInjector {
    private static final int TAVERN_WEIGHT = 35;
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSORS = ResourceKey.create(
            Registries.PROCESSOR_LIST,
            ResourceLocation.fromNamespaceAndPath("minecraft", "empty")
    );
    private static final String[] VILLAGE_TYPES = {"desert", "plains", "savanna", "snowy", "taiga"};

    private LucyVillageTavernPoolInjector() {
    }

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        HcCharacters.LOGGER.info(
                "Lucy/Soa tavern template-pool injection is disabled to avoid interfering with vanilla and modded village generation."
        );
    }

    private static void injectTavern(
            Registry<StructureTemplatePool> templatePools,
            Holder<StructureProcessorList> emptyProcessors,
            String villageType
    ) {
        ResourceLocation poolId = ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                "village/" + villageType + "/houses"
        );
        ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(
                HcCharacters.MODID,
                "village/taverns/" + villageType + "/lucy_soa_tavern"
        );

        StructureTemplatePool pool = templatePools.get(poolId);
        if (pool == null) {
            HcCharacters.LOGGER.warn("Could not inject Lucy/Soa tavern: template pool {} was not found", poolId);
            return;
        }

        int currentExpandedWeight = countExpandedTemplates(pool, templateId);
        int currentRawWeight = countRawWeight(pool, templateId);
        int weightToAdd = Math.max(0, TAVERN_WEIGHT - currentExpandedWeight);
        if (weightToAdd == 0) {
            HcCharacters.LOGGER.info(
                    "Lucy/Soa tavern {} is already present in template pool {} with expanded weight {} and raw weight {}",
                    templateId,
                    poolId,
                    currentExpandedWeight,
                    currentRawWeight
            );
            return;
        }

        StructurePoolElement tavernElement = StructurePoolElement
                .legacy(templateId.toString(), emptyProcessors)
                .apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weightToAdd; i++) {
            pool.templates.add(tavernElement);
        }

        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(pool.rawTemplates);
        rawTemplates.add(Pair.of(tavernElement, weightToAdd));
        pool.rawTemplates = rawTemplates;

        HcCharacters.LOGGER.info(
                "Injected Lucy/Soa tavern {} into template pool {}: added weight {}, expanded weight {} -> {}, raw weight {} -> {}",
                templateId,
                poolId,
                weightToAdd,
                currentExpandedWeight,
                currentExpandedWeight + weightToAdd,
                currentRawWeight,
                currentRawWeight + weightToAdd
        );
    }

    private static int countRawWeight(StructureTemplatePool pool, ResourceLocation templateId) {
        String needle = templateId.toString();
        int weight = 0;
        for (Pair<StructurePoolElement, Integer> entry : pool.rawTemplates) {
            if (entry.getFirst().toString().contains(needle)) {
                weight += entry.getSecond();
            }
        }
        return weight;
    }

    private static int countExpandedTemplates(StructureTemplatePool pool, ResourceLocation templateId) {
        String needle = templateId.toString();
        int count = 0;
        for (StructurePoolElement element : pool.templates) {
            if (element.toString().contains(needle)) {
                count++;
            }
        }
        return count;
    }
}
