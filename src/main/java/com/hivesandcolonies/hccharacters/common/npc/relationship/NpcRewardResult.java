package com.hivesandcolonies.hccharacters.common.npc.relationship;

import net.minecraft.world.item.ItemStack;

public record NpcRewardResult(NpcRewardTier tier, ItemStack stack, String message) {
    public static NpcRewardResult none() {
        return new NpcRewardResult(NpcRewardTier.NONE, ItemStack.EMPTY, "");
    }

    public boolean hasReward() {
        return this.tier != NpcRewardTier.NONE && !this.stack.isEmpty();
    }
}
