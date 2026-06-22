package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenWorldEventTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class PolenFocusItemInteractionHandler {
    private static final int BLOOM_FOCUS_COOLDOWN = 50;

    private PolenFocusItemInteractionHandler() {
    }

    static InteractionResult useBloomFocusOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = PolenItemInteractionSupport.findNearestPolen(player, serverLevel);
        if (polen == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack stack = context.getItemInHand();

        if (clickedState.is(BlockTags.FLOWERS) || isHive(clickedState)) {
            polen.rememberInterestingSpot(clickedPos);
            PolenWorldStateManager.adjustInterest(serverLevel, clickedState.is(BlockTags.FLOWERS) ? PolenInterest.EXPLORATION : PolenInterest.BEES, 4);
            PolenRelationshipEvents.focus(
                    player,
                    clickedState.is(BlockTags.FLOWERS) ? "nature" : "bees",
                    1,
                    clickedState.is(BlockTags.FLOWERS)
                            ? "Polen notices that you pointed her toward something alive."
                            : "Polen recognizes the quiet rhythm around a hive."
            );
            PolenItemInteractionSupport.spawnFocusBurst(serverLevel, clickedPos);
            PolenItemInteractionSupport.spawnPolenResponse(serverLevel, polen);
            PolenAmbientDialogueController.tryPlay(polen, clickedState.is(BlockTags.FLOWERS)
                    ? PolenDialogueManager.AMBIENT_CURIOSITY
                    : PolenDialogueManager.AMBIENT_MAGIC);
            PolenItemInteractionSupport.damageUtilityItem(stack);
            player.getCooldowns().addCooldown(stack.getItem(), BLOOM_FOCUS_COOLDOWN);
            PolenItemInteractionSupport.sendStatus(
                    player,
                    clickedState.is(BlockTags.FLOWERS)
                            ? "message.polen.item.bloom_focus.marked_flower"
                            : "message.polen.item.bloom_focus.marked_hive"
            );
            return InteractionResult.SUCCESS;
        }

        if (isSourceLike(clickedState)) {
            polen.rememberInterestingSpot(clickedPos);
            PolenWorldStateManager.adjustInterest(serverLevel, PolenInterest.MAGIC, 5);
            PolenWorldEventTriggers.onFirstSourceDiscovered(serverLevel, clickedPos);
            PolenRelationshipEvents.focus(player, "source", 1, "A faint thread of Source answers near Polen.");
            PolenItemInteractionSupport.spawnSourceBurst(serverLevel, clickedPos);
            PolenItemInteractionSupport.spawnPolenResponse(serverLevel, polen);
            PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
            PolenItemInteractionSupport.damageUtilityItem(stack);
            player.getCooldowns().addCooldown(stack.getItem(), BLOOM_FOCUS_COOLDOWN);
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.bloom_focus.resonated_source");
            return InteractionResult.SUCCESS;
        }

        PolenItemInteractionSupport.sendStatus(player, "message.polen.item.bloom_focus.no_resonance");
        return InteractionResult.FAIL;
    }

    private static boolean isHive(BlockState state) {
        return state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE);
    }

    private static boolean isSourceLike(BlockState state) {
        return state.is(Blocks.ENCHANTING_TABLE)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.AMETHYST_CLUSTER);
    }
}
