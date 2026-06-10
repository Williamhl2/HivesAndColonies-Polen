package com.hivesandcolonies.hccharacters.character.polen.block;

import com.mojang.serialization.MapCodec;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PolenBeeBedBlock extends BedBlock {
    public static final MapCodec<BedBlock> CODEC = simpleCodec(PolenBeeBedBlock::new);
    public static final EnumProperty<BedPart> PART = BedBlock.PART;
    public static final BooleanProperty OCCUPIED = BedBlock.OCCUPIED;

    public PolenBeeBedBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.YELLOW, properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, Boolean.FALSE));
    }

    @Override
    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return PolenItemInteractionController.tryBindBeeBed(player, level, pos);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.polen.block.bee_bed.reserved"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public static BlockPos getFootPos(BlockState state, BlockPos pos) {
        if (!(state.getBlock() instanceof PolenBeeBedBlock) || !state.hasProperty(PART) || !state.hasProperty(FACING)) {
            return pos;
        }
        return state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(state.getValue(FACING).getOpposite());
    }

    public static BlockPos getHeadPos(BlockState state, BlockPos pos) {
        if (!(state.getBlock() instanceof PolenBeeBedBlock) || !state.hasProperty(PART) || !state.hasProperty(FACING)) {
            return pos;
        }
        return state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(FACING));
    }

    public static BlockPos getOtherPartPos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) == BedPart.FOOT ? pos.relative(facing) : pos.relative(facing.getOpposite());
    }
}
