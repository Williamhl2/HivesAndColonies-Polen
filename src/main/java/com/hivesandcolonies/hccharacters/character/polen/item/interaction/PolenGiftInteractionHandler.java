package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenItemTags;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;

final class PolenGiftInteractionHandler {
    private PolenGiftInteractionHandler() {
    }

    static InteractionResult useItemOnPolen(PolenEntity polen, Player player, InteractionHand hand) {
        if (polen == null || player == null || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        GiftResult gift = classifyGift(stack);
        if (gift == null) {
            if (isRejectedGift(stack)) {
                PolenItemInteractionSupport.sendStatus(player, "message.polen.gift.rejected");
                PolenRelationshipEvents.gift(player, "rejected", -1, "Polen remembers being offered something that felt unsafe.");
                player.getCooldowns().addCooldown(stack.getItem(), 40);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (!PolenRelationshipEvents.gift(player, gift.category(), gift.affinity(), gift.reasonText())) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.gift.cooldown");
            return InteractionResult.SUCCESS;
        }

        PolenWorldStateManager.adjustInterest(serverPlayer.serverLevel(), gift.interest(), gift.interestAmount());
        PolenItemInteractionSupport.spawnPolenResponse(serverPlayer.serverLevel(), polen);
        PolenAmbientDialogueController.tryPlay(polen, gift.dialogueCategory());

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(stack.getItem(), 40);
        PolenItemInteractionSupport.sendStatus(player, gift.messageKey());
        return InteractionResult.SUCCESS;
    }

    private static GiftResult classifyGift(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.is(PolenItemTags.POLEN_GIFTS_BEES)
                || item == Items.HONEYCOMB
                || item == Items.HONEY_BOTTLE
                || item == ModItems.RESONANT_WAX.get()) {
            return new GiftResult("bees", 2, PolenInterest.BEES, 5, "message.polen.gift.accepted_bees", "Polen recognizes something gentle in this bee gift.", PolenDialogueManager.AMBIENT_CURIOSITY);
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_NATURE) || isFlowerLike(stack)) {
            return new GiftResult("nature", 1, PolenInterest.EXPLORATION, 3, "message.polen.gift.accepted_nature", "Polen studies the gift as if it might remember her too.", PolenDialogueManager.AMBIENT_CURIOSITY);
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_SOURCE)
                || item == Items.AMETHYST_SHARD
                || item == ModItems.SOURCE_TOUCHED_PETAL.get()) {
            return new GiftResult("source", 2, PolenInterest.MAGIC, 5, "message.polen.gift.accepted_source", "A small thread of Source answers the gift.", PolenDialogueManager.AMBIENT_MAGIC);
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_FOOD)
                || item == Items.BREAD
                || item == Items.SWEET_BERRIES
                || item == Items.GLOW_BERRIES) {
            return new GiftResult("food", 1, PolenInterest.FOOD, 4, "message.polen.gift.accepted_food", "Polen accepts the food quietly.", PolenDialogueManager.AMBIENT_APPROACH);
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_HOME)
                || item == Items.LANTERN
                || item == Items.CANDLE
                || item == ModItems.POLEN_LANTERN.get()) {
            return new GiftResult("home", 2, PolenInterest.DECORATION, 4, "message.polen.gift.accepted_home", "Polen seems calmer around things meant to make a place safe.", PolenDialogueManager.AMBIENT_REFLECTION);
        }

        return null;
    }

    private static boolean isRejectedGift(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.ROTTEN_FLESH
                || item == Items.POISONOUS_POTATO
                || item == Items.SPIDER_EYE
                || item == Items.FERMENTED_SPIDER_EYE
                || item == Items.GUNPOWDER
                || item == Items.TNT
                || item == Items.BONE
                || item == Items.BONE_MEAL
                || item == Items.WITHER_ROSE;
    }

    private static boolean isFlowerLike(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof FlowerBlock || block.defaultBlockState().is(BlockTags.FLOWERS);
    }

    private record GiftResult(
            String category,
            int affinity,
            PolenInterest interest,
            int interestAmount,
            String messageKey,
            String reasonText,
            String dialogueCategory
    ) {
    }
}
