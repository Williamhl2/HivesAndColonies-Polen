package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class NpcRelationshipInteraction {
    public static final int REWARD_UNLOCK_TIER = 3;
    public static final long RARE_REWARD_COOLDOWN_TICKS = 7L * 24_000L;
    public static final long LEGENDARY_REWARD_COOLDOWN_TICKS = 14L * 24_000L;
    public static final long UNIQUE_REWARD_COOLDOWN_TICKS = 30L * 24_000L;

    private NpcRelationshipInteraction() {}

    public static Result interact(
            Entity npc,
            Player player,
            String profileId,
            String speakerName,
            List<String> tier0,
            List<String> tier1,
            List<String> tier2,
            List<String> tier3,
            NpcRewardPool rewards
    ) {
        return interact(npc, player, profileId, speakerName, tier0, tier1, tier2, tier3, rewards, NpcRelationshipManager.DEFAULT_RANK_RESOLVER);
    }

    public static Result interact(
            Entity npc,
            Player player,
            String profileId,
            String speakerName,
            List<String> tier0,
            List<String> tier1,
            List<String> tier2,
            List<String> tier3,
            NpcRewardPool rewards,
            IntFunction<String> rankResolver
    ) {
        return interact(npc, player, profileId, speakerName, tier0, tier1, tier2, tier3, rewards, rankResolver, speakerName + " noto tu visita.");
    }

    public static Result interact(
            Entity npc,
            Player player,
            String profileId,
            String speakerName,
            List<String> tier0,
            List<String> tier1,
            List<String> tier2,
            List<String> tier3,
            NpcRewardPool rewards,
            IntFunction<String> rankResolver,
            String interactionReasonText
    ) {
        NpcRelationshipRecord record;
        if (player instanceof ServerPlayer serverPlayer) {
            record = NpcRelationshipManager.touch(
                    serverPlayer,
                    profileId,
                    speakerName,
                    interactionReasonText,
                    rankResolver
            );
        } else {
            NpcRelationshipMemory memory = new NpcRelationshipMemory(npc, profileId);
            NpcRelationshipMemory.PlayerRecord legacyRecord = memory.touch(player);
            record = new NpcRelationshipRecord();
            record.setAffinity(legacyRecord.trust);
            record.setInteractions(legacyRecord.interactions);
            record.setLastInteractionGameTime(legacyRecord.lastInteractionGameTime);
            record.setNextRewardGameTime(legacyRecord.nextRewardGameTime);
            record.setSpecialRewards(legacyRecord.specialRewards);
        }

        int tier = record.trustTier();
        String dialogue = pickDialogue(npc, tier, tier0, tier1, tier2, tier3);
        player.displayClientMessage(formatSpeech(speakerName, dialogue), false);

        NpcRewardResult reward = NpcRewardResult.none();
        long now = npc.level().getGameTime();
        if (tier >= REWARD_UNLOCK_TIER && now >= record.nextRewardGameTime()) {
            reward = rewards.roll(player, npc.level().random);
            if (reward.hasReward()) {
                give(player, reward.stack().copy());
                long cooldown = cooldownFor(reward.tier());
                record.setNextRewardGameTime(now + cooldown);
                record.incrementSpecialRewards();
                if (player instanceof ServerPlayer serverPlayer) {
                    NpcRelationshipManager.markDirty(serverPlayer);
                }
                player.displayClientMessage(formatSpeech(speakerName, reward.message()), false);
            }
        }
        return new Result(tier, reward);
    }

    private static String pickDialogue(Entity npc, int tier, List<String> tier0, List<String> tier1, List<String> tier2, List<String> tier3) {
        List<String> pool = switch (tier) {
            case 0 -> tier0;
            case 1 -> tier1.isEmpty() ? tier0 : tier1;
            case 2 -> tier2.isEmpty() ? tier1 : tier2;
            default -> tier3.isEmpty() ? tier2 : tier3;
        };
        if (pool.isEmpty()) {
            return "dialogue.hc_characters.common.relationship.fallback";
        }
        return pool.get(npc.level().random.nextInt(pool.size()));
    }

    private static Component formatSpeech(String speakerName, String textOrKey) {
        return Component.literal("<")
                .append(componentFromTextOrKey(speakerName))
                .append("> ")
                .append(componentFromTextOrKey(textOrKey));
    }

    private static Component componentFromTextOrKey(String textOrKey) {
        if (textOrKey == null || textOrKey.isBlank()) {
            return Component.empty();
        }
        if (!textOrKey.contains(" ")) {
            return Component.translatable(textOrKey);
        }
        return Component.literal(textOrKey);
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static long cooldownFor(NpcRewardTier tier) {
        return switch (tier) {
            case UNIQUE -> UNIQUE_REWARD_COOLDOWN_TICKS;
            case LEGENDARY -> LEGENDARY_REWARD_COOLDOWN_TICKS;
            case RARE -> RARE_REWARD_COOLDOWN_TICKS;
            default -> RARE_REWARD_COOLDOWN_TICKS;
        };
    }

    public record Result(int trustTier, NpcRewardResult reward) {}
}
