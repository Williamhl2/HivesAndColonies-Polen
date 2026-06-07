package com.hivesandcolonies.characters.bootstrap.registry;

import com.hivesandcolonies.characters.bootstrap.Characters;
import com.hivesandcolonies.characters.character.polen.block.PolenBeeBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Characters.MODID);

    public static final DeferredBlock<Block> POLEN_LANTERN = BLOCKS.register(
            "polen_lantern",
            () -> new LanternBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
                            .strength(3.5F)
                            .lightLevel(state -> 15)
            )
    );
    public static final DeferredBlock<Block> POLEN_BEE_BED = BLOCKS.register(
            "polen_bee_bed",
            () -> new PolenBeeBedBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_WOOL)
                            .strength(0.8F)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
            )
    );

    private ModBlocks() {
    }
}
