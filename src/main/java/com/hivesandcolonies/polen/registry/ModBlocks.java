package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.Polen;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Polen.MODID);

    public static final DeferredBlock<Block> POLEN_LANTERN = BLOCKS.register(
            "polen_lantern",
            () -> new LanternBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
                            .strength(3.5F)
                            .lightLevel(state -> 15)
            )
    );

    private ModBlocks() {
    }
}
