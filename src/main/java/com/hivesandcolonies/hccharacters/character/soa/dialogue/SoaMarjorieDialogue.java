package com.hivesandcolonies.hccharacters.character.soa.dialogue;

import java.util.List;

import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRewardPool;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SoaMarjorieDialogue {
    public static final String PROFILE_ID = "soa_marjorie_mining_guide";
    public static final String SPEAKER = "entity.hc_characters.soa_marjorie";

    public static final List<String> TIER_0 = List.of(
            "dialogue.soa.marjorie.tier0.line1",
            "dialogue.soa.marjorie.tier0.line2",
            "dialogue.soa.marjorie.tier0.line3",
            "dialogue.soa.marjorie.tier0.line4",
            "dialogue.soa.marjorie.tier0.line5"
    );

    public static final List<String> TIER_1 = List.of(
            "dialogue.soa.marjorie.tier1.line1",
            "dialogue.soa.marjorie.tier1.line2",
            "dialogue.soa.marjorie.tier1.line3",
            "dialogue.soa.marjorie.tier1.line4",
            "dialogue.soa.marjorie.tier1.line5"
    );

    public static final List<String> TIER_2 = List.of(
            "dialogue.soa.marjorie.tier2.line1",
            "dialogue.soa.marjorie.tier2.line2",
            "dialogue.soa.marjorie.tier2.line3",
            "dialogue.soa.marjorie.tier2.line4",
            "dialogue.soa.marjorie.tier2.line5"
    );

    public static final List<String> TIER_3 = List.of(
            "dialogue.soa.marjorie.tier3.line1",
            "dialogue.soa.marjorie.tier3.line2",
            "dialogue.soa.marjorie.tier3.line3",
            "dialogue.soa.marjorie.tier3.line4",
            "dialogue.soa.marjorie.tier3.line5"
    );

    private SoaMarjorieDialogue() {}

    public static NpcRewardPool rewardPool() {
        return new NpcRewardPool()
                .rare(player -> new ItemStack(Items.TORCH, 24 + player.level().random.nextInt(17)),
                        "dialogue.soa.marjorie.reward.torches")
                .rare(player -> new ItemStack(Items.COAL, 8 + player.level().random.nextInt(9)),
                        "dialogue.soa.marjorie.reward.coal")
                .rare(player -> new ItemStack(Items.RAW_IRON, 2 + player.level().random.nextInt(4)),
                        "dialogue.soa.marjorie.reward.raw_iron")
                .rare(player -> new ItemStack(Items.LAPIS_LAZULI, 4 + player.level().random.nextInt(6)),
                        "dialogue.soa.marjorie.reward.lapis")
                .legendary(player -> new ItemStack(Items.DIAMOND),
                        "dialogue.soa.marjorie.reward.diamond")
                .legendary(player -> new ItemStack(Items.EMERALD, 2 + player.level().random.nextInt(3)),
                        "dialogue.soa.marjorie.reward.emerald");
    }
}
